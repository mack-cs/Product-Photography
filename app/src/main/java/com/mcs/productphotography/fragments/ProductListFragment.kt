package com.mcs.productphotography.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.mcs.productphotography.*
import com.mcs.productphotography.databinding.FragmentProductListBinding
import com.mcs.productphotography.models.Product
import com.mcs.productphotography.utils.ProductApplication
import com.mcs.productphotography.utils.Utility
import com.mcs.productphotography.viewmodels.ProductViewModel
import com.mcs.productphotography.viewmodels.ProductViewModelFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ProductListFragment : Fragment() {
    private lateinit var binding:FragmentProductListBinding
    private lateinit var  productsList:List<Product>
    private val productViewModel: ProductViewModel by activityViewModels {
        ProductViewModelFactory((activity?.application as ProductApplication).repository) }
    private val saveCSVLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        Utility.saveCSV(result.data,requireContext(),productsList)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductListBinding.inflate(inflater,container,false)
        val view = binding.root

        val adapter = ProductListAdapter()
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(activity)
        productViewModel.allProducts.observe(viewLifecycleOwner){
                products->products.let { adapter.submitList(it)
            productsList = it
                }
        }
        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        Log.i("Frag-List","OnCreateView")
        return view

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.export->{
                Utility.createCSV(saveCSVLauncher)
                true
            }
            R.id.clear->{
                clearAllEntries()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

   private fun clearAllEntries() {
        if (productsList.isEmpty()){
            Toast.makeText(requireContext(),"Nothing to clear",Toast.LENGTH_LONG).show()
        }else {
            clearContents(requireContext()).show()
        }
    }
    private fun clearContents(context: Context): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle("Clear all")
            .setMessage(getString(R.string.delete_msg))
            .setIcon(R.drawable.ic_baseline_delete_forever_24)
            .setPositiveButton(R.string.yes){_, _ ->
                productViewModel.deleteAll()
                Toast.makeText(requireContext(),"You have cleared all the contents",Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(R.string.cancel){_, _ ->
                Toast.makeText(requireContext(),"You didn't clear the contents",Toast.LENGTH_LONG).show()
            }.create()
    }
}