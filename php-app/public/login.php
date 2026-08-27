<?php
require_once __DIR__ . '/../src/bootstrap.php';

if (Auth::estaLogado()) {
    header('Location: /index.php');
    exit;
}

$erro = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    Auth::validarCsrf($_POST['csrf'] ?? null);
    $email = trim($_POST['email'] ?? '');
    $senha = $_POST['senha'] ?? '';

    if ($email === '' || $senha === '') {
        $erro = 'Preencha email e senha.';
    } else {
        $usuario = $auth->login($email, $senha);
        if ($usuario) {
            Auth::iniciar($usuario);
            header('Location: /index.php');
            exit;
        } else {
            $erro = 'Credenciais inválidas ou usuário não é administrador.';
        }
    }
}

$titulo = 'Login';
require __DIR__ . '/../src/partials/header.php';
?>

<div class="row justify-content-center mt-5">
    <div class="col-md-5">
        <div class="card p-4 shadow">
            <h2 class="text-center mb-4">Acesso Administrativo</h2>

            <?php if ($erro): ?>
                <div class="alert alert-danger"><?= e($erro) ?></div>
            <?php endif; ?>

            <form method="POST">
                <input type="hidden" name="csrf" value="<?= e(Auth::csrfToken()) ?>">

                <div class="mb-3">
                    <label class="form-label fw-bold">Email:</label>
                    <input type="email" name="email" class="form-control" required autofocus
                           value="<?= e($_POST['email'] ?? '') ?>">
                </div>

                <div class="mb-3">
                    <label class="form-label fw-bold">Senha:</label>
                    <input type="password" name="senha" class="form-control" required>
                </div>

                <button type="submit" class="btn btn-rosa w-100">Entrar</button>
            </form>

            <hr class="my-4">
            <small class="text-muted d-block text-center">
                Só usuários com <strong>is_admin = true</strong> podem entrar.
            </small>
        </div>
    </div>
</div>

<?php require __DIR__ . '/../src/partials/footer.php'; ?>
