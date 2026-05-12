package com.nammasanthe.ledger.data.local.dao

import androidx.room.*
import com.nammasanthe.ledger.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

/** Holds the net balance for one customer — used in the Home Dashboard sort. */
data class CustomerBalance(
    val customerId : Long,
    val netBalance : Double   // positive = customer owes vendor; negative = vendor overpaid
)

@Dao
interface TransactionDao {

    // ── Write ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    // ── Per-customer reads ─────────────────────────────────────────────

    /** All transactions for one customer, newest first. Drives the Ledger screen. */
    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<Transaction>>

    /**
     * Net balance for one customer.
     * Formula: SUM(CREDIT amounts) - SUM(PAYMENT amounts)
     * A positive result means the customer still owes money.
     * COALESCE returns 0.0 if no transactions exist.
     */
    @Query(
        "SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0.0) " +
                "FROM transactions WHERE customerId = :customerId"
    )
    fun getNetBalanceForCustomer(customerId: Long): Flow<Double>

    // ── Dashboard / summary reads ──────────────────────────────────────

    /**
     * Grand total outstanding across ALL customers.
     * This is the headline number on the Home Dashboard.
     */
    @Query(
        "SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0.0) " +
                "FROM transactions"
    )
    fun getTotalOutstanding(): Flow<Double>

    /** Sum of all CREDIT transactions today (Udari given). */
    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) " +
                "FROM transactions " +
                "WHERE type = 'CREDIT' AND timestamp >= :startOfDay AND timestamp <= :endOfDay"
    )
    fun getTodayCredits(startOfDay: Long, endOfDay: Long): Flow<Double>

    /** Sum of all PAYMENT transactions today (collections received). */
    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) " +
                "FROM transactions " +
                "WHERE type = 'PAYMENT' AND timestamp >= :startOfDay AND timestamp <= :endOfDay"
    )
    fun getTodayPayments(startOfDay: Long, endOfDay: Long): Flow<Double>

    /**
     * All transactions for a specific date range.
     * Used by the Daily Summary screen — pass start and end of a calendar day.
     */
    @Query(
        "SELECT * FROM transactions " +
                "WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay " +
                "ORDER BY timestamp DESC"
    )
    fun getTransactionsForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<Transaction>>

    /**
     * Net balance for EVERY customer in one query.
     * Returns a list of (customerId, netBalance) pairs.
     * Used to sort the Home Dashboard by highest outstanding balance.
     */
    @Query(
        "SELECT customerId, " +
                "COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0.0) AS netBalance " +
                "FROM transactions " +
                "GROUP BY customerId"
    )
    fun getCustomerBalances(): Flow<List<CustomerBalance>>
}