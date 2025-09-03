package ind.shop.expense_tracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Expense_List_Database(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Expense_DB"
        private const val DATABASE_VERSION = 2 // bumped to apply REAL change

        private const val TABLE_NAME = "Expense_List"
        private const val COLUMN_ID = "id"
        private const val COLUMN_PAID_TO = "paid_to"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_REMARK = "remark"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // amount as REAL for proper math/sorting
        val createTable = """
            CREATE TABLE $TABLE_NAME(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PAID_TO TEXT,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_DATE INTEGER,
                $COLUMN_REMARK TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Simple migration: if upgrading from v1 (amount TEXT) to v2 (amount REAL).
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_NAME RENAME TO ${TABLE_NAME}_old")
            db?.execSQL("""
                CREATE TABLE $TABLE_NAME(
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_PAID_TO TEXT,
                    $COLUMN_AMOUNT REAL NOT NULL,
                    $COLUMN_CATEGORY TEXT,
                    $COLUMN_DATE INTEGER,
                    $COLUMN_REMARK TEXT
                )
            """.trimIndent())
            // Copy with CAST to REAL
            db?.execSQL("""
                INSERT INTO $TABLE_NAME ($COLUMN_ID, $COLUMN_PAID_TO, $COLUMN_AMOUNT, $COLUMN_CATEGORY, $COLUMN_DATE, $COLUMN_REMARK)
                SELECT $COLUMN_ID, $COLUMN_PAID_TO, CAST($COLUMN_AMOUNT AS REAL), $COLUMN_CATEGORY, $COLUMN_DATE, $COLUMN_REMARK
                FROM ${TABLE_NAME}_old
            """.trimIndent())
            db?.execSQL("DROP TABLE ${TABLE_NAME}_old")
        }
    }

    fun add_expense(expense_info: Expense_Info) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PAID_TO, expense_info.paid_to)
            // ensure numeric storage
            put(COLUMN_AMOUNT, expense_info.amount.toFloatOrNull() ?: 0f)
            put(COLUMN_CATEGORY, expense_info.category)
            put(COLUMN_DATE, expense_info.date)
            put(COLUMN_REMARK, expense_info.remark)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun get_all_expense(): List<Expense_Info> {
        val list = mutableListOf<Expense_Info>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_ID,$COLUMN_PAID_TO,$COLUMN_AMOUNT,$COLUMN_DATE,$COLUMN_CATEGORY,$COLUMN_REMARK FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC",
            null
        )
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val paid_to = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAID_TO))
            // read back as REAL but keep Expense_Info.amount as String to avoid ripple changes
            val amount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)).toString()
            val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
            val date = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val remark = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMARK))
            list.add(Expense_Info(id, paid_to, amount, category, date, remark))
        }
        cursor.close()
        db.close()
        return list
    }

    fun delete_expense(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
    }

    fun edit_task(expense: Expense_Info) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PAID_TO, expense.paid_to)
            put(COLUMN_AMOUNT, expense.amount.toFloatOrNull() ?: 0f)
            put(COLUMN_CATEGORY, expense.category)
            put(COLUMN_DATE, expense.date)
            put(COLUMN_REMARK, expense.remark)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID =?", arrayOf(expense.id.toString()))
        db.close()
    }

    fun filterAndSort(category: String, sortBy: String): List<Expense_Info> {
        val list = mutableListOf<Expense_Info>()
        val db = readableDatabase

        val whereClause = if (category.equals("ALL", true)) "" else "WHERE $COLUMN_CATEGORY = ? COLLATE NOCASE"
        val orderByClause = when (sortBy.uppercase()) {
            "LOW TO HIGH" -> "ORDER BY CAST($COLUMN_AMOUNT AS REAL) ASC"
            "HIGH TO LOW" -> "ORDER BY CAST($COLUMN_AMOUNT AS REAL) DESC"
            "NEWEST" -> "ORDER BY $COLUMN_DATE DESC"
            "OLDEST" -> "ORDER BY $COLUMN_DATE ASC"
            else -> ""
        }

        val query = "SELECT * FROM $TABLE_NAME $whereClause $orderByClause"
        val args = if (category.equals("ALL", true)) null else arrayOf(category)

        val cursor = db.rawQuery(query, args)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val paid_to = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAID_TO))
            val amount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)).toString()
            val categoryVal = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
            val date = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val remark = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMARK))
            list.add(Expense_Info(id, paid_to, amount, categoryVal, date, remark))
        }
        cursor.close()
        return list
    }

    fun clearAllTables() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM $TABLE_NAME")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getCategoryTotals(): Map<String, Float> {
        val categoryTotals = mutableMapOf<String, Float>()
        val db = readableDatabase
        val query = """
            SELECT $COLUMN_CATEGORY, SUM(CAST($COLUMN_AMOUNT AS REAL)) AS total
            FROM $TABLE_NAME
            GROUP BY $COLUMN_CATEGORY
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
            val total = cursor.getFloat(cursor.getColumnIndexOrThrow("total"))
            categoryTotals[category] = total
        }
        cursor.close()
        db.close()
        return categoryTotals
    }

    fun getTotalAmount(): Float {
        var total = 0f
        val db = readableDatabase
        val query = """
            SELECT SUM(CAST($COLUMN_AMOUNT AS REAL)) AS total
            FROM $TABLE_NAME
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            total = cursor.getFloat(cursor.getColumnIndexOrThrow("total"))
        }
        cursor.close()
        db.close()
        return total
    }



}
