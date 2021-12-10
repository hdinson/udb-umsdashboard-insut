package com.intretech.app.umsdashboard_new.api

import com.intretech.app.umsdashboard_new.BuildConfig
import com.intretech.app.umsdashboard_new.bean.ApkUpdateInfoKt
import com.intretech.app.umsdashboard_new.bean.BoardInfoKt
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface ServiceApi {

    /**
     * 获取界面的加载地址
     */
    @Headers("Domain-Name: uKanban")
    @GET("/API/Base/Board/NonLogin/BoardEvent/GetBoardFormJson?version=${BuildConfig.VERSION_NAME}")
    fun getHomePage(@Query("MacAddress") MacAddress: String): Observable<Response<BoardInfoKt>>

    /**
     * 版本更新
     */
    @GET("/API/Base/Board/NonLogin/BoardConfigureEvent/GetBoardConfigureJson")
    fun checkAppVersion():Observable<ApkUpdateInfoKt>

}