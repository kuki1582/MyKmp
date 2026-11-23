This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
### Presentation architecture (MVI)

```
UI ──Intent──▶ Processor ──Action──▶ Reducer ──State──▶ UI
                  └──SideEffect──▶ UI（画面遷移・トーストなど 1 回きりのイベント）
```

`shared/src/commonMain/.../presentation` 配下:

| フォルダ | 中身 |
| --- | --- |
| `mvi` | 全画面共通の土台。汎用 `Store<Intent, Action, State, SideEffect>`、`Processor`、`Reducer`、`storeScope()` |
| `productlist` | 商品一覧画面の Intent / Action / UiState / SideEffect / Processor / Reducer / Store と、流れの図 [`ProductList.md`](./shared/src/commonMain/kotlin/com/example/mykmp/presentation/productlist/ProductList.md)（Mermaid） |

| 役割 | 何をするか |
| --- | --- |
| Intent | ユーザーがしたいこと（UI から届く） |
| Processor | Intent を受けて UseCase を呼び、Action / SideEffect を出す |
| Action | 実際に起きたこと（Store 内部専用・internal） |
| Reducer | State + Action → 次の State（副作用なしの純粋関数） |
| State | 画面の状態 |
| SideEffect | 1 回だけ届けるイベント |

新しい画面は `presentation/<画面名>/` フォルダを作り、Intent / Action / State / SideEffect を定義して、Processor と Reducer を書き、画面ごとの Store から `mvi` の汎用 `Store` に渡すだけでよい。

- Android: `androidApp` の `ViewModel` が `viewModelScope` で Store を持つ（画面回転で作り直さないため）
- iOS: [SKIE](https://skie.touchlab.co) により `StateFlow` / `Flow` を `for await` で、sealed を `onEnum(of:)` で扱う。Store は `KmpSdk.shared.createProductListStore()` で作り、破棄時に `close()` する
