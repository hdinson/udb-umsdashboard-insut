package com.intretech.app.umsdashboard_new.activity

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.intretech.app.umsdashboard_new.crash.ActivityCrash
import com.intretech.app.umsdashboard_new.crash.CrashProfile
import com.tencent.mmkv.MMKV


class UdbApplication : Application() {


    var isUpdateAlreadyDone = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()


        CrashProfile.Builder.create().enabled(true)
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(true) //default: true
            .trackActivities(true) //default: false
            .minTimeBetweenCrashesMs(2000) //default: 3000
            .restartActivity(WebkitSystemActivity::class.java) //default: null (your app's launch activity)
            .errorActivity(ActivityCrash::class.java) //default: null (default error activity)
            .apply()

        context = this
        MMKV.initialize(this)
    }

    override fun getApplicationContext() = this


}