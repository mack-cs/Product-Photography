package com.mcs.productphotography

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.mcs.productphotography.databinding.ActivityProductBinding
import com.mcs.productphotography.fragments.AddProductFragment
import com.mcs.productphotography.fragments.ProductListFragment

class ProductActivity : AppCompatActivity() {
    private var returnedId: Long = 0
    private lateinit var binding:ActivityProductBinding
    private val productViewModel: ProductViewModel by viewModels {
        ProductViewModelFactory((application as ProductApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("On Create","Created")


        val addProductFragment = AddProductFragment()
        val productsListFragment = ProductListFragment()

        makeCurrentFragment(addProductFragment)
        binding.bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.addProduct -> makeCurrentFragment(addProductFragment)
                R.id.productList -> makeCurrentFragment(productsListFragment)
            }
            true
        }
        productViewModel.returnedId.observe(this, Observer {
            returnedId  = productViewModel.returnedId.value!!
            if(returnedId > 0){
                Toast.makeText(this, "Product added!",Toast.LENGTH_LONG).show()
            }else if(returnedId == -1L){
                Toast.makeText(this, "Product already exists!",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Product not added!",Toast.LENGTH_LONG).show()
            }
            Log.i("Activity - OnCreate-i","In VMid = $returnedId")
        })
        Log.i("Activity - OnCreate-ot","After VMid = $returnedId")
    }
    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }
