package com.intretech.app.umsdashboard_new.utils

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