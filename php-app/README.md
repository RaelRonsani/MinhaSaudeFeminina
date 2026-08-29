# Bem-vinda — Gestão Web de Artigos

Interface web administrativa para criar, editar e excluir artigos do app
**Bem-vinda/MinhaSaudeFeminina**. Integra com o mesmo Supabase que o app.

## Stack

PHP 8.2 puro (sem framework)
Bootstrap 5.3 (CDN, sem build)
Quill.js 2.0 (editor rich text, CDN)
Supabase REST API (via cURL)

## Estrutura

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
