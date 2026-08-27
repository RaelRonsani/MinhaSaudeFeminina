<?php
/**
 * Ponto de entrada comum. Inclua no topo de todas as páginas com:
 *   require_once __DIR__ . '/../src/bootstrap.php';
 *
 * Deixa disponíveis:
 *   $config   → array de configuração
 *   $supabase → SupabaseClient já autenticado
 *   $auth     → objeto Auth pronto para uso
 */
require_once __DIR__ . '/lib/SupabaseClient.php';
require_once __DIR__ . '/lib/Auth.php';
require_once __DIR__ . '/lib/helpers.php';

$config = require __DIR__ . '/config.php';
$supabase = new SupabaseClient($config['supabase_url'], $config['supabase_anon_key']);
$auth = new Auth($supabase);
