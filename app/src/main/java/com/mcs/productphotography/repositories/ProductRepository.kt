package com.mcs.productphotography.repositories

import android.util.Log
import androidx.annotation.WorkerThread
import com.mcs.productphotography.daos.ProductDao
import com.mcs.productphotography.models.Product
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
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        productDao.deleteAll()
    }

}