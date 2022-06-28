package com.devss.familyalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeUtils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_setting)

        orange_tv_btn.setOnClickListener {
            themeUtils.changeToTheme(this, themeUtils.BLACK)
        }
        blue_tv_btn.setOnClickListener {
            themeUtils.changeToTheme(this, themeUtils.BLUE)
        }
    }
}