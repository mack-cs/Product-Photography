package com.mcs.productphotography.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(@PrimaryKey var barcode:String, @ColumnInfo(name = "description") var description:String, @ColumnInfo(name = "length") var length:Double,
                   @ColumnInfo(name = "width") var width:Double, @ColumnInfo(name = "height") var height:Double,
                   @ColumnInfo(name = "weight") var weight:Double)