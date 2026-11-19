package com.example.agricultural

import android.content.ContentValues
import android.database.Cursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ABSTRACTION + TEMPLATE METHOD
 *
 * All the repeated SQL plumbing (insert, update, delete, getById,
 * getAll, running on Dispatchers.IO) is written once here.
 * A concrete DAO only says:
 *   - which table / id column it uses,
 *   - how to turn a record into ContentValues ([toValues]),
 *   - how to turn a Cursor row into a record ([fromCursor]).
 */
abstract class BaseDao<T : AgriculturalRecord>(
    protected val helper: AppDatabase,
    private val table: String,
    private val idColumn: String
) : ReadRepository<T>, WriteRepository<T> {

    protected abstract fun toValues(item: T): ContentValues

    protected abstract fun fromCursor(cursor: Cursor): T

    /** Base SELECT. Subclasses can override it to add a JOIN. */
    protected open val selectSql: String
        get() = "SELECT * FROM $table"

    protected open val orderBy: String
        get() = "$idColumn ASC"

    protected suspend fun <R> io(block: () -> R): R =
        withContext(Dispatchers.IO) { block() }

    protected fun query(sql: String, args: Array<String>? = null): List<T> {
        val result = mutableListOf<T>()
        helper.readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(fromCursor(cursor))
            }
        }
        return result
    }

    override suspend fun insert(item: T): Long = io {
        helper.writableDatabase.insertOrThrow(table, null, toValues(item))
    }

    override suspend fun update(item: T): Int = io {
        helper.writableDatabase.update(
            table,
            toValues(item),
            "$idColumn = ?",
            arrayOf(item.id.toString())
        )
    }

    override suspend fun delete(id: Int): Int = io {
        helper.writableDatabase.delete(
            table,
            "$idColumn = ?",
            arrayOf(id.toString())
        )
    }

    override suspend fun getById(id: Int): T? = io {
        query("$selectSql WHERE $idColumn = ?", arrayOf(id.toString()))
            .firstOrNull()
    }

    override suspend fun getAll(): List<T> = io {
        query("$selectSql ORDER BY $orderBy")
    }
}

/**
 * INHERITANCE: a [BaseDao] for records that belong to a farm.
 * Adds getByFarmId once for Crop, ProductionArea, Harvest and FarmBusiness.
 */
abstract class FarmScopedDao<T : AgriculturalRecord>(
    helper: AppDatabase,
    table: String,
    idColumn: String
) : BaseDao<T>(helper, table, idColumn), FarmScopedRepository<T> {

    /** Column used to filter by farm. A JOIN-based DAO can override it. */
    protected open val farmColumn: String
        get() = "farmId"

    override suspend fun getByFarmId(farmId: Int): List<T> = io {
        query(
            "$selectSql WHERE $farmColumn = ? ORDER BY $orderBy",
            arrayOf(farmId.toString())
        )
    }
}

// ---- Small cursor helpers (read a column by name) ----

internal fun Cursor.int(column: String): Int =
    getInt(getColumnIndexOrThrow(column))

internal fun Cursor.double(column: String): Double =
    getDouble(getColumnIndexOrThrow(column))

internal fun Cursor.string(column: String): String =
    getString(getColumnIndexOrThrow(column)) ?: ""
