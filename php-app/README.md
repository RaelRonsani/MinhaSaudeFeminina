# Bem-vinda — Gestão Web de Artigos

Interface web administrativa para criar, editar e excluir artigos do app
**Bem-vinda/MinhaSaudeFeminina**. Integra com o mesmo Supabase que o app.

## Stack

- PHP 8.2 puro (sem framework)
- Bootstrap 5.3 (CDN, sem build)
- Quill.js 2.0 (editor rich text, CDN)
- Supabase REST API (via cURL)

## Estrutura

```
php-app/
├── public/               # webroot (o que o navegador acessa)
│   ├── index.php         # lista de artigos
│   ├── login.php         # login (só admins)
│   ├── logout.php
│   ├── edit.php          # criar + editar artigo (Quill.js)
│   └── delete.php        # POST delete
├── src/
│   ├── bootstrap.php     # includes comuns
│   ├── config.php        # lê env vars
│   ├── lib/
│   │   ├── SupabaseClient.php
│   │   ├── Auth.php
│   │   └── helpers.php
│   └── partials/
│       ├── header.php
│       └── footer.php
├── composer.json         # detecção de PHP no Railway
├── nixpacks.toml         # config de build
└── Procfile              # start command
```

## Setup

### Passo 1 — Aplicar migração no Supabase

No SQL Editor do Supabase, cole e execute `supabase_migration_v3.sql`
(está na pasta raiz do update, um nível acima desta pasta).

**Depois**, promova seu usuário a admin:

```sql
update usuarios set is_admin = true where email = 'seu@email.com';
```

Sem isso, o login web vai recusar com "usuário não é administrador".

### Passo 2 — Deploy no Railway

1. Crie conta em https://railway.com (login com GitHub).
2. Suba esta pasta (`php-app/`) para um repositório GitHub próprio, ou
   crie um subdiretório no seu repo existente.
3. Railway → **New Project** → **Deploy from GitHub repo** → escolhe o repo.
   Se estiver em subpasta, defina **Root Directory** apontando pra `php-app/`.
4. Aguarde o build (2-5 min primeira vez).
5. Vá em **Variables** e adicione:
   - `SUPABASE_URL` = sua Project URL (a mesma do app)
   - `SUPABASE_ANON_KEY` = sua anon public key
   - `APP_ENV` = `production`
6. Vá em **Settings** → **Networking** → **Generate Domain**. Railway
   te dá uma URL tipo `bemvinda-web-production.up.railway.app`.
7. Abra a URL. Deve mostrar o login.

### Passo 3 — Domínio próprio (opcional)

Em **Settings** → **Networking** → **Custom Domain**:
1. Cole seu domínio (ex: `admin.seudominio.com.br`).
2. Railway te dá um `CNAME` para configurar no painel DNS do seu
   registrador de domínio.
3. Aguarde propagação (5min-2h).
4. HTTPS ativa sozinho (Let's Encrypt automático).

### Passo 4 — Rodar localmente (opcional, pra dev)

```bash
cd php-app

# Windows (PowerShell)
$env:SUPABASE_URL="https://SEU-PROJETO.supabase.co"
$env:SUPABASE_ANON_KEY="sua-key"
php -S localhost:8000 -t public

# Linux/Mac
export SUPABASE_URL=https://SEU-PROJETO.supabase.co
export SUPABASE_ANON_KEY=sua-key
php -S localhost:8000 -t public
```

Abre http://localhost:8000

## Fluxo de uso

1. **Login** com email/senha do usuário admin (usa a mesma tabela `usuarios`
   do Supabase; a senha é comparada por hash SHA-256 como no app).
2. **Lista** todos os artigos ordenados do mais novo pro mais antigo.
3. **+ Novo Artigo** abre editor com Quill:
   - Selecione categoria (bate com o enum do app)
   - Escreva título e resumo
   - Formate o conteúdo (títulos, negrito, cores, listas, imagens via URL,
     vídeos via URL do YouTube/Vimeo)
   - Salvar
4. **Editar** carrega HTML existente no Quill; salvar substitui.
5. **Excluir** confirma via popup e deleta permanentemente.
6. O app Android/iOS pega as mudanças na próxima vez que carregar a lista
   (pull-to-refresh implementado no update do app).

## Segurança

Como decisões conscientes de projeto acadêmico:

- **Auth manual** (não Supabase Auth) — bate com o app.
- **RLS aberta** para anon no Supabase — validação de admin é feita
  na camada PHP (checa `is_admin` no login).
- **CSRF tokens** em todos os forms.
- **Sanitização de HTML** via whitelist antes de gravar no banco.
- **Sessão PHP** guarda o usuário logado (sem token; funciona porque
  Railway roda instância única).

Em produção real:
- Migrar para Supabase Auth (JWT + refresh tokens).
- Substituir sanitizador caseiro por HTMLPurifier via Composer.
- Adicionar rate limiting no login.
- Ativar HSTS.

## Limitações conhecidas

- Imagens/vídeos **por URL apenas** (não faz upload). Cole URL de
  imagem hospedada em qualquer lugar (Imgur, Cloudinary, etc) ou de
  vídeo do YouTube/Vimeo.
- Sem preview do artigo antes de salvar (o app é que renderiza).
- Sem histórico de versões (edição é destrutiva).
