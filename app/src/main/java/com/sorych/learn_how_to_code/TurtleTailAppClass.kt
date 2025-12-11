package com.sorych.learn_how_to_code

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.sorych.learn_how_to_code.data.UserProgressRepository


private const val USER_PROGRESS = "user_progress"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PROGRESS
)

class TurtleTailAppClass : Application() {
    lateinit var userProgressRepository: UserProgressRepository
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        userProgressRepository = UserProgressRepository(dataStore)
        Log.d("TurtleTailApp", "📂 DataStore initialized")
    }
}