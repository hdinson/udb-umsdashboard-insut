package com.intretech.app.umsdashboard_new.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.intretech.app.umsdashboard_new.R
import com.intretech.app.umsdashboard_new.utils.MMKVUtils
import com.tbruyelle.rxpermissions2.RxPermissions


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val rxp = RxPermissions(this).request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { b ->
                if (!b) {
                    Toast.makeText(this, "请同意存储卡权限，用来安装浏览器内核", Toast.LENGTH_SHORT).show()
                    return@subscribe
                }
                when (MMKVUtils.getRenderingEngine()) {
                    0 -> startActivity(Intent(this, WebkitSystemActivity::class.java))
                    1 -> startActivity(Intent(this, XWalkWebViewActivity::class.java))
                    else -> startActivity(Intent(this, WebkitSystemActivity::class.java))
                }
                finish()
            }
    }
}