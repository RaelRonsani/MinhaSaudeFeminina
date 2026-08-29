<?php
$usuario = Auth::usuarioLogado();
$titulo = $titulo ?? 'Gestão de Artigos - Minha Saúde Feminina';
?><!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= e($titulo) ?></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root {
            --rosa-forte: #F25C8A;
            --rosa-claro: #F8C8DC;
            --rosa-card: #FCE7F1;
        }
        body { background: var(--rosa-claro); min-height: 100vh; }
        .navbar { background: var(--rosa-forte) !important; }
        .navbar-brand, .nav-link { color: #000 !important; font-weight: bold; }
        .btn-rosa { background: var(--rosa-forte); color: #000; font-weight: bold; }
        .btn-rosa:hover { background: #d94e78; color: #fff; }
        .card { background: var(--rosa-card); }
        .ql-editor { min-height: 300px; background: #fff; }
        .preview-conteudo img { max-width: 100%; height: auto; }
        .preview-conteudo iframe { max-width: 100%; }
    </style>
</head>
<body>
<nav class="navbar navbar-expand-lg mb-4">
    <div class="container">
        <a class="navbar-brand" href="/index.php">Minha Saude Femina — Gestão</a>
        <?php if ($usuario): ?>
            <div class="d-flex align-items-center">
                <span class="me-3 fw-bold"><?= e($usuario['nome']) ?></span>
                <a href="/logout.php" class="btn btn-outline-dark btn-sm">Sair</a>
            </div>
        <?php endif; ?>
    </div>
</nav>
<div class="container">
