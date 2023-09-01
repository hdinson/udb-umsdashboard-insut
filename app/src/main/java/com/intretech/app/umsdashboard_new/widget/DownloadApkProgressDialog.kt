package com.intretech.app.umsdashboard_new.widget

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.FileProvider
import com.intretech.app.umsdashboard_new.R
import com.intretech.app.umsdashboard_new.download.RxNet
import com.intretech.app.umsdashboard_new.download.callback.DownloadCallback
import com.intretech.app.umsdashboard_new.utils.loge
import com.intretech.app.umsdashboard_new.utils.logw
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.app_progress_download.*
import java.io.File

class DownloadApkProgressDialog @JvmOverloads constructor(context: Context, val url: String, theme: Int = R.style.AppDialog) :
    AlertDialog(context, theme) {
    private var mCancelable = true
    private var mLocaleFilePath: String? = null
    /*var mTimer: Timer? = null
    var mId: Long = 0
    var mTask: TimerTask? = null

    val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val bundle = msg.data
            val pro = bundle.getInt("pro")
            val name = bundle.getString("name")
            pb_update.progress = pro
            progress.text = "$pro%"
        }
    }*/

    constructor(context: Context, url: String, cancelable: Boolean) : this(context, url, R.style.AppDialog) {
        mCancelable = cancelable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(mCancelable)
        setCanceledOnTouchOutside(mCancelable)
        val inflate = layoutInflater.inflate(R.layout.app_progress_download, null, false)
        setContentView(inflate)
        window?.apply {
            val params = this.attributes
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.attributes = params
        }
        tv_cancel_install.visibility = if (mCancelable) View.VISIBLE else View.INVISIBLE
        tv_install_now.setOnClickListener {
            installApkByGuide()
        }
        tv_cancel_install.setOnClickListener {
            this.dismiss()
        }
        downloadApk()
    }

    /**
     * 下载文件
     */
    private fun downloadApk() {
        if (context.externalCacheDir == null) {
            loge { "下载失败，externalCacheDir==null" }
            return
        }

        val fileName = ".${url.split(".").last()}"
        val path = context.externalCacheDir!!.path + File.separator + System.currentTimeMillis() + fileName
        RxNet.download(url, path, object : DownloadCallback {
            override fun onStart(d: Disposable?) {
                loge { "onStart: 开始下载" }
            }

            override fun onProgress(totalByte: Long, currentByte: Long, progress: Int) {
                logw { "totalByte:$totalByte, currentByte:$currentByte, progress:$progress" }

                val pro = currentByte * 100 / totalByte
                pb_update.progress = progress
                tv_progress.text = "${progress}%"
            }

            override fun onFinish(file: File?) {
                logw { "onFinish " + file?.absolutePath }
                ll_install.visibility = View.VISIBLE
                tv_install_now.requestFocus()
                mLocaleFilePath = file?.absolutePath
                installApkByGuide()
            }

            override fun onError(msg: String?) {
                loge { "onError $msg" }
                this@DownloadApkProgressDialog.dismiss()
            }
        })

        /*val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir()
        val request = DownloadManager.Request(Uri.parse(url))
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "app-release.apk"
        );
        val id: Long = downloadManager.enqueue(request)
        val query = DownloadManager.Query()
        mTimer = Timer()
        mTask = timerTask {
            val cursor = downloadManager.query(query.setFilterById(id))
            if (cursor != null && cursor.moveToFirst()) {
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    installApkByGuide("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/app-release.apk")
                    mTask?.cancel()
                }
                val title = cursor . getString (cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                val address = cursor . getString (cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                val bytes_downloaded = cursor . getInt (cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytes_total = cursor . getInt (cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val pro =(bytes_downloaded * 100) / bytes_total
                val msg = Message . obtain ()
                val bundle =   Bundle()
                bundle.putInt("pro", pro)
                bundle.putString("name", title)
                msg.data = bundle
                mHandler.sendMessage(msg)
            }
            cursor.close()

        }
        mTimer?.schedule(mTask, 0, 1000)*/
    }

    //安装apk
    private fun installApkByGuide() {
        if (mLocaleFilePath.isNullOrEmpty()) {
            loge { "本地安装地址不能为空" }
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri: Uri?
        if (Build.VERSION.SDK_INT >= 24) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileProvider",
                File(mLocaleFilePath)
            )
        } else {
            uri = Uri.fromFile(File(mLocaleFilePath))
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

}