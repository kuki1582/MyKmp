import Foundation
import Shared

/// shared の ProductListStore を SwiftUI から監視するためのクラス
///
/// SKIE のおかげで、Kotlin の StateFlow / Flow を Swift の AsyncSequence として
/// `for await` で受け取れる。コールバック用のラッパーを Kotlin 側に書く必要はない。
@MainActor
final class ProductListObservable: ObservableObject {
    @Published private(set) var state: ProductListUiState

    /// 詳細画面への遷移要求（SideEffect）。View 側で監視して画面遷移する
    @Published var navigateToDetailId: String?

    private let store: ProductListStore

    init() {
        store = KmpSdk.shared.createProductListStore()
        state = store.state.value
    }

    deinit {
        // iOS では Store の scope（MainScope）をこちらで管理しているので、破棄時に止める
        store.close()
    }

    /// State と SideEffect の監視を始める。View の `.task` から呼ぶ（View が消えると自動でキャンセルされる）
    func observe() async {
        await withTaskGroup(of: Void.self) { group in
            group.addTask { @MainActor in
                for await newState in self.store.state {
                    self.state = newState
                }
            }
            group.addTask { @MainActor in
                for await effect in self.store.sideEffect {
                    self.handle(effect)
                }
            }
        }
    }

    func load() {
        store.dispatch(intent: ProductListIntentLoad.shared)
    }

    func clickProduct(id: String) {
        store.dispatch(intent: ProductListIntentClickProduct(id: id))
    }

    private func handle(_ effect: ProductListSideEffect) {
        // SKIE により sealed interface を Swift の enum として網羅的に switch できる
        switch onEnum(of: effect) {
        case .navigateToDetail(let navigate):
            navigateToDetailId = navigate.id
        }
    }
}
