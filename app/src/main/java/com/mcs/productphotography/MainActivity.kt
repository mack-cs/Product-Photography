package com.mcs.productphotography

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mcs.productphotography.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val iconIV = binding.mainScreenIV

        iconIV.alpha = 0f
        iconIV.animate().setDuration(1500).alpha(1f).withEndAction{
            val productActivityIntent = Intent(this,ProductActivity::class.java)
            startActivity(productActivityIntent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }
    }
}