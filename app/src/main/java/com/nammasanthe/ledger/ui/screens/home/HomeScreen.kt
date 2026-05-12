package com.nammasanthe.ledger.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.ui.components.BalanceChip
import com.nammasanthe.ledger.ui.components.CustomerAvatar
import com.nammasanthe.ledger.ui.components.EmptyState
import com.nammasanthe.ledger.ui.theme.*
import com.nammasanthe.ledger.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCustomerList: () -> Unit,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToLedger: (Long) -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToAddTx: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLanguageSheet by remember { mutableStateOf(false) }

    if (showLanguageSheet) {
        LanguageSelectionSheet(
            currentLanguage = state.languageCode,
            onLanguageSelected = { code ->
                viewModel.changeLanguage(code)
                showLanguageSheet = false
            },
            onDismiss = { showLanguageSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = SurfaceWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            style = MaterialTheme.typography.labelMedium,
                            color = SurfaceWhite.copy(alpha = 0.82f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLanguageSheet = true }) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = stringResource(R.string.change_language),
                            tint = SurfaceWhite
                        )
                    }
                    IconButton(onClick = onNavigateToSummary) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = stringResource(R.string.nav_summary),
                            tint = SurfaceWhite
                        )
                    }
                    IconButton(onClick = onNavigateToCustomerList) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = stringResource(R.string.nav_customers),
                            tint = SurfaceWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Saffron700)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustomer,
                containerColor = Saffron700,
                contentColor = SurfaceWhite,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.add_customer))
            }
        },
        containerColor = ScaffoldBg
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Saffron700)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DashboardHeroCard(
                        totalOutstanding = state.totalOutstanding,
                        customerCount = state.customers.size,
                        currentLanguage = state.languageCode
                    )
                }

                item {
                    SectionHeader(title = stringResource(R.string.dashboard_overview))
                }

                item {
                    DashboardMetricsGrid(
                        totalOutstanding = state.totalOutstanding,
                        todayCredits = state.todayCredits,
                        todayPayments = state.todayPayments,
                        customerCount = state.customers.size
                    )
                }

                item {
                    SectionHeader(title = stringResource(R.string.dashboard_quick_actions))
                }

                item {
                    QuickActionsCard(
                        currentLanguage = state.languageCode,
                        onAddCustomer = onNavigateToAddCustomer,
                        onSummary = onNavigateToSummary,
                        onLanguage = { showLanguageSheet = true }
                    )
                }

                item {
                    SectionHeader(
                        title = stringResource(R.string.nav_customers),
                        trailing = stringResource(R.string.customer_count, state.customers.size)
                    )
                }

                if (state.customers.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.People,
                            title = stringResource(R.string.dashboard_no_customers),
                            subtitle = stringResource(R.string.dashboard_no_customers_hint)
                        )
                    }
                }

                val sortedCustomers = state.customers.sortedByDescending { state.balanceMap[it.id] ?: 0.0 }
                items(sortedCustomers, key = { it.id }) { customer ->
                    val balance = state.balanceMap[customer.id] ?: 0.0
                    CustomerHomeRow(
                        customer = customer,
                        balance = balance,
                        onClick = { onNavigateToLedger(customer.id) },
                        onAddTx = { onNavigateToAddTx(customer.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelectionSheet(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.dashboard_current_language, languageLabel(currentLanguage)),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            LanguageOption(
                label = stringResource(R.string.english),
                isSelected = currentLanguage == "en",
                onClick = { onLanguageSelected("en") }
            )
            LanguageOption(
                label = stringResource(R.string.kannada),
                isSelected = currentLanguage == "kn",
                onClick = { onLanguageSelected("kn") }
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Saffron50 else SurfaceWhite,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun DashboardHeroCard(
    totalOutstanding: Double,
    customerCount: Int,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dashboard_overview),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.app_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Surface(
                    color = Saffron50,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = languageLabel(currentLanguage),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Saffron700,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroStat(
                    label = stringResource(R.string.total_outstanding),
                    value = FormatUtils.formatCurrency(totalOutstanding),
                    accent = if (totalOutstanding > 0) OutstandingRed else BalanceGreen,
                    modifier = Modifier.weight(1f)
                )
                HeroStat(
                    label = stringResource(R.string.dashboard_customers),
                    value = stringResource(R.string.customer_count, customerCount),
                    accent = Saffron700,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun DashboardMetricsGrid(
    totalOutstanding: Double,
    todayCredits: Double,
    todayPayments: Double,
    customerCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardMetricCard(
                title = stringResource(R.string.total_outstanding),
                value = FormatUtils.formatCurrency(totalOutstanding),
                accent = if (totalOutstanding > 0) OutstandingRed else BalanceGreen,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                title = stringResource(R.string.udari_credit),
                value = FormatUtils.formatCurrency(todayCredits),
                accent = CreditGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardMetricCard(
                title = stringResource(R.string.payment_received),
                value = FormatUtils.formatCurrency(todayPayments),
                accent = PaymentRed,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                title = stringResource(R.string.dashboard_customers),
                value = stringResource(R.string.customer_count, customerCount),
                accent = Saffron700,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        }
    }
}

@Composable
private fun QuickActionsCard(
    currentLanguage: String,
    onAddCustomer: () -> Unit,
    onSummary: () -> Unit,
    onLanguage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionRow(
                icon = Icons.Default.PersonAdd,
                title = stringResource(R.string.add_customer),
                subtitle = stringResource(R.string.dashboard_no_customers_hint),
                accent = Saffron700,
                onClick = onAddCustomer
            )
            ActionRow(
                icon = Icons.Default.BarChart,
                title = stringResource(R.string.nav_summary),
                subtitle = stringResource(R.string.dashboard_overview),
                accent = CreditGreen,
                onClick = onSummary
            )
            ActionRow(
                icon = Icons.Default.Language,
                title = stringResource(R.string.change_language),
                subtitle = languageLabel(currentLanguage),
                accent = TextSecondary,
                onClick = onLanguage
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            supportingContent = {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            },
            leadingContent = {
                Icon(icon, contentDescription = null, tint = accent)
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailing: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

private fun languageLabel(code: String): String = when (code) {
    "kn" -> "ಕನ್ನಡ"
    else -> "English"
}

@Composable
private fun CustomerHomeRow(
    customer: com.nammasanthe.ledger.data.local.entity.Customer,
    balance: Double,
    onClick: () -> Unit,
    onAddTx: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CustomerAvatar(name = customer.name, size = 48)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                BalanceChip(amount = balance)
            }
            IconButton(onClick = onAddTx) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = stringResource(R.string.add_entry),
                    tint = Saffron700,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
