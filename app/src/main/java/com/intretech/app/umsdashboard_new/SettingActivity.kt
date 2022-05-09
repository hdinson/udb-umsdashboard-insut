package com.intretech.app.umsdashboard_new

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.intretech.app.umsdashboard_new.http.HttpHelper
import com.intretech.app.umsdashboard_new.utils.MMKVUtils
import kotlinx.android.synthetic.main.activity_setting.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.thread
import com.intretech.app.umsdashboard_new.ping.PingJob
import com.intretech.app.umsdashboard_new.utils.AtyContainer


class SettingActivity : AppCompatActivity() {

    private val mRenderingEngine = hashMapOf(0 to R.id.rbSystem, 1 to R.id.rbCrosswalk )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        tvVersion.text = "版本号:   v${getVersionName()}  Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
        tvHomePage.text = "网页加载地址: ${intent.getStringExtra(EXTRA_HOME_PAGE)}"


        rgRenderingEngine.check(mRenderingEngine.getValue(MMKVUtils.getRenderingEngine()))

        etServerAddr.setText(MMKVUtils.getBaseUrl())
        etMacAddr.setText(MMKVUtils.getMac())

        btnSubmit.setOnClickListener { applyChange(false) }
        btnReset.setOnClickListener { applyChange(true) }


       /* cbShowQrCode.isChecked = MMKVUtils.isShowHomePageQrCode()
        cbShowQrCode.setOnCheckedChangeListener { _, isChecked ->
            hasChange = true
            MMKVUtils.setHomePageIsShowQrCode(isChecked)
        }*/


    }

    private fun applyChange(isResetChange: Boolean) {
        val tips = if (isResetChange) "恢复默认设置需要重新启动app后才能生效，是否重启?" else "修改设置需要重新启动app后才能生效，是否重启?"
        AlertDialog.Builder(this).setTitle("注意")
            .setMessage(tips)
            .setNegativeButton("否") { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton("是") { dialog, _ ->
                if (isResetChange) {
                    //清除所有保存的设置数据
                    MMKVUtils.clearAppConfig()
                } else {
                    //保存参数



                    val host = etServerAddr.text.toString().trim()
                    val mac = etMacAddr.text.toString().trim()

                    //MAC地址或者host改变
                    MMKVUtils.saveBaseUrl(host)
                    MMKVUtils.saveMac(mac)
                    HttpHelper.updateBaseUrl(host)
                    setResult(RESULT_OK)

                    finish()

                    val id = rgRenderingEngine.checkedRadioButtonId
                    mRenderingEngine.forEach {
                        if (it.value==id){
                            MMKVUtils.saveRenderingEngine(it.key)
                            return@forEach
                        }
                    }
                }
                dialog.dismiss()
                AtyContainer.finishAllActivity()
            }.create().show()

    }

    private fun getVersionName(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "Unknown"
        }
    }

    companion object {
        private const val REQ_CODE = 11001
        private const val EXTRA_HOME_PAGE = "home_page"
        fun start(act: Activity, homePage: String) {
            val intent = Intent(act, SettingActivity::class.java)
            intent.putExtra(EXTRA_HOME_PAGE, homePage)
            act.startActivityForResult(intent, REQ_CODE)
        }
    }



}