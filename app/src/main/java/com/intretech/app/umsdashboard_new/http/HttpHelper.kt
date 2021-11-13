package com.intretech.app.umsdashboard_new.http

import com.intretech.app.umsdashboard_new.utils.MMKVUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit统一封装设置,返回retrofit对象
 */
object HttpHelper {

    private const val DEFAULT_TIMEOUT = 10  //超时时间

    private var mOkHttpClient = OkHttpClient.Builder()
        .readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(RetrofitRetry(5))
        .addInterceptor(LoggingInterceptor())
        .addInterceptor(RetryAndChangeIpInterceptor.Builder().retryCount(Int.MAX_VALUE).retryInterval(2000).build())
        .build()
    private var mRetrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .baseUrl(MMKVUtils.getBaseUrl())
        .client(mOkHttpClient)
        .build()

    fun <T> create(tc: Class<T>): T {
        return mRetrofit.create(tc)
    }

    fun updateBaseUrl(baseUrl: String) {
        mRetrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(baseUrl)
            .client(mOkHttpClient)
            .build()
    }
}
