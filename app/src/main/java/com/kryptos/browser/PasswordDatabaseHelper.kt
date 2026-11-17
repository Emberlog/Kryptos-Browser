package com.kryptos.browser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PasswordDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "KryptosPasswords.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE passwords (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                domain TEXT,
                username TEXT,
                password TEXT,
                UNIQUE(domain, username) ON CONFLICT REPLACE
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS passwords")
        onCreate(db)
    }

    fun savePassword(domain: String, username: String, password: String) {
        if (username.isEmpty() || password.isEmpty()) return
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("domain", domain)
            put("username", username)
            put("password", password)
        }
        db.insert("passwords", null, values)
        db.close()
    }

    fun getPassword(domain: String): Pair<String, String>? {
        val db = this.readableDatabase
        // Fetch the most recently saved password for this domain
        val cursor = db.rawQuery("SELECT username, password FROM passwords WHERE domain=? ORDER BY id DESC LIMIT 1", arrayOf(domain))
        
        var credentials: Pair<String, String>? = null
        if (cursor.moveToFirst()) {
            val user = cursor.getString(0)
            val pass = cursor.getString(1)
            credentials = Pair(user, pass)
        }
        cursor.close()
        db.close()
        return credentials
    }

    fun deletePassword(domain: String) {
        val db = this.writableDatabase
        db.delete("passwords", "domain=?", arrayOf(domain))
        db.close()
    }
}
