package com.intretech.app.umsdashboard_new.download.core;

import android.text.TextUtils;


import com.intretech.app.umsdashboard_new.download.callback.DownloadListener;
import com.intretech.app.umsdashboard_new.download.utils.LogUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RetrofitFactory {
    private static final int TIME_OUT_SECNOD = 15;
    private static OkHttpClient.Builder mBuilder;

    private static Retrofit getDownloadRetrofit(DownloadListener downloadListener) {
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder requestBuilder = originalRequest.newBuilder()
                        .addHeader("Accept-Encoding", "gzip")
                        .method(originalRequest.method(), originalRequest.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };

        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(  String message) {
                if (!TextUtils.isEmpty(message)) {
                    LogUtils.d(message);
                }
            }
        });
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        if (null == mBuilder) {
            mBuilder = new OkHttpClient.Builder()
                    .connectTimeout(TIME_OUT_SECNOD, TimeUnit.SECONDS)
                    .readTimeout(TIME_OUT_SECNOD, TimeUnit.SECONDS)
                    .writeTimeout(TIME_OUT_SECNOD, TimeUnit.SECONDS)
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(logInterceptor)
                    .addInterceptor(new DownloadInterceptor(downloadListener));
        }

        return new Retrofit.Builder()
                .baseUrl("http://www.baidu.com")
                //.addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mBuilder.build())
                .build();
    }

    /**
     * 取消网络请求
     */
    public static void cancel(Disposable d) {
        if (null != d && !d.isDisposed()) {
            d.dispose();
        }
    }

    /**
     * 下载文件请求
     */
    public static void downloadFile(String url, long startPos, DownloadListener downloadListener, Observer<ResponseBody> observer) {
        getDownloadRetrofit(downloadListener).create(BaseApi.class).downloadFile("bytes=" + startPos + "-", url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

}
