package com.mcs.productphotography.viewmodels

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.*
import android.provider.MediaStore.Images.*
import android.provider.MediaStore.Images.Media.*
import android.util.Log
import androidx.lifecycle.*
import com.mcs.productphotography.models.Product
import com.mcs.productphotography.models.ExternalStoragePhoto
import com.mcs.productphotography.repositories.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream

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
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun saveBitmapQ(context: Context, bitmap: Bitmap,folder: String):Boolean{
        var photoSaved:Boolean = false
        val relativeLocation = Environment.DIRECTORY_PICTURES + File.separator + folder

        val contentValues = ContentValues()
        contentValues.put(MediaColumns.DISPLAY_NAME, "$folder.jpg") //this is the file name you want to save
        contentValues.put(MediaColumns.MIME_TYPE, "image/jpg") // Content-Type
        contentValues.put(MediaColumns.RELATIVE_PATH, relativeLocation)

        val resolver = context.contentResolver

        var stream: OutputStream? = null
        var uri: Uri? = null

        try {
            val contentUri = EXTERNAL_CONTENT_URI
            uri = resolver.insert(contentUri, contentValues)

            if (uri == null) {
                throw IOException("Failed to create new MediaStore record.")
            }

            stream = resolver.openOutputStream(uri)

            if (stream == null) {
                throw IOException("Failed to get output stream.")
            }

            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                throw IOException("Failed to save bitmap.")
            }
            photoSaved = true
        } catch (e: IOException) {
            if (uri != null) {
                resolver.delete(uri, null, null)
            }
            throw e
        } finally {
            stream?.close()
        }
        return photoSaved
    }

     suspend fun getImages(fileFolder: String, context: Context): List<ExternalStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val path = fileFolder //Custom Directory name which you used earlier for storing images

            val selection = Files.FileColumns.RELATIVE_PATH + " like ? "

            val selectionargs = arrayOf("%$path%")

            val externalUri = EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                Files.FileColumns._ID,
                DATE_TAKEN,
                MediaColumns.TITLE,
                HEIGHT,
                WIDTH,
                MIME_TYPE,
                MediaColumns.RELATIVE_PATH
            )
            val photos = mutableListOf<ExternalStoragePhoto>()
            val cursor = context.contentResolver?.query(
                externalUri,
                projection,
                selection,
                selectionargs,
                DATE_TAKEN
            )
            val idColumn = cursor?.getColumnIndex(MediaColumns._ID)

            while (cursor?.moveToNext()!!) {
                val photoUri = Uri.withAppendedPath(
                    EXTERNAL_CONTENT_URI,
                    cursor.getString(idColumn!!)
                )
                //This will give you the uri of the images from that custom folder
                if (photoUri != null) {
                    var fileName: String? = null
                    if (photoUri.toString().startsWith("file:")) {
                        fileName = photoUri.path
                    } else {
                        val c = context.contentResolver?.query(photoUri, null, null, null, null)
                        if (c != null && c.moveToFirst()) {
                            val id = c.getColumnIndex(DATA)
                            if (id != -1) {
                                fileName = c.getString(id)
                            }
                        }
                    }
                    Log.e("PHOTO", "Name is$fileName")
                }
                Log.e("PHOTO", "URI is$photoUri")
                val `is` = context.contentResolver?.openInputStream(photoUri)
                val bitmap = BitmapFactory.decodeStream(`is`)
                photos.add(ExternalStoragePhoto(bitmap))
                // Do whatever you want to do with your bitmaps
            }
            photos.toList()
        } ?: listOf()
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