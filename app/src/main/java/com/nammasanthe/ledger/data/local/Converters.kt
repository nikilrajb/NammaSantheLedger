package com.nammasanthe.ledger.data.local

import androidx.room.TypeConverter
import com.nammasanthe.ledger.data.local.entity.TransactionType

/**
 * Room Type Converters — Room cannot store Kotlin enums directly.
 * These converters translate TransactionType ↔ String for DB storage.
 */
class Converters {

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        TransactionType.valueOf(value)
}