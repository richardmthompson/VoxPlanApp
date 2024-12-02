package com.voxplanapp

import android.app.Application
import com.voxplanapp.data.AppContainer
import com.voxplanapp.data.AppDataContainer

class VoxPlanApplication: Application() {

    lateinit var container: AppContainer

    // piggyback application's onCreate() method to create the container when app starts
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        (container as? AppDataContainer)?.soundPlayer?.release()
    }
}
