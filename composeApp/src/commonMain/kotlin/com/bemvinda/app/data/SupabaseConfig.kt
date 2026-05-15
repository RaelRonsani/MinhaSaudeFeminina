package com.bemvinda.app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    const val SUPABASE_URL = "https://ttphbmirfuztgdslcwds.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR0cGhibWlyZnV6dGdkc2xjd2RzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg3MDU5MzAsImV4cCI6MjA5NDI4MTkzMH0.KXfxtT2LP1A0G8uC5G6a2SkDlfm8dFDzBfgxyD1xsAA"
}

/**
 * Cliente Supabase singleton compartilhado entre Android e iOS.
 *
 */
val supabase: SupabaseClient by lazy {
    createSupabaseClient(
        supabaseUrl = SupabaseConfig.SUPABASE_URL,
        supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
    }
}
