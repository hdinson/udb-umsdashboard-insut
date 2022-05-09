package com.intretech.app.umsdashboard_new.utils

import android.app.Activity
import kotlin.system.exitProcess


object AtyContainer {

    private var activityStack: ArrayList<Activity> = ArrayList()

    /**
     * 添加Activity到堆栈
     */
    fun addActivity(activity: Activity) {
        activityStack.add(activity)
    }


    /**
     * 移除指定的Activity
     */
    fun removeActivity(activity: Activity) {
        activityStack.remove(activity)
    }

    /**
     * 结束所有Activity
     */
    fun finishAllActivity() {
        activityStack.forEach {
            it.finish()
        }
        activityStack.clear()
        System.exit(0)
    }
}