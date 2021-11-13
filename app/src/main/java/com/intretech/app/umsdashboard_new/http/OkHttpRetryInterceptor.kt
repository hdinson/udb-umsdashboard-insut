package com.intretech.app.umsdashboard_new.http

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class OkHttpRetryInterceptor(private val maxRetryCount: Int, private val retryInterval: Long) : Interceptor {

    private var times = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = doRequest(chain, request)
        var retryNum = 1
        Log.e("TAG", "OkHttpRetryInterceptor: retryNum: $retryNum" )
        while (((response == null) || response.isSuccessful) && retryNum <= maxRetryCount) {
            try {
                Thread.sleep(retryInterval)
                times++
                Log.e("TAG", "Thread.sleep : $times" )
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            retryNum++
        }
        return response
    }


    private fun doRequest(chain: Interceptor.Chain, request: Request): Response  {

            return chain.proceed(request)

    }

        class Builder {
        private var mRetryCount = 1
        private var mRetryInterval = 1000L

        fun retryCount(retryCount: Int): Builder {
            this.mRetryCount = retryCount
            return this
        }

        fun retryInterval(retryInterval: Long): Builder {
            this.mRetryInterval = retryInterval
            return this
        }

        fun build(): OkHttpRetryInterceptor {
            return OkHttpRetryInterceptor(mRetryCount, mRetryInterval)
        }
    }
}

