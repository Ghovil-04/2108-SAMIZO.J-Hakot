import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

const PUBLIC_ACTIONS = ['VERIFY_ACTIVATION_CODE', 'RESEND_ACTIVATION_CODE', 'ACTIVATE_ACCOUNT_PASSWORD', 'UPLOAD_CERTIFICATE', 'REGISTER_ORGANIZATION', 'GET_PROFILE', 'GET_CLIPPED_ROADS', 'CHECK_TABLES']

async function sendActivationEmail(email: string, fullName: string, code: string) {
  const resendApiKey = Deno.env.get('RESEND_API_KEY')
  if (!resendApiKey) {
    console.warn("RESEND_API_KEY is not configured in Supabase Secrets. Email delivery skipped. Code: " + code)
    return { sent: false, error: "RESEND_API_KEY is missing in Supabase Edge Function secrets. Please configure Resend API key using 'supabase secrets set RESEND_API_KEY=...'" }
  }

  try {
    const res = await fetch('https://api.resend.com/emails', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${resendApiKey}`
      },
      body: JSON.stringify({
        from: 'HAKOT Platform <onboarding@resend.dev>',
        to: [email],
        subject: 'HAKOT Account Activation Code',
        html: `
          <div style="font-family: Arial, sans-serif; padding: 20px; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #E5E7EB; border-radius: 8px;">
            <h2 style="color: #10B981; margin-top: 0;">HAKOT Waste Management Platform</h2>
            <p>Hello <b>${fullName}</b>,</p>
            <p>Your municipal organization account application has been <b>approved</b> by the System Owner.</p>
            <p>Please use the following 6-digit activation code to set up your password and activate your account:</p>
            <div style="background: #F3F4F6; padding: 15px; font-size: 28px; font-weight: bold; letter-spacing: 6px; text-align: center; color: #1F2937; margin: 20px 0; border-radius: 6px;">
              ${code}
            </div>
            <p><b>Important:</b> This activation code will expire in <b>15 minutes</b> and can only be used once.</p>
            <p>If you did not request this account activation, please contact system support.</p>
            <br>
            <p>Best regards,<br><b>HAKOT System Operations</b></p>
          </div>
        `
      })
    })

    if (!res.ok) {
      const errText = await res.text()
      console.error("Resend API error:", errText)
      return { sent: false, error: `Resend API error: ${errText}` }
    }

    return { sent: true }
  } catch (e) {
    console.error("Email sending exception:", e)
    return { sent: false, error: e.message }
  }
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  if (req.method !== 'POST') {
    return new Response(JSON.stringify({ error: 'Method not allowed. Use POST.' }), {
      status: 405,
      headers: { ...corsHeaders, 'Content-Type': 'application/json', 'Allow': 'POST, OPTIONS' },
    })
  }

  try {
    let body
    try {
      body = await req.json()
    } catch (_) {
      return new Response(JSON.stringify({ error: 'Invalid JSON request body' }), {
        status: 400,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    const { action, applicationId, email, code, password, fileName, mimeType, fileBase64 } = body
    if (!action || typeof action !== 'string') {
      return new Response(JSON.stringify({ error: 'Missing or invalid action parameter' }), {
        status: 400,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? ''
    const supabaseAnonKey = Deno.env.get('SUPABASE_ANON_KEY') ?? ''
    const supabaseServiceKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''

    if (!supabaseUrl || !supabaseServiceKey) {
      return new Response(JSON.stringify({ error: 'Server configuration error' }), {
        status: 500,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    const supabaseAdmin = createClient(supabaseUrl, supabaseServiceKey)

    if (!PUBLIC_ACTIONS.includes(action)) {
      const authHeader = req.headers.get('Authorization')
      if (!authHeader) {
        return new Response(JSON.stringify({ error: 'Missing Authorization header' }), {
          status: 401,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const supabaseClient = createClient(supabaseUrl, supabaseAnonKey, {
        global: { headers: { Authorization: authHeader } },
      })

      const { data: { user }, error: userError } = await supabaseClient.auth.getUser()
      if (userError || !user) {
        return new Response(JSON.stringify({ error: 'Unauthorized: Invalid or expired token' }), {
          status: 401,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const { data: profile, error: profileError } = await supabaseClient
        .from('profiles')
        .select('role, account_status')
        .eq('id', user.id)
        .single()

      if (profileError || !profile || profile.role !== 'system_owner' || profile.account_status !== 'active') {
        return new Response(JSON.stringify({ error: 'Forbidden: Platform System Owner access required' }), {
          status: 403,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }
    }

    if (action === 'UPLOAD_CERTIFICATE') {
      if (!fileName || !fileBase64) {
        return new Response(JSON.stringify({ error: 'Missing file name or base64 data' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      // Check encoded payload length before decoding
      const estimatedLength = Math.floor((fileBase64.length * 3) / 4)
      if (estimatedLength > 5 * 1024 * 1024 + 1024) {
        return new Response(JSON.stringify({ error: 'Encoded payload exceeds the 5MB file limit.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      try {
        const binaryString = atob(fileBase64)
        const len = binaryString.length

        if (len > 5 * 1024 * 1024) {
          return new Response(JSON.stringify({ error: 'Certificate file size exceeds the 5MB limit.' }), {
            status: 400,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }

        if (len < 4) {
          return new Response(JSON.stringify({ error: 'Corrupted file payload. File is too small.' }), {
            status: 400,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }

        const fileBytes = new Uint8Array(len)
        for (let i = 0; i < len; i++) {
          fileBytes[i] = binaryString.charCodeAt(i)
        }

        const b0 = fileBytes[0], b1 = fileBytes[1], b2 = fileBytes[2], b3 = fileBytes[3]

        // Detect actual mime type from binary magic bytes
        let actualMime = ''
        if (b0 === 0x25 && b1 === 0x50 && b2 === 0x44 && b3 === 0x46) {
          actualMime = 'application/pdf'
        } else if (b0 === 0x89 && b1 === 0x50 && b2 === 0x4E && b3 === 0x47) {
          actualMime = 'image/png'
        } else if (b0 === 0xFF && b1 === 0xD8 && b2 === 0xFF) {
          actualMime = 'image/jpeg'
        }

        const allowedMimetypes = ['image/jpeg', 'image/jpg', 'image/png', 'application/pdf']
        const declaredMime = (mimeType || '').trim().toLowerCase()

        if (!actualMime && (!declaredMime || !allowedMimetypes.includes(declaredMime))) {
          return new Response(JSON.stringify({ error: 'Invalid file format. Only JPG, PNG, and PDF files are allowed.' }), {
            status: 400,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }

        const finalMime = actualMime || declaredMime

        if (!allowedMimetypes.includes(finalMime)) {
          return new Response(JSON.stringify({ error: 'Invalid file format. Only JPG, PNG, and PDF files are allowed.' }), {
            status: 400,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }

        if (declaredMime && declaredMime !== 'application/octet-stream' && actualMime && declaredMime !== actualMime && !(declaredMime === 'image/jpg' && actualMime === 'image/jpeg')) {
          return new Response(JSON.stringify({ error: 'Invalid file content or file signature does not match declared MIME type.' }), {
            status: 400,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }

        await supabaseAdmin.storage.createBucket('certificates', { public: false, fileSizeLimit: '5242880' }).catch(() => {})

        const cleanName = `cert_${Date.now()}_${fileName.replace(/[^a-zA-Z0-9.-]/g, '_')}`
        const objectPath = `account-applications/${cleanName}`

        const { data, error } = await supabaseAdmin.storage
          .from('certificates')
          .upload(objectPath, fileBytes, {
            contentType: finalMime,
            upsert: false
          })

        if (error) {
          return new Response(JSON.stringify({ error: `Supabase Storage upload failed: ${error.message}` }), {
            status: 400,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }

        return new Response(JSON.stringify({ success: true, certificatePath: `certificates/${objectPath}` }), {
          status: 200,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      } catch (e) {
        return new Response(JSON.stringify({ error: `Upload exception: ${e.message}` }), {
          status: 500,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }
    }

    if (action === 'CHECK_TABLES') {
      const { data, error } = await supabaseAdmin.rpc('exec_sql', { sql: `
        SELECT table_name, column_name, data_type
        FROM information_schema.columns
        WHERE table_schema = 'public';
      ` })
      return new Response(JSON.stringify({ data, error }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'GET_PROFILE') {
      const authHeader = req.headers.get('Authorization')
      if (!authHeader) {
        return new Response(JSON.stringify({ error: 'Missing Authorization header' }), {
          status: 401,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const supabaseClient = createClient(supabaseUrl, supabaseAnonKey, {
        global: { headers: { Authorization: authHeader } },
      })

      const { data: { user }, error: userError } = await supabaseClient.auth.getUser()
      if (userError || !user) {
        return new Response(JSON.stringify({ error: 'Unauthorized: Invalid or expired token' }), {
          status: 401,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      let { data: profile } = await supabaseAdmin
        .from('profiles')
        .select('*')
        .eq('id', user.id)
        .maybeSingle()

      if (!profile) {
        const { data: orgs } = await supabaseAdmin
          .from('organizations')
          .select('id')
          .limit(1)

        const orgId = orgs?.[0]?.id || null

        const { data: newProf, error: insErr } = await supabaseAdmin
          .from('profiles')
          .upsert({
            id: user.id,
            full_name: user.user_metadata?.full_name || user.email?.split('@')[0] || 'Administrator',
            role: 'municipal_admin',
            account_status: 'active',
            organization_id: orgId
          })
          .select()
          .maybeSingle()

        if (insErr) {
          return new Response(JSON.stringify({ error: `Profile creation failed: ${insErr.message}` }), {
            status: 500,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }
        profile = newProf
      }

      return new Response(JSON.stringify({ success: true, profile }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'GET_APP_ID') {
      const { data } = await supabaseAdmin
        .from('account_applications')
        .select('id, email, auth_user_id, status')
        .eq('email', email)
        .maybeSingle()

      return new Response(JSON.stringify({ data }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'GET_CLIPPED_ROADS') {
      const { barangay_id, barangay_name } = body

      if (!barangay_id && !barangay_name) {
        return new Response(JSON.stringify({ error: 'Missing barangay_id or barangay_name' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      let bgyId = barangay_id
      if (!bgyId && barangay_name) {
        const { data: bgyData } = await supabaseAdmin
          .from('barangays')
          .select('id')
          .ilike('name', barangay_name)
          .maybeSingle()
        bgyId = bgyData?.id
      }

      if (!bgyId) {
        return new Response(JSON.stringify({ error: 'Barangay not found' }), {
          status: 404,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      let { data: clippedRoads, error: rpcErr } = await supabaseAdmin.rpc('get_clipped_roads_for_bgy', { bgy_id: bgyId })

      if (rpcErr) {
        try {
          await supabaseAdmin.rpc('exec_sql', { sql: `
            CREATE OR REPLACE FUNCTION public.get_clipped_roads_for_bgy(bgy_id uuid)
            RETURNS TABLE (
                segment_id uuid,
                street_id uuid,
                segment_name text,
                road_type text,
                osm_way_id bigint,
                clipped_geojson json
            ) AS $$
            BEGIN
                RETURN QUERY
                SELECT
                    s.id AS segment_id,
                    s.street_id,
                    COALESCE(NULLIF(s.segment_name, ''), st.name, 'Unnamed road · ' || INITCAP(s.road_type)) AS segment_name,
                    s.road_type,
                    s.osm_way_id,
                    ST_AsGeoJSON(ST_Intersection(s.geometry, b.boundary))::JSON AS clipped_geojson
                FROM public.street_segments s
                LEFT JOIN public.streets st ON s.street_id = st.id
                JOIN public.barangays b ON b.id = bgy_id
                WHERE ST_Intersects(s.geometry, b.boundary)
                  AND NOT ST_IsEmpty(ST_Intersection(s.geometry, b.boundary));
            END;
            $$ LANGUAGE plpgsql STABLE;
          ` })
        } catch (_: any) {}

        const retryRes = await supabaseAdmin.rpc('get_clipped_roads_for_bgy', { bgy_id: bgyId })
        clippedRoads = retryRes.data
        rpcErr = retryRes.error
      }

      return new Response(JSON.stringify({ data: clippedRoads, error: rpcErr?.message }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'REGISTER_ORGANIZATION') {
      const { full_name, email, password, contact_number, municipality_id, office_department, organization_name, organization_type, cert_document_url } = body

      if (!full_name || !email || !password || !contact_number || !municipality_id || !office_department || !organization_name || !cert_document_url) {
        return new Response(JSON.stringify({ error: 'Missing required registration fields.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      if (password.length < 8) {
        return new Response(JSON.stringify({ error: 'Password must be at least 8 characters long.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const cleanEmail = email.trim().toLowerCase()

      const { data: listUsersData } = await supabaseAdmin.auth.admin.listUsers()
      const existingUser = listUsersData?.users?.find(u => u.email?.toLowerCase() === cleanEmail)
      if (existingUser) {
        return new Response(JSON.stringify({ error: 'An account with this email address already exists.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const { data: userData, error: createAuthErr } = await supabaseAdmin.auth.admin.createUser({
        email: cleanEmail,
        password: password,
        email_confirm: true,
        user_metadata: { full_name }
      })

      if (createAuthErr || !userData.user) {
        return new Response(JSON.stringify({ error: `Failed to create authentication account: ${createAuthErr?.message || 'Unknown error'}` }), {
          status: 500,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const userId = userData.user.id
      const assignedRole = 'municipal_admin'

      await supabaseAdmin
        .from('profiles')
        .upsert({
          id: userId,
          full_name,
          role: assignedRole,
          account_status: 'pending',
          municipality_id
        })

      const { error: appErr } = await supabaseAdmin
        .from('account_applications')
        .insert({
          auth_user_id: userId,
          full_name,
          email: cleanEmail,
          contact_number,
          municipality_id,
          office_department,
          official_position: 'Administrator',
          requested_role: assignedRole,
          organization_name,
          organization_type: organization_type || 'LGU',
          cert_document_url,
          status: 'PENDING'
        })

      if (appErr) {
        return new Response(JSON.stringify({ error: `Failed to submit application record: ${appErr.message}` }), {
          status: 500,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      return new Response(JSON.stringify({ success: true, message: 'Organization registration submitted successfully.' }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'CHECK_SCHEMA') {
      const { data: apps, error } = await supabaseAdmin
        .from('account_applications')
        .select('*')
        .limit(5)

      return new Response(JSON.stringify({ apps, error }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'LIST_PENDING') {
      const { data, error } = await supabaseAdmin
        .from('account_applications')
        .select('id, full_name, email, contact_number, municipality_id, office_department, official_position, requested_role, status, organization_name, organization_type, cert_document_url, submitted_at')
        .eq('status', 'PENDING')
        .order('submitted_at', { ascending: false })

      if (error) {
        return new Response(JSON.stringify({ error: error.message }), {
          status: 500,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      return new Response(JSON.stringify({ data }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'APPROVE') {
      if (!applicationId || typeof applicationId !== 'string') {
        return new Response(JSON.stringify({ error: 'Missing or invalid applicationId' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      // 1. Fetch application record with status PENDING
      const { data: claimedApp, error: fetchError } = await supabaseAdmin
        .from('account_applications')
        .select('*')
        .eq('id', applicationId)
        .eq('status', 'PENDING')
        .single()

      if (fetchError || !claimedApp) {
        const { data: existingApp } = await supabaseAdmin
          .from('account_applications')
          .select('status')
          .eq('id', applicationId)
          .single()

        if (existingApp?.status === 'APPROVED') {
          return new Response(JSON.stringify({ error: 'Conflict: Application has already been approved.' }), {
            status: 409,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }
        return new Response(JSON.stringify({ error: 'Conflict: Application not found or no longer pending.' }), {
          status: 409,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      // 2. Securely enforce Admin role (municipal_admin); never allow system_owner via public registration
      const assignedRole = 'municipal_admin'

      if (!claimedApp.municipality_id) {
        await supabaseAdmin.from('account_applications').update({ status: 'PENDING' }).eq('id', applicationId)
        return new Response(JSON.stringify({ error: 'Validation failed: Municipality scope ID is required for administrative accounts.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      // 3. Generate activation code (6 digits) and expiry (15 mins)
      const activationCode = Math.floor(100000 + Math.random() * 900000).toString()
      const activationExpiresAt = new Date(Date.now() + 15 * 60 * 1000).toISOString()

      // 4. Try inserting into verifieds table if created
      try {
        const { error: insertVerifiedErr } = await supabaseAdmin
          .from('verifieds')
          .insert({
            application_id: claimedApp.id,
            auth_user_id: claimedApp.auth_user_id,
            organization_name: claimedApp.organization_name,
            organization_type: claimedApp.organization_type,
            municipality_id: claimedApp.municipality_id,
            full_name: claimedApp.full_name,
            email: claimedApp.email,
            contact_number: claimedApp.contact_number,
            office_department: claimedApp.office_department,
            cert_document_url: claimedApp.cert_document_url,
            account_status: 'ACTIVE',
            approved_at: new Date().toISOString()
          })

        if (insertVerifiedErr) {
          console.warn("Verifieds table insert notice:", insertVerifiedErr.message)
        }
      } catch (vErr) {
        console.warn("Verifieds table insertion exception:", vErr)
      }

      // 5. Activate profile account status
      const { data: orgs } = await supabaseAdmin
        .from('organizations')
        .select('id')
        .limit(1)

      const orgId = claimedApp.organization_id || orgs?.[0]?.id || null

      await supabaseAdmin
        .from('profiles')
        .upsert({
          id: claimedApp.auth_user_id,
          full_name: claimedApp.full_name,
          role: assignedRole,
          account_status: 'active',
          municipality_id: claimedApp.municipality_id,
          organization_id: orgId
        })

      // 6. Permanently remove application record from account_applications
      await supabaseAdmin
        .from('account_applications')
        .delete()
        .eq('id', applicationId)

      return new Response(JSON.stringify({ success: true, message: 'Application approved, profile activated, and application record processed successfully.' }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'REJECT') {
      if (!applicationId || typeof applicationId !== 'string') {
        return new Response(JSON.stringify({ error: 'Missing or invalid applicationId' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const { data: appRecord, error: fetchErr } = await supabaseAdmin
        .from('account_applications')
        .select('*')
        .eq('id', applicationId)
        .eq('status', 'PENDING')
        .single()

      if (fetchErr || !appRecord) {
        return new Response(JSON.stringify({ error: 'Conflict: Application not found or no longer pending.' }), {
          status: 409,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      if (appRecord.requested_role === 'system_owner') {
        return new Response(JSON.stringify({ error: 'Forbidden: Cannot reject a System Owner application.' }), {
          status: 403,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      if (appRecord.auth_user_id) {
        const { data: profileCheck } = await supabaseAdmin
          .from('profiles')
          .select('role')
          .eq('id', appRecord.auth_user_id)
          .single()

        if (profileCheck && profileCheck.role === 'system_owner') {
          return new Response(JSON.stringify({ error: 'Forbidden: Cannot delete System Owner auth user.' }), {
            status: 403,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }

        const { error: deleteAuthErr } = await supabaseAdmin.auth.admin.deleteUser(appRecord.auth_user_id)
        if (deleteAuthErr) {
          console.error("Failed to delete rejected auth user:", deleteAuthErr)
        }
      }

      await supabaseAdmin
        .from('profiles')
        .update({ account_status: 'rejected' })
        .eq('email', appRecord.email)

      const { error: deleteAppErr } = await supabaseAdmin
        .from('account_applications')
        .delete()
        .eq('id', applicationId)

      if (deleteAppErr) {
        return new Response(JSON.stringify({ error: `Failed to remove rejected application: ${deleteAppErr.message}` }), {
          status: 500,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      return new Response(JSON.stringify({ success: true, message: 'Application rejected and removed successfully.' }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'VERIFY_ACTIVATION_CODE') {
      if (!email || !code) {
        return new Response(JSON.stringify({ error: 'Missing email or activation code' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const cleanEmail = email.trim().toLowerCase()
      const cleanCode = code.trim()

      const { data: apps, error } = await supabaseAdmin
        .from('account_applications')
        .select('*')
        .ilike('email', cleanEmail)
        .eq('status', 'APPROVED')
        .order('created_at', { ascending: false })
        .limit(1)

      if (error || !apps || apps.length === 0) {
        return new Response(JSON.stringify({ error: 'No approved account application found for this email address.' }), {
          status: 404,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const app = apps[0]
      if (app.activation_used) {
        return new Response(JSON.stringify({ error: 'This activation code has already been used.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      if (new Date(app.activation_expires_at) < new Date()) {
        return new Response(JSON.stringify({ error: 'Activation code has expired. Please request a new code.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      if (app.activation_code !== cleanCode) {
        return new Response(JSON.stringify({ error: 'Invalid activation code.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      return new Response(JSON.stringify({ success: true, message: 'Code verified successfully.' }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'RESEND_ACTIVATION_CODE') {
      if (!email) {
        return new Response(JSON.stringify({ error: 'Missing email address' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const cleanEmail = email.trim().toLowerCase()
      const { data: apps, error } = await supabaseAdmin
        .from('account_applications')
        .select('*')
        .ilike('email', cleanEmail)
        .eq('status', 'APPROVED')
        .order('created_at', { ascending: false })
        .limit(1)

      if (error || !apps || apps.length === 0) {
        return new Response(JSON.stringify({ error: 'No approved account application found for this email address.' }), {
          status: 404,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const app = apps[0]
      const newCode = Math.floor(100000 + Math.random() * 900000).toString()
      const newExpiry = new Date(Date.now() + 15 * 60 * 1000).toISOString()

      const { error: updateErr } = await supabaseAdmin
        .from('account_applications')
        .update({
          activation_code: newCode,
          activation_expires_at: newExpiry,
          activation_used: false
        })
        .eq('id', app.id)

      if (updateErr) {
        return new Response(JSON.stringify({ error: `Failed to generate new activation code: ${updateErr.message}` }), {
          status: 500,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const emailResult = await sendActivationEmail(app.email, app.full_name, newCode)
      if (!emailResult.sent) {
        return new Response(JSON.stringify({ error: `Failed to resend activation code email: ${emailResult.error}` }), {
          status: 500,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      return new Response(JSON.stringify({ success: true, message: 'New activation code generated and sent successfully.' }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    if (action === 'ACTIVATE_ACCOUNT_PASSWORD') {
      if (!email || !code || !password) {
        return new Response(JSON.stringify({ error: 'Missing email, activation code, or password' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      if (password.length < 6) {
        return new Response(JSON.stringify({ error: 'Password must be at least 6 characters long.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const cleanEmail = email.trim().toLowerCase()
      const cleanCode = code.trim()

      const { data: apps, error } = await supabaseAdmin
        .from('account_applications')
        .select('*')
        .ilike('email', cleanEmail)
        .eq('status', 'APPROVED')
        .order('created_at', { ascending: false })
        .limit(1)

      if (error || !apps || apps.length === 0) {
        return new Response(JSON.stringify({ error: 'No approved account application found for this email address.' }), {
          status: 404,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      const app = apps[0]
      if (app.activation_used) {
        return new Response(JSON.stringify({ error: 'This activation code has already been used.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      if (new Date(app.activation_expires_at) < new Date()) {
        return new Response(JSON.stringify({ error: 'Activation code has expired. Please request a new code.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      if (app.activation_code !== cleanCode) {
        return new Response(JSON.stringify({ error: 'Invalid activation code.' }), {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        })
      }

      // Check or create auth user
      let userId: string | null = null
      const { data: listUsersData } = await supabaseAdmin.auth.admin.listUsers()
      const existingUser = listUsersData?.users?.find(u => u.email?.toLowerCase() === cleanEmail)

      if (existingUser) {
        userId = existingUser.id
        const { error: updateAuthErr } = await supabaseAdmin.auth.admin.updateUserById(userId, { password })
        if (updateAuthErr) {
          return new Response(JSON.stringify({ error: `Failed to update auth password: ${updateAuthErr.message}` }), {
            status: 500,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }
      } else {
        const { data: createData, error: createErr } = await supabaseAdmin.auth.admin.createUser({
          email: cleanEmail,
          password: password,
          email_confirm: true,
          user_metadata: { full_name: app.full_name }
        })
        if (createErr || !createData.user) {
          return new Response(JSON.stringify({ error: `Failed to create auth user: ${createErr?.message}` }), {
            status: 500,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }
        userId = createData.user.id
      }

      // Create or verify profile with secure admin role ('municipal_admin'); never system_owner
      const assignedRole = 'municipal_admin'
      const { data: existingProfile } = await supabaseAdmin
        .from('profiles')
        .select('id')
        .eq('id', userId)
        .single()

      if (!existingProfile) {
        const { error: profileErr } = await supabaseAdmin
          .from('profiles')
          .insert({
            id: userId,
            full_name: app.full_name,
            email: cleanEmail,
            role: assignedRole,
            account_status: 'active',
            municipality_id: app.municipality_id,
            office_department: app.office_department,
            official_position: 'Administrator'
          })
        if (profileErr) {
          return new Response(JSON.stringify({ error: `Failed to create profile: ${profileErr.message}` }), {
            status: 500,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          })
        }
      } else {
        await supabaseAdmin
          .from('profiles')
          .update({ account_status: 'active', role: assignedRole })
          .eq('id', userId)
      }

      // Mark activation code as used
      await supabaseAdmin
        .from('account_applications')
        .update({ activation_used: true })
        .eq('id', app.id)

      return new Response(JSON.stringify({ success: true, message: 'Account activated successfully. You may now sign in.' }), {
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      })
    }

    return new Response(JSON.stringify({ error: 'Invalid action parameter' }), {
      status: 400,
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
    })

  } catch (err) {
    return new Response(JSON.stringify({ error: err.message ?? 'Internal server error' }), {
      status: 500,
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
    })
  }
})
