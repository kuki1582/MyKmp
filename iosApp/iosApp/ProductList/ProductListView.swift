import SwiftUI
import Shared

struct ProductListView: View {
    @StateObject private var observable = ProductListObservable()

    var body: some View {
        NavigationStack {
            content
                .navigationTitle("商品一覧（お気に入り \(observable.state.favoriteCount) 件）")
                .navigationBarTitleDisplayMode(.inline)
                .navigationDestination(item: $observable.navigateToDetailId) { id in
                    // TODO: 詳細画面ができたら置き換える
                    Text("商品ID: \(id)")
                        .navigationTitle("商品詳細")
                }
        }
        .alert(
            observable.message ?? "",
            isPresented: Binding(
                get: { observable.message != nil },
                set: { if !$0 { observable.message = nil } }
            )
        ) {
            Button("OK", role: .cancel) {}
        }
        .task {
            await observable.observe()
        }
    }

    @ViewBuilder
    private var content: some View {
        let state = observable.state

        // 保存済みの商品が無いときだけ全画面で表示する。あるときは一覧を出したまま上部に表示する
        if state.items.isEmpty && state.isLoading {
            ProgressView()
        } else if state.items.isEmpty, let message = state.errorMessage {
            VStack(spacing: 12) {
                Text(message)
                    .foregroundStyle(.red)
                Button("再読み込み") {
                    observable.refresh()
                }
                .buttonStyle(.borderedProminent)
            }
        } else if state.items.isEmpty {
            Text("商品がありません")
        } else {
            List {
                if let message = state.errorMessage {
                    HStack {
                        Text("\(message)（保存済みの一覧を表示しています）")
                            .font(.footnote)
                            .foregroundStyle(.red)
                        Spacer()
                        Button("再読み込み") {
                            observable.refresh()
                        }
                        .font(.footnote)
                    }
                }
                ForEach(state.items, id: \.product.id) { item in
                    ProductRow(
                        item: item,
                        onClick: { observable.clickProduct(id: item.product.id) },
                        onToggleFavorite: { observable.toggleFavorite(id: item.product.id) }
                    )
                }
            }
            .listStyle(.plain)
            .overlay(alignment: .top) {
                if state.isLoading {
                    ProgressView()
                }
            }
        }
    }
}

private struct ProductRow: View {
    let item: ProductListItem
    let onClick: () -> Void
    let onToggleFavorite: () -> Void

    var body: some View {
        HStack {
            Button(action: onClick) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(item.product.categoryName)
                        .font(.caption)
                        .foregroundStyle(.tint)
                    Text(item.product.name)
                        .font(.headline)
                    Text("¥\(item.product.price)")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .foregroundStyle(.primary)

            Button(action: onToggleFavorite) {
                Image(systemName: item.isFavorite ? "star.fill" : "star")
                    .foregroundStyle(.yellow)
            }
        }
        // 行内に 2 つのボタンがあるので、行全体がタップ対象にならないようにする
        .buttonStyle(.borderless)
        .padding(.vertical, 4)
    }
}
