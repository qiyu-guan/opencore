package com.opencore.app

import android.app.Application
import com.opencore.app.utils.LogHelper

class OpenCoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LogHelper.init(this)
    }
}
