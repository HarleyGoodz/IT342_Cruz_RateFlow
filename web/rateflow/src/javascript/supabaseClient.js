import { createClient } from '@supabase/supabase-js'

const supabaseUrl =
  'https://rgjdwqmjaywfmxaeboqj.supabase.co'

const supabaseKey =
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJnamR3cW1qYXl3Zm14YWVib3FqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI4MTg2MDksImV4cCI6MjA4ODM5NDYwOX0.hlqDR8ZjzrPSHLIdY_uo0Q7lFPakZNDkb8oiCT1PLAo'

export const supabase = createClient(
  supabaseUrl,
  supabaseKey,
  {
    auth: {
      persistSession: true,
      autoRefreshToken: true,
      detectSessionInUrl: true
    }
  }
)