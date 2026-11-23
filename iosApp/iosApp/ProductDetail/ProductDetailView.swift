import SwiftUI
import Shared

struct ProductDetailView: View {
    @StateObject private var observable: ProductDetailObservable

    init(productId: String) {
        _observable = StateObject(wrappedValue: ProductDetailObservable(productId: productId))
    }

    var body: some View {
        content
            .navigationTitle(observable.state.detail?.name ?? "商品詳細")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        observable.toggleFavorite()
                    } label: {
                        Image(systemName: observable.state.isFavorite ? "star.fill" : "star")
                            .foregroundStyle(.yellow)
                    }
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

        if let detail = state.detail {
            DetailContent(detail: detail)
        } else if state.isLoading {
            ProgressView()
        } else if let message = state.errorMessage {
            VStack(spacing: 12) {
                Text(message)
                    .foregroundStyle(.red)
                Button("再読み込み") {
                    observable.retry()
                }
                .buttonStyle(.borderedProminent)
            }
        }
    }
}

private struct DetailContent: View {
    let detail: ProductDetail

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // 画像（横スクロール）
                if !detail.imageUrls.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(detail.imageUrls, id: \.self) { url in
                                AsyncImage(url: URL(string: url)) { image in
                                    image.resizable().scaledToFill()
                                } placeholder: {
                                    Color.secondary.opacity(0.1)
                                }
                                .frame(width: 240, height: 240)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                        }
                        .padding(.horizontal)
                    }
                }

                // 基本情報
                VStack(alignment: .leading, spacing: 4) {
                    Text([detail.categoryName, detail.brand].compactMap { $0 }.joined(separator: " / "))
                        .font(.caption)
                        .foregroundStyle(.tint)
                    Text(detail.name)
                        .font(.title2)
                        .bold()
                    HStack(alignment: .firstTextBaseline, spacing: 8) {
                        Text("¥\(detail.price)")
                            .font(.title3)
                        if detail.discountPercentage > 0 {
                            Text("\(detail.discountPercentage, specifier: "%.2f")% OFF")
                                .font(.subheadline)
                                .foregroundStyle(.red)
                        }
                    }
                    Text("★ " + String(format: "%.2f", detail.rating) + "　在庫 \(detail.stock)" + (detail.availabilityStatus.map { "（\($0)）" } ?? ""))
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    Text(detail.description)
                        .padding(.top, 8)
                }
                .padding(.horizontal)

                // 保証・配送・返品
                let infos: [(String, String)] = [
                    ("保証", detail.warrantyInformation),
                    ("配送", detail.shippingInformation),
                    ("返品", detail.returnPolicy),
                ].compactMap { label, value in value.map { (label, $0) } }
                if !infos.isEmpty {
                    SectionTitle(text: "ご購入について")
                    ForEach(infos, id: \.0) { label, value in
                        HStack(alignment: .top) {
                            Text(label).frame(width: 48, alignment: .leading)
                            Text(value)
                        }
                        .font(.subheadline)
                        .padding(.horizontal)
                    }
                }

                // レビュー
                if !detail.reviews.isEmpty {
                    SectionTitle(text: "レビュー（\(detail.reviews.count) 件）")
                    ForEach(Array(detail.reviews.enumerated()), id: \.offset) { _, review in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(String(repeating: "★", count: Int(review.rating)) +
                                 String(repeating: "☆", count: 5 - Int(review.rating)) +
                                 "　\(review.reviewerName)　\(review.date)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Text(review.comment)
                                .font(.subheadline)
                        }
                        .padding(.horizontal)
                        Divider().padding(.horizontal)
                    }
                }
            }
            .padding(.vertical)
        }
    }
}

private struct SectionTitle: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.headline)
            .padding(.horizontal)
            .padding(.top, 8)
    }
}
