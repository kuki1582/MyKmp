package com.example.mykmp.ui.productdetail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.mykmp.domain.model.ProductDetail
import com.example.mykmp.domain.model.Review
import com.example.mykmp.presentation.productdetail.ProductDetailIntent
import com.example.mykmp.presentation.productdetail.ProductDetailSideEffect
import com.example.mykmp.presentation.productdetail.ProductDetailUiState

/** ViewModel と接続する画面のエントリポイント */
@Composable
fun ProductDetailRoute(
    productId: String,
    onBack: () -> Unit,
    viewModel: ProductDetailViewModel = viewModel { ProductDetailViewModel(productId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ProductDetailSideEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ProductDetailScreen(state = state, onIntent = viewModel::dispatch, onBack = onBack)
}

/** 状態だけを受け取って描画する画面（Preview・テスト用に ViewModel から切り離している） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    state: ProductDetailUiState,
    onIntent: (ProductDetailIntent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.detail?.name ?: "商品詳細", maxLines = 1) },
                navigationIcon = { TextButton(onClick = onBack) { Text("＜ 戻る") } },
                actions = {
                    TextButton(onClick = { onIntent(ProductDetailIntent.ToggleFavorite) }) {
                        Text(text = if (state.isFavorite) "★" else "☆", style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            val detail = state.detail
            val errorMessage = state.errorMessage
            when {
                detail != null -> DetailContent(detail)

                state.isLoading -> CircularProgressIndicator()

                errorMessage != null -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { onIntent(ProductDetailIntent.Retry) }) { Text("再読み込み") }
                }
            }
        }
    }
}

@Composable
private fun DetailContent(detail: ProductDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // 画像（横スクロール）
        if (detail.imageUrls.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(detail.imageUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = detail.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        )
                    }
                }
            }
        }

        // 基本情報
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = listOfNotNull(detail.categoryName, detail.brand).joinToString(" / "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(text = detail.name, style = MaterialTheme.typography.headlineSmall)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "¥${detail.price}", style = MaterialTheme.typography.titleLarge)
                    if (detail.discountPercentage > 0) {
                        Text(
                            text = "${detail.discountPercentage}% OFF",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Text(
                    text = "★ ${detail.rating}　在庫 ${detail.stock}" +
                        (detail.availabilityStatus?.let { "（$it）" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = detail.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        // 保証・配送・返品
        val infos = listOfNotNull(
            detail.warrantyInformation?.let { "保証" to it },
            detail.shippingInformation?.let { "配送" to it },
            detail.returnPolicy?.let { "返品" to it },
        )
        if (infos.isNotEmpty()) {
            item { SectionTitle("ご購入について") }
            items(infos) { (label, value) ->
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(text = label, modifier = Modifier.width(56.dp), style = MaterialTheme.typography.bodyMedium)
                    Text(text = value, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // レビュー
        if (detail.reviews.isNotEmpty()) {
            item { SectionTitle("レビュー（${detail.reviews.size} 件）") }
            items(detail.reviews) { review ->
                ReviewRow(review)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun ReviewRow(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "★".repeat(review.rating) + "☆".repeat(5 - review.rating) + "　${review.reviewerName}　${review.date}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
private fun ProductDetailScreenPreview() {
    MaterialTheme {
        ProductDetailScreen(
            state = ProductDetailUiState(
                detail = ProductDetail(
                    id = "1", name = "医療機器A", description = "説明文がここに入ります。", categoryName = "医療",
                    price = 120, discountPercentage = 10.0, rating = 4.5, stock = 3, brand = "ブランド",
                    availabilityStatus = "In Stock", warrantyInformation = "1 年保証",
                    shippingInformation = "3 日以内に発送", returnPolicy = "30 日以内返品可", imageUrls = emptyList(),
                    reviews = listOf(Review(rating = 4, comment = "よかった", reviewerName = "山田", date = "2024-05-23")),
                ),
                isFavorite = true,
            ),
            onIntent = {},
            onBack = {},
        )
    }
}
