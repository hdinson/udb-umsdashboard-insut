package com.intretech.app.umsdashboard_new

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.intretech.app.umsdashboard_new.api.ServiceApi
import com.intretech.app.umsdashboard_new.bean.BoardInfoKt
import com.intretech.app.umsdashboard_new.http.HttpHelper
import com.intretech.app.umsdashboard_new.utils.MMKVUtils
import com.intretech.app.umsdashboard_new.utils.MainErrorLayoutController
import com.just.agentweb.AgentWeb
import com.just.agentweb.AgentWebConfig
import com.just.agentweb.WebViewClient
import com.tencent.mmkv.MMKV
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private val mCompositeDisposable = CompositeDisposable()
    private var mHomePage = ""
    private val mAgentWeb by lazy {
        AgentWeb.with(this)
            .setAgentWebParent(mainWebView, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator(ActivityCompat.getColor(this, R.color.gray), 4)
            .setAgentWebUIController(MainErrorLayoutController())
            .setWebViewClient(MainWebViewClient())
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
    }

    private fun formatUrl(url: String): String {
        if (url.isEmpty()) return ""
        var newUrl = url
        if (url.startsWith("http").not()) {
            newUrl = "http://$url"
        }
        val qrCode = if (MMKVUtils.isShowHomePageQrCode()) "&isQRCode=1" else ""
        return "${newUrl}&IsNewApp=1$qrCode"
    }


    /**
     * 初始化WebView
     * */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun initWebView() {
        if (mAgentWeb == null) return
        mAgentWeb.webCreator.webView.apply {
            overScrollMode = View.SCROLLBARS_INSIDE_OVERLAY

            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            manager.defaultDisplay.getMetrics(outMetrics)

            val screenWidth = outMetrics.widthPixels
            val screenHeight = outMetrics.heightPixels
            val scaleRate =
                sqrt((screenWidth * screenWidth + screenHeight * screenHeight).toDouble()).toFloat() / sqrt(
                    (1920 * 1920 + 1080 * 1080).toDouble()
                ).toFloat() // 使用对角线比
            val scaleNumber = (scaleRate * 100).toInt()
            setInitialScale(scaleNumber)
            setBackgroundColor(Color.TRANSPARENT)
            setBackgroundResource(R.mipmap.img_ukanban)
        }
        mAgentWeb.webCreator.webParentLayout.apply {
            setBackgroundColor(Color.TRANSPARENT)
            setBackgroundResource(R.mipmap.img_ukanban)
        }
        mAgentWeb.agentWebSettings.webSettings.apply {
            loadWithOverviewMode = true     // 当页面宽度大于WebView宽度时，缩小使页面宽度等于WebView宽度
        }
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
        mCompositeDisposable.clear()
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
            SettingActivity.start(this,mHomePage)
        }
        builder.show()
    }

    inner class MainWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.i("TAG", "开始加载界面：$url")
        }


        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.e("TAG", "加载失败: -- url:${request?.url}, isForMainFrame:${request?.isForMainFrame}" )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.e("TAG", "加载失败: -- description: ${error?.description}, errorCode:${error?.errorCode}" )
                }
            }
            if (mLoadErrorDialog.isShowing) return
            mLoadErrorDialog.show()
            val down = countdown(10).subscribe {
                mLoadErrorDialog.setMessage("您是否要使用外部浏览器打开？ ${it}秒后重试")
                if (it <= 0) {
                    mLoadErrorDialog.dismiss()
                    loadBaseUrl()
                }
            }
            mCompositeDisposable.add(down)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.i("TAG", "${mAgentWeb.webCreator.webView.progress} url:${url} --- 加载进度")
            if (mAgentWeb.webCreator.webView.progress != 100) return
            // if (url != "about:blank") iv.hide(true)
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
            Log.i("TAG", "正在加载：$url")
        }


        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            super.onReceivedHttpError(view, request, errorResponse)
            Log.i("TAG", "onReceivedHttpError：${request}")
        }

        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
            return super.onRenderProcessGone(view, detail)
        }
    }

    private val mLoadErrorDialog by lazy {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("加载失败")
        builder.setMessage("您是否要使用外部浏览器打开？")
        builder.setPositiveButton("是") { dialog, _ ->
            try {
                val formatHomePageUrl = formatUrl(mHomePage)
                val uri = Uri.parse(formatHomePageUrl)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "当前设备未安装外部浏览器", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        builder.setNegativeButton("重试") { dialog, _ ->
            dialog.dismiss()
            loadBaseUrl()
        }
        builder.setCancelable(false)
        builder.create()
    }

    /**
     * 倒计时
     * @param time 从第几秒开始倒计时
     */
    fun countdown(time: Int): Observable<Int> {
        var countTime = time
        if (countTime < 0) {
            countTime = 0
        }
        return Observable.interval(0, 1, TimeUnit.SECONDS)
            .map { countTime - it.toInt() }
            .take((countTime + 1).toLong())
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loadBaseUrl()
    }

    /**
     * 重新加载网页地址
     */
    private val mApi by lazy { HttpHelper.create(ServiceApi::class.java) }
    private fun loadBaseUrl() {
        mApi.getHomePage(MMKVUtils.getMacAddrWithoutDot())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setHttpResult(it)
            }, {
                it.printStackTrace()
            })
    }

    private fun refreshUIByTimer() {
        Observable.interval(0L, BuildConfig.POLLING_HOME_PAGE_URL, TimeUnit.SECONDS)
            .flatMap { mApi.getHomePage(MMKVUtils.getMacAddrWithoutDot()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.boardHomePage.isNotEmpty()) {
                    val newUrl = formatUrl(it.boardHomePage)
                    if (mHomePage != newUrl) {
                        //未加载网页，或者服务端更新网页地址，重新加载
                        Log.e("TAG", "未加载网页，或者服务端更新网页地址，重新加载")
                        setHttpResult(it)
                    }
                }
            }, {
                it.printStackTrace()
            })
    }

    private fun setHttpResult(info: BoardInfoKt) {
        Log.e("TAG", "loadBaseUrl before: $info")
        mHomePage = formatUrl(info.boardHomePage)
        Log.e("TAG", "loadBaseUrl format: ${info.boardHomePage}")
        mAgentWeb.urlLoader.stopLoading()
        mAgentWeb.urlLoader.loadUrl(mHomePage)
    }
}