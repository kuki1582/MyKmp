import SwiftUI
import Shared

struct ProductListView: View {
    @StateObject private var observable = ProductListObservable()

    var body: some View {
        NavigationStack {
            content
                .navigationTitle("商品一覧")
                .navigationDestination(item: $observable.navigateToDetailId) { id in
                    // TODO: 詳細画面ができたら置き換える
                    Text("商品ID: \(id)")
                        .navigationTitle("商品詳細")
                }
        }
        .task {
            observable.load()
            await observable.observe()
        }
    }

    @ViewBuilder
    private var content: some View {
        let state = observable.state

        if state.isLoading {
            ProgressView()
        } else if let message = state.errorMessage {
            VStack(spacing: 12) {
                Text(message)
                    .foregroundStyle(.red)
                Button("再読み込み") {
                    observable.load()
                }
                .buttonStyle(.borderedProminent)
            }
        } else if state.products.isEmpty {
            Text("商品がありません")
        } else {
            List(state.products, id: \.id) { product in
                Button {
                    observable.clickProduct(id: product.id)
                } label: {
                    ProductRow(product: product)
                }
                .foregroundStyle(.primary)
            }
            .listStyle(.plain)
        }
    }
}

private struct ProductRow: View {
    let product: Product

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(product.name)
                .font(.headline)
            Text("¥\(product.price)")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
}
