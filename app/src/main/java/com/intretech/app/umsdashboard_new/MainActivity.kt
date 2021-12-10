package com.intretech.app.umsdashboard_new

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.intretech.app.umsdashboard_new.api.ServiceApi
import com.intretech.app.umsdashboard_new.bean.ApkUpdateInfoKt
import com.intretech.app.umsdashboard_new.bean.BoardInfoKt
import com.intretech.app.umsdashboard_new.bean.LogMessage
import com.intretech.app.umsdashboard_new.http.HttpHelper
import com.intretech.app.umsdashboard_new.utils.MMKVUtils
import com.intretech.app.umsdashboard_new.utils.MainErrorLayoutController
import com.intretech.app.umsdashboard_new.widget.DownloadApkProgressDialog
import com.just.agentweb.AgentWeb
import com.just.agentweb.AgentWebConfig
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import com.tencent.mmkv.MMKV
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.yirong.library.annotation.NetType
import com.yirong.library.annotation.NetworkListener
import com.yirong.library.manager.NetworkManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt


class MainActivity : RxAppCompatActivity() {

    private var mHttpDisposable: Disposable? = null
    private var mPollingDisposable: Disposable? = null
    private var mHomePage = ""
    private val mAgentWeb by lazy {
        AgentWeb.with(this)
            .setAgentWebParent(mainWebView, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator(ActivityCompat.getColor(this, R.color.gray), 4)
            .setAgentWebUIController(MainErrorLayoutController())
            .setWebViewClient(MainWebViewClient())
            .setWebChromeClient(MainChromeWebViewClient())
            .createAgentWeb()
            .get()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MMKV.initialize(this)
        initWebView()

        if (BuildConfig.POLLING_HOME_PAGE_URL > 0) {
            refreshUIByTimer()
        } else {
            loadBaseUrl()
        }
        NetworkManager.getDefault().init(application)
        NetworkManager.getDefault().registerObserver(this)
        checkUpdateApk()
    }

    /**
     * 检查更新
     */
    private fun checkUpdateApk() {

        HttpHelper.create(ServiceApi::class.java).checkAppVersion()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.minVersion > BuildConfig.VERSION_CODE) {
                    showUpdateApkDialog(true, it) //强更新
                } else if (it.appVersionNum > BuildConfig.VERSION_CODE) {
                    showUpdateApkDialog(apkUpdateInfo = it)// 通过versionName更新app名称以及下载地址
                }
            }, {
                it.message?.apply { Toast.makeText(this@MainActivity, this, Toast.LENGTH_SHORT).show() }
            })
    }

    //网络监听
    @NetworkListener(type = NetType.WIFI)
    fun netork(@NetType type: String) {
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

    /**
     * 初始化WebView
     * */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun initWebView() {
        if (mAgentWeb == null) return
        mAgentWeb.webCreator.webView.apply {
            overScrollMode = View.SCROLLBARS_INSIDE_OVERLAY

            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            manager.defaultDisplay.getMetrics(outMetrics)
            val screenWidth = outMetrics.widthPixels
            val screenHeight = outMetrics.heightPixels
            val scaleRate = sqrt((screenWidth * screenWidth + screenHeight * screenHeight).toDouble()).toFloat() / sqrt((1920 * 1920 + 1080 * 1080).toDouble()).toFloat() // 使用对角线比
            val scaleNumber = (scaleRate * 100).toInt()
            setInitialScale(scaleNumber)
            setBackgroundColor(Color.TRANSPARENT)
            setBackgroundResource(R.mipmap.img_ukanban)
            settings.apply {
                javaScriptEnabled = true //启用js
                javaScriptCanOpenWindowsAutomatically = true//支持通过JS打开新窗口
                domStorageEnabled = true //保存数据 
                blockNetworkImage = false //解决图片不显示 
                loadsImagesAutomatically = true //支持自动加载图片
                loadWithOverviewMode = true     //当页面宽度大于WebView宽度时，缩小使页面宽度等于WebView宽度
                layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            }
        }
        mAgentWeb.webCreator.webParentLayout.apply {
            setBackgroundColor(Color.TRANSPARENT)
            setBackgroundResource(R.mipmap.img_ukanban)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LogMessage?) {
        tvLog.text = event?.msg ?: ""
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        mAgentWeb?.webLifeCycle?.onPause()
        super.onPause()
    }

    override fun onResume() {
        mAgentWeb?.webLifeCycle?.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        mAgentWeb?.webLifeCycle?.onDestroy()
        //mCompositeDisposable.clear()
        NetworkManager.getDefault().unRegisterAllObserver()
        super.onDestroy()
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
            AgentWebConfig.clearDiskCache(this)
            finish()
        }
        builder.setNeutralButton("否") { dialog, _ -> dialog.dismiss() }
        builder.setNegativeButton("设置参数") { _, _ ->
            SettingActivity.start(this, mHomePage)
        }
        builder.show()
    }

    inner class MainChromeWebViewClient : WebChromeClient() {
        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            result?.cancel()
            return true
        }
    }

    inner class MainWebViewClient : WebViewClient() {

        val LONGTIMEOUT = 15000L //超时时间


        val mHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                when (msg?.what) {
                    0x01 -> {
                        homePageReload()
                        EventBus.getDefault().post(LogMessage("Reloading page.."))
                    }
                }
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            Log.i("TAG", "开始加载界面：$url")
            super.onPageStarted(view, url, favicon)
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
            Log.i("TAG", "正在加载：$url")
        }

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            Log.e("TAG", "加载失败2: -- url:${failingUrl}, description:${description}")
            homePageReload()
        }


        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.e(
                    "TAG",
                    "加载失败: -- url:${request?.url}, isForMainFrame:${request?.isForMainFrame}"
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (request?.isForMainFrame == false) return
                    EventBus.getDefault().post(LogMessage(error?.description.toString()))
                    mHandler.sendEmptyMessageDelayed(0x01, 2000)
                }
            } else {
                homePageReload()
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.i("TAG", "${mAgentWeb.webCreator.webView.progress} url:${url} --- 加载进度")
            if (mAgentWeb.webCreator.webView.progress != 100) return
            EventBus.getDefault().post(LogMessage())
        }


        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            super.onReceivedHttpError(view, request, errorResponse)
            Log.i("TAG", "onReceivedHttpError：${request}")
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            loadBaseUrl()
        } else if (BuildConfig.POLLING_HOME_PAGE_URL <= 0) {
            loadBaseUrl()
        }
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
            .compose(bindToLifecycle())
            .subscribe({
                if (!it.isSuccessful) return@subscribe
                val bean = it.body() ?: return@subscribe
                homePageReload(bean)
            }, {
                it.printStackTrace()
            })
    }

    private fun refreshUIByTimer() {
        mPollingDisposable?.dispose()
        mPollingDisposable =
            Observable.interval(0L, BuildConfig.POLLING_HOME_PAGE_URL, TimeUnit.SECONDS)
                .flatMap { mApi.getHomePage(MMKVUtils.getMacAddrWithoutDot()) }
                .compose(bindToLifecycle())
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

    private fun homePageReload(info: BoardInfoKt? = null) {
        info?.apply { if (!boardHomePage.isNullOrEmpty()) mHomePage = boardHomePage }
        Log.e("TAG", "LoadBaseUrl: $mHomePage")
        mAgentWeb.clearWebCache()
        mAgentWeb.webCreator.webView.reload()
        mAgentWeb.urlLoader.stopLoading()
        mAgentWeb.urlLoader.loadUrl(mHomePage)
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
                    mAgentWeb.webCreator.webView.loadUrl("about:blank")
                    AgentWebConfig.clearDiskCache(this)
                    this.finish()
                    return@OnKeyListener true
                }
                false
            })
            dialog.setCanceledOnTouchOutside(false)
        }
        dialog.show()
    }


}