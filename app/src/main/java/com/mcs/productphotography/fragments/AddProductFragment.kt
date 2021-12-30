package com.mcs.productphotography.fragments

import android.Manifest
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


import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar

import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.mcs.productphotography.utils.Utility
import com.mcs.productphotography.utils.Utility.saveProduct
import com.mcs.productphotography.models.ExternalStoragePhoto
import com.mcs.productphotography.utils.ProductApplication
import com.mcs.productphotography.viewmodels.ProductViewModel
import com.mcs.productphotography.viewmodels.ProductViewModelFactory
import kotlinx.coroutines.launch
import java.io.File


class AddProductFragment : Fragment() {
    private lateinit var capturePhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var fragmentLauncher: ActivityResultLauncher<ScanOptions>
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mView: View
    private lateinit var photoFile: File
    private val FILE_NAME = "photo.jpg"
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var contentObserver: ContentObserver

    private lateinit var externalStoragePhotoAdapter: ListPhotosAdapter
    var isSaved:Boolean = false
    private var fileFolder:String = ""
    private  var _binding: FragmentAddProductBinding? = null
    private var photosList: List<ExternalStoragePhoto>? = null
    private val binding get() = _binding!!
    private  var returnedId:Long = 0
    private val productViewModel: ProductViewModel by activityViewModels{
        ProductViewModelFactory(
        (activity?.application as ProductApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        mView = binding.root

        val editTexts = mutableListOf<EditText>()
        editTexts.add(binding.barcodeET)
        editTexts.add(binding.descriptionET)
        editTexts.add(binding.lengthET)
        editTexts.add(binding.widthET)
        editTexts.add(binding.heightET)
        editTexts.add(binding.weightET)

        fragmentLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(context, "Scan Canceled", Toast.LENGTH_LONG).show()
            } else {
                binding.barcodeET.setText(result.contents.toString())
            }
        }
        capturePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            processImage(result.resultCode)
        }

        binding.saveProductET.setOnClickListener {
            isSaved = saveProduct(editTexts, productViewModel,requireContext(),mView)
            if (isSaved){
                loadPhotosFromExternalStorageIntoRecyclerView("non-existing-folder")
                isSaved = false
            }
        }

        productViewModel.returnedId.observe(requireActivity(), Observer {
            returnedId  = productViewModel.returnedId.value!!
        })
        binding.barcodeET.onFocusChangeListener = View.OnFocusChangeListener{ _, b ->
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
        return mView
    }

    private fun captureImage() {
        if (binding.barcodeET.text.toString() != "") {
            fileFolder = binding.barcodeET.text.toString()
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = Utility.getPhotoFile(requireActivity(), FILE_NAME)
            val fileProvider = activity?.application?.let { it1 ->
                FileProvider.getUriForFile(
                    it1,
                    "package com.mcs.productphotography.fileprovider",
                    photoFile
                )
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            // Check if there is camera
            if (requireActivity().let { it1 -> takePictureIntent.resolveActivity(it1.packageManager) } != null) {
                capturePhotoLauncher.launch(takePictureIntent)
            } else {
                Snackbar.make(mView, "Unable to open camera", Snackbar.LENGTH_LONG).show()
            }
        } else {
            Snackbar.make(mView,"Barcode required",Snackbar.LENGTH_SHORT).show()
        }
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
        binding.btnCapture.setOnClickListener {
            captureImage()
        }
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
            if (!isSaved) {
                photosList = productViewModel.getImages(folder, requireContext())
                externalStoragePhotoAdapter.submitList(photosList)
            }else{
                externalStoragePhotoAdapter.submitList(null)
            }

        }
    }




    private fun processImage(resultCode: Int) {
        var isSavedPhoto:Boolean = false
        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                //val takenImage = data?.extras?.get("data") as Bitmap
                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                val rotatedImg = Utility.rotateBitmap(takenImage,90f)
                activity?.let {
                    isSavedPhoto = productViewModel.saveBitmapQ(it, rotatedImg,fileFolder) } //save full quality
                if (isSavedPhoto){
                    takeAnotherPhoto().show()
                }
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
        _binding = null
        requireActivity().contentResolver?.unregisterContentObserver(contentObserver)
    }

    private fun takeAnotherPhoto(): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Take another Photo")
            .setIcon(R.drawable.ic_baseline_add_a_photo_24)
            .setPositiveButton(R.string.yes){_, _ ->
                captureImage()
            }
            .setNegativeButton(R.string.cancel){_, _ ->
                //Toast.makeText(requireContext(),"You didn't clear the contents",Toast.LENGTH_LONG).show()
            }.create()
    }


}