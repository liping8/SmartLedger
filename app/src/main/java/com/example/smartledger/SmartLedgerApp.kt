package com.example.smartledger

import android.app.Application
import com.example.smartledger.data.AppDatabase

class SmartLedgerApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}
