package com.nammasanthe.ledger.ui.screens.transaction

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
import com.nammasanthe.ledger.ui.components.TransactionTypeToggle
import com.nammasanthe.ledger.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    customerId    : Long,
    transactionId : Long?,   // null = new, non-null = edit
    onBack        : () -> Unit,
    viewModel     : TransactionViewModel = hiltViewModel()
) {
    val state    by viewModel.formState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val isEdit   = transactionId != null

    LaunchedEffect(customerId, transactionId) {
        viewModel.init(customerId, transactionId)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is TxFormEvent.SaveSuccess -> onBack()
                is TxFormEvent.Error       -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEdit) stringResource(R.string.edit_transaction) else stringResource(R.string.add_entry),
                        color = SurfaceWhite,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Saffron700)
            )
        },
        snackbarHost   = { SnackbarHost(snackbar) },
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

            // ── Type Toggle ────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.transaction_type), 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TransactionTypeToggle(
                    selected = state.type,
                    onSelect = viewModel::onTypeChange
                )
            }

            // ── Amount Field ───────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.amount),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value  = state.amountText,
                    onValueChange = viewModel::onAmountChange,
                    label  = { Text(stringResource(R.string.amount)) },
                    placeholder = { Text(stringResource(R.string.placeholder_amount)) },
                    leadingIcon = { 
                        Text(
                            text = stringResource(R.string.placeholder_amount).let { "₹ $it" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Saffron700,
                            modifier = Modifier.padding(start = 12.dp)
                        ) 
                    },
                    isError     = state.amountError != null,
                    supportingText = { 
                        if (state.amountError != null) {
                            Text(stringResource(R.string.error_amount_invalid), color = PaymentRed) 
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine  = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Saffron700,
                        unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    modifier    = Modifier.fillMaxWidth()
                )
            }

            // ── Note Field ─────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.note_optional),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value  = state.note,
                    onValueChange = viewModel::onNoteChange,
                    label  = { Text(stringResource(R.string.note_optional)) },
                    placeholder = { Text(stringResource(R.string.placeholder_note)) },
                    leadingIcon = { Icon(Icons.Default.Note, null, tint = Saffron700) },
                    isError     = state.noteError != null,
                    supportingText = { 
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                stringResource(R.string.character_limit, state.note.length),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.noteError != null) PaymentRed else TextSecondary
                            )
                        }
                    },
                    singleLine  = false,
                    maxLines    = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Saffron700,
                        unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    modifier    = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Save Button ────────────────────────────────────────────
            Button(
                onClick  = { viewModel.save(existingTxId = transactionId) },
                enabled  = !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Saffron700)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = SurfaceWhite, strokeWidth = 3.dp)
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}