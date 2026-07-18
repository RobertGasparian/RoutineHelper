package com.robertgasparian.routinehelper.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration1To2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE routine_items ADD COLUMN pendingRemovalAtMillis INTEGER",
        )
    }
}
