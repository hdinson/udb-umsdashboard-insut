package com.intretech.app.umsdashboard_new

import android.os.Bundle
import android.view.WindowManager
import com.intretech.app.umsdashboard_new.utils.AtyContainer
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity

open class BaseActivity : RxAppCompatActivity() {
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