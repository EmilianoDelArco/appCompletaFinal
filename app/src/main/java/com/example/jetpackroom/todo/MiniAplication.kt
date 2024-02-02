package com.example.jetpackroom.todo

import android.app.Application
import androidx.room.Room
import com.example.jetpackroom.todo.db.AppDatabase

class MainApplication : Application() {
    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}