package com.intretech.app.umsdashboard_new.bean

data class ApkUpdateInfoKt(
    var appPath //APP发布路径；
    : String = "",
    var appVersionNum //APP版本号；
    : Int = 0,
    var appVersionName //APP版本名称；
    : String = "",
    var appRemark //APP发布说明；
    : String = "",
    var dataRefreshTime //数据刷新时间；
    : Int = 0,
    var recordQty //显示记录条目;
    : Int = 0,
    var autoShutDownTime //自动关机时间；
    : String = "",
    var fpyWarnValue //直通率预警值；
    : Int = 0,
    var boardNotice //全局看板通知；
    : String = "",
    var minVersion: Int = 0
)