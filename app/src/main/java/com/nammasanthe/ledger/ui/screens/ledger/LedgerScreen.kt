package com.nammasanthe.ledger.ui.screens.ledger

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.local.entity.Transaction
import com.nammasanthe.ledger.data.local.entity.TransactionType
import com.nammasanthe.ledger.ui.components.*
import com.nammasanthe.ledger.ui.theme.*
import com.nammasanthe.ledger.utils.DateUtils
import com.nammasanthe.ledger.utils.FormatUtils
import com.nammasanthe.ledger.utils.WhatsAppHelper
import com.nammasanthe.ledger.utils.rememberTranslatedText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    customerId     : Long,
    onBack         : () -> Unit,
    onEditCustomer : () -> Unit,
    onAddTx        : () -> Unit,
    viewModel      : LedgerViewModel = hiltViewModel()
) {
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    val context  = LocalContext.current
    var txToDelete by remember { mutableStateOf<Transaction?>(null) }
    val translatedTitle by rememberTranslatedText(state.customer?.name.orEmpty(), state.languageCode)

    LaunchedEffect(customerId) { viewModel.loadCustomer(customerId) }

    txToDelete?.let { tx ->
        ConfirmDeleteDialog(
            title    = stringResource(R.string.delete),
            message  = "${stringResource(R.string.delete)} ${if (tx.type == TransactionType.CREDIT) stringResource(R.string.udari_credit) else stringResource(R.string.payment_received)} of ${FormatUtils.formatCurrency(tx.amount)}?",
            onConfirm = { viewModel.deleteTransaction(tx); txToDelete = null },
            onDismiss = { txToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.customer == null) stringResource(R.string.nav_home) else translatedTitle,
                        color = SurfaceWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = SurfaceWhite)
                    }
                },
                actions = {
                    IconButton(onClick = onEditCustomer) {
                        Icon(Icons.Default.Edit, stringResource(R.string.edit_customer), tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Saffron700)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTx,
                icon    = { Icon(Icons.Default.Add, null) },
                text    = { Text(stringResource(R.string.add_entry)) },
                containerColor = Saffron700, contentColor = SurfaceWhite
            )
        },
        containerColor = ScaffoldBg
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Saffron700)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Customer Header Card ───────────────────────────────
                state.customer?.let {
                    item {
                        CustomerHeaderCard(
                            customer = it,
                            balance  = state.netBalance,
                            currentLanguage = state.languageCode,
                            context  = context
                        )
                    }
                }

                // ── Transactions List ──────────────────────────────────
                item {
                    SearchAndFilterBar(
                        searchQuery = state.searchQuery,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        selectedFilter = state.selectedFilter,
                        onFilterChange = { viewModel.updateFilter(it) },
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }

                item {
                    Text(stringResource(R.string.transaction_history),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )
                }

                if (state.transactions.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title    = stringResource(R.string.no_transactions_yet),
                            subtitle = stringResource(R.string.add_transaction_hint)
                        )
                    }
                } else if (state.filteredTransactions.isEmpty()) {
                    item {
                        EmptyState(
                            icon     = Icons.Default.SearchOff,
                            title    = stringResource(R.string.no_results_found),
                            subtitle = stringResource(R.string.try_different_search)
                        )
                    }
                } else {
                    items(state.filteredTransactions, key = { it.id }) { tx ->
                        TransactionRow(
                            transaction = tx,
                            languageCode = state.languageCode,
                            onDelete    = { txToDelete = tx }
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun CustomerHeaderCard(
    customer : com.nammasanthe.ledger.data.local.entity.Customer,
    balance  : Double,
    currentLanguage: String,
    context  : Context
) {
    // In a real app, vendorName could come from DataStore/Settings
    val vendorName = "Namma Santhe Vendor"
    val translatedName by rememberTranslatedText(customer.name, currentLanguage)
    val translatedVendorName by rememberTranslatedText(vendorName, currentLanguage)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomerAvatar(name = translatedName, size = 64)
                Column(modifier = Modifier.weight(1f)) {
                    Text(translatedName, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(stringResource(R.string.phone_prefix) + customer.phone,
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }

            HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.net_balance), style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary)
                    Text(
                        text  = FormatUtils.formatCurrency(balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            balance > 0.0 -> OutstandingRed
                            balance < 0.0 -> BalanceGreen
                            else          -> NeutralGray
                        }
                    )
                    Text(
                        text  = when {
                            balance > 0.0 -> stringResource(R.string.customer_owes)
                            balance < 0.0 -> stringResource(R.string.you_owe)
                            else          -> stringResource(R.string.all_settled)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                if (balance > 0.0) {
                    Button(
                        onClick = {
                            WhatsAppHelper.sendReminder(context, customer.copy(name = translatedName), balance, translatedVendorName)
                        },
                        colors  = ButtonDefaults.buttonColors(containerColor = CreditGreen),
                        shape   = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.remind), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction : Transaction,
    languageCode: String,
    onDelete    : () -> Unit
) {
    val isCredit = transaction.type == TransactionType.CREDIT
    val rowBg    = if (isCredit) CreditLight.copy(alpha = 0.5f) else PaymentLight.copy(alpha = 0.5f)
    val amtColor = if (isCredit) CreditGreen else PaymentRed
    val label    = if (isCredit) stringResource(R.string.udari_credit) else stringResource(R.string.payment_received)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = rowBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = amtColor.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCredit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = amtColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(label, style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = amtColor)
                    Text(FormatUtils.formatCurrency(transaction.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold, color = amtColor)
                }
                Spacer(Modifier.height(2.dp))
                Text(DateUtils.formatDateTime(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (!transaction.note.isNullOrBlank()) {
                    val translatedNote by rememberTranslatedText(transaction.note.orEmpty(), languageCode)
                    Text(translatedNote, style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.delete), tint = NeutralGray)
            }
        }
    }
}

@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search TextField
        TextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            placeholder = {
                Text(
                    stringResource(R.string.search_transactions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_transactions),
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ScaffoldBg,
                unfocusedContainerColor = ScaffoldBg,
                focusedIndicatorColor = Saffron700,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { /* trigger search */ })
        )

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == TransactionFilter.ALL,
                onClick = { onFilterChange(TransactionFilter.ALL) },
                label = { Text(stringResource(R.string.all_transactions)) }
            )

            FilterChip(
                selected = selectedFilter == TransactionFilter.CREDIT,
                onClick = { onFilterChange(TransactionFilter.CREDIT) },
                label = { Text(stringResource(R.string.filter_udari)) },
                leadingIcon = if (selectedFilter == TransactionFilter.CREDIT) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                } else null
            )

            FilterChip(
                selected = selectedFilter == TransactionFilter.PAYMENT,
                onClick = { onFilterChange(TransactionFilter.PAYMENT) },
                label = { Text(stringResource(R.string.filter_payment)) },
                leadingIcon = if (selectedFilter == TransactionFilter.PAYMENT) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }

        // Clear Filters Button (show only if filters are active)
        if (searchQuery.isNotBlank() || selectedFilter != TransactionFilter.ALL) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FilterAlt, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.clear_filters))
            }
        }
    }
}