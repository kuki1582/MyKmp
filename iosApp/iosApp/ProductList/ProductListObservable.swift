import Foundation
import Shared

/// shared の ProductListViewModel を SwiftUI から監視するためのクラス
@MainActor
final class ProductListObservable: ObservableObject {
    @Published private(set) var state: ProductListUiState

    /// 詳細画面への遷移要求（SideEffect）。View 側で監視して画面遷移する
    @Published var navigateToDetailId: String?

    private let wrapper = ProductListViewModelWrapper()

    init() {
        state = wrapper.currentState

        // Kotlin 側は MainScope で collect しているので、コールバックはメインスレッドで届く
        wrapper.observeState { [weak self] newState in
            MainActor.assumeIsolated {
                self?.state = newState
            }
        }
        wrapper.observeSideEffect { [weak self] effect in
            MainActor.assumeIsolated {
                if let navigate = effect as? ProductListSideEffectNavigateToDetail {
                    self?.navigateToDetailId = navigate.id
                }
            }
        }
    }

    deinit {
        wrapper.dispose()
    }

    func load() {
        wrapper.onIntent(intent: ProductListIntentLoad.shared)
    }

    func clickProduct(id: String) {
        wrapper.onIntent(intent: ProductListIntentClickProduct(id: id))
    }
}
