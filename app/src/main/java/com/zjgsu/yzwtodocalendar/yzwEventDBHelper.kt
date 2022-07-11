package com.zjgsu.yzwtodocalendar

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class todoEventDBHelper(val context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {
    private val createTable = "create table TodoEventTable (" +
            "id integer primary key autoincrement," +
            "name text," +
            "isFinished integer," +
            "year integer," +       // date
            "month integer," +
            "day integer," +
            "cate text," +
            "persons text," +
            "location text," +
            "notification text," +
            "hourOfDay integer," +      // time
            "minute integer)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTable)
        Toast.makeText(context, "Create succeeded", Toast.LENGTH_SHORT).show()
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}