package com.example.mykmp.ui.productlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mykmp.domain.model.Product
import com.example.mykmp.presentation.productlist.ProductListIntent
import com.example.mykmp.presentation.productlist.ProductListSideEffect
import com.example.mykmp.presentation.productlist.ProductListUiState

/** ViewModel と接続する画面のエントリポイント */
@Composable
fun ProductListRoute(
    onNavigateToDetail: (id: String) -> Unit,
    viewModel: ProductListViewModel = viewModel { ProductListViewModel() },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.dispatch(ProductListIntent.Load)
    }

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ProductListSideEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
            }
        }
    }

    ProductListScreen(state = state, onIntent = viewModel::dispatch)
}

/** 状態だけを受け取って描画する画面（Preview・テスト用に ViewModel から切り離している） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    state: ProductListUiState,
    onIntent: (ProductListIntent) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("商品一覧") }) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            val errorMessage = state.errorMessage
            when {
                state.isLoading -> CircularProgressIndicator()

                errorMessage != null -> ErrorContent(
                    message = errorMessage,
                    onRetry = { onIntent(ProductListIntent.Load) },
                )

                state.products.isEmpty() -> Text("商品がありません")

                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.products, key = { it.id }) { product ->
                        ProductRow(
                            product = product,
                            onClick = { onIntent(ProductListIntent.ClickProduct(product.id)) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(product: Product, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = product.categoryName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(text = product.name, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "¥${product.price}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry) { Text("再読み込み") }
    }
}

@Preview
@Composable
private fun ProductListScreenPreview() {
    MaterialTheme {
        ProductListScreen(
            state = ProductListUiState(
                products = listOf(
                    Product(id = "1", name = "医療機器A", price = 120, categoryName = "医療"),
                    Product(id = "2", name = "医療機器B", price = 3400, categoryName = "医療"),
                ),
            ),
            onIntent = {},
        )
    }
}
