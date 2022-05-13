package com.intretech.app.umsdashboard_new.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
import com.intretech.app.umsdashboard_new.R
import com.intretech.app.umsdashboard_new.bean.LogMessage
import org.greenrobot.eventbus.EventBus
import kotlin.math.sqrt

class WebkitSystemActivity : BaseWebViewActivity() {

    private var mWebView: WebView? = null

    /**
     * 初始化WebView
     * */
    @SuppressLint("SetJavaScriptEnabled")
    override fun initWebView(flWebViewContainer: FrameLayout) {
        if (mWebView == null) {
            mWebView = WebView(this)
            flWebViewContainer.removeAllViews()
            flWebViewContainer.addView(mWebView)
        }
        mWebView?.apply {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    //才可以点击之后正常播放音频
                    mediaPlaybackRequiresUserGesture = false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
            }
            webChromeClient = MainChromeWebViewClient()
            webViewClient = MainWebViewClient()
        }

        /* mWebView.webParentLayout.apply {
             setBackgroundColor(Color.TRANSPARENT)
             setBackgroundResource(R.mipmap.img_ukanban)
         }*/
    }

    override fun onWebViewLoadUrl(url: String?) {
        mWebView?.stopLoading()
        mWebView?.loadUrl(url)
    }

    inner class MainChromeWebViewClient : WebChromeClient() {

        private var callback: CustomViewCallback?=null

        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            Toast.makeText(this@WebkitSystemActivity, message, Toast.LENGTH_SHORT).show()
            result?.cancel()
            return true
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            super.onShowCustomView(view, callback)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            mWebView?.addView(view)
            this.callback = callback
        }

        override fun onHideCustomView() {
            super.onHideCustomView()
            callback?.onCustomViewHidden()
        }
    }

    inner class MainWebViewClient : WebViewClient() {

        val LONGTIMEOUT = 15000L //超时时间


        val mHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                when (msg?.what) {
                    0x01 -> {
                        currentUrlReload()
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
            currentUrlReload()
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
                    mHandler.sendEmptyMessageDelayed(0x01, LONGTIMEOUT)
                }
            } else {
                currentUrlReload()
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.i("TAG", "${mWebView?.progress} url:${url} --- 加载进度")
            if (mWebView?.progress != 100) return
            EventBus.getDefault().post(LogMessage())
        }


        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            super.onReceivedHttpError(view, request, errorResponse)
            Log.i("TAG", "onReceivedHttpError：${request}")
        }

    }
}