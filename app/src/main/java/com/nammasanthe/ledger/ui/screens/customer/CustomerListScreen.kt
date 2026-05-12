package com.nammasanthe.ledger.ui.screens.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.nammasanthe.ledger.ui.components.*
import com.nammasanthe.ledger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onBack             : () -> Unit,
    onNavigateToLedger : (Long) -> Unit,
    onAddCustomer      : () -> Unit,
    viewModel          : CustomerViewModel = hiltViewModel()
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val languageCode by viewModel.languageCode.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_customers), color = SurfaceWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Saffron700)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCustomer,
                containerColor = Saffron700,
                contentColor = SurfaceWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.add_customer))
            }
        },
        containerColor = ScaffoldBg
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ── Search Bar ─────────────────────────────────────────────
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { q ->
                    searchQuery = q
                    viewModel.onSearchQueryChange(q)
                },
                placeholder   = { Text(stringResource(R.string.customer_name) + "...") },
                leadingIcon   = { Icon(Icons.Default.Search, null, tint = Saffron700) },
                trailingIcon  = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron700,
                    unfocusedBorderColor = DividerColor,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                ),
                modifier      = Modifier.fillMaxWidth().padding(16.dp)
            )

            // ── List ───────────────────────────────────────────────────
            if (customers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon     = Icons.Default.SearchOff,
                        title    = if (searchQuery.isEmpty()) stringResource(R.string.dashboard_no_customers) else stringResource(R.string.dashboard_no_results),
                        subtitle = if (searchQuery.isEmpty())
                            stringResource(R.string.dashboard_no_customers_hint)
                        else
                            stringResource(R.string.dashboard_no_results_hint)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        val displayName by com.nammasanthe.ledger.utils.rememberTranslatedText(customer.name, languageCode)
                        Card(
                            modifier  = Modifier.fillMaxWidth().clickable { onNavigateToLedger(customer.id) },
                            colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape     = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CustomerAvatar(name = displayName, size = 52)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary)
                                    Text(stringResource(R.string.phone_prefix) + customer.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary)
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = DividerColor)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}