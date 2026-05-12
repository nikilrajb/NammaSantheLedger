package com.nammasanthe.ledger.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.local.entity.Transaction
import com.nammasanthe.ledger.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class SummaryUiState(
    val selectedDateMs  : Long              = System.currentTimeMillis(),
    val transactions    : List<Transaction> = emptyList(),
    val totalCredits    : Double            = 0.0,
    val totalPayments   : Double            = 0.0,
    val netFlow         : Double            = 0.0,
    val isLoading       : Boolean           = true
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val txRepo: TransactionRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<SummaryUiState> = _selectedDate
        .flatMapLatest { dateMs ->
            txRepo.getTransactionsForDate(dateMs).map { txs ->
                val credits  = txs.filter { it.type.name == "CREDIT"  }.sumOf { it.amount }
                val payments = txs.filter { it.type.name == "PAYMENT" }.sumOf { it.amount }
                SummaryUiState(
                    selectedDateMs = dateMs,
                    transactions   = txs,
                    totalCredits   = credits,
                    totalPayments  = payments,
                    netFlow        = credits - payments,
                    isLoading      = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SummaryUiState())

    fun onDateSelected(epochMs: Long) { _selectedDate.value = epochMs }

    fun goToPreviousDay() {
        _selectedDate.update { it - 86_400_000L }
    }

    fun goToNextDay() {
        val next = _selectedDate.value + 86_400_000L
        if (next <= System.currentTimeMillis()) _selectedDate.update { next }
    }
}