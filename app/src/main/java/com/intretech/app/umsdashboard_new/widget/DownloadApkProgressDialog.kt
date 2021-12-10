package com.intretech.app.umsdashboard_new.widget

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat.getExternalFilesDirs
import androidx.core.content.FileProvider
import com.intretech.app.umsdashboard_new.R
import com.intretech.app.umsdashboard_new.bean.DownloadInfo
import com.intretech.app.umsdashboard_new.download.DownloadManager
import com.intretech.app.umsdashboard_new.download.listener.HttpDownOnNextListener
import com.intretech.app.umsdashboard_new.download.model.DownloadState
import com.intretech.app.umsdashboard_new.download.utils.DbDownUtil
import kotlinx.android.synthetic.main.app_progress_download.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DownloadApkProgressDialog @JvmOverloads constructor(context: Context, val url: String,  theme: Int = R.style.AppDialog) :
    AlertDialog(context, theme) {
    private var mCancelable = true

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
        downloadApk()
    }

    /**
     * 下载文件
     */
    private fun downloadApk() {
        val info = DownloadInfo()
        val localPath = getExternalFilesDirs(context, "apk")[0].path + File.separator
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())
        info.savePath = localPath + currentDateTime + url.split("/").last()
        info.url = url
        val downloadInfo = DbDownUtil.getInstance().queryDownBy(info.url)
        if (downloadInfo == null || downloadInfo.state != DownloadState.FINISH) {
            info.state = DownloadState.START
            info.listener = object : HttpDownOnNextListener<DownloadInfo>() {
                override fun onComplete() {
                    installApkByGuide(info.savePath)
                }

                override fun updateProgress(readLength: Long, countLength: Long) {
                    val poi = (readLength / countLength) * 100
                    uvProgressBar.progress = poi.toInt()
                }
            }
            DownloadManager.getInstance().startDown(info)
        } else {
            installApkByGuide(downloadInfo.savePath)
        }
    }

    //安装apk
    private fun installApkByGuide(localFilePath: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri: Uri?
        if (Build.VERSION.SDK_INT >= 24) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(localFilePath))
        } else {
            uri = Uri.fromFile(File(localFilePath))
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

}