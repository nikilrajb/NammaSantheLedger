package com.nammasanthe.ledger.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.local.entity.Customer
import com.nammasanthe.ledger.data.repository.CustomerRepository
import com.nammasanthe.ledger.data.repository.SettingsRepository
import com.nammasanthe.ledger.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state snapshot for the Home Dashboard screen */
data class HomeUiState(
    val totalOutstanding : Double              = 0.0,
    val todayCredits     : Double              = 0.0,
    val todayPayments    : Double              = 0.0,
    val customers        : List<Customer>      = emptyList(),
    val balanceMap       : Map<Long, Double>   = emptyMap(),  // customerId -> netBalance
    val languageCode     : String              = "en",
    val isLoading        : Boolean             = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val customerRepo    : CustomerRepository,
    private val transactionRepo : TransactionRepository,
    private val settingsRepo    : SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        customerRepo.getAllCustomers(),
        transactionRepo.getTotalOutstanding(),
        transactionRepo.getTodayCredits(),
        transactionRepo.getTodayPayments(),
        transactionRepo.getCustomerBalances(),
        settingsRepo.languageCode
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val customers = args[0] as List<Customer>
        val totalOut = args[1] as Double
        val todayC = args[2] as Double
        val todayP = args[3] as Double
        @Suppress("UNCHECKED_CAST")
        val balances = args[4] as List<com.nammasanthe.ledger.data.local.dao.CustomerBalance>
        val lang = args[5] as String

        HomeUiState(
            totalOutstanding = totalOut,
            todayCredits     = todayC,
            todayPayments    = todayP,
            customers        = customers,
            balanceMap       = balances.associate { it.customerId to it.netBalance },
            languageCode     = lang,
            isLoading        = false
        )
    }.stateIn(
        scope          = viewModelScope,
        started        = SharingStarted.WhileSubscribed(5_000),
        initialValue   = HomeUiState()
    )

    fun changeLanguage(code: String) {
        viewModelScope.launch {
            settingsRepo.updateLanguage(code)
        }
    }
}