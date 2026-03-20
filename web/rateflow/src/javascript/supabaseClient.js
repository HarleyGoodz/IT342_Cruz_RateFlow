import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://rgjdwqmjaywfmxaeboqj.supabase.co'
const supabaseKey = 'YOUR_ANON_KEY'

export const supabase = createClient(supabaseUrl, supabaseKey)