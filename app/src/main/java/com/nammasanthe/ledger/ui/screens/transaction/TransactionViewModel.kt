package com.nammasanthe.ledger.ui.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.local.entity.Transaction
import com.nammasanthe.ledger.data.local.entity.TransactionType
import com.nammasanthe.ledger.data.repository.TransactionRepository
import com.nammasanthe.ledger.utils.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TxFormEvent {
    object SaveSuccess : TxFormEvent()
    data class Error(val message: String) : TxFormEvent()
}

data class TxFormState(
    val customerId    : Long            = 0L,
    val type          : TransactionType = TransactionType.CREDIT,
    val amountText    : String          = "",
    val note          : String          = "",
    val amountError   : String?         = null,
    val noteError     : String?         = null,
    val isSaving      : Boolean         = false
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val txRepo: TransactionRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(TxFormState())
    val formState: StateFlow<TxFormState> = _formState.asStateFlow()

    private val _event = MutableSharedFlow<TxFormEvent>()
    val event: SharedFlow<TxFormEvent> = _event.asSharedFlow()

    fun init(customerId: Long, transactionId: Long?) {
        _formState.update { it.copy(customerId = customerId) }
        if (transactionId != null) loadForEdit(transactionId)
    }

    private fun loadForEdit(txId: Long) {
        viewModelScope.launch {
            txRepo.getTransactionsForCustomer(_formState.value.customerId)
                .firstOrNull()
                ?.find { it.id == txId }
                ?.let { tx ->
                    _formState.update { it.copy(
                        type       = tx.type,
                        amountText = tx.amount.toString(),
                        note       = tx.note ?: ""
                    )}
                }
        }
    }

    fun onTypeChange(type: TransactionType) {
        _formState.update { it.copy(type = type, amountError = null) }
    }

    fun onAmountChange(v: String) {
        // Only allow digits and one decimal point
        if (v.isEmpty() || v.matches(Regex("^\\d{0,8}(\\.\\d{0,2})?$"))) {
            _formState.update { it.copy(amountText = v, amountError = null) }
        }
    }

    fun onNoteChange(v: String) {
        if (v.length <= 80) {
            _formState.update { it.copy(note = v, noteError = null) }
        } else {
            _formState.update { it.copy(noteError = "Note must be 80 characters or less") }
        }
    }

    fun save(existingTxId: Long? = null) {
        val state = _formState.value
        if (!validate(state)) return
        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val tx = Transaction(
                    id         = existingTxId ?: 0L,
                    customerId = state.customerId,
                    type       = state.type,
                    amount     = state.amountText.toDouble(),
                    note       = state.note.trim().ifBlank { null }
                )
                if (existingTxId == null) txRepo.insertTransaction(tx)
                else txRepo.updateTransaction(tx)
                _event.emit(TxFormEvent.SaveSuccess)
            } catch (e: Exception) {
                _event.emit(TxFormEvent.Error("Save failed: ${e.message}"))
            } finally {
                _formState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch { txRepo.deleteTransaction(tx) }
    }

    private fun validate(state: TxFormState): Boolean {
        if (!FormatUtils.isValidAmount(state.amountText)) {
            _formState.update { it.copy(amountError = "Enter a valid amount greater than 0") }
            return false
        }
        return true
    }
}