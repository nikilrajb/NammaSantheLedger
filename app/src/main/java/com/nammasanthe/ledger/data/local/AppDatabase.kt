package com.nammasanthe.ledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nammasanthe.ledger.data.local.dao.CustomerDao
import com.nammasanthe.ledger.data.local.dao.TransactionDao
import com.nammasanthe.ledger.data.local.entity.Customer
import com.nammasanthe.ledger.data.local.entity.Transaction

/**
 * AppDatabase — the single Room database for the entire app.
 *
 * IMPORTANT RULES:
 * 1. Never call DAO methods directly from a Composable — always via ViewModel.
 * 2. To change the schema after Week 2 freeze: increment version AND add a
 *    @Migration(from=X, to=Y) object, otherwise existing user data is wiped.
 * 3. exportSchema = true stores schema JSON in app/schemas/ — commit this to git.
 */
@Database(
    entities  = [Customer::class, Transaction::class],
    version   = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "namma_santhe_ledger.db"
    }
}