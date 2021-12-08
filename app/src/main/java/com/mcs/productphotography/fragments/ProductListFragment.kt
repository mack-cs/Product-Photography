package com.mcs.productphotography.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mcs.productphotography.*


class ProductListFragment : Fragment() {
    private val productViewModel: ProductViewModel by activityViewModels {
        ProductViewModelFactory((activity?.application as ProductApplication).repository) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_product_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = ProductListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        productViewModel.allProducts.observe(viewLifecycleOwner){
                products->products.let { adapter.submitList(it) }
        }
        setHasOptionsMenu(true)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        return view

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.export->{
                exportDataToCSV()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportDataToCSV() {
        Toast.makeText(requireContext(),"Yes Clickable",Toast.LENGTH_LONG).show()
    }
}