package com.example.mykmp.data

import com.example.mykmp.data.mapper.toDomain
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
}
