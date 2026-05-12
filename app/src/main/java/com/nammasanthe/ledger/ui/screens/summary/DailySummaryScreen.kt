package com.nammasanthe.ledger.ui.screens.summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.local.entity.TransactionType
import com.nammasanthe.ledger.ui.components.EmptyState
import com.nammasanthe.ledger.ui.components.SummaryCard
import com.nammasanthe.ledger.ui.theme.*
import com.nammasanthe.ledger.utils.DateUtils
import com.nammasanthe.ledger.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryScreen(
    onBack    : () -> Unit,
    viewModel : SummaryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_summary), color = SurfaceWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Saffron700)
            )
        },
        containerColor = ScaffoldBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Date Navigator ─────────────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceWhite,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = viewModel::goToPreviousDay) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.previous_day), tint = Saffron700)
                        }
                        Text(
                            text  = DateUtils.formatRelativeDate(state.selectedDateMs),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold, color = TextPrimary
                        )
                        val canGoNext = state.selectedDateMs + 86_400_000L <= System.currentTimeMillis()
                        IconButton(onClick = viewModel::goToNextDay, enabled = canGoNext) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.next_day),
                                tint = if (canGoNext) Saffron700 else DividerColor)
                        }
                    }
                }
            }

            // ── Summary Cards ──────────────────────────────────────────
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        label = stringResource(R.string.udari_credit), 
                        amount = state.totalCredits, 
                        amountColor = CreditGreen, 
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label = stringResource(R.string.payment_received), 
                        amount = state.totalPayments, 
                        amountColor = PaymentRed, 
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape    = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.net_balance), style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary)
                            Text(
                                FormatUtils.formatCurrency(state.netFlow),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (state.netFlow >= 0) OutstandingRed else BalanceGreen
                            )
                        }
                        Text(
                            if (state.netFlow >= 0) stringResource(R.string.net_outflow) else stringResource(R.string.net_inflow), 
                            style = MaterialTheme.typography.bodySmall, 
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Transaction List ───────────────────────────────────────
            item {
                Text(
                    text = stringResource(R.string.transaction_history) + " (${state.transactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = TextPrimary
                )
            }

            if (state.transactions.isEmpty()) {
                item {
                    EmptyState(
                        icon     = Icons.Default.EventBusy,
                        title    = stringResource(R.string.no_transactions_yet),
                        subtitle = stringResource(R.string.try_different_date)
                    )
                }
            }

            items(state.transactions) { tx ->
                val isCredit = tx.type == TransactionType.CREDIT
                val amtColor = if (isCredit) CreditGreen else PaymentRed
                val bgColor = if(isCredit) CreditLight.copy(alpha = 0.5f) else PaymentLight.copy(alpha = 0.5f)
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    colors    = CardDefaults.cardColors(containerColor = bgColor),
                    shape     = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = amtColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(44.dp)
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
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(if (isCredit) stringResource(R.string.udari_credit) else stringResource(R.string.payment_received),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = amtColor)
                            Text(DateUtils.formatTime(tx.timestamp),
                                style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            tx.note?.let { 
                                Text(it, style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary.copy(alpha = 0.8f),
                                    maxLines = 1)
                            }
                        }
                        Text(
                            FormatUtils.formatCurrency(tx.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = amtColor
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}