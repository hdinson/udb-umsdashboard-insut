package com.intretech.app.umsdashboard_new.utils

import android.util.Log

/**
 *  日志工具类
 */
private const val filterTag = "\uD83D\uDE02 "
private const val TAG = "Ums"

@JvmSuppressWildcards
fun logv(func: () -> String) {
    val method = getLineNumber("logv")
    Log.v(method, "$filterTag${func()}")
}

@JvmSuppressWildcards
fun logi(func: () -> String) {
    val method = getLineNumber("logi")
    Log.i(method, "$filterTag${func()}")
}

@JvmSuppressWildcards
fun logw(func: () -> String) {
    val method = getLineNumber("logw")
    Log.w(method, "$filterTag${func()}")
}

@JvmSuppressWildcards
fun loge(func: () -> String) {
    getLineNumber("loge")
    Log.e(TAG, "$filterTag${func()}")
}


fun getLineNumber(methodName: String): String {
    val stackTraceElement = Thread.currentThread().stackTrace
    val currentIndex = stackTraceElement.indices.firstOrNull { stackTraceElement[it].methodName.compareTo(methodName) == 0 }?.let { it + 1 } ?: return ""
    val fileName = stackTraceElement[currentIndex].fileName
    val lineNumber = stackTraceElement[currentIndex].lineNumber.toString()
    return "($fileName:$lineNumber)"
}
