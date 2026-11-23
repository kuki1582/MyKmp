package com.example.mykmp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mykmp.ui.productdetail.ProductDetailRoute
import com.example.mykmp.ui.productlist.ProductListRoute
import kotlinx.serialization.Serializable

/** 画面の行き先。@Serializable にすると、引数（id）を型安全に渡せる */
@Serializable
private data object ProductListDestination

@Serializable
private data class ProductDetailDestination(val id: String)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ProductListDestination) {
        composable<ProductListDestination> {
            ProductListRoute(
                onNavigateToDetail = { id -> navController.navigate(ProductDetailDestination(id)) },
            )
        }
        composable<ProductDetailDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ProductDetailDestination>()
            ProductDetailRoute(
                productId = destination.id,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
