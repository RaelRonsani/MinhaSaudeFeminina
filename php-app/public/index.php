<?php
require_once __DIR__ . '/../src/bootstrap.php';
Auth::exigirLogin();

$msg = $_GET['msg'] ?? null;

try {
    $noticias = $supabase->select('noticias', [
        'select' => '*',
        'order'  => 'id.desc',
    ]);
} catch (Exception $e) {
    $noticias = [];
    $msg = 'Erro ao carregar: ' . $e->getMessage();
}

$titulo = 'Artigos';
require __DIR__ . '/../src/partials/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h1>Artigos</h1>
    <a href="/edit.php" class="btn btn-rosa">+ Novo Artigo</a>
</div>

<?php if ($msg): ?>
    <div class="alert alert-info alert-dismissible fade show">
        <?= e($msg) ?>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
<?php endif; ?>

<?php if (empty($noticias)): ?>
    <div class="card p-4 text-center">
        <p class="mb-3">Nenhum artigo ainda.</p>
        <a href="/edit.php" class="btn btn-rosa">Criar primeiro artigo</a>
    </div>
<?php else: ?>
    <div class="row g-3">
        <?php foreach ($noticias as $n): ?>
            <div class="col-md-6 col-lg-4">
                <div class="card h-100 shadow-sm">
                    <div class="card-body">
                        <span class="badge bg-dark mb-2"><?= e($n['categoria']) ?></span>
                        <h5 class="card-title"><?= e($n['titulo']) ?></h5>
                        <p class="card-text small text-muted">
                            <?= e(extrairPreview($n['conteudo'] ?? '', 120)) ?>
                        </p>
                    </div>
                    <div class="card-footer bg-transparent d-flex justify-content-between align-items-center">
                        <small class="text-muted">
                            Atualizado: <?= e(formatarData($n['updated_at'] ?? $n['created_at'] ?? null)) ?>
                        </small>
                        <div class="btn-group btn-group-sm">
                            <a href="/edit.php?id=<?= (int)$n['id'] ?>"
                               class="btn btn-outline-primary">Editar</a>
                            <form method="POST" action="/delete.php" class="d-inline"
                                  onsubmit="return confirm('Excluir permanentemente este artigo?');">
                                <input type="hidden" name="csrf" value="<?= e(Auth::csrfToken()) ?>">
                                <input type="hidden" name="id" value="<?= (int)$n['id'] ?>">
                                <button class="btn btn-outline-danger">Excluir</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        <?php endforeach; ?>
    </div>
<?php endif; ?>

<?php require __DIR__ . '/../src/partials/footer.php'; ?>
