package com.mcs.productphotography.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.mcs.productphotography.models.Product
import com.mcs.productphotography.viewmodels.ProductViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object Utility {

    fun rotateBitmap(source: Bitmap, degrees:Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(
            source,0,0,source.width,source.height,matrix,true
        )
    }
    fun getPhotoFile(context: Context,fileName: String): File {
        // Use getExternalFilesDiron Context to access package-specific directoties
        val storageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName,".jpg",storageDirectory)
    }
    fun saveProduct(editTexts: MutableList<EditText>, productViewModel: ProductViewModel, context: Context):Boolean {
        val barcode = editTexts[0].text.toString()
        val desc = editTexts[1].text.toString()
        val length = editTexts[2].text.toString()
        val width = editTexts[3].text.toString()
        val height = editTexts[4].text.toString()
        val weight = editTexts[5].text.toString()

        val isSaved: Boolean = if (checkIfEmpty(barcode, desc, length, width, height, weight)){
            Toast.makeText(context,"All fields are required!", Toast.LENGTH_LONG).show()
            false
        }else{
            productViewModel.insert(Product(barcode,desc,length.toDouble(),width.toDouble(),height.toDouble(),weight.toDouble()))
            emptyAllFields(editTexts,context)
            true
        }
        return isSaved
    }
    private fun emptyAllFields(editTexts: MutableList<EditText>,context: Context) {
        for (et in editTexts){
            et.setText("")
        }
        editTexts[0].requestFocus()
        val imm: InputMethodManager? =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(editTexts[0], InputMethodManager.SHOW_IMPLICIT)
    }
    private fun checkIfEmpty(vararg containers: String): Boolean{
        var isEmpty = false
        for (container in containers){
            if(container.isEmpty()){
                isEmpty = true
            }
        }
        return isEmpty
    }

    private fun createFileName(): String? {
        val date = Date(System.currentTimeMillis())
        val format = SimpleDateFormat("yyyyMMdd_HH-mm-ss")
        return format.format(date)
    }

    fun createCSV(launcher: ActivityResultLauncher<Intent>) {
        val fileName:String = createFileName().toString()+".csv"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/csv"
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        launcher.launch(intent)
    }

    fun saveCSV(data: Intent?, context: Context, products: List<Product>) {
        if (data != null) {

            val row1 = listOf("Barcode", "Length", "Width", "Height","Weight","Description")

            val uri = data.data
            try {
                val stream = context.contentResolver.openOutputStream(uri!!)
                if (stream != null) {
                    csvWriter().open(stream) {
                        writeRow(row1)
                        for (product in products){
                            writeRow(listOf(product.barcode, product.length, product.width, product.height,product.weight,product.description))
                        }
                    }
                }
                stream?.close()
                Toast.makeText(context, "File successfully saved", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "File not saved", Toast.LENGTH_SHORT).show()
        }

    }
}