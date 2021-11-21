package com.mcs.productphotography.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.zxing.integration.android.IntentIntegrator
import com.mcs.productphotography.*
import com.mcs.productphotography.databinding.FragmentAddProductBinding
import androidx.core.content.ContextCompat.getSystemService

import android.R
import android.content.Context
import android.view.inputmethod.InputMethodManager

import android.widget.EditText
import androidx.core.content.ContextCompat


class AddProductFragment : Fragment() {
    private  var _binding: FragmentAddProductBinding? = null
    private lateinit var barcodeScanner: IntentIntegrator
    private val binding get() = _binding!!
    private  var returnedId:Long = 0
    private val productViewModel: ProductViewModel by activityViewModels{ProductViewModelFactory(
        (activity?.application as ProductApplication).repository)}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.saveProductET.setOnClickListener { saveProduct() }
        productViewModel.returnedId.observe(requireActivity(), Observer {
            returnedId  = productViewModel.returnedId.value!!
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListener()
        setupScanner()
    }

    private fun setupScanner() {
        barcodeScanner = IntentIntegrator.forSupportFragment(this)
        barcodeScanner.captureActivity = CaptureActivityPortait::class.java
        barcodeScanner.setOrientationLocked(true)
    }

    private fun setOnClickListener() {
        binding.cameraScanIV.setOnClickListener { performAction() }
    }

    private fun saveProduct() {
        val barcode = binding.barcodeET.text.toString()
        val desc = binding.descriptionET.text.toString()
        val length = binding.lengthET.text.toString()
        val width = binding.widthET.text.toString()
        val height = binding.heightET.text.toString()
        val weight = binding.weightET.text.toString()


        if (checkIfEmpty(barcode, desc, length, width, height, weight)){
            Toast.makeText(activity,"All fields are required!",Toast.LENGTH_LONG).show()
        }else{
            productViewModel.insert(Product(barcode,desc,length.toDouble(),width.toDouble(),height.toDouble(),weight.toDouble()))
            emptyFields()
        }
    }

    private fun emptyFields() {
        binding.barcodeET.setText("")
        binding.descriptionET.setText("")
        binding.lengthET.setText("")
        binding.widthET.setText("")
        binding.heightET.setText("")
        binding.weightET.setText("")
        binding.barcodeET.requestFocus()
        val imm: InputMethodManager? =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(binding.barcodeET, InputMethodManager.SHOW_IMPLICIT)


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

    private fun performAction() {
        barcodeScanner.initiateScan()
    }

    override fun onResume() {
        Log.i("Frag-Resume","Resumed")
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result.contents == null){
            Toast.makeText(activity, "Canceled!", Toast.LENGTH_LONG).show()
        } else {
            var a = result.contents.toString()
            binding.barcodeET.setText(result.contents.toString())
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Frag-D","Frag Destroyed")
        _binding = null
    }
}