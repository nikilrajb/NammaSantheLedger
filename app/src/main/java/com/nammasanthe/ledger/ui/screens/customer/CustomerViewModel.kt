package com.nammasanthe.ledger.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.local.entity.Customer
import com.nammasanthe.ledger.data.repository.CustomerRepository
import com.nammasanthe.ledger.utils.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CustomerFormEvent {
    object SaveSuccess : CustomerFormEvent()
    data class Error(val message: String) : CustomerFormEvent()
}

data class CustomerFormState(
    val name         : String  = "",
    val phone        : String  = "",
    val photoUri     : String? = null,
    val nameError    : String? = null,
    val phoneError   : String? = null,
    val isSaving     : Boolean = false,
    val isEditMode   : Boolean = false
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepo: CustomerRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(CustomerFormState())
    val formState: StateFlow<CustomerFormState> = _formState.asStateFlow()

    private val _event = MutableSharedFlow<CustomerFormEvent>()
    val event: SharedFlow<CustomerFormEvent> = _event.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val customers: StateFlow<List<Customer>> = _searchQuery
        .debounce(300L)  // wait 300ms after last keystroke before querying
        .flatMapLatest { q ->
            if (q.isBlank()) customerRepo.getAllCustomers()
            else customerRepo.searchCustomers(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchQueryChange(q: String) { _searchQuery.value = q }

    fun onNameChange(v: String) {
        _formState.update { it.copy(name = v, nameError = null) }
    }

    fun onPhoneChange(v: String) {
        if (v.length <= 10 && v.all { c -> c.isDigit() }) {
            _formState.update { it.copy(phone = v, phoneError = null) }
        }
    }

    fun onPhotoUriChange(uri: String?) {
        _formState.update { it.copy(photoUri = uri) }
    }

    fun loadCustomerForEdit(customerId: Long) {
        viewModelScope.launch {
            customerRepo.getCustomerById(customerId).firstOrNull()?.let { c ->
                _formState.update { it.copy(
                    name = c.name, phone = c.phone,
                    photoUri = c.photoUri, isEditMode = true
                )}
            }
        }
    }

    fun saveCustomer(existingId: Long? = null) {
        val state = _formState.value
        if (!validate(state)) return
        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val customer = Customer(
                    id       = existingId ?: 0L,
                    name     = state.name.trim(),
                    phone    = state.phone.trim(),
                    photoUri = state.photoUri
                )
                if (existingId == null) customerRepo.insertCustomer(customer)
                else customerRepo.updateCustomer(customer)
                _event.emit(CustomerFormEvent.SaveSuccess)
            } catch (e: Exception) {
                val msg = if (e.message?.contains("UNIQUE") == true)
                    "A customer with this phone number already exists."
                else "Save failed. Please try again."
                _event.emit(CustomerFormEvent.Error(msg))
            } finally {
                _formState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch { customerRepo.deleteCustomer(customer) }
    }

    private fun validate(state: CustomerFormState): Boolean {
        var valid = true
        if (state.name.isBlank()) {
            _formState.update { it.copy(nameError = "Name is required") }
            valid = false
        } else if (state.name.trim().length > 60) {
            _formState.update { it.copy(nameError = "Name must be 60 characters or less") }
            valid = false
        }
        if (!FormatUtils.isValidPhone(state.phone)) {
            _formState.update { it.copy(phoneError = "Enter a valid 10-digit mobile number") }
            valid = false
        }
        return valid
    }
}