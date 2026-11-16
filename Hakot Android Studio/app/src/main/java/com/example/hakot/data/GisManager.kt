package com.example.hakot.data

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GisBarangayFeature(
    val id: String,
    val name: String,
    val code: String,
    val polygonRings: List<List<Pair<Double, Double>>> // Outer ring and inner holes: list of [ [lng, lat], ... ]
)

data class GisRoadSegmentFeature(
    val id: String,
    val segmentName: String,
    val roadType: String,
    val osmWayId: Long,
    val lineStringCoordinates: List<Pair<Double, Double>>, // Original full geometry
    var clippedCoordinates: List<Pair<Double, Double>> = emptyList(), // Exact clipped geometry inside active barangay
    var isReversed: Boolean = false // Traversal direction flag
)

object GisManager {
    var isLoaded: Boolean = false
    var loadError: String? = null

    val barangays = mutableListOf<GisBarangayFeature>()
    val roadSegments = mutableListOf<GisRoadSegmentFeature>()

    suspend fun loadGisData(): Boolean {
        if (isLoaded && barangays.isNotEmpty() && roadSegments.isNotEmpty()) {
            return true
        }

        try {
            val geoJsonPair = SupabaseClient.fetchNasugbuMapGeoJson()
            if (geoJsonPair == null) {
                loadError = "Failed to fetch GIS GeoJSON from Supabase"
                return false
            }

            val bgyJson = JSONObject(geoJsonPair.first)
            val roadJson = JSONObject(geoJsonPair.second)

            val parsedBarangays = mutableListOf<GisBarangayFeature>()
            val bgyFeatures = bgyJson.optJSONArray("features")
            if (bgyFeatures != null) {
                for (i in 0 until bgyFeatures.length()) {
                    val feat = bgyFeatures.getJSONObject(i)
                    val props = feat.optJSONObject("properties") ?: JSONObject()
                    val geom = feat.optJSONObject("geometry") ?: JSONObject()

                    val id = props.optString("id", "bgy_$i")
                    val name = props.optString("name", "Barangay $i")
                    val code = props.optString("code", "")

                    val rings = mutableListOf<List<Pair<Double, Double>>>()
                    val geomType = geom.optString("type", "")
                    val coordsArr = geom.optJSONArray("coordinates")

                    if (coordsArr != null) {
                        if (geomType.equals("Polygon", ignoreCase = true)) {
                            parsePolygonRings(coordsArr, rings)
                        } else if (geomType.equals("MultiPolygon", ignoreCase = true)) {
                            for (p in 0 until coordsArr.length()) {
                                val polyArr = coordsArr.optJSONArray(p)
                                if (polyArr != null) {
                                    parsePolygonRings(polyArr, rings)
                                }
                            }
                        }
                    }

                    parsedBarangays.add(GisBarangayFeature(id, name, code, rings))
                }
            }

            val parsedRoads = mutableListOf<GisRoadSegmentFeature>()
            val roadFeatures = roadJson.optJSONArray("features")
            if (roadFeatures != null) {
                for (i in 0 until roadFeatures.length()) {
                    val feat = roadFeatures.getJSONObject(i)
                    val props = feat.optJSONObject("properties") ?: JSONObject()
                    val geom = feat.optJSONObject("geometry") ?: JSONObject()

                    val id = props.optString("id", "road_$i")
                    val rawName = props.optString("segment_name").ifBlank { props.optString("name", "") }
                    val roadType = props.optString("road_type", "primary")
                    val wayId = props.optLong("osm_way_id", 0L)

                    val segName = if (rawName.isBlank() || rawName.lowercase() == "null" || rawName.lowercase() == "unnamed road segment") {
                        "Unnamed road — ${roadType.replaceFirstChar { it.uppercase() }}"
                    } else {
                        rawName
                    }

                    val lineCoords = mutableListOf<Pair<Double, Double>>()
                    val coordsArr = geom.optJSONArray("coordinates")
                    if (coordsArr != null) {
                        for (c in 0 until coordsArr.length()) {
                            val pt = coordsArr.optJSONArray(c)
                            if (pt != null && pt.length() >= 2) {
                                val lng = pt.optDouble(0)
                                val lat = pt.optDouble(1)
                                lineCoords.add(Pair(lng, lat))
                            }
                        }
                    }

                    if (lineCoords.isNotEmpty()) {
                        parsedRoads.add(GisRoadSegmentFeature(id, segName, roadType, wayId, lineCoords))
                    }
                }
            }

            barangays.clear()
            barangays.addAll(parsedBarangays.sortedBy { it.name })

            roadSegments.clear()
            roadSegments.addAll(parsedRoads)

            isLoaded = true
            loadError = null
            Log.d("HAKOT_GIS", "Successfully loaded ${barangays.size} barangays and ${roadSegments.size} road segments from PostGIS.")
            return true
        } catch (e: Exception) {
            loadError = "GIS parse error: ${e.message}"
            Log.e("HAKOT_GIS", "Failed to load GIS data", e)
            return false
        }
    }

    private fun parsePolygonRings(coordsArr: JSONArray, rings: MutableList<List<Pair<Double, Double>>>) {
        for (r in 0 until coordsArr.length()) {
            val ringArr = coordsArr.optJSONArray(r)
            if (ringArr != null) {
                val pts = mutableListOf<Pair<Double, Double>>()
                for (p in 0 until ringArr.length()) {
                    val pt = ringArr.optJSONArray(p)
                    if (pt != null && pt.length() >= 2) {
                        val lng = pt.optDouble(0)
                        val lat = pt.optDouble(1)
                        pts.add(Pair(lng, lat))
                    }
                }
                if (pts.isNotEmpty()) {
                    rings.add(pts)
                }
            }
        }
    }

    // Get road segments intersecting selected barangay and compute exact clipped geometry inside boundary
    fun getRoadSegmentsInBarangay(barangayName: String): List<GisRoadSegmentFeature> {
        val bgy = barangays.find { it.name.equals(barangayName, ignoreCase = true) }
            ?: return emptyList()

        if (bgy.polygonRings.isEmpty()) return emptyList()

        val results = mutableListOf<GisRoadSegmentFeature>()
        val primaryRing = bgy.polygonRings.firstOrNull() ?: return emptyList()

        for (road in roadSegments) {
            val clippedLines = clipLineStringToPolygon(road.lineStringCoordinates, primaryRing)
            if (clippedLines.isNotEmpty() && clippedLines[0].size >= 2) {
                val flatClipped = clippedLines.flatten()
                if (flatClipped.size >= 2) {
                    val cloned = road.copy(clippedCoordinates = flatClipped)
                    results.add(cloned)
                }
            }
        }

        Log.d("HAKOT_GIS", "Barangay '${bgy.name}' returned ${results.size} clipped road segments.")
        return results
    }

    // Exact LineString to Polygon ring clipping
    private fun clipLineStringToPolygon(lineCoords: List<Pair<Double, Double>>, ring: List<Pair<Double, Double>>): List<List<Pair<Double, Double>>> {
        if (lineCoords.size < 2 || ring.size < 3) return emptyList()

        val resultLines = mutableListOf<List<Pair<Double, Double>>>()
        var currentSegmentPoints = mutableListOf<Pair<Double, Double>>()

        for (i in 0 until lineCoords.size - 1) {
            val p1 = lineCoords[i]
            val p2 = lineCoords[i + 1]

            val p1Inside = isPointInPolygon(p1, ring)
            val p2Inside = isPointInPolygon(p2, ring)

            if (p1Inside && currentSegmentPoints.isEmpty()) {
                currentSegmentPoints.add(p1)
            }

            val intersections = mutableListOf<Pair<Double, Double>>()
            for (j in 0 until ring.size - 1) {
                val r1 = ring[j]
                val r2 = ring[j + 1]
                val inter = getLineIntersection(p1, p2, r1, r2)
                if (inter != null) {
                    intersections.add(inter)
                }
            }

            intersections.sortBy { pt ->
                val dx = p2.first - p1.first
                val dy = p2.second - p1.second
                if (abs(dx) > abs(dy)) (pt.first - p1.first) / dx
                else (pt.second - p1.second) / dy
            }

            if (p1Inside) {
                for (inter in intersections) {
                    if (currentSegmentPoints.lastOrNull() != inter) {
                        currentSegmentPoints.add(inter)
                        resultLines.add(currentSegmentPoints.toList())
                        currentSegmentPoints = mutableListOf()
                    }
                }
                if (p2Inside) {
                    currentSegmentPoints.add(p2)
                }
            } else {
                if (intersections.isNotEmpty()) {
                    val firstInter = intersections.first()
                    currentSegmentPoints.add(firstInter)
                    if (p2Inside) {
                        currentSegmentPoints.add(p2)
                    } else if (intersections.size > 1) {
                        val secondInter = intersections[1]
                        currentSegmentPoints.add(secondInter)
                        resultLines.add(currentSegmentPoints.toList())
                        currentSegmentPoints = mutableListOf()
                    } else {
                        resultLines.add(currentSegmentPoints.toList())
                        currentSegmentPoints = mutableListOf()
                    }
                } else if (p2Inside) {
                    currentSegmentPoints.add(p2)
                }
            }
        }

        if (currentSegmentPoints.size >= 2) {
            resultLines.add(currentSegmentPoints.toList())
        }

        if (resultLines.isEmpty()) {
            val insidePoints = lineCoords.filter { isPointInPolygon(it, ring) }
            if (insidePoints.size >= 2) {
                resultLines.add(insidePoints)
            }
        }

        return resultLines
    }

    private fun getLineIntersection(p1: Pair<Double, Double>, p2: Pair<Double, Double>, p3: Pair<Double, Double>, p4: Pair<Double, Double>): Pair<Double, Double>? {
        val s1x = p2.first - p1.first
        val s1y = p2.second - p1.second
        val s2x = p4.first - p3.first
        val s2y = p4.second - p3.second

        val denom = -s2x * s1y + s1x * s2y
        if (abs(denom) < 1e-10) return null

        val s = (-s1y * (p1.first - p3.first) + s1x * (p1.second - p3.second)) / denom
        val t = (s2x * (p1.second - p3.second) - s2y * (p1.first - p3.first)) / denom

        if (s in 0.0..1.0 && t in 0.0..1.0) {
            val ix = p1.first + (t * s1x)
            val iy = p1.second + (t * s1y)
            return Pair(ix, iy)
        }
        return null
    }

    fun isPointInPolygon(point: Pair<Double, Double>, ring: List<Pair<Double, Double>>): Boolean {
        val x = point.first  // Lng
        val y = point.second // Lat
        var inside = false
        var j = ring.size - 1
        for (i in ring.indices) {
            val xi = ring[i].first
            val yi = ring[i].second
            val xj = ring[j].first
            val yj = ring[j].second

            val intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }

    fun validateConnectivity(segments: List<GisRoadSegmentFeature>): Pair<Boolean, String> {
        if (segments.size <= 1) return Pair(true, "✓ Connected Route Corridor Verified")

        for (i in 0 until segments.size - 1) {
            val current = segments[i]
            val next = segments[i + 1]

            val currentEnd = current.clippedCoordinates.lastOrNull() ?: current.lineStringCoordinates.last()
            val nextStart = next.clippedCoordinates.firstOrNull() ?: next.lineStringCoordinates.first()

            val minDistanceMeters = haversineMeters(currentEnd, nextStart)

            if (minDistanceMeters > 500.0) {
                return Pair(
                    false,
                    "⚠ Warning: Disconnected gap (${minDistanceMeters.toInt()}m) between '${current.segmentName}' and '${next.segmentName}'"
                )
            }
        }

        return Pair(true, "✓ Connected Route Corridor Verified (${segments.size} PostGIS Segments)")
    }

    fun haversineMeters(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(p2.second - p1.second)
        val dLng = Math.toRadians(p2.first - p1.first)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(p1.second)) * cos(Math.toRadians(p2.second)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun findNearestRoadInBarangay(lat: Double, lng: Double, barangayName: String, maxToleranceMeters: Double = 100.0): GisRoadSegmentFeature? {
        val roads = getRoadSegmentsInBarangay(barangayName)
        var nearestRoad: GisRoadSegmentFeature? = null
        var minDistance = Double.MAX_VALUE

        for (road in roads) {
            for (pt in road.clippedCoordinates) {
                val dist = haversineMeters(Pair(lng, lat), pt)
                if (dist < minDistance) {
                    minDistance = dist
                    nearestRoad = road
                }
            }
        }

        return if (minDistance <= maxToleranceMeters) nearestRoad else null
    }
}
