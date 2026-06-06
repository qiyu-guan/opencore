package com.opencore.app.utils

import java.io.BufferedReader
import java.io.InputStreamReader

object RootChecker {
    fun isRooted(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c exit")
            val result = process.waitFor()
            result == 0
        } catch (e: Exception) {
            false
        }
    }

    fun isRootedSync(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c exit")
            val result = process.waitFor()
            result == 0
        } catch (e: Exception) {
            false
        }
    }
}
