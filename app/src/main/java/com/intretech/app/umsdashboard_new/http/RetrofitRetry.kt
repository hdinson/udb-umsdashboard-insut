package com.intretech.app.umsdashboard_new.http

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response


/**
 * 自定义的，重试N次的拦截器
 * 通过：addInterceptor 设置
 */
class RetrofitRetry(var maxRetry: Int? = null) : Interceptor {

    private var retryNum = 0 //假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response = chain.proceed(request)
        Log.i("Retry", "num:$retryNum")
        while (!response.isSuccessful && (maxRetry == null || retryNum < maxRetry!!)) {
            retryNum++
            Log.i("Retry", "num:$retryNum")
            response = chain.proceed(request)
        }
        return response
    }

}