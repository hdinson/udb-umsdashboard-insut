package com.intretech.app.umsdashboard_new.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.intretech.app.umsdashboard_new.R
import org.xwalk.core.XWalkPreferences
import org.xwalk.core.XWalkUIClient
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

            setBackgroundColor(ContextCompat.getColor(this@XWalkWebViewActivity,R.color.baseBackground))

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
                useWideViewPort = false                         //一定要false，否则在一些看板上自适应不好会变形
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

            setUIClient(object :XWalkUIClient(this){
                override fun onPageLoadStarted(view: XWalkView?, url: String?) {
                    super.onPageLoadStarted(view, url)
                    Log.w("TAG", "--------开始加载界面：$url")
                    setCenterLayout(true )
                }

                override fun onPageLoadStopped(view: XWalkView?, url: String?, status: LoadStatus?) {
                    super.onPageLoadStopped(view, url, status)
                    Log.w("TAG", "-----------结束加载：$url")
                    setCenterLayout(false )
                }
            })
            requestFocus()
        }
        currentUrlReload()//刷新一下界面
    }

    override fun onDestroy() {
        super.onDestroy()
        mWebView?.onDestroy()
    }
}