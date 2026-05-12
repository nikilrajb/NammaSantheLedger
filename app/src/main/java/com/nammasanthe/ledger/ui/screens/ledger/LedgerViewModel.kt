package com.nammasanthe.ledger.ui.screens.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.local.entity.Customer
import com.nammasanthe.ledger.data.local.entity.Transaction
import com.nammasanthe.ledger.data.local.entity.TransactionType
import com.nammasanthe.ledger.data.repository.CustomerRepository
import com.nammasanthe.ledger.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Filter options for transactions */
enum class TransactionFilter {
    ALL, CREDIT, PAYMENT
}

data class LedgerUiState(
    val customer          : Customer?          = null,
    val transactions      : List<Transaction>  = emptyList(),
    val filteredTransactions : List<Transaction> = emptyList(),
    val netBalance        : Double             = 0.0,
    val isLoading         : Boolean            = true,
    val searchQuery       : String             = "",
    val selectedFilter    : TransactionFilter  = TransactionFilter.ALL
)

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val customerRepo    : CustomerRepository,
    private val transactionRepo : TransactionRepository
) : ViewModel() {

    private val _customerId = MutableStateFlow(0L)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)

    val uiState: StateFlow<LedgerUiState> = _customerId
        .filter { it > 0L }
        .flatMapLatest { cid ->
            combine(
                customerRepo.getCustomerById(cid),
                transactionRepo.getTransactionsForCustomer(cid),
                transactionRepo.getNetBalanceForCustomer(cid),
                _searchQuery,
                _selectedFilter
            ) { customer, txs, balance, query, filter ->
                val filtered = filterAndSearchTransactions(txs, query, filter)
                LedgerUiState(
                    customer      = customer,
                    transactions  = txs,
                    filteredTransactions = filtered,
                    netBalance    = balance,
                    isLoading     = false,
                    searchQuery   = query,
                    selectedFilter = filter
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LedgerUiState())

    fun loadCustomer(id: Long) { _customerId.value = id }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun updateFilter(filter: TransactionFilter) { _selectedFilter.value = filter }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedFilter.value = TransactionFilter.ALL
    }

    private fun filterAndSearchTransactions(
        transactions: List<Transaction>,
        searchQuery: String,
        filter: TransactionFilter
    ): List<Transaction> {
        var filtered = transactions

        // Apply type filter
        if (filter != TransactionFilter.ALL) {
            filtered = filtered.filter { tx ->
                when (filter) {
                    TransactionFilter.CREDIT -> tx.type == TransactionType.CREDIT
                    TransactionFilter.PAYMENT -> tx.type == TransactionType.PAYMENT
                    else -> true
                }
            }
        }

        // Apply search query
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.trim().lowercase()
            filtered = filtered.filter { tx ->
                tx.note?.lowercase()?.contains(query) == true ||
                tx.amount.toString().contains(query)
            }
        }

        // Return sorted by date (newest first)
        return filtered.sortedByDescending { it.timestamp }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch { transactionRepo.deleteTransaction(tx) }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch { customerRepo.deleteCustomer(customer) }
    }
}