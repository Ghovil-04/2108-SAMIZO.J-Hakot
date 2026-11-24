<img width="10302" height="5501" alt="diagram (1)" src="https://github.com/user-attachments/assets/82107c8d-0cdc-49d3-b424-df7d1aef6afa" />


HAKOT – APPLICATION OVERVIEW

HAKOT is a mobile-based waste collection management and monitoring application designed to improve garbage collection operations, route management, and communication between municipal administrators, garbage truck drivers, and residents.

The application aims to provide a centralized digital system for managing waste collection schedules, monitoring garbage trucks, viewing collection routes, and reporting waste management concerns.

1\. MAIN OBJECTIVES

- Digitize municipal garbage collection operations.
- Provide accurate and organized garbage collection schedules.
- Monitor garbage trucks through GPS tracking.
- Display planned and actual truck routes on a map.
- Improve coordination between municipal administrators and truck drivers.
- Allow residents to report missed collections and other waste management concerns.
- Maintain centralized, municipality-specific waste management data.

2\. USER ROLES

ADMINISTRATOR

Municipal or authorized district personnel responsible for managing waste collection operations.

Main functions:

- Manage garbage collection schedules.
- Assign garbage trucks and drivers.
- Select streets and barangays for collection schedules.
- Manage collection routes.
- Monitor truck locations and collection progress.
- Review and respond to reported issues.
- Manage authorized accounts within their assigned municipality or district.

TRUCKER / DRIVER

Authorized garbage truck drivers who use the application during collection operations.

Main functions:

- Log in using their registered email and password.
- View assigned collection schedules.
- View assigned collection routes.
- Share their current GPS location while working.
- Update collection status.
- View relevant route and destination information.

RESIDENTS / PUBLIC USERS

Users who need information about municipal garbage collection services.

Main functions:

- View garbage collection schedules.
- Check collection routes.
- Monitor available truck location information.
- Submit reports about missed collections or inaccessible collection areas.
- Receive relevant updates regarding waste collection.

3\. MAIN APPLICATION FEATURES

A. INTERACTIVE MAP AND GPS TRACKING

The map is one of HAKOT's primary features.

It should display:

- Municipal and barangay boundaries.
- Streets and road segments.
- Planned garbage collection routes.
- Garbage truck locations when their devices are online.
- Route information when trucks are offline.

Tracking behavior:

- When a truck is online, display its current GPS location.
- When a truck is offline, display its planned route for the scheduled collection period.
- Keep route overlays separate from the underlying map.
- Only users belonging to the truck's assigned municipality can see its GPS location.

B. COLLECTION SCHEDULE MANAGEMENT

Administrators can create and manage garbage collection schedules.

Schedule information includes:

- Collection date.
- Start and end time.
- Municipality.
- Barangay.
- Street or streets.
- Assigned truck.
- Assigned driver.
- Collection status.

The system should support streets that pass through multiple barangays.

C. GARBAGE TRUCK MANAGEMENT

The application maintains truck information, including:

- Truck identification and plate number.
- Assigned municipality.
- Assigned driver.
- GPS availability.
- Current collection status.
- Assigned collection route.

D. REPORTS AND ISSUE MANAGEMENT

Residents can report waste collection concerns, including:

- Missed garbage collection.
- Streets without collection routes.
- Inaccessible collection areas.
- Other waste management concerns.

Reports can be forwarded to the appropriate municipal administrator for review and response. Anonymous reporting should also be supported.

E. ACCOUNT AND ACCESS MANAGEMENT

HAKOT supports role-based access.

- Administrators receive municipality- or district-specific access.
- Trucker accounts are authorized by the appropriate administrator.
- Truckers authenticate using their registered email and password.
- Usernames are retained as account/profile information, not used as login credentials.
- Users must only access information permitted by their assigned municipality and role.

4\. GIS AND DATABASE INFRASTRUCTURE

HAKOT uses Supabase and PostgreSQL with PostGIS for geographic data management.

The database is intended to store:

- Municipalities and provinces.
- Barangays and their boundaries.
- Streets and road segments.
- Collection schedules.
- Garbage trucks and drivers.
- GPS location records.
- Collection routes.
- User accounts and roles.
- Reports and audit logs.

Geographic data allows HAKOT to associate streets and road segments with their respective barangays and municipalities.

5\. MAIN APPLICATION WORKFLOW

1\. Administrator prepares collection operations.

Creates schedules, selects streets and barangays, and assigns trucks and drivers.

2\. Driver receives assigned schedule.

Logs in and views the collection schedule and route.

3\. Truck begins collection.

The application shares the truck's GPS location while it is online.

4\. Users view collection information.

Authorized users can view planned routes and available truck locations.

5\. Residents submit reports.

Reports are forwarded to the responsible municipal administrator.

6\. Administrator monitors operations.

Reviews collection progress, reports, and operational information.

6\. TECHNOLOGY STACK

- Mobile application: Android Studio
- Programming language: Kotlin
- Database: Supabase
- Database engine: PostgreSQL
- Geographic database: PostGIS
- Map data: OpenStreetMap
- Map display: OpenStreetMap-based map integration
- GPS tracking: Android Location Services
- Backend communication: Supabase APIs

7\. PILOT IMPLEMENTATION

The initial pilot municipality is Nasugbu, Batangas, Philippines.

The pilot establishes the geographic foundation for future expansion to other municipalities.

The system is intended to support multiple municipalities without hardcoding the application to Nasugbu. Each municipality should have its own administrators, trucks, drivers, schedules, routes, and geographic information.

8\. OVERALL VISION

HAKOT aims to transform traditional garbage collection management into a more organized, transparent, and data-driven system.

By combining GPS tracking, GIS mapping, digital scheduling, and resident reporting, HAKOT provides a centralized platform for municipalities to improve collection efficiency, monitor operations, and deliver better waste management services to their communities.

==================================================================================================
Nov 9 - Draft Diagram
<img width="6238" height="2502" alt="image" src="https://github.com/user-attachments/assets/2a2471ba-b09b-4cbb-8ab0-ef3afcd40e7c" />
Nov 10 - Removed Road macthing
NOV 10 - Updated Diagram
<img width="3129" height="2103" alt="HAKOT_Updated_UML" src="https://github.com/user-attachments/assets/76003649-6632-4da0-8376-6840ff21ef5c" />
