package com.example.mykmp.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: Int,
    /** 表示用のカテゴリ名（例: "Beauty"） */
    val categoryName: String,
)
