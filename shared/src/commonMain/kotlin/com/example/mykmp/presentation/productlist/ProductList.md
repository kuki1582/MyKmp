# ProductList の流れ

```
Intent ──▶ Processor ──Action──▶ Reducer ──State──▶ UI
               │
               └──SideEffect──▶ UI
```

```mermaid
flowchart LR
    UI["UI<br/>(Compose / SwiftUI)"]
    Processor["ProductListProcessor"]
    Reducer["ProductListReducer"]

    UI -- "Intent<br/>Load<br/>ClickProduct(id)" --> Processor
    Processor -- "Action<br/>LoadStarted<br/>LoadSucceeded(products)<br/>LoadFailed(message)" --> Reducer
    Reducer -- "State<br/>ProductListUiState" --> UI
    Processor -. "SideEffect（1 回きり）<br/>NavigateToDetail(id)" .-> UI
```

| 役割 | ProductList での中身 |
| --- | --- |
| Intent | `Load`、`ClickProduct(id)` |
| Processor | `ProductListProcessor`：`GetProductsUseCase` を呼び、Action / SideEffect を出す |
| Action | `LoadStarted`、`LoadSucceeded(products)`、`LoadFailed(message)` |
| Reducer | `ProductListReducer`：State + Action → 次の State |
| State | `ProductListUiState`（`isLoading`、`products`、`errorMessage`） |
| SideEffect | `NavigateToDetail(id)` |
