package com.intretech.app.umsdashboard_new.utils

import android.annotation.SuppressLint
import android.util.Log
import com.intretech.app.umsdashboard_new.BuildConfig
import com.tencent.mmkv.MMKV
import java.net.NetworkInterface

object MMKVUtils {

    private val sAppConfig by lazy { MMKV.mmkvWithID("app_config") }

    private const val KEY_NET_MAC = "mac"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_HOME_PAGE_URL = "home_page_url"
    private const val KEY_SHOW_QR_CODE = "show_qr_code"
    private const val KEY_RENDERING_ENGINE = "rendering_engine"

    /**
     * 清除所有保存的App设置
     */
    fun clearAppConfig() {
        sAppConfig.clearAll()
    }

    fun saveMac(addr: String) {
        sAppConfig.encode(KEY_NET_MAC, addr)
    }

    /**
     * 获取mac地址
     */
    fun getMac(): String {
        return sAppConfig.decodeString(KEY_NET_MAC, getNetMac())
    }

    /**
     * 获取不带冒号的mac地址
     */
    fun getMacAddrWithoutDot(): String = getMac().replace(":", "")


    fun saveBaseUrl(baseUrl: String) {
        sAppConfig.encode(KEY_BASE_URL, baseUrl)
    }

    fun getBaseUrl(): String {
        return sAppConfig.decodeString(KEY_BASE_URL, BuildConfig.BASE_URL)
    }

    fun saveHomePageUrl(homePageUrl: String) {
        sAppConfig.encode(KEY_HOME_PAGE_URL, homePageUrl)
    }

    fun setHomePageIsShowQrCode(isShow: Boolean) {
        sAppConfig.encode(KEY_SHOW_QR_CODE, isShow)
    }

    fun isShowHomePageQrCode(): Boolean {
        return sAppConfig.decodeBool(KEY_SHOW_QR_CODE, true)
    }

    fun getHomePageUrl(): String = sAppConfig.decodeString(KEY_HOME_PAGE_URL, "")


    /**
     * 保存选择的浏览器引擎
     */
    fun saveRenderingEngine(number: Int) {
        sAppConfig.encode(KEY_RENDERING_ENGINE, number)
    }

    /**
     * 获取浏览器引擎
     *
     * 0:表示系统引擎
     * 1:表示Crosswalk引擎
     * 2:表示腾讯X5引擎
     */
    fun getRenderingEngine(): Int {
        return sAppConfig.decodeInt(KEY_RENDERING_ENGINE, 0)
    }

    /**
     *  清除保存的浏览器引擎
     */
    @SuppressLint("unused")
    fun clearRenderingEngine() {
        sAppConfig.remove(KEY_RENDERING_ENGINE)
    }


    private fun getNetMac(): String {
        if (BuildConfig.DEBUG && BuildConfig.MAC_ADDRESS.isNotEmpty()) {
            return BuildConfig.MAC_ADDRESS
        }
        val interfaces = NetworkInterface.getNetworkInterfaces()
        var mac = ""
        return try {
            while (interfaces.hasMoreElements()) {
                val iF = interfaces.nextElement()
                val address = iF.hardwareAddress
                if (address == null || address.isEmpty()) {
                    continue
                }
                mac = address.joinToString("") { String.format("%02X", it) }
            }
            mac
        } catch (e: Exception) {
            e.printStackTrace()
            mac
        }
    }
}