package com.intretech.app.umsdashboard_new.http

import com.intretech.app.umsdashboard_new.bean.LogMessage
import com.intretech.app.umsdashboard_new.utils.logi
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import java.io.IOException

class RetryAndChangeIpInterceptor(private val maxRetryCount: Int, private val retryInterval: Long, private val baseUrl: String?, private val serviceList: ArrayList<String>?) : Interceptor {

    private var times = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // try the request
        var response = doRequest(chain, request)
        var tryCount = 0
        var url = request.url().toString()
        while (response == null && tryCount <= maxRetryCount) {
            url = switchServer(url)
            val newRequest = request.newBuilder().url(url).build()
            logi { "Request is not successful - $tryCount" }
            EventBus.getDefault().post(LogMessage("接口请求不成功，正在重试 - ${tryCount + 1} 次"))
            tryCount++
            // retry the request
            try {
                Thread.sleep(retryInterval)
                times++
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            response = doRequest(chain, newRequest)
        }
        if (response != null && tryCount != 0) {
            response = response.newBuilder().addHeader("isRetry", "true").build()
        }
        if (response == null) {
            throw IOException()
        }
        if (tryCount == 0) {
            EventBus.getDefault().post(LogMessage())
        }
        return response
    }

    private fun switchServer(url: String): String {
        var newUrlString = url
        if (baseUrl == null || serviceList == null) return url
        if (url.contains(baseUrl)) {
            for (server in serviceList) {
                if (baseUrl != server) {
                    newUrlString = url.replace(baseUrl, server)
                    break
                }
            }
        } else {
            for (server in serviceList) {
                if (url.contains(server)) {
                    newUrlString = url.replace(server, baseUrl)
                    break
                }
            }
        }
        return newUrlString
    }

    private fun doRequest(chain: Interceptor.Chain, request: Request): Response? {
        var response: Response? = null
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
        }
        return response
    }


    class Builder {

        private var mRetryCount = -1
        private var mRetryInterval = 1000L
        private var mBaseUrl: String? = null
        private var mServiceHost: ArrayList<String>? = null

        fun retryCount(retryCount: Int): Builder {
            this.mRetryCount = retryCount
            return this
        }

        fun retryInterval(retryInterval: Long): Builder {
            this.mRetryInterval = retryInterval
            return this
        }

        fun serviceList(serviceList: ArrayList<String>): Builder {
            this.mServiceHost = serviceList
            return this
        }

        fun setBaseUrl(baseUrl: String): Builder {
            this.mBaseUrl = baseUrl
            return this
        }

        fun build(): RetryAndChangeIpInterceptor {
            return RetryAndChangeIpInterceptor(mRetryCount, mRetryInterval, mBaseUrl, mServiceHost)
        }
    }
}