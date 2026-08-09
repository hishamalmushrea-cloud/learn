package com.indolearn

import android.app.Application
import androidx.room.Room
import com.indolearn.data.local.AppDatabase

class IndoLearnApp : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "indolearn_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
}