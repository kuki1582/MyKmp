package com.example.mykmp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.example.mykmp.ui.productlist.ProductListRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ProductListRoute(
                    onNavigateToDetail = { id ->
                        // TODO: 詳細画面ができたら画面遷移に置き換える
                        Toast.makeText(this, "商品ID: $id", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }
}
