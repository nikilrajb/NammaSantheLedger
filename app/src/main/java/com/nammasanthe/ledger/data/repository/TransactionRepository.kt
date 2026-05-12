package com.nammasanthe.ledger.data.repository

import com.nammasanthe.ledger.data.local.dao.CustomerBalance
import com.nammasanthe.ledger.data.local.dao.TransactionDao
import com.nammasanthe.ledger.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    // ── Per-customer ───────────────────────────────────────────────────
    fun getTransactionsForCustomer(customerId: Long) =
        transactionDao.getTransactionsForCustomer(customerId)

    fun getNetBalanceForCustomer(customerId: Long): Flow<Double> =
        transactionDao.getNetBalanceForCustomer(customerId)

    // ── Dashboard ──────────────────────────────────────────────────────
    fun getTotalOutstanding(): Flow<Double> =
        transactionDao.getTotalOutstanding()

    fun getTodayCredits(): Flow<Double> {
        val (start, end) = todayRange()
        return transactionDao.getTodayCredits(start, end)
    }

    fun getTodayPayments(): Flow<Double> {
        val (start, end) = todayRange()
        return transactionDao.getTodayPayments(start, end)
    }

    fun getCustomerBalances(): Flow<List<CustomerBalance>> =
        transactionDao.getCustomerBalances()

    // ── Daily summary ──────────────────────────────────────────────────
    fun getTransactionsForDate(epochMs: Long): Flow<List<Transaction>> {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);       cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end   = start + 86_400_000L - 1L
        return transactionDao.getTransactionsForDateRange(start, end)
    }

    // ── Write ──────────────────────────────────────────────────────────
    suspend fun insertTransaction(tx: Transaction): Long =
        transactionDao.insertTransaction(tx)

    suspend fun updateTransaction(tx: Transaction) =
        transactionDao.updateTransaction(tx)

    suspend fun deleteTransaction(tx: Transaction) =
        transactionDao.deleteTransaction(tx)

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)

    // ── Private helpers ────────────────────────────────────────────────
    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);       cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        return Pair(start, start + 86_400_000L - 1L)
    }
}