import Foundation
import Shared

/// shared の ProductDetailStore を SwiftUI から監視するためのクラス
@MainActor
final class ProductDetailObservable: ObservableObject {
    @Published private(set) var state: ProductDetailUiState

    /// 1 回だけ表示するメッセージ（SideEffect）。View 側でアラートを出し、閉じたら nil に戻る
    @Published var message: String?

    private let store: ProductDetailStore

    init(productId: String) {
        store = KmpSdk.shared.createProductDetailStore(productId: productId)
        state = store.state.value
        // 監視を始める Load は 1 回だけ送る（.task は画面が出直すたびに呼ばれるので、ここで送る）
        store.dispatch(intent: ProductDetailIntentLoad.shared)
    }

    deinit {
        // iOS では Store の scope をこちらで管理しているので、破棄時に止める
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

    func retry() {
        store.dispatch(intent: ProductDetailIntentRetry.shared)
    }

    func toggleFavorite() {
        store.dispatch(intent: ProductDetailIntentToggleFavorite.shared)
    }

    private func handle(_ effect: ProductDetailSideEffect) {
        switch onEnum(of: effect) {
        case .showMessage(let show):
            message = show.message
        }
    }
}
