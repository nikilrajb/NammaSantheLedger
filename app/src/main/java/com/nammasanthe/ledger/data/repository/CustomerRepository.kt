package com.nammasanthe.ledger.data.repository

import com.nammasanthe.ledger.data.local.dao.CustomerDao
import com.nammasanthe.ledger.data.local.entity.Customer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CustomerRepository — single source of truth for Customer data.
 * ViewModels interact ONLY with the repository, never with DAOs directly.
 * @Inject constructor enables Hilt to provide this automatically.
 */
@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    fun getAllCustomers(): Flow<List<Customer>> =
        customerDao.getAllCustomers()

    fun getCustomerById(id: Long): Flow<Customer?> =
        customerDao.getCustomerById(id)

    fun searchCustomers(query: String): Flow<List<Customer>> =
        customerDao.searchCustomers(query)

    suspend fun insertCustomer(customer: Customer): Long =
        customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: Customer) =
        customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: Customer) =
        customerDao.deleteCustomer(customer)
}