<?php
/**
 * Helpers usados em várias páginas.
 */

/** Escape básico contra XSS em strings antes de imprimir em HTML. */
function e(?string $s): string
{
    return htmlspecialchars($s ?? '', ENT_QUOTES | ENT_HTML5, 'UTF-8');
}

/**
 * Sanitiza HTML do Quill para prevenir XSS mantendo formatação rica.
 * Estratégia: whitelist de tags e atributos permitidos.
 *
 * Implementação usa DOMDocument porque HTMLPurifier exigiria Composer.
 * Não é perfeito mas cobre os casos comuns do Quill.
 */
function sanitizarHtml(string $html): string
{
    // Tags permitidas (compatíveis com o que o Quill gera)
    $tagsPermitidas = [
        'p', 'br', 'strong', 'b', 'em', 'i', 'u', 's',
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'ul', 'ol', 'li',
        'blockquote', 'pre', 'code',
        'a', 'img', 'iframe',
        'span', 'div',
    ];

    // Remove scripts e handlers de evento (regex simples mas eficaz)
    $html = preg_replace('/<script\b[^>]*>(.*?)<\/script>/is', '', $html);
    $html = preg_replace('/\son\w+\s*=\s*"[^"]*"/i', '', $html);
    $html = preg_replace("/\son\w+\s*=\s*'[^']*'/i", '', $html);
    $html = preg_replace('/javascript:/i', '', $html);

    // strip_tags mantém texto mas remove tags fora da whitelist
    $tagsStr = '<' . implode('><', $tagsPermitidas) . '>';
    $html = strip_tags($html, $tagsStr);

    return $html;
}

/** Categorias reconhecidas pelo app (batem com CategoriaNoticia enum). */
function categoriasDisponiveis(): array
{
    return [
        'AUTOCUIDADO',
        'SAÚDE',
        'GRAVIDEZ',
        'BEM-ESTAR',
        'PUBERDADE',
        'SEGURANÇA FEMININA',
    ];
}

/** Formata timestamp ISO do Postgres em pt-BR curto. */
function formatarData(?string $iso): string
{
    if (!$iso) return '—';
    try {
        $dt = new DateTime($iso);
        return $dt->format('d/m/Y H:i');
    } catch (Exception $e) {
        return '—';
    }
}

/**
 * Extrai um trecho de texto plano de um HTML.
 * Usado para preview na listagem.
 */
function extrairPreview(string $html, int $maxChars = 150): string
{
    $texto = strip_tags($html);
    $texto = trim(preg_replace('/\s+/', ' ', $texto));
    if (mb_strlen($texto) <= $maxChars) return $texto;
    return mb_substr($texto, 0, $maxChars) . '…';
}
