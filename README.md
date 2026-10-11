# AssistZaya (Android, Kotlin + Jetpack Compose)

## Como compilar
1. Abra esta pasta no **Android Studio** (Koala ou mais novo). Ele baixa o Gradle e gera o wrapper sozinho.
2. Aguarde o *Gradle Sync* e rode no celular/emulador (`Run ▶`) ou gere o APK em
   `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

> Este código foi escrito sem poder compilar no ambiente de criação. Se o Android Studio apontar
> algum erro de compilação, envie a mensagem que eu corrijo.

## O que já está feito (conforme a especificação)
| # | Item | Estado |
|---|------|--------|
| 1 | Tela inicial animada + verificação de internet + "Tentar novamente" | Pronto |
| 2 | Conta: entrar/criar, Google, perfil, Minha lista, Favoritos, Histórico | Local (sem servidor). Google precisa de Firebase |
| 3 | Início: banners em destaque, continuar assistindo, últimos episódios, Top 10, lançamentos, em alta, populares, animes/séries/filmes | Pronto (catálogo de demonstração) |
| 4 | Avaliar: 1–5 estrelas, gostei/não gostei, texto, média | Pronto, mas local (média de 1 usuário até haver backend) |
| 5 | Downloads offline: histórico, data, tamanho, assistir offline, remover | Pronto |
| 6 | Player AssistZaya: play/pause, progresso, volume, tela cheia, retomar | Pronto |
| 7 | VIP flutuante com X; liberado/bloqueado pela administração | Pronto (`AdminConfig.CONFIG_URL`) |
| 8 | Configurações (idioma, notificações, reprodução, downloads, conta, privacidade, termos, sobre) | Pronto |
| 9 | Idiomas: troca a interface inteira + "usar idioma do dispositivo" | Mecanismo pronto. Textos traduzidos: **Português, Inglês, Espanhol**. Os outros 17 caem em inglês até receberem `values-xx/strings.xml` |
| 10 | Suporte: WhatsApp +258 878 632 350 | Pronto |
| 11 | Privacidade/Termos/coleta de dados/apagar dados | Textos-modelo: **revise com um advogado** |
| 12 | Navegação: Início, Gêneros, Explorar, Downloads, Conta | Pronto |
| 13 | Backend/API, banco, painel admin, armazenamento de vídeos | **Falta** (veja abaixo) |

Também inclui o menu "O que deseja fazer?" (Assistir, Espelhar, Baixar, Ver detalhes).
Espelhar abre as opções de transmissão do Android.

## Conteúdo
O catálogo (`data/Catalog.kt`) usa só filmes abertos (Blender, Creative Commons) e vídeos de teste do Google.
Os downloads são para conteúdo que você tem autorização para distribuir.

## Próximos passos (backend)
- API de catálogo → trocar `Catalog.items` por chamadas de rede.
- Login real (Firebase Auth ou seu servidor) → `AppViewModel.signIn`.
- Avaliações compartilhadas → `AppViewModel.rate/average`.
- Painel admin → JSON apontado por `AdminConfig.CONFIG_URL` (`{"vipEnabled": true}`).
- Traduções: copie `res/values/strings.xml` para `values-fr`, `values-de`, etc.

## Compilar sem computador (GitHub Actions)
1. Crie um repositório no GitHub e envie TODOS os arquivos desta pasta (inclusive a pasta oculta `.github`).
2. Abra a aba **Actions** > **Build APK** > aguarde ficar verde (uns 5–10 min).
3. Abra a execução e baixe **AssistZaya-debug-apk** em *Artifacts* (vem em .zip com o APK dentro).
