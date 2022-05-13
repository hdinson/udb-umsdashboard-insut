package com.intretech.app.umsdashboard_new.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.intretech.app.umsdashboard_new.R
import org.xwalk.core.XWalkPreferences
import org.xwalk.core.XWalkView
import kotlin.math.sqrt

class XWalkWebViewActivity : BaseWebViewActivity() {

    private var mWebView: XWalkView? = null


    override fun onWebViewLoadUrl(url: String?) {
         if (isXWalkReady) {
            mWebView?.loadUrl(url)
        }
    }

    override fun initWebView(flWebViewContainer: FrameLayout) {
        mWebView = XWalkView(this)
        flWebViewContainer.addView(mWebView)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onXWalkReady() {
        super.onXWalkReady()
        //开启web调试模式
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true)
        mWebView?.apply {

            overScrollMode = View.SCROLLBARS_INSIDE_OVERLAY

            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            setBackgroundResource(R.mipmap.img_ukanban)

            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            manager.defaultDisplay.getMetrics(outMetrics)
            val screenWidth = outMetrics.widthPixels
            val screenHeight = outMetrics.heightPixels
            val scaleRate = sqrt((screenWidth * screenWidth + screenHeight * screenHeight).toDouble()).toFloat() / sqrt((1920 * 1920 + 1080 * 1080).toDouble()).toFloat() // 使用对角线比
            val scaleNumber = (scaleRate * 100).toInt()
            setInitialScale(scaleNumber)

            settings.apply {
                loadWithOverviewMode = false
                javaScriptEnabled = true                        //支持js
                javaScriptCanOpenWindowsAutomatically = true    //支持通过JS打开新窗口
                useWideViewPort = true                          //将图片调整到合适webview的大小
                loadWithOverviewMode = true                     //缩放至屏幕的大小
                loadsImagesAutomatically = true                 //支持自动加载图片
                supportMultipleWindows()                        //支持多窗口
                setSupportZoom(true)
                allowFileAccess = true
                domStorageEnabled = true
                allowContentAccess = true
                allowContentAccess = true
                domStorageEnabled = true
            }
            requestFocus()
        }
        currentUrlReload()//刷新一下界面
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mWebView?.onNewIntent(intent)
    }
}