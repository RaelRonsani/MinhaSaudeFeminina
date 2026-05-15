package com.bemvinda.app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * ========================================================================
 *                  CONFIGURAÇÃO DO SUPABASE
 * ========================================================================
 *
 * Coloque aqui as credenciais do seu projeto Supabase.
 * Você obtém em: https://supabase.com/dashboard/project/_/settings/api
 *
 *   1. SUPABASE_URL  = "Project URL"
 *   2. SUPABASE_ANON_KEY = "anon public" key (NÃO use a service_role key
 *      no app cliente — ela bypassa RLS).
 *
 * Para projeto acadêmico está OK deixar hardcoded.
 * Em produção: use BuildConfig (Android) e Info.plist (iOS) ou
 * variáveis de ambiente injetadas no build.
 *
 * As tabelas necessárias estão definidas em /supabase_schema.sql na raiz
 * do projeto. Execute esse SQL no editor SQL do seu Supabase antes de
 * rodar o app.
 * ========================================================================
 */
object SupabaseConfig {
    const val SUPABASE_URL = "https://ttphbmirfuztgdslcwds.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR0cGhibWlyZnV6dGdkc2xjd2RzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg3MDU5MzAsImV4cCI6MjA5NDI4MTkzMH0.KXfxtT2LP1A0G8uC5G6a2SkDlfm8dFDzBfgxyD1xsAA"
}

/**
 * Cliente Supabase singleton compartilhado entre Android e iOS.
 * O Ktor engine é injetado automaticamente em cada plataforma
 * (ver dependências em build.gradle.kts: ktor-client-android e
 * ktor-client-darwin).
 */
val supabase: SupabaseClient by lazy {
    createSupabaseClient(
        supabaseUrl = SupabaseConfig.SUPABASE_URL,
        supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
    }
}
