package com.nammasanthe.ledger.ui.screens.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.ui.components.ConfirmDeleteDialog
import com.nammasanthe.ledger.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    customerId : Long?,   // null = Add mode, non-null = Edit mode
    onBack     : () -> Unit,
    viewModel  : CustomerViewModel = hiltViewModel()
) {
    val state    by viewModel.formState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val isEdit   = customerId != null
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load customer data when in edit mode
    LaunchedEffect(customerId) {
        if (customerId != null) viewModel.loadCustomerForEdit(customerId)
    }

    // Listen for save/error events
    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is CustomerFormEvent.SaveSuccess -> onBack()
                is CustomerFormEvent.Error -> snackbar.showSnackbar(event.message)
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title    = stringResource(R.string.delete),
            message  = stringResource(R.string.delete_customer_confirm),
            onConfirm = {
                // In a real app, you'd call a delete method in viewModel
                showDeleteDialog = false
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text(if (isEdit) stringResource(R.string.edit_customer) else stringResource(R.string.add_customer),
                    color = SurfaceWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back),
                            tint = SurfaceWhite)
                    }
                },
                actions = {
                    if (isEdit) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete),
                                tint = SurfaceWhite)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Saffron700)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = ScaffoldBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Name Field ─────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.customer_name),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value         = state.name,
                    onValueChange = viewModel::onNameChange,
                    label         = { Text(stringResource(R.string.customer_name)) },
                    placeholder   = { Text("e.g. Kamala") },
                    leadingIcon   = { Icon(Icons.Default.Person, null, tint = Saffron700) },
                    isError       = state.nameError != null,
                    supportingText= { 
                        if (state.nameError != null) {
                            Text(stringResource(R.string.error_name_required), color = PaymentRed) 
                        }
                    },
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Saffron700,
                        unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            // ── Phone Field ────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.mobile_number),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value         = state.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label         = { Text(stringResource(R.string.mobile_number)) },
                    placeholder   = { Text("10-digit number") },
                    leadingIcon   = { Icon(Icons.Default.Phone, null, tint = Saffron700) },
                    prefix        = { Text("+91 ", fontWeight = FontWeight.Bold) },
                    isError       = state.phoneError != null,
                    supportingText= { 
                        if (state.phoneError != null) {
                            Text(stringResource(R.string.error_phone_invalid), color = PaymentRed) 
                        }
                    },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Saffron700,
                        unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Save Button ────────────────────────────────────────────
            Button(
                onClick  = { viewModel.saveCustomer(existingId = customerId) },
                enabled  = !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Saffron700)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp),
                        color = SurfaceWhite, strokeWidth = 3.dp)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(if (isEdit) stringResource(R.string.save) else stringResource(R.string.add_customer),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}