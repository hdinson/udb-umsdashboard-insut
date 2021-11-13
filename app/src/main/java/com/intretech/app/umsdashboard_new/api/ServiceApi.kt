package com.intretech.app.umsdashboard_new.api

import com.intretech.app.umsdashboard_new.bean.BoardInfoKt
import io.reactivex.Observable
import retrofit2.http.*

interface ServiceApi {

    /**
     * 获取界面的加载地址
     */
    @Headers("Domain-Name: uKanban")
    @GET("/API/Base/Board/NonLogin/BoardEvent/GetBoardFormJson")
    fun getHomePage(@Query("MacAddress") MacAddress: String): Observable<BoardInfoKt>

}