package com.intretech.app.umsdashboard_new.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.intretech.app.umsdashboard_new.utils.AtyContainer

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        AtyContainer.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        AtyContainer.removeActivity(this)
    }
}