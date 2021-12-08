package com.mcs.productphotography

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.mcs.takesaveloadpic.ExternalStoragePhoto
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

    fun saveBitmapQ(context: Context, bitmap: Bitmap,folder: String) {
        //val relativeLocation = Environment.DIRECTORY_PICTURE

        val relativeLocation = Environment.DIRECTORY_PICTURES + File.separator + folder

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$folder.jpg") //this is the file name you want to save
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg") // Content-Type
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)

        val resolver = context.contentResolver

        var stream: OutputStream? = null
        var uri: Uri? = null

        try {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            uri = resolver.insert(contentUri, contentValues)

            if (uri == null) {
                throw IOException("Failed to create new MediaStore record.")
            }

            stream = resolver.openOutputStream(uri)

            if (stream == null) {
                throw IOException("Failed to get output stream.")
            }

            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream) == false) {
                throw IOException("Failed to save bitmap.")
            }
        } catch (e: IOException) {
            if (uri != null) {
                resolver.delete(uri, null, null)
            }
            throw e
        } finally {
            stream?.close()
        }
    }

     suspend fun getImages(fileFolder: String, context: Context): List<ExternalStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val path = fileFolder //Custom Directory name which you used earlier for storing images

            val selection = MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? "

            val selectionargs = arrayOf("%$path%")

            val externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.MediaColumns.TITLE,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.MediaColumns.RELATIVE_PATH
            )
            val photos = mutableListOf<ExternalStoragePhoto>()
            val cursor = context.contentResolver?.query(
                externalUri,
                projection,
                selection,
                selectionargs,
                MediaStore.Images.Media.DATE_TAKEN
            )
            val idColumn = cursor?.getColumnIndex(MediaStore.MediaColumns._ID)

            while (cursor?.moveToNext()!!) {
                val photoUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getString(idColumn!!)
                )
                //This will give you the uri of the images from that custom folder
                if (photoUri != null) {
                    var fileName: String? = null
                    if (photoUri.toString().startsWith("file:")) {
                        fileName = photoUri.getPath()
                    } else {
                        val c = context.contentResolver?.query(photoUri, null, null, null, null)
                        if (c != null && c.moveToFirst()) {
                            val id = c.getColumnIndex(MediaStore.Images.Media.DATA)
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