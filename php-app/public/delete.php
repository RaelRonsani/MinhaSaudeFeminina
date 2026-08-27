<?php
require_once __DIR__ . '/../src/bootstrap.php';
Auth::exigirLogin();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    exit('Método não permitido');
}

Auth::validarCsrf($_POST['csrf'] ?? null);

$id = (int)($_POST['id'] ?? 0);
if ($id <= 0) {
    header('Location: /index.php?msg=ID inválido');
    exit;
}

try {
    $supabase->delete('noticias', ['id' => "eq.$id"]);
    header('Location: /index.php?msg=Artigo excluído');
} catch (Exception $e) {
    header('Location: /index.php?msg=Erro ao excluir: ' . urlencode($e->getMessage()));
}
exit;
