package com.mcs.productphotography

import android.util.Log
import androidx.lifecycle.*
import com.mcs.productphotography.Product
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository):ViewModel() {

    val allProducts: LiveData<List<Product>> = repository.allProducts.asLiveData()
    var returnedId = MutableLiveData<Long>()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(product: Product) = viewModelScope.launch {
        val rId = repository.insert(product)
        returnedId.value = rId
        Log.i("ModelView ID", "$rId")
    }
}
class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}