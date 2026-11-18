# Tempero Room – Trabalho 4 (Arquitetura MVVM)

**Aluno:** Igor  
**Turma:** 2 [Android - ECO] - Ter/Qua Manhã  
**Disciplina:** Desenvolvimento de Aplicativos Móveis – Prof. Andre Luis

> O README do Trabalho 3 (Room) continua neste repositório em [README_T3.md](README_T3.md).

## 1. Sobre o projeto

Este é o **mesmo app do Trabalho 3** (Tempero Room: receitas + lista de compras salvas
no Room), agora **reorganizado em MVVM**. Nenhuma funcionalidade nova foi adicionada:
o objetivo foi separar as responsabilidades em camadas. O app continua fazendo tudo
o que fazia no T3 (cadastrar, editar, excluir, favoritar, marcar comprado, mudar
quantidade, adicionar os ingredientes de uma receita na lista, e os dados continuam
salvos depois de fechar o app).

## 2. Como rodar

1. Abra a pasta `TemperoRoom` no **Android Studio**.
2. Espere o **Gradle Sync** terminar.
3. Escolha um emulador (ou celular) e clique em **Run ▶**.
4. Se o banco estiver vazio, toque em **"Carregar receitas de exemplo"**.

## 3. O que foi feito (requisitos)

| Requisito | Onde está |
|---|---|
| Pacotes `ui`, `data.local`, `data.repository`, `viewmodel` | `com.igor.tempero.*` (estrutura abaixo) |
| 1 Repository por Entity (só ele fala com o DAO) | `ReceitaRepository.kt` e `ItemCompraRepository.kt` |
| `buscarTodos()` do DAO devolve `Flow<List<...>>` (sem `suspend`) | `ReceitaDao.kt` e `ItemCompraDao.kt` |
| 2 ViewModels, cada um com `data class UiState` | `ReceitasViewModel` + `ReceitasUiState`, `ComprasViewModel` + `ComprasUiState` |
| `private val _uiState = MutableStateFlow(...)` e `val uiState: StateFlow<...> = _uiState.asStateFlow()` | Nos 2 ViewModels |
| Ações com `viewModelScope.launch` | Todas as funções dos ViewModels que mexem no banco |
| ViewModelFactory (`ViewModelProvider.Factory`) que injeta os Repositories | `viewmodel/AppViewModelFactory.kt` |
| ViewModels obtidos com `viewModel(factory = ...)` | `navigation/AppNavigation.kt` |
| Telas sem `dao`, sem `CoroutineScope` e sem `Dispatchers.IO`, usando `collectAsStateWithLifecycle` | Todas as telas em `ui/telas/` |

### Estrutura

```
com.igor.tempero
├── MainActivity.kt              → monta Banco → DAOs → Repositories → Factory
├── data/
│   ├── local/                   → Receita, ItemCompra (@Entity), DAOs, AppDatabase
│   └── repository/              → ReceitaRepository, ItemCompraRepository
├── viewmodel/                   → ReceitasViewModel, ComprasViewModel, AppViewModelFactory
├── navigation/                  → Rotas e AppNavigation (NavHost)
└── ui/                          → telas, componentes e tema (só mostram e avisam o ViewModel)
```

Caminho de um clique (ex: favoritar):
`Tela → ReceitasViewModel.alternarFavorita() → ReceitaRepository.atualizar() → ReceitaDao.atualizar() → Room`  
e a volta: `Room → Flow do DAO → Repository → ViewModel (_uiState) → uiState → Tela redesenha`.

## 4. Respostas e evidências

**Como era no T3 e o que mudou?**  
No T3 cada tela fazia tudo: pegava o `AppDatabase`, chamava o DAO dentro de
`CoroutineScope(Dispatchers.IO).launch`, tinha o `try/catch`, guardava a lista num
`mutableStateListOf` e precisava buscar de novo no banco depois de cada alteração.
Agora a tela só **mostra** o `uiState` e **avisa** o ViewModel ("o usuário clicou em
favoritar"). Quem decide o que fazer é o ViewModel, e quem fala com o banco é o Repository.
Como o `buscarTodos()` virou `Flow`, a lista se atualiza sozinha: não precisa mais
recarregar depois de inserir, editar ou apagar.

**Como dividi os pacotes?**  
- `data.local`: tudo do Room (Entities, DAOs e o AppDatabase).
- `data.repository`: um Repository para cada Entity.
- `viewmodel`: os ViewModels, os UiStates e a Factory.
- `ui`: telas, componentes visuais e tema. A navegação ficou no pacote `navigation`.

**Por que usar Repository?**  
Para a tela e o ViewModel não dependerem do Room. O Repository é o único que conhece
o DAO; se um dia os dados vierem de uma API, só ele muda. Ele também concentra o
tratamento de erro (`try/catch` com `Log.e`) que antes estava espalhado nas telas.

**O que tem em cada UiState?**  
- `ReceitasUiState`: `receitas` (do banco), `busca`, `categoria`, `carregando`,
  `receitaSelecionada` (detalhe/edição), `selecionadaCarregou`, `erroFormulario`,
  `mensagem` (Toast) e `filtradas` (lista já filtrada, calculada).
- `ComprasUiState`: `itens`, `receitas` (para mostrar a origem de cada item),
  `carregando`, `itemSelecionado`, `selecionadoCarregou`, `erroFormulario`, `mensagem`,
  `comprados` (calculado), `receitaDe(item)` e `ingredientesFaltando(receita)`.

**O que quebrou durante a refatoração?**  
- Ao trocar `buscarTodos()` para `Flow`, todas as telas que chamavam esse método
  pararam de compilar, porque não dava mais para chamar como `suspend`.
- O detalhe e o formulário de edição dependiam do `buscarPorId` dentro da tela;
  passaram a pedir ao ViewModel (`selecionarReceita(id)` / `selecionarItem(id)`).
- Os ViewModels são compartilhados entre as telas, então o detalhe podia mostrar por um
  instante o item escolhido antes; resolvi conferindo se o id selecionado é o da rota.
- O formulário de edição precisou de um `LaunchedEffect` para preencher os campos só
  quando a receita chega do banco.

**Dificuldades**  
- Entender o caminho do dado entre as camadas (tela → ViewModel → Repository → DAO e a volta pelo Flow).
- Criar a Factory para o ViewModel receber o Repository no construtor.
- Tirar toda a lógica das telas sem perder nenhuma funcionalidade do T3.

### Prints

| Tela | Print |
|---|---|
| Receitas | ![](docs/prints/t4-01-receitas.png) |
| Detalhe da receita | ![](docs/prints/t4-02-detalhe-receita.png) |
| Lista de compras | ![](docs/prints/t4-03-compras.png) |
| Detalhe do item | ![](docs/prints/t4-04-detalhe-item.png) |
| Estrutura de pacotes no Android Studio | ![](docs/prints/t4-05-pacotes.png) |

### Vídeo

[Vídeo do app funcionando depois da refatoração](docs/video-tempero-mvvm.mp4)
