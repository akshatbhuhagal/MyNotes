package com.akshatbhuhagal.mynotes.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.akshatbhuhagal.mynotes.R
import com.akshatbhuhagal.mynotes.databinding.ActivityMainBinding
import com.akshatbhuhagal.mynotes.presentation.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        replaceFragment(HomeFragment.newInstance(), true)
        
    }

    private fun replaceFragment(fragment: Fragment, isTransition: Boolean) {

        val fragmentTransition = supportFragmentManager.beginTransaction()

        if (isTransition) {
            fragmentTransition.setCustomAnimations(
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left
            )
        }
        fragmentTransition.replace(R.id.flFragmenet, fragment).addToBackStack(fragment.javaClass.simpleName)
        fragmentTransition.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}
