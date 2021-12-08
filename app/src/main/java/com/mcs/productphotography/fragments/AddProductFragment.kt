package com.mcs.productphotography.fragments

import android.Manifest
import android.content.ContentValues
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
import com.mcs.productphotography.*
import com.mcs.productphotography.databinding.FragmentAddProductBinding


import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope

import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.mcs.productphotography.fragments.Utility.saveProduct
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.OutputStream


class AddProductFragment : Fragment() {
    private lateinit var photoFile: File
    private val FILE_NAME = "photo.jpg"
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var contentObserver: ContentObserver

    private lateinit var externalStoragePhotoAdapter: ListPhotosAdapter
    private var fileFolder:String = ""
    private  var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private  var returnedId:Long = 0
    private val productViewModel: ProductViewModel by activityViewModels{ProductViewModelFactory(
        (activity?.application as ProductApplication).repository)}
    private val fragmentLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(context, "Scan Canceled", Toast.LENGTH_LONG).show()
        } else {
            binding.barcodeET.setText(result.contents.toString())
        }
    }
    private val capturePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        processImage(result.resultCode)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        val view = binding.root

        val editTexts = mutableListOf<EditText>()
        editTexts.add(binding.barcodeET)
        editTexts.add(binding.descriptionET)
        editTexts.add(binding.lengthET)
        editTexts.add(binding.widthET)
        editTexts.add(binding.heightET)
        editTexts.add(binding.weightET)

        binding.saveProductET.setOnClickListener { saveProduct(editTexts, productViewModel,requireContext()) }
        productViewModel.returnedId.observe(requireActivity(), Observer {
            returnedId  = productViewModel.returnedId.value!!
        })
        binding.barcodeET.onFocusChangeListener = View.OnFocusChangeListener{ view, b ->
            if (!b) {
                fileFolder = binding.barcodeET.text.toString()
            }
        }

        externalStoragePhotoAdapter = ListPhotosAdapter()
        setupExternalStorageRecyclerView()
        initContentObserver()

        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

            if (readPermissionGranted) {
                if (fileFolder != "") {
                    loadPhotosFromExternalStorageIntoRecyclerView(fileFolder)
                }
            } else {
                Toast.makeText(activity, "Can't read files without permission.", Toast.LENGTH_LONG)
                    .show()
            }
        }
        updateOrRequestPermissions()
        binding.btnCapture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = Utility.getPhotoFile(requireActivity(),FILE_NAME)

            // This Doesnt work for API >= 24,(starting2016)
            //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)
            val fileProvider = activity?.application?.let { it1 -> FileProvider.getUriForFile(it1,"package com.mcs.productphotography.fileprovider", photoFile) }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileProvider)
            // Check if there is camera
            if (activity?.let { it1 -> takePictureIntent.resolveActivity(it1.packageManager) } !=null) {
                capturePhotoLauncher.launch(takePictureIntent)
            }else{
                Toast.makeText(activity,"Unable to open camera", Toast.LENGTH_LONG).show()
            }
        }
        if (fileFolder != "")loadPhotosFromExternalStorageIntoRecyclerView(fileFolder)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListener()
    }

    private fun setupScanner() {
        fragmentLauncher.launch(ScanOptions().setCaptureActivity(CaptureActivityPortait::class.java))
    }

    private fun setOnClickListener() {
        binding.cameraScanIV.setOnClickListener { setupScanner() }
    }







    private fun initContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                if (readPermissionGranted && fileFolder != "") {
                    loadPhotosFromExternalStorageIntoRecyclerView(fileFolder)
                }
            }
        }
        activity?.contentResolver?.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }
    private fun setupExternalStorageRecyclerView() = binding.rvPhotos.apply {
        adapter = externalStoragePhotoAdapter

    }
    private fun loadPhotosFromExternalStorageIntoRecyclerView(folder:String) {

        lifecycleScope.launch {
            val photos = productViewModel.getImages(folder,requireContext())
            Log.i("Photos","${photos.size}")
            externalStoragePhotoAdapter.submitList(photos)
        }
    }




    private fun processImage(resultCode: Int) {
        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                //val takenImage = data?.extras?.get("data") as Bitmap
                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                val rotatedImg = Utility.rotateBitmap(takenImage,90f)
                activity?.let { productViewModel.saveBitmapQ(it, rotatedImg,fileFolder) } //save full quality
            }
            else -> {
                Toast.makeText(activity, "Photo Capture Canceled", Toast.LENGTH_LONG).show()
            }
        }
    }




    private fun updateOrRequestPermissions() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if(!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Frag-D","Frag Destroyed")
        _binding = null
        requireActivity().contentResolver?.unregisterContentObserver(contentObserver)
    }
}