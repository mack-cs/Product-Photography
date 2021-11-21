package com.mcs.productphotography

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ProductApplication  : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    val applicationScope = CoroutineScope(SupervisorJob())
    // rather than when the application starts
    val database by lazy { ProductRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { ProductRepository(database.productDao()) }
}