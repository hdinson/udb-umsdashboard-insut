package com.intretech.app.umsdashboard_new.crash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.intretech.app.umsdashboard_new.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_crash.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ActivityCrash : FragmentActivity() {
    @SuppressLint("PrivateResource", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val a = obtainStyledAttributes(R.styleable.AppCompatTheme)
        if (!a.hasValue(R.styleable.AppCompatTheme_windowActionBar)) {
            setTheme(R.style.Theme_AppCompat_Light_DarkActionBar)
        }
        a.recycle()
        setContentView(R.layout.activity_crash)

        val config = CrashTool.getConfigFromIntent(intent)
        if (config == null) {
            finish()
            return
        }



        val message = CrashTool.getAllErrorDetailsFromIntent(this@ActivityCrash, intent)
        val file = log2File(message)

        tvLogDetails.text = message
        tvCrashErrorLocateMoreInfo.text = "${tvCrashErrorLocateMoreInfo.text}\n\n${file.absolutePath}\n"



        val b= Observable.timer(10L,TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                CrashTool.restartApplication(this@ActivityCrash, config)
            }
    }



    /**
     * 打开日志文件并写入日志
     *
     * @return
     */
    @Synchronized
    private fun log2File(text: String): File {
        val now = Date()
        val date = SimpleDateFormat("HH点mm分ss秒", Locale.getDefault()).format(now)

//        val path = externalCacheDirs[0].parentFile!!.path + File.separator + "log" + File.separator +
//            SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(now)
        //todo
        val path =""
        val destDir = File(path)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        val file = File(path, "Log_$date.txt")
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            val filerWriter = FileWriter(file, true)
            val bufWriter = BufferedWriter(filerWriter)
            bufWriter.write(text)
            bufWriter.newLine()
            bufWriter.close()
            filerWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }
}