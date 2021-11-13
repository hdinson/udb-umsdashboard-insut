package com.intretech.app.umsdashboard_new

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.intretech.app.umsdashboard_new.http.HttpHelper
import com.intretech.app.umsdashboard_new.utils.MMKVUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_setting.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.thread

class SettingActivity : AppCompatActivity() {

    private var hasChange = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        tvVersion.text = "版本号:   v${getVersionName()}"
        etServerAddr.setText(MMKVUtils.getBaseUrl())
        etMacAddr.setText(MMKVUtils.getMac())

        btnSubmit.setOnClickListener {
            val host = etServerAddr.text.toString().trim()
            val mac = etMacAddr.text.toString().trim()

            //MAC地址或者host改变
            MMKVUtils.saveBaseUrl(host)
            MMKVUtils.saveMac(mac)
            HttpHelper.updateBaseUrl(host)
            setResult(RESULT_OK)

            finish()
        }

        cbShowQrCode.isChecked = MMKVUtils.isShowHomePageQrCode()
        cbShowQrCode.setOnCheckedChangeListener { _, isChecked ->
            hasChange = true
            MMKVUtils.setHomePageIsShowQrCode(isChecked)
        }

        btnPing.setOnClickListener {
            tvPingDetails.text = ""
           ping()
        }


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

        fun start(act: Activity) {
            act.startActivityForResult(Intent(act, SettingActivity::class.java), REQ_CODE)
        }
    }

    private val lines = ArrayList<String>()
    private fun ping() {
        val ip = BuildConfig.BASE_URL
        val sizeStr = "64"
        val time = "5"

        val size = sizeStr.toInt() - 8
        val command = "ping -s $size -w $time api.longtechcc.com"

        // 注：正常ping数据和错误ping数据可能会交替输出，所以需要开两个线程同时读取
        val process = Runtime.getRuntime().exec(command)
        val inputStreamThread = readData(process.inputStream) // 读取正常ping数据
        val errorStreamThread = readData(process.errorStream) // 读取错误ping数据

        // 等待两个读取线程结束
        inputStreamThread.join()
        errorStreamThread.join()

        Log.e("TAG", "ping: $lines" )
    }

    private fun readData(inputStream: InputStream?) = thread {
        try {
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val lineTemp = line!!
                    runOnUiThread { addData(lineTemp) } // 这里切换到了UI线程，子线程继续执行时可以已经把line对象又赋值为null了，所以使用了lineTemp来预防值被重新赋值
                }
            }
        } catch (e: Exception) {
            runOnUiThread { addData("出现异常：${e.javaClass.simpleName}: ${e.message}") }
        }
    }

    private fun addData(data: String) {
        lines.add(data)
        tvPingDetails.append(data)
    }



    /**
     * 验证给定的ip地址是否有效
     * @param ip
     */
    private fun isValidIpAddress(ip: String?): Boolean {
        if (ip.isNullOrBlank()) return false
        val regex = "(2(5[0-5]{1}|[0-4]\\d{1})|[0-1]?\\d{1,2})(\\.(2(5[0-5]{1}|[0-4]\\d{1})|[0-1]?\\d{1,2})){3}"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(ip)
        return matcher.matches()
    }
}