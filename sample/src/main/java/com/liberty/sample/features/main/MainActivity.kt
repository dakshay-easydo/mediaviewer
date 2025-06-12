package com.liberty.sample.features.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.liberty.sample.R
import com.liberty.sample.databinding.ActivityMainBinding
import com.liberty.sample.features.demo.grid.PostersGridDemoActivity
import com.liberty.sample.features.demo.rotation.RotationDemoActivity
import com.liberty.sample.features.demo.scroll.ScrollingImagesDemoActivity
import com.liberty.sample.features.demo.styled.StylingDemoActivity
import com.liberty.sample.features.main.adapter.MainActivityPagerAdapter
import com.liberty.sample.features.main.adapter.MainActivityPagerAdapter.Companion.ID_IMAGES_GRID
import com.liberty.sample.features.main.adapter.MainActivityPagerAdapter.Companion.ID_ROTATION
import com.liberty.sample.features.main.adapter.MainActivityPagerAdapter.Companion.ID_SCROLL
import com.liberty.sample.features.main.adapter.MainActivityPagerAdapter.Companion.ID_STYLING
import com.liberty.sample.features.main.card.DemoCardFragment

class MainActivity : AppCompatActivity(),
    DemoCardFragment.OnCardActionListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainCardsViewPager.apply {
            adapter = MainActivityPagerAdapter(this@MainActivity, supportFragmentManager)
            pageMargin = resources.getDimension(R.dimen.card_padding).toInt() / 4
            offscreenPageLimit = 3
        }
    }

    override fun onCardAction(actionId: Int) {
        when (actionId) {
            ID_IMAGES_GRID -> {
                startActivity(Intent(this, PostersGridDemoActivity::class.java))
            }
            ID_SCROLL -> {
                startActivity(Intent(this, ScrollingImagesDemoActivity::class.java))
            }
            ID_STYLING -> {
                startActivity(Intent(this, StylingDemoActivity::class.java))
            }
            ID_ROTATION -> {
                startActivity(Intent(this, RotationDemoActivity::class.java))
            }
        }
    }
}
