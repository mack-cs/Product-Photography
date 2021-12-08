package com.mcs.productphotography.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.mcs.productphotography.Product
import com.mcs.productphotography.ProductViewModel
import com.mcs.takesaveloadpic.ExternalStoragePhoto
import java.io.File

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
    fun saveProduct(editTexts: MutableList<EditText>, productViewModel: ProductViewModel,context: Context) {
        val barcode = editTexts[0].text.toString()
        val desc = editTexts[1].text.toString()
        val length = editTexts[2].text.toString()
        val width = editTexts[3].text.toString()
        val height = editTexts[4].text.toString()
        val weight = editTexts[5].text.toString()

        if (checkIfEmpty(barcode, desc, length, width, height, weight)){
            Toast.makeText(context,"All fields are required!", Toast.LENGTH_LONG).show()
        }else{
            productViewModel.insert(Product(barcode,desc,length.toDouble(),width.toDouble(),height.toDouble(),weight.toDouble()))
            emptyAllFields(editTexts,context)
        }
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
}