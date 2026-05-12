package com.nammasanthe.ledger.data.local.dao

import androidx.room.*
import com.nammasanthe.ledger.data.local.entity.Customer
import kotlinx.coroutines.flow.Flow

/**
 * CustomerDao — all database operations for the Customer table.
 * Every function returning Flow is automatically observed — the UI
 * updates in real-time whenever the underlying data changes.
 */
@Dao
interface CustomerDao {

    // ── Write operations ───────────────────────────────────────────────

    /**
     * Insert a new customer. Uses ABORT strategy so a duplicate phone
     * number throws SQLiteConstraintException (caught in ViewModel).
     * Returns the new row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomer(customer: Customer): Long

    /** Update all fields of an existing customer record. */
    @Update
    suspend fun updateCustomer(customer: Customer)

    /**
     * Delete a customer. Room @Delete uses the primary key.
     * All related Transaction records are deleted automatically
     * by the ON DELETE CASCADE foreign key constraint.
     */
    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // ── Read operations (return Flow for reactive UI) ──────────────────

    /** All customers sorted alphabetically — drives the Customer List screen. */
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    /** Single customer by ID — drives the Ledger and Edit screens. */
    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<Customer?>

    /**
     * Real-time search by name OR phone (partial match, case-insensitive).
     * Used in the Search screen and the customer picker in Add Transaction.
     */
    @Query(
        "SELECT * FROM customers " +
                "WHERE name LIKE '%' || :query || '%' " +
                "   OR phone LIKE '%' || :query || '%' " +
                "ORDER BY name ASC"
    )
    fun searchCustomers(query: String): Flow<List<Customer>>
}