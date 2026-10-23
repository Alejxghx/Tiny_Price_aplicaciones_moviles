package com.alejx.tiny_price

import android.app.Application
import com.alejx.tiny_price.data.session.SessionRegistry

class TinyPriceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionRegistry.initialize(this)
    }
}
