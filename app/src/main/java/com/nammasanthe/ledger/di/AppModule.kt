package com.nammasanthe.ledger.di

import android.content.Context
import androidx.room.Room
import com.nammasanthe.ledger.data.local.AppDatabase
import com.nammasanthe.ledger.data.local.dao.CustomerDao
import com.nammasanthe.ledger.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule — Hilt module that tells the DI framework how to create
 * all app-wide singleton dependencies.
 *
 * HOW HILT WORKS (simple mental model):
 *  - @Module = a factory class for creating objects
 *  - @Provides = a recipe for creating one specific object
 *  - @Singleton = create it once, share the same instance forever
 *  - @InstallIn(SingletonComponent) = these live as long as the app lives
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room database instance.
     * Room.databaseBuilder creates the .db file on first run.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration() // Only for dev — use proper @Migration in production
            .build()

    @Provides
    @Singleton
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()

    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
}