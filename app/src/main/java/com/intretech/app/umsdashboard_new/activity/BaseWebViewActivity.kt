package com.intretech.app.umsdashboard_new.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.intretech.app.umsdashboard_new.BuildConfig
import com.intretech.app.umsdashboard_new.R
import com.intretech.app.umsdashboard_new.api.ServiceApi
import com.intretech.app.umsdashboard_new.bean.ApkUpdateInfoKt
import com.intretech.app.umsdashboard_new.bean.BoardInfoKt
import com.intretech.app.umsdashboard_new.bean.LogMessage
import com.intretech.app.umsdashboard_new.http.HttpHelper
import com.intretech.app.umsdashboard_new.utils.AtyContainer
import com.intretech.app.umsdashboard_new.utils.MMKVUtils
import com.intretech.app.umsdashboard_new.widget.DownloadApkProgressDialog
import com.yirong.library.annotation.NetType
import com.yirong.library.annotation.NetworkListener
import com.yirong.library.manager.NetworkManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_base_webview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xwalk.core.XWalkActivity
import java.util.concurrent.TimeUnit

abstract class BaseWebViewActivity : XWalkActivity() {

    private var mHttpDisposable: Disposable? = null
    private var mPollingDisposable: Disposable? = null
    private var mHomePage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        AtyContainer.addActivity(this)

        setContentView(R.layout.activity_base_webview)
        EventBus.getDefault().register(this)

        initWebView(flWebViewContainer)
        if (BuildConfig.POLLING_HOME_PAGE_URL > 0) {
            refreshUIByTimer()
        } else {
            loadBaseUrl()
        }

        /*NetworkManager.getDefault().init(application)
        NetworkManager.getDefault().registerObserver(this)*/
        checkUpdateApk()
    }

    /**
     * 重新加载网页地址
     */
    private val mApi by lazy { HttpHelper.create(ServiceApi::class.java) }
    private fun loadBaseUrl() {
        mHttpDisposable?.dispose()
        mHttpDisposable = mApi.getHomePage(MMKVUtils.getMacAddrWithoutDot())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (!it.isSuccessful) return@subscribe
                val bean = it.body() ?: return@subscribe
                homePageReload(bean)
            }, {
                it.printStackTrace()
            })
    }

    /**
     * 轮训获取首页接口
     */
    private fun refreshUIByTimer() {
        mPollingDisposable?.dispose()
        mPollingDisposable = null
        mPollingDisposable =
            Observable.interval(0L, BuildConfig.POLLING_HOME_PAGE_URL, TimeUnit.SECONDS)
                .flatMap { mApi.getHomePage(MMKVUtils.getMacAddrWithoutDot()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!it.isSuccessful) return@subscribe
                    val bean = it.body() ?: return@subscribe
                    if (bean.boardHomePage.isNullOrEmpty().not()) {
                        val isRetry = it.headers()["isRetry"]
                        if (isRetry.isNullOrEmpty()) {
                            //非重试
                            if (mHomePage != bean.boardHomePage) {
                                //未加载网页，或者服务端更新网页地址，重新加载
                                Log.e("TAG", "未加载网页，或者服务端更新网页地址，重新加载")
                                homePageReload(bean)
                            }
                        } else {
                            homePageReload(bean)
                        }
                    }
                }, {
                    it.printStackTrace()
                })
    }

    /**
     * 检查更新
     */
    private fun checkUpdateApk() {
        val a = HttpHelper.create(ServiceApi::class.java).checkAppVersion()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.minVersion > BuildConfig.VERSION_CODE) {
                    showUpdateApkDialog(true, it) //强更新
                } else if (it.appVersionNum > BuildConfig.VERSION_CODE) {
                    showUpdateApkDialog(apkUpdateInfo = it)// 通过versionName更新app名称以及下载地址
                }
            }, {
                it.message?.apply { Toast.makeText(this@BaseWebViewActivity, this, Toast.LENGTH_SHORT).show() }
            })
    }

    /**
     * 显示更新apk对话框
     */
    private fun showUpdateApkDialog(force: Boolean = false, apkUpdateInfo: ApkUpdateInfoKt) {
        if (apkUpdateInfo.appPath.isEmpty()) return
        val dialog = AlertDialog.Builder(this).setTitle("软件更新").setMessage(
            """
          版本名称：v${apkUpdateInfo.appVersionName}
          
          更新日志：
          ${apkUpdateInfo.appRemark}
          
          发现新版本，是否立即更新？
          """
        ).setNegativeButton("是") { dialog, _ ->
            dialog.dismiss()
            DownloadApkProgressDialog(this, apkUpdateInfo.appPath, force.not()).show()
        }.create()
        if (force) {
            dialog.setOnKeyListener(DialogInterface.OnKeyListener { dia, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dia.dismiss()
                    AtyContainer.finishAllActivity()
                    return@OnKeyListener true
                }
                false
            })
            dialog.setCanceledOnTouchOutside(false)
        }
        dialog.show()
    }

    //网络监听
    @NetworkListener(type = NetType.WIFI)
    fun network(@NetType type: String) {
        when (type) {
            NetType.AUTO,
            NetType.CMNET,
            NetType.CMWAP,
            NetType.WIFI -> {
                homePageReload()
                EventBus.getDefault().post(LogMessage("网络已连接, 正在刷新"))
            }
            NetType.NONE -> {
                EventBus.getDefault().post(LogMessage("网络已断开"))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LogMessage) {
        setMessageCardTvText(tvLogCard, event.msg)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        NetworkManager.getDefault().unRegisterAllObserver()
        AtyContainer.removeActivity(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            showSetDialog()
            return false
        }
        return true
    }

    // 点击返回键提示退出
    private fun showSetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("提示")
        builder.setMessage("您是否要退出该软件？")
        builder.setPositiveButton("是") { dialog, _ ->
            dialog.dismiss()
            AtyContainer.finishAllActivity()
        }
        builder.setNeutralButton("否") { dialog, _ -> dialog.dismiss() }
        builder.setNegativeButton("设置参数") { _, _ ->
            SettingActivity.start(this, mHomePage)
        }
        builder.show()
    }

    override fun onXWalkReady() {

    }


    /**
     * 界面刷新，加载url，当看板列表为空或者服务器数据异常是，url为null
     * @param url url
     */
    abstract fun onWebViewLoadUrl(url: String?)

    abstract fun initWebView(flWebViewContainer: FrameLayout)


    /**
     * 当前界面刷新加载
     */
    protected fun currentUrlReload() {
        onWebViewLoadUrl(mHomePage)
    }


    private fun homePageReload(info: BoardInfoKt? = null) {
        info?.apply { if (!boardHomePage.isNullOrEmpty()) mHomePage = boardHomePage }
        Log.e("TAG", "LoadBaseUrl: $mHomePage")
        currentUrlReload()
    }


    /**
     * 设置卡片信息
     */
    private fun setMessageCardTvText(tv: TextView, text: String? = null) {
        if (text.isNullOrEmpty()) {
            tv.text = text ?: ""
            tv.visibility = View.GONE
        } else {
            tv.visibility = View.VISIBLE
            tv.text = text
        }
    }
}