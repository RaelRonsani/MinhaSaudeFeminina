<?php
/**
 * Cliente mínimo para a REST API do Supabase (PostgREST).
 * Usa curl direto para não depender de Composer/vendor pesado.
 *
 * Documentação: https://postgrest.org/en/stable/api.html
 */
class SupabaseClient
{
    private string $url;
    private string $key;

    public function __construct(string $url, string $key)
    {
        // Remove barra final para evitar // nas URLs
        $this->url = rtrim($url, '/');
        $this->key = $key;
    }

    /**
     * GET /rest/v1/{tabela}?{query}
     *
     * @param string $tabela  Ex: "noticias"
     * @param array  $query   Ex: ['id' => 'eq.1', 'select' => '*', 'order' => 'id.desc']
     * @return array          Lista de registros decodificada de JSON
     */
    public function select(string $tabela, array $query = []): array
    {
        $qs = http_build_query($query);
        $url = "{$this->url}/rest/v1/{$tabela}" . ($qs ? "?{$qs}" : '');
        return $this->request('GET', $url);
    }

    /**
     * POST /rest/v1/{tabela} com body JSON.
     * Retorna o registro criado (Prefer: return=representation).
     */
    public function insert(string $tabela, array $dados): array
    {
        $url = "{$this->url}/rest/v1/{$tabela}";
        return $this->request('POST', $url, $dados, ['Prefer: return=representation']);
    }

    /**
     * PATCH /rest/v1/{tabela}?id=eq.{id} com body JSON.
     * Retorna o registro atualizado.
     */
    public function update(string $tabela, array $filtros, array $dados): array
    {
        $qs = http_build_query($filtros);
        $url = "{$this->url}/rest/v1/{$tabela}?{$qs}";
        return $this->request('PATCH', $url, $dados, ['Prefer: return=representation']);
    }

    /**
     * DELETE /rest/v1/{tabela}?id=eq.{id}
     * Retorna [] se sucesso.
     */
    public function delete(string $tabela, array $filtros): array
    {
        $qs = http_build_query($filtros);
        $url = "{$this->url}/rest/v1/{$tabela}?{$qs}";
        return $this->request('DELETE', $url);
    }

    /**
     * Executa a requisição HTTP com os headers de auth do Supabase.
     *
     * @throws RuntimeException se a resposta não for 2xx
     */
    private function request(string $method, string $url, ?array $body = null, array $extraHeaders = []): array
    {
        $headers = array_merge([
            "apikey: {$this->key}",
            "Authorization: Bearer {$this->key}",
            "Content-Type: application/json",
            "Accept: application/json",
        ], $extraHeaders);

        $ch = curl_init($url);
        curl_setopt_array($ch, [
            CURLOPT_CUSTOMREQUEST  => $method,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_HTTPHEADER     => $headers,
            CURLOPT_TIMEOUT        => 20,
        ]);

        if ($body !== null) {
            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($body, JSON_UNESCAPED_UNICODE));
        }

        $resposta = curl_exec($ch);
        $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $erro = curl_error($ch);
        curl_close($ch);

        if ($resposta === false) {
            throw new RuntimeException("Erro cURL: $erro");
        }

        if ($status < 200 || $status >= 300) {
            throw new RuntimeException("Supabase retornou HTTP $status: $resposta");
        }

        // DELETE pode voltar vazio
        if ($resposta === '' || $resposta === null) {
            return [];
        }

        $json = json_decode($resposta, true);
        if (!is_array($json)) {
            return [];
        }
        return $json;
    }
}
