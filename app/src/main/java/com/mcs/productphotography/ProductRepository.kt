package com.mcs.productphotography

import android.util.Log
import androidx.annotation.WorkerThread
import com.mcs.productphotography.Product
import com.mcs.productphotography.ProductDao
import kotlinx.coroutines.flow.Flow


class ProductRepository(private val productDao: ProductDao) {
    val allProducts : Flow<List<Product>> = productDao.getOrderedProducts()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(product: Product):Long {
        val id = productDao.insert(product)
        Log.i("Repository id ->","( $id )")
       return id
    }

}