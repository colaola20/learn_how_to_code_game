package com.sorych.learn_how_to_code

import android.app.Application
import com.google.firebase.FirebaseApp


class TurtleTailAppClass : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}