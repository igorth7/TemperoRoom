# Tempero Room – Trabalho 3 (Persistência com Room)

**Aluno:** Igor  
**Turma:** 2 [Android - ECO] - Ter/Qua Manhã  
**Disciplina:** Desenvolvimento de Aplicativos Móveis – Prof. Andre Luis

## 1. Sobre o projeto

O **Tempero Room** é a evolução do app do Trabalho 2 (Tempero MAF): o mesmo app de
receitas e lista de compras, com as mesmas 7 telas, mas agora **tudo fica salvo no
banco de dados do celular com o Room**. Fechou o app e abriu de novo, os dados continuam lá.

## 2. Como rodar

1. Abra a pasta `TemperoRoom` no **Android Studio**.
2. Espere o **Gradle Sync** terminar (ele baixa o Room e o KSP).
3. Escolha um emulador (ou celular) e clique em **Run ▶**.
4. Na primeira vez o banco está vazio: toque em **"Carregar receitas de exemplo"** ou cadastre no **+**.

## 3. O que foi feito (requisitos)

| Requisito | Onde está |
|---|---|
| Dependências do Room (runtime + compilador com KSP) | `app/build.gradle.kts` e `build.gradle.kts` |
| 2 `@Entity` com `@PrimaryKey(autoGenerate = true) val id: Int = 0` | `data/local/Receita.kt` e `data/local/ItemCompra.kt` |
| 2 `@Dao` com `suspend` `@Insert`, `@Query` (buscarTodos), `@Update`, `@Delete` | `data/local/ReceitaDao.kt` e `data/local/ItemCompraDao.kt` |
| Consulta filtrada `buscarPorId(id: Int)` | Nos dois DAOs (e `buscarPorReceita` / `limparComprados` no de itens) |
| Tela de detalhe busca o item no banco pelo id da navegação | `TelaDetalheReceita.kt` e `TelaDetalheItem.kt` |
| `AppDatabase` abstrata com `companion object`, `@Volatile` e `synchronized` (Singleton) | `data/local/AppDatabase.kt` |
| Toda chamada ao banco dentro de `CoroutineScope(Dispatchers.IO).launch { }` | Todas as telas em `ui/telas/` |
| `LaunchedEffect(Unit)` para carregar ao abrir a tela | Todas as telas em `ui/telas/` |
| `try/catch` com `Log.e` | Em toda operação no banco |
| Adicionar, editar (Update) e remover salvos no banco | Formulários (novo/editar), favoritar, marcar comprado, quantidade, excluir |

### Estrutura

```
com.igor.tempero
├── MainActivity.kt
├── data/local/
│   ├── Receita.kt, ItemCompra.kt       → @Entity (tabelas)
│   ├── ReceitaDao.kt, ItemCompraDao.kt → @Dao (SQL)
│   ├── AppDatabase.kt                  → @Database (Singleton)
│   └── ReceitasExemplo.kt              → receitas do botão "Carregar exemplos"
├── navigation/  → Rotas.kt e AppNavigation.kt (NavHost)
└── ui/          → componentes, telas e tema
```

## 4. Respostas e evidências

**Como os dados eram guardados no Trabalho 2?**  
Em duas listas `mutableStateListOf` criadas no `AppNavigation`. Elas existiam só
enquanto o app estava aberto: fechou o app, tudo voltava para os dados iniciais.

**Quais Entity, DAO e Database criei e por quê?**  
- `Receita` (tabela `receitas`) e `ItemCompra` (tabela `itens_compra`): são os dois
  dados do app. O `id` é gerado pelo Room (`autoGenerate = true`). Como o Room não
  guarda `List<String>` direto, os ingredientes viraram um texto com um ingrediente
  por linha (`listaIngredientes()` transforma de volta em lista).
  `ItemCompra.receitaId` liga o item à receita de onde ele veio.
- `ReceitaDao` e `ItemCompraDao`: cada um com o CRUD da sua tabela, tudo `suspend`
  porque acessar banco é demorado e não pode travar a tela.
- `AppDatabase`: junta as duas tabelas. É um Singleton (`@Volatile` + `synchronized`)
  para existir só uma conexão com o banco no app inteiro.

**Quais consultas (queries) usei?**  
- `SELECT * FROM receitas ORDER BY id DESC` – todas as receitas, as novas primeiro.
- `SELECT * FROM receitas WHERE id = :id` – uma receita (tela de detalhe e edição).
- `SELECT * FROM itens_compra ORDER BY comprado ASC, id ASC` – itens, os pendentes primeiro.
- `SELECT * FROM itens_compra WHERE id = :id` – um item.
- `SELECT * FROM itens_compra WHERE receitaId = :receitaId` – itens de uma receita.
- `DELETE FROM itens_compra WHERE comprado = 1` – botão "Limpar comprados".
- Inserir, atualizar e apagar usam `@Insert`, `@Update` e `@Delete`.

**Qual foi a dificuldade de sair do `mutableStateListOf` para o Room?**  
- Antes a tela mexia direto na lista; agora toda operação precisa ir para uma
  thread de fundo (`Dispatchers.IO`) e, depois, voltar para a thread da tela
  (`withContext(Dispatchers.Main)`) para atualizar o que aparece.
- Depois de gravar, a tela precisa buscar de novo no banco para mostrar a mudança.
- Passar o objeto inteiro pela navegação não funciona bem; passei só o **id** e
  a tela de detalhe busca no banco com `buscarPorId(id)`.
- Guardar a lista de ingredientes (o Room não salva `List` direto).
- Configurar o KSP e o Room no Gradle.

**Teste de persistência**  
Cadastrei uma receita, favoritei, adicionei os ingredientes na lista e marquei um
item como comprado. Fechei o app (removi dos recentes) e abri de novo: tudo continuava lá.

### Prints

| Tela | Print |
|---|---|
| Receitas (do banco) | ![](docs/prints/01-receitas.png) |
| Detalhe (busca por id) | ![](docs/prints/02-detalhe-receita.png) |
| Editar receita | ![](docs/prints/03-editar-receita.png) |
| Lista de compras | ![](docs/prints/04-compras.png) |
| Detalhe do item | ![](docs/prints/05-detalhe-item.png) |
| Perfil | ![](docs/prints/06-perfil.png) |

### Vídeo (fechar e abrir o app de novo)

[Vídeo do teste de persistência](docs/video-tempero-room.mp4)
