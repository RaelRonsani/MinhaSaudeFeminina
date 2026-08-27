<?php
/**
 * Autenticação com a mesma tabela `usuarios` do Supabase.
 *
 * Reproduz a lógica do app Android: hasheia a senha com SHA-256 e
 * compara com o campo `senha_hash`. Só permite login se `is_admin = true`.
 */
require_once __DIR__ . '/SupabaseClient.php';

class Auth
{
    private SupabaseClient $client;

    public function __construct(SupabaseClient $client)
    {
        $this->client = $client;
    }

    /**
     * Tenta login. Retorna o usuário se OK, null se falhou.
     * Requer is_admin = true.
     */
    public function login(string $email, string $senha): ?array
    {
        $hash = hash('sha256', $senha);
        try {
            $resultado = $this->client->select('usuarios', [
                'email'      => "eq.$email",
                'senha_hash' => "eq.$hash",
                'is_admin'   => 'eq.true',
                'select'     => '*',
                'limit'      => 1,
            ]);
        } catch (Exception $e) {
            error_log("Auth::login erro: " . $e->getMessage());
            return null;
        }

        if (empty($resultado)) {
            return null;
        }

        $usuario = $resultado[0];
        // Nunca expõe o hash na sessão
        unset($usuario['senha_hash']);
        return $usuario;
    }

    public static function iniciar(array $usuario): void
    {
        $_SESSION['usuario'] = $usuario;
        $_SESSION['login_at'] = time();
    }

    public static function logout(): void
    {
        $_SESSION = [];
        if (ini_get("session.use_cookies")) {
            $params = session_get_cookie_params();
            setcookie(session_name(), '', time() - 42000,
                $params["path"], $params["domain"],
                $params["secure"], $params["httponly"]
            );
        }
        session_destroy();
    }

    public static function usuarioLogado(): ?array
    {
        return $_SESSION['usuario'] ?? null;
    }

    public static function estaLogado(): bool
    {
        return isset($_SESSION['usuario']);
    }

    /**
     * Middleware: se não está logado, redireciona pro login.
     * Chame no topo de páginas protegidas.
     */
    public static function exigirLogin(string $loginUrl = '/login.php'): void
    {
        if (!self::estaLogado()) {
            header("Location: $loginUrl");
            exit;
        }
    }

    /**
     * Gera um token CSRF por sessão. Renova por request se preferir
     * — aqui reutilizamos, é aceitável para o escopo.
     */
    public static function csrfToken(): string
    {
        if (empty($_SESSION['csrf'])) {
            $_SESSION['csrf'] = bin2hex(random_bytes(32));
        }
        return $_SESSION['csrf'];
    }

    /**
     * Valida token CSRF vindo do form. Aborta se inválido.
     */
    public static function validarCsrf(?string $token): void
    {
        if (!$token || !hash_equals($_SESSION['csrf'] ?? '', $token)) {
            http_response_code(403);
            die('Token CSRF inválido. Recarregue a página e tente novamente.');
        }
    }
}
