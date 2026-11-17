package com.kryptos.browser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SmartSpeedDialDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "KryptosSmartDials.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE smart_dials (
                domain TEXT PRIMARY KEY, 
                title TEXT, 
                url TEXT, 
                visit_count INTEGER, 
                days_visited INTEGER, 
                last_visit_timestamp INTEGER, 
                first_visit_date INTEGER, 
                total_duration_sec INTEGER, 
                score REAL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS smart_dials")
        onCreate(db)
    }

    fun recordVisit(domain: String, title: String, url: String, activeDurationSec: Long) {
        if (activeDurationSec < 5) return // Ignore short visits

        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT * FROM smart_dials WHERE domain=?", arrayOf(domain))
        
        val now = System.currentTimeMillis()
        val todayStart = now - (now % (24 * 60 * 60 * 1000))

        if (cursor.moveToFirst()) {
            val currentCount = cursor.getInt(cursor.getColumnIndexOrThrow("visit_count"))
            val currentDays = cursor.getInt(cursor.getColumnIndexOrThrow("days_visited"))
            val lastVisitTime = cursor.getLong(cursor.getColumnIndexOrThrow("last_visit_timestamp"))
            val totalDuration = cursor.getLong(cursor.getColumnIndexOrThrow("total_duration_sec"))
            
            val lastVisitDay = lastVisitTime - (lastVisitTime % (24 * 60 * 60 * 1000))
            val newDaysCount = if (todayStart > lastVisitDay) currentDays + 1 else currentDays

            val values = ContentValues().apply {
                put("visit_count", currentCount + 1)
                put("days_visited", newDaysCount)
                put("last_visit_timestamp", now)
                put("total_duration_sec", totalDuration + activeDurationSec)
                put("title", title)
                put("url", url)
            }
            db.update("smart_dials", values, "domain=?", arrayOf(domain))
        } else {
            val values = ContentValues().apply {
                put("domain", domain)
                put("title", title)
                put("url", url)
                put("visit_count", 1)
                put("days_visited", 1)
                put("last_visit_timestamp", now)
                put("first_visit_date", now)
                put("total_duration_sec", activeDurationSec)
                put("score", 0.0)
            }
            db.insert("smart_dials", null, values)
        }
        cursor.close()
        
        updateAllScoresAndLimit(db)
        db.close()
    }

    // New Rule: Just resets stats, doesn't blacklist permanently
    fun resetSiteStats(domain: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("visit_count", 0)
            put("days_visited", 0)
            put("total_duration_sec", 0)
            put("score", 0.0)
        }
        db.update("smart_dials", values, "domain=?", arrayOf(domain))
        db.close()
    }

    private fun updateAllScoresAndLimit(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT * FROM smart_dials", null)
        val now = System.currentTimeMillis()

        if (cursor.moveToFirst()) {
            do {
                val domain = cursor.getString(cursor.getColumnIndexOrThrow("domain"))
                val visitCount = cursor.getInt(cursor.getColumnIndexOrThrow("visit_count"))
                val daysVisited = cursor.getInt(cursor.getColumnIndexOrThrow("days_visited"))
                val lastVisit = cursor.getLong(cursor.getColumnIndexOrThrow("last_visit_timestamp"))
                val totalDuration = cursor.getLong(cursor.getColumnIndexOrThrow("total_duration_sec"))

                val F = (visitCount / 20.0).coerceAtMost(1.0) * 100
                val daysSinceLastVisit = ((now - lastVisit) / (1000 * 60 * 60 * 24.0)).toInt()
                val R = (100 - (daysSinceLastVisit * 3.33)).coerceAtLeast(0.0)
                val D = (totalDuration / 3600.0).coerceAtMost(1.0) * 100
                val G = (daysVisited / 10.0).coerceAtMost(1.0) * 100
                val E = ((visitCount / 10.0 * 0.5) + (totalDuration / 1800.0 * 0.5)).coerceAtMost(1.0) * 100

                var score = (0.35 * F) + (0.25 * R) + (0.15 * D) + (0.15 * G) + (0.10 * E)
                
                if (visitCount < 3 || daysVisited < 2) {
                    score = 0.0
                }

                val values = ContentValues().apply { put("score", score) }
                db.update("smart_dials", values, "domain=?", arrayOf(domain))

            } while (cursor.moveToNext())
        }
        cursor.close()

        // Enforce Top 8 limit strictly
        db.execSQL("""
            UPDATE smart_dials 
            SET score = 0.0 
            WHERE domain NOT IN (
                SELECT domain FROM smart_dials 
                WHERE score >= 70 
                ORDER BY score DESC LIMIT 8
            )
        """)
    }

    fun getTop8Sites(): List<HistoryItem> {
        val list = mutableListOf<HistoryItem>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM smart_dials WHERE score >= 70 ORDER BY score DESC LIMIT 8", null)

        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val url = cursor.getString(cursor.getColumnIndexOrThrow("url"))
                list.add(HistoryItem(title, url, 0L))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}
