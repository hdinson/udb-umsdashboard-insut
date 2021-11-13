package com.intretech.app.umsdashboard_new.http

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class LoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response? {

        Log.i("TAG", "\t│ Request{method= ${chain.request().method()}, url=${chain.request().url()}}")

        val response = chain.proceed(chain.request())

        val mediaType = response.body()!!.contentType()
        val content = response.body()!!.string()
        if (content.isNotEmpty()) Log.i("TAG",  "\t│ ${content.replace("\n", "")}" )

        return response.newBuilder()
            .body(okhttp3.ResponseBody.create(mediaType, content)).build()
    }

}


