package com.example.mykmp.data

import com.example.mykmp.data.mapper.toDomain
import com.example.mykmp.data.model.ProductDetailDto
import com.example.mykmp.data.model.ProductDto
import com.example.mykmp.domain.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductMapperTest {
    private val categoryNames = mapOf("beauty" to "Beauty")

    @Test
    fun toDomain_convertsIdRoundsPriceAndResolvesCategoryName() {
        val dto = ProductDto(id = 1, title = "Essence Mascara Lash Princess", price = 9.49, category = "beauty")

        assertEquals(
            Product(id = "1", name = "Essence Mascara Lash Princess", price = 9, categoryName = "Beauty"),
            dto.toDomain(categoryNames),
        )
    }

    @Test
    fun toDomain_usesSlugWhenCategoryNameIsUnknown() {
        val dto = ProductDto(id = 2, title = "Unknown", price = 1.0, category = "new-category")

        assertEquals("new-category", dto.toDomain(categoryNames).categoryName)
    }

    @Test
    fun detailToDomain_usesDefaultsForMissingFields() {
        // brand や reviews が無い商品もある
        val dto = ProductDetailDto(id = 7, title = "Apple", category = "groceries", price = 1.99)

        val detail = dto.toDomain(mapOf("groceries" to "Groceries"))

        assertEquals("Groceries", detail.categoryName)
        assertEquals(2, detail.price)
        assertEquals(null, detail.brand)
        assertEquals(emptyList(), detail.reviews)
    }
}
