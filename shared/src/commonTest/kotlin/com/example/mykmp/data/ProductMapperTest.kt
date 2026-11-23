package com.example.mykmp.data

import com.example.mykmp.data.mapper.toDomain
import com.example.mykmp.data.model.ProductDto
import com.example.mykmp.domain.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductMapperTest {
    @Test
    fun toDomain_convertsIdToStringAndRoundsPrice() {
        val dto = ProductDto(id = 1, title = "Essence Mascara Lash Princess", price = 9.49)

        assertEquals(Product(id = "1", name = "Essence Mascara Lash Princess", price = 9), dto.toDomain())
    }
}
