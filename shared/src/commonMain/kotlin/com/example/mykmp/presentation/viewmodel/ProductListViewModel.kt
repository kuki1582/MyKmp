package com.example.mykmp.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.usecase.GetProductsUseCase
import com.example.mykmp.presentation.intent.ProductListIntent
import com.example.mykmp.presentation.sideEffect.ProductListSideEffect
import com.example.mykmp.presentation.state.ProductListUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductListViewModel(
    private val getProductsUseCase: GetProductsUseCase
): ViewModel() {
    private val _state = MutableStateFlow(ProductListUiState())
    val state = _state.asStateFlow()

    private val _sideEffect = Channel<ProductListSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onIntent(intent: ProductListIntent){
        when(intent){
            is ProductListIntent.Load -> loadProducts()
            is ProductListIntent.ClickProduct -> {
                viewModelScope.launch {
                    _sideEffect.send(
                        ProductListSideEffect.NavigateToDetail(intent.id)
                    )
                }
            }
        }
    }

    private fun loadProducts(){
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when(val result = getProductsUseCase()){
                is DomainResult.Success-> _state.update {
                    it.copy(isLoading = false, products = result.data)
                }
                is DomainResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
