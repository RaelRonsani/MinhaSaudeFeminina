<?php
/**
 * Configuração da aplicação. Lê variáveis de ambiente definidas no Railway.
 * Em desenvolvimento local, defina no XAMPP ou via php -S com variáveis.
 *
 * Se as env vars não existirem, usa placeholders para não crashar (mas
 * o app não vai funcionar corretamente).
 */

// Habilita erros em dev, esconde em prod
$isDev = getenv('APP_ENV') !== 'production';
if ($isDev) {
    ini_set('display_errors', '1');
    error_reporting(E_ALL);
} else {
    ini_set('display_errors', '0');
    error_reporting(0);
}

session_start();

return [
    'supabase_url'      => getenv('SUPABASE_URL')      ?: 'https://SEU-PROJETO.supabase.co',
    'supabase_anon_key' => getenv('SUPABASE_ANON_KEY') ?: 'sua-anon-key-aqui',
    'app_url'           => getenv('APP_URL')           ?: 'http://localhost:8000',
    'is_dev'            => $isDev,
];
