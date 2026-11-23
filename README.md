# MyKMP

Kotlin Multiplatform（KMP）で Android と iOS のロジックを共有する商品アプリです。
UI は各 OS のネイティブ（Android: Jetpack Compose / iOS: SwiftUI）で書き、それより下（画面の状態管理・ビジネスロジック・通信・DB）は `shared` モジュールで共有しています。

## 目次

- [できること](#できること)
- [技術スタック](#技術スタック)
- [全体のアーキテクチャ](#全体のアーキテクチャ)
- [フォルダ構成](#フォルダ構成)
- [各層の役割](#各層の役割)
  - [presentation 層（MVI）](#presentation-層mvi)
  - [domain 層](#domain-層)
  - [data 層](#data-層)
  - [DI（Koin）と KmpSdk](#dikoin-と-kmpsdk)
  - [Android / iOS との接続](#android--ios-との接続)
- [画面ごとの処理の流れ](#画面ごとの処理の流れ)
- [新しい画面の追加手順](#新しい画面の追加手順)
- [ビルド・実行・テスト](#ビルド実行テスト)
- [既知の課題](#既知の課題)

## できること

| 画面 | 機能 |
| --- | --- |
| 商品一覧 | [DummyJSON](https://dummyjson.com/docs/products) から商品を取得して表示（カテゴリ名つき）／お気に入りの登録・解除とお気に入り件数の表示／取得結果を DB に保存し、通信できないときも前回の一覧を表示 |
| 商品詳細 | 画像・説明・価格・割引率・評価・在庫・保証/配送/返品・レビューを表示／お気に入りの登録・解除（一覧と連動） |

## 技術スタック

| 用途 | ライブラリ | バージョン |
| --- | --- | --- |
| 言語 | Kotlin | 2.4.21 |
| 非同期 | kotlinx.coroutines（Flow） | 1.11.0 |
| 通信 | Ktor Client（Android: OkHttp / iOS: Darwin）＋ kotlinx.serialization | 3.6.1 |
| DB | SQLDelight（Android: AndroidSqliteDriver / iOS: NativeSqliteDriver） | 2.4.1 |
| DI | Koin | 4.2.2 |
| Swift 連携 | SKIE（Flow → AsyncSequence、sealed → enum） | 0.10.16 |
| Android UI | Jetpack Compose / Material3 | Compose Multiplatform 1.12.2 |
| Android 画面遷移 | Navigation Compose（型安全なルート） | 2.10.0 |
| Android 画像 | Coil 3 | 3.6.3 |
| Android ViewModel | Lifecycle ViewModel | 2.11.0 |
| iOS UI | SwiftUI（画像は `AsyncImage`） | - |

## 全体のアーキテクチャ

Clean Architecture の考え方で 3 つの層に分け、画面の状態管理には MVI を使っています。

```
┌──────────────────────────────┐   ┌──────────────────────────────┐
│ androidApp（Compose）        │   │ iosApp（SwiftUI）            │
│  Screen ─▶ ViewModel         │   │  View ─▶ Observable          │
└──────────────┬───────────────┘   └──────────────┬───────────────┘
               │ Store を持つ（viewModelScope）    │ Store を持つ（storeScope()）
               ▼                                   ▼
┌─────────────────────────────────────────────────────────────────┐
│ shared                                                          │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ presentation（MVI）                                          │ │
│ │  Store ─ Processor ─ Reducer（画面ごと）＋ mvi（共通の土台） │ │
│ └──────────────────────────────┬──────────────────────────────┘ │
│                                ▼ UseCase を呼ぶ                 │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ domain                                                       │ │
│ │  UseCase ─▶ Repository（インターフェース）、model            │ │
│ └──────────────────────────────▲──────────────────────────────┘ │
│                                │ インターフェースを実装          │
│ ┌──────────────────────────────┴──────────────────────────────┐ │
│ │ data                                                         │ │
│ │  RepositoryImpl ─▶ DataSource（remote / local）、mapper      │ │
│ └───────────────┬──────────────────────────────┬──────────────┘ │
└─────────────────┼──────────────────────────────┼────────────────┘
                  ▼                              ▼
          DummyJSON API（Ktor）            SQLite（SQLDelight）
```

**依存の向きは `UI → presentation → domain ← data`** です。

- domain は他のどの層にも依存しません（Ktor や SQLDelight を知らない）
- data は domain のインターフェース（`ProductRepository` など）を実装します
- そのため、通信ライブラリや DB を差し替えても domain と presentation は変わりません

## フォルダ構成

```
MyKMP/
├── androidApp/                         Android アプリ（Compose）
│   └── src/main/kotlin/com/example/mykmp/
│       ├── MyKmpApplication.kt         KmpSdk の初期化
│       ├── MainActivity.kt
│       └── ui/
│           ├── navigation/AppNavHost.kt     画面遷移（Navigation Compose）
│           ├── productlist/                 一覧の Screen と ViewModel
│           └── productdetail/               詳細の Screen と ViewModel
│
├── iosApp/                             iOS アプリ（SwiftUI）
│   └── iosApp/
│       ├── iOSApp.swift                KmpSdk の初期化
│       ├── ProductList/                一覧の View と Observable
│       └── ProductDetail/              詳細の View と Observable
│
└── shared/                             共有モジュール（KMP）
    └── src/
        ├── commonMain/
        │   ├── kotlin/com/example/mykmp/
        │   │   ├── di/                 KmpSdk（入口）、Koin モジュール、PlatformContext
        │   │   ├── presentation/
        │   │   │   ├── mvi/            Store / Processor / Reducer / storeScope（共通の土台）
        │   │   │   ├── productlist/    一覧の Intent / Action / UiState / SideEffect / Processor / Reducer / Store
        │   │   │   └── productdetail/  詳細の 〃
        │   │   ├── domain/
        │   │   │   ├── model/          Product / ProductDetail / Review
        │   │   │   ├── repository/     ProductRepository / FavoriteRepository（インターフェース）
        │   │   │   ├── usecase/        UseCase
        │   │   │   └── common/         DomainResult（成功 / 失敗）
        │   │   └── data/
        │   │       ├── datasource/
        │   │       │   ├── remote/     ProductRemoteDataSource（Ktor）、HttpClient の共通設定
        │   │       │   └── local/      ProductLocalDataSource / FavoriteLocalDataSource（SQLDelight）
        │   │       ├── model/          ProductDto / CategoryDto / ProductDetailDto
        │   │       ├── mapper/         DTO・Entity → ドメインモデルの変換
        │   │       └── repository/     ProductRepositoryImpl / FavoriteRepositoryImpl
        │   └── sqldelight/             DB スキーマ（Product.sq / Favorite.sq）
        ├── androidMain/                Android 固有（OkHttp、AndroidSqliteDriver、Context）
        ├── iosMain/                    iOS 固有（Darwin、NativeSqliteDriver）
        ├── commonTest/                 共通のテスト
        └── androidHostTest/            JVM で動かすテスト（SQLDelight のインメモリ DB）
```

## 各層の役割

### presentation 層（MVI）

```
UI ──Intent──▶ Processor ──Action──▶ Reducer ──State──▶ UI
                  │
                  └──SideEffect──▶ UI（画面遷移・トーストなど 1 回きりのイベント）
```

| 役割 | 何をするか | 例（一覧） |
| --- | --- | --- |
| Intent | ユーザーがしたいこと（UI から届く） | `Load`、`Refresh`、`ToggleFavorite(id)`、`ClickProduct(id)` |
| Processor | Intent を受けて UseCase を呼び、結果を Action / SideEffect として `Flow` で出す | `refreshProductsUseCase()` を呼ぶ |
| Action | 実際に起きたこと（Store の内部だけで使う、`internal`） | `RefreshStarted`、`ItemsUpdated(items, favoriteCount)` |
| Reducer | 今の State と Action から次の State を作る純粋関数 | `isLoading = true` にする |
| State | 画面の状態（`StateFlow` で公開） | `ProductListUiState` |
| SideEffect | 1 回だけ届けるイベント（`Channel` で公開） | `NavigateToDetail(id)`、`ShowMessage(message)` |

**Intent と Action を分ける理由**：1 つの Intent から複数の Action が出る（`Load` → `RefreshStarted` → `RefreshSucceeded`）ため。非同期処理は Processor、State の計算は Reducer と分けることで、Reducer を「State と Action を渡すと次の State が返る」だけの関数にでき、テストが簡単になります。State を変える場所も Reducer の 1 か所にまとまります。

**共通の土台（`presentation/mvi/`）**

| ファイル | 中身 |
| --- | --- |
| `Store.kt` | 汎用の `Store<Intent, Action, State, SideEffect>`。`dispatch(intent)` で Processor を動かし、Action は Reducer で State に、SideEffect は Channel に流す |
| `Processor.kt` | `Processor` インターフェースと、出力の型 `ProcessorOutput`（Action か SideEffect） |
| `Reducer.kt` | `Reducer` インターフェース |
| `StoreScope.kt` | iOS 用の CoroutineScope（`SupervisorJob() + Dispatchers.Main.immediate`。Android の `viewModelScope` と同じ構成） |

画面ごとには Processor と Reducer を書き、画面ごとの Store（例：`ProductListStore`）から汎用 `Store` に処理を任せます。画面ごとの Store は型を具体的に書いて公開しているので、Swift 側でも SKIE で `StateFlow<ProductListUiState>` をそのまま扱えます。

**Processor の書き方のポイント**

- 1 つの Intent から値を順番に出すだけなら `flow { emit(...) }`
- 監視（終わらない Flow）と取得を同時に動かすときは `channelFlow { launch { ... }; send(...) }`
- 複数の UseCase の Flow から 1 つの値を作るときは `combine`、1 つの Flow を変換するだけなら `map`

### domain 層

アプリのルールを表す層で、他の層やライブラリに依存しません。

| 種類 | 中身 |
| --- | --- |
| model | `Product`（一覧の 1 件）、`ProductDetail` / `Review`（詳細） |
| repository | `ProductRepository`（`observeProducts()` / `refreshProducts()` / `getProductDetail(id)`）、`FavoriteRepository`（`observeFavoriteIds()` / `toggleFavorite(id)`） |
| usecase | `ObserveProductsUseCase`、`RefreshProductsUseCase`、`GetProductDetailUseCase`、`ObserveFavoriteIdsUseCase`、`ToggleFavoriteUseCase` |
| common | `DomainResult`（`Success(data)` / `Error(message)`） |

- 失敗しうる UseCase は例外を投げず、`DomainResult` を返します（Processor で `when` で分岐できる）
- `CancellationException`（画面を閉じたときのキャンセル）はエラーにせず、そのまま投げ直します
- 監視する UseCase（`Observe〜`）は `Flow` を返します

### data 層

| フォルダ | 中身 |
| --- | --- |
| `datasource/remote` | `ProductRemoteDataSource`：Ktor で DummyJSON の `GET /products`、`GET /products/{id}`、`GET /products/categories` を呼ぶ。`HttpClientFactory.kt` に共通設定（JSON 変換・タイムアウト・ログ・2xx 以外は例外） |
| `datasource/local` | `ProductLocalDataSource`（商品のキャッシュ）、`FavoriteLocalDataSource`（お気に入り）。インターフェースと SQLDelight の実装。読む処理は `Flow`、書く処理は `suspend` |
| `model` | API のレスポンスの型（`ProductDto`、`CategoryDto`、`ProductDetailDto`）。DB の `ProductEntity` / `FavoriteEntity` は SQLDelight が `.sq` から生成する |
| `mapper` | DTO・Entity ⇔ ドメインモデルの変換。domain が data を知らないように、変換は data 層に置く |
| `repository` | `ProductRepositoryImpl`、`FavoriteRepositoryImpl`。DataSource を組み合わせ、mapper で変換して返す |

クラス名は接尾辞で区別しています：`ProductDto`（API）／`ProductEntity`（DB）／`Product`（ドメイン）。

**一覧：DB を唯一の情報源にする**

```
API ──refreshProducts()──▶ DB に保存
                            │
DB ──observeProducts()（Flow）──▶ 画面
```

- 画面に出すデータは常に DB から Flow で流す。DB が変わると自動で新しい一覧が流れる
- API は DB を更新するだけ。失敗しても DB の前回の一覧は表示されたまま（オフラインでも見られる）
- 2 つの API の結果を一部ずつ使って 1 つの `Product` を作る（`/products` の category slug と `/products/categories` の表示名を突き合わせる）。2 つは `async` で並行に呼ぶ

**詳細：DB に保存せず毎回 API から取る**

- `/products/{id}` と `/products/categories` を並行に呼び、`ProductDetail` を作る

**お気に入り**

- DB の `FavoriteEntity` テーブルに商品 ID を保存
- 一覧と詳細は同じ `observeFavoriteIds()` の Flow を見ているので、どちらで切り替えても両方に反映される

### DI（Koin）と KmpSdk

`shared/src/commonMain/.../di/KmpSdk.kt` が Android / iOS から使う入口です。

| 関数 | 用途 |
| --- | --- |
| `initialize(context: PlatformContext)` | アプリ起動時に 1 回呼ぶ（Koin を組み立てる） |
| `createProductListStore(scope)` / `createProductListStore()` | 一覧の Store を作る（引数なしは iOS 用で `storeScope()` を使う） |
| `createProductDetailStore(productId, scope)` / `createProductDetailStore(productId)` | 詳細の Store を作る |

- **OS ごとの部分**は `platformModule(context)`（androidMain / iosMain）で提供：`HttpClientEngine`（OkHttp / Darwin）、`SqlDriver`、IO 用の `CoroutineDispatcher`
- **`PlatformContext`** は expect/actual。Android では DB の作成に必要な `Context` を包み、iOS では空のクラス
- `HttpClient`・`AppDatabase`・DataSource・Repository は `single`（1 つを共有）、UseCase は `factory`（毎回作る）。特に `AppDatabase` は 1 つでないと、DB の変更が Flow に通知されない
- Store は画面ごとに State と scope が違うので Koin には登録せず、`KmpSdk` で毎回作る

### Android / iOS との接続

| | Android | iOS |
| --- | --- | --- |
| 初期化 | `KmpSdk.initialize(PlatformContext(this))`（Application） | `KmpSdk.shared.initialize(context: PlatformContext())`（App） |
| Store を持つ | `ViewModel`（画面回転で作り直さないため） | `ObservableObject`（`@StateObject` で保持） |
| scope | `viewModelScope`（ViewModel 破棄で自動停止） | `storeScope()`（`deinit` で `store.close()`） |
| State の受け取り | `collectAsStateWithLifecycle()` | SKIE により `for await state in store.state` |
| SideEffect の受け取り | `LaunchedEffect` で `collect` | `for await` ＋ `switch onEnum(of: effect)` |
| 最初の `Load` | ViewModel の `init` で 1 回だけ送る | Observable の `init` で 1 回だけ送る |
| 画面遷移 | Navigation Compose（`@Serializable` のルート） | `NavigationStack` ＋ `navigationDestination` |

`Load` を UI の `LaunchedEffect` や `.task` から送らないのは、画面回転や画面の出直しで再実行され、監視が二重になるのを防ぐためです。

## 画面ごとの処理の流れ

### 商品一覧（`presentation/productlist/`）

| Intent | 呼ぶ UseCase | 出す Action / SideEffect | State の変化 |
| --- | --- | --- | --- |
| `Load`（最初に 1 回） | `ObserveProductsUseCase` と `ObserveFavoriteIdsUseCase` を **combine** して監視 | DB が変わるたびに `ItemsUpdated(items, favoriteCount)` | `items`、`favoriteCount` |
| 〃 | `RefreshProductsUseCase` | `RefreshStarted` → `RefreshSucceeded` / `RefreshFailed` | `isLoading`、`errorMessage` |
| `Refresh` | `RefreshProductsUseCase` | `RefreshStarted` → `RefreshSucceeded` / `RefreshFailed` | 〃 |
| `ToggleFavorite(id)` | `ToggleFavoriteUseCase` | 成功：なし（DB が変わり、監視中の combine から `ItemsUpdated` が届く）／失敗：SideEffect `ShowMessage` | 成功時は `items` の ★ が変わる |
| `ClickProduct(id)` | なし | SideEffect `NavigateToDetail(id)` | 変化なし（詳細画面へ遷移） |

```kotlin
// ProductListProcessor：2 つの UseCase の Flow を combine し、map で画面用の値を計算する
combine(observeProducts(), observeFavoriteIds()) { products, favoriteIds ->
    products.map { ProductListItem(product = it, isFavorite = it.id in favoriteIds) }
}
    .distinctUntilChanged()
    .map { items -> ItemsUpdated(items, favoriteCount = items.count { it.isFavorite }) }
```

### 商品詳細（`presentation/productdetail/`）

表示する商品 ID は Intent ではなく、Store を作るときに渡します。

| Intent | 呼ぶ UseCase | 出す Action / SideEffect | State の変化 |
| --- | --- | --- | --- |
| `Load`（最初に 1 回） | `ObserveFavoriteIdsUseCase` を「この商品が含まれるか」に **map** して監視 | お気に入りが変わるたびに `FavoriteChanged(isFavorite)` | `isFavorite` |
| 〃 | `GetProductDetailUseCase` | `LoadStarted` → `LoadSucceeded(detail)` / `LoadFailed` | `isLoading`、`detail`、`errorMessage` |
| `Retry` | `GetProductDetailUseCase` | `LoadStarted` → `LoadSucceeded(detail)` / `LoadFailed` | 〃 |
| `ToggleFavorite` | `ToggleFavoriteUseCase` | 成功：なし（監視中の Flow から `FavoriteChanged` が届く）／失敗：SideEffect `ShowMessage` | 成功時は `isFavorite` が変わる |

一覧は「商品とお気に入りの両方から各行の ★ を作る」ので `combine`、詳細は「お気に入りかどうかは商品 ID だけで決まる」ので `map` を使っています。

## 新しい画面の追加手順

例：`orderhistory` 画面を追加する場合

1. **domain**：必要なら model、Repository のメソッド、UseCase を追加する
2. **data**：DataSource・DTO・mapper・RepositoryImpl を追加・更新する
3. **presentation**：`presentation/orderhistory/` を作り、次を書く
   - `OrderHistoryIntent` / `OrderHistoryAction`（`internal`）/ `OrderHistoryUiState` / `OrderHistorySideEffect`
   - `OrderHistoryProcessor`（`Processor` を実装）と `OrderHistoryReducer`（`Reducer` を実装）
   - `OrderHistoryStore`（汎用 `Store` に処理を任せ、`state` / `sideEffect` / `dispatch` / `close` を公開）
4. **DI**：`KmpSdk.kt` の Koin モジュールに UseCase などを登録し、`createOrderHistoryStore(...)` を追加する
5. **Android**：`ViewModel`（`init` で `Load`）と Screen を作り、`AppNavHost` にルートを追加する
6. **iOS**：`ObservableObject`（`init` で `Load`、`deinit` で `close()`）と View を作る
7. **テスト**：Reducer のテストと、偽の Repository を使った Store のテストを `commonTest` に追加する

## ビルド・実行・テスト

| 内容 | 方法 |
| --- | --- |
| Android アプリ | `./gradlew :androidApp:assembleDebug`、または Android Studio から実行 |
| iOS アプリ | `iosApp/` を Xcode で開いて実行（ビルド時に Gradle が `Shared` フレームワークを作る） |
| 共通のテスト（JVM） | `./gradlew :shared:testAndroidHostTest` |
| 共通のテスト（iOS シミュレータ） | `./gradlew :shared:iosSimulatorArm64Test` |

**iOS の補足**

- `Shared` は static framework です。SQLDelight の NativeSqliteDriver が SQLite を使うため、Xcode の `OTHER_LDFLAGS` に `-lsqlite3` を設定しています
- SKIE により、Kotlin の `StateFlow` / `Flow` は Swift の `AsyncSequence`、sealed interface は `onEnum(of:)` で網羅的に `switch` できる enum として使えます

**テストの構成**

| テスト | 場所 | 内容 |
| --- | --- | --- |
| `ProductListReducerTest` | commonTest | Reducer の State 遷移 |
| `ProductListStoreTest` / `ProductDetailStoreTest` | commonTest | 偽の Repository を使い、combine・お気に入り切り替え・失敗時の動作を確認 |
| `ProductRepositoryImplTest` | commonTest | Ktor の `MockEngine` で API を模擬し、2 つの API の結合・DB への保存を確認 |
| `ProductMapperTest` | commonTest | DTO → ドメインモデルの変換 |
| `SqlDelightProductLocalDataSourceTest` | androidHostTest | JVM のインメモリ SQLite で SQLDelight の実装を確認 |

## 既知の課題

- **価格の表示**：DummyJSON の価格はドル（例：9.99）ですが、`Product.price` は `Int` のため四捨五入し、「¥」で表示しています。セント単位の整数で持って「$」で表示するなどの見直しが必要です
- **詳細のオフライン対応なし**：詳細は DB に保存していないため、通信できないと表示できません
