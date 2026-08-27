<?php
require_once __DIR__ . '/../src/bootstrap.php';
Auth::exigirLogin();

$id = isset($_GET['id']) ? (int)$_GET['id'] : null;
$modo = $id ? 'editar' : 'criar';
$erro = null;

// Carrega artigo existente se for edição
$artigo = [
    'categoria' => '',
    'titulo'    => '',
    'resumo'    => '',
    'conteudo'  => '',
];
if ($modo === 'editar') {
    try {
        $resultado = $supabase->select('noticias', [
            'id'     => "eq.$id",
            'select' => '*',
            'limit'  => 1,
        ]);
        if (empty($resultado)) {
            header('Location: /index.php?msg=Artigo não encontrado');
            exit;
        }
        $artigo = $resultado[0];
    } catch (Exception $e) {
        $erro = 'Erro ao carregar: ' . $e->getMessage();
    }
}

// Processa submit
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    Auth::validarCsrf($_POST['csrf'] ?? null);

    $categoria = trim($_POST['categoria'] ?? '');
    $titulo    = trim($_POST['titulo'] ?? '');
    $resumo    = trim($_POST['resumo'] ?? '');
    $conteudoBruto = $_POST['conteudo'] ?? '';

    // Validações
    if ($categoria === '' || $titulo === '' || trim(strip_tags($conteudoBruto)) === '') {
        $erro = 'Categoria, título e conteúdo são obrigatórios.';
    } elseif (!in_array($categoria, categoriasDisponiveis(), true)) {
        $erro = 'Categoria inválida.';
    } else {
        $conteudo = sanitizarHtml($conteudoBruto);

        $dados = [
            'categoria' => $categoria,
            'titulo'    => $titulo,
            'resumo'    => $resumo,
            'conteudo'  => $conteudo,
        ];

        try {
            if ($modo === 'criar') {
                $supabase->insert('noticias', $dados);
                header('Location: /index.php?msg=Artigo criado com sucesso');
            } else {
                $supabase->update('noticias', ['id' => "eq.$id"], $dados);
                header('Location: /index.php?msg=Artigo atualizado com sucesso');
            }
            exit;
        } catch (Exception $e) {
            $erro = 'Erro ao salvar: ' . $e->getMessage();
        }
    }

    // Se falhou, mantém o que o usuário digitou
    $artigo = array_merge($artigo, [
        'categoria' => $categoria,
        'titulo'    => $titulo,
        'resumo'    => $resumo,
        'conteudo'  => $conteudoBruto,
    ]);
}

$titulo = $modo === 'criar' ? 'Novo Artigo' : 'Editar Artigo';
require __DIR__ . '/../src/partials/header.php';
?>

<link href="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.snow.css" rel="stylesheet">

<div class="d-flex justify-content-between align-items-center mb-3">
    <h1><?= $modo === 'criar' ? 'Novo Artigo' : 'Editar Artigo' ?></h1>
    <a href="/index.php" class="btn btn-outline-dark">← Voltar</a>
</div>

<?php if ($erro): ?>
    <div class="alert alert-danger"><?= e($erro) ?></div>
<?php endif; ?>

<form method="POST" id="formArtigo">
    <input type="hidden" name="csrf" value="<?= e(Auth::csrfToken()) ?>">
    <!-- Quill escreve HTML aqui via JS antes do submit -->
    <input type="hidden" name="conteudo" id="conteudoHidden">

    <div class="card p-4 mb-3">
        <div class="row g-3">
            <div class="col-md-4">
                <label class="form-label fw-bold">Categoria:</label>
                <select name="categoria" class="form-select" required>
                    <option value="">Selecione…</option>
                    <?php foreach (categoriasDisponiveis() as $cat): ?>
                        <option value="<?= e($cat) ?>"
                            <?= $artigo['categoria'] === $cat ? 'selected' : '' ?>>
                            <?= e($cat) ?>
                        </option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div class="col-md-8">
                <label class="form-label fw-bold">Título:</label>
                <input type="text" name="titulo" class="form-control" required
                       maxlength="200" value="<?= e($artigo['titulo']) ?>">
            </div>
        </div>

        <div class="mt-3">
            <label class="form-label fw-bold">Resumo (opcional, aparece na lista):</label>
            <textarea name="resumo" class="form-control" rows="2" maxlength="300"><?= e($artigo['resumo']) ?></textarea>
        </div>

        <div class="mt-3">
            <label class="form-label fw-bold">Conteúdo:</label>
            <small class="text-muted d-block mb-2">
                Formate, insira imagens e vídeos. Use ícone de imagem para colar URL.
            </small>
            <div id="editor"></div>
        </div>
    </div>

    <div class="d-flex justify-content-end gap-2">
        <a href="/index.php" class="btn btn-outline-dark">Cancelar</a>
        <button type="submit" class="btn btn-rosa">Salvar</button>
    </div>
</form>

<script src="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.js"></script>
<script>
    // Toolbar completa com títulos, formatação, cor, listas, links, imagem, vídeo
    const quill = new Quill('#editor', {
        theme: 'snow',
        placeholder: 'Escreva o artigo aqui…',
        modules: {
            toolbar: [
                [{ header: [1, 2, 3, false] }],
                ['bold', 'italic', 'underline', 'strike'],
                [{ color: [] }, { background: [] }],
                [{ list: 'ordered' }, { list: 'bullet' }],
                ['blockquote', 'code-block'],
                ['link', 'image', 'video'],
                ['clean'],
            ],
        },
    });

    // Preenche o editor com conteúdo existente (se editando)
    const conteudoInicial = <?= json_encode($artigo['conteudo'], JSON_UNESCAPED_UNICODE | JSON_HEX_TAG) ?>;
    if (conteudoInicial) {
        quill.clipboard.dangerouslyPasteHTML(conteudoInicial);
    }

    // Antes de submeter, copia HTML do Quill para o input hidden
    document.getElementById('formArtigo').addEventListener('submit', function (e) {
        const html = quill.root.innerHTML.trim();
        // Quill retorna <p><br></p> quando vazio - trata como vazio
        if (html === '<p><br></p>' || html === '') {
            e.preventDefault();
            alert('Escreva o conteúdo do artigo antes de salvar.');
            return false;
        }
        document.getElementById('conteudoHidden').value = html;
    });
</script>

<?php require __DIR__ . '/../src/partials/footer.php'; ?>
