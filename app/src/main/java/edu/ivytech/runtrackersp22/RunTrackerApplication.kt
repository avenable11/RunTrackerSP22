package edu.ivytech.runtrackersp22

import android.app.Application

class RunTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LocationRepository.initialize(this)
    }
}