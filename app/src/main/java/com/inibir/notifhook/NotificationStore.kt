package com.inibir.notifhook

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotificationStore private constructor(context: Context) :
	SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

	data class Row(val id: Long, val payload: String)

	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL("CREATE TABLE pending (id INTEGER PRIMARY KEY AUTOINCREMENT, payload TEXT NOT NULL)")
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

	fun insert(payload: String) {
		val db = writableDatabase
		db.execSQL("INSERT INTO pending (payload) VALUES (?)", arrayOf(payload))
		db.execSQL("DELETE FROM pending WHERE id <= (SELECT MAX(id) FROM pending) - $MAX_ROWS")
	}

	fun oldest(limit: Int): List<Row> =
		readableDatabase.rawQuery("SELECT id, payload FROM pending ORDER BY id LIMIT $limit", null).use { cursor ->
			val rows = ArrayList<Row>()
			while (cursor.moveToNext()) rows.add(Row(cursor.getLong(0), cursor.getString(1)))
			rows
		}

	fun delete(id: Long) {
		writableDatabase.delete("pending", "id = ?", arrayOf(id.toString()))
	}

	companion object {
		private const val DB_NAME = "notifhook.db"
		private const val DB_VERSION = 1
		private const val MAX_ROWS = 5000

		@Volatile
		private var instance: NotificationStore? = null

		fun get(context: Context): NotificationStore =
			instance ?: synchronized(this) {
				instance ?: NotificationStore(context.applicationContext).also { instance = it }
			}
	}
}
