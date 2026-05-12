package com.nammasanthe.ledger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nammasanthe.ledger.ui.screens.customer.AddEditCustomerScreen
import com.nammasanthe.ledger.ui.screens.customer.CustomerListScreen
import com.nammasanthe.ledger.ui.screens.home.HomeScreen
import com.nammasanthe.ledger.ui.screens.ledger.LedgerScreen
import com.nammasanthe.ledger.ui.screens.summary.DailySummaryScreen
import com.nammasanthe.ledger.ui.screens.transaction.AddTransactionScreen

/**
 * LedgerNavGraph — the complete navigation graph for the app.
 * Every screen is declared here as a composable() destination.
 * Passes navController actions as lambdas (not the controller itself)
 * so screens don't depend on navigation directly.
 */
@Composable
fun LedgerNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController  = navController,
        startDestination = Screen.Home.route
    ) {

        // ── Home Dashboard ─────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCustomerList = { navController.navigate(Screen.CustomerList.route) },
                onNavigateToAddCustomer  = { navController.navigate(Screen.AddCustomer.route) },
                onNavigateToLedger       = { id -> navController.navigate(Screen.Ledger.createRoute(id)) },
                onNavigateToSummary      = { navController.navigate(Screen.DailySummary.route) },
                onNavigateToAddTx        = { cid -> navController.navigate(Screen.AddTransaction.createRoute(cid)) }
            )
        }

        // ── Customer List ──────────────────────────────────────────────
        composable(Screen.CustomerList.route) {
            CustomerListScreen(
                onBack             = { navController.popBackStack() },
                onNavigateToLedger = { id -> navController.navigate(Screen.Ledger.createRoute(id)) },
                onAddCustomer      = { navController.navigate(Screen.AddCustomer.route) }
            )
        }

        // ── Add Customer ───────────────────────────────────────────────
        composable(Screen.AddCustomer.route) {
            AddEditCustomerScreen(
                customerId = null,
                onBack     = { navController.popBackStack() }
            )
        }

        // ── Edit Customer (receives customerId) ────────────────────────
        composable(
            route     = Screen.EditCustomer.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStack ->
            val customerId = backStack.arguments?.getLong("customerId") ?: return@composable
            AddEditCustomerScreen(
                customerId = customerId,
                onBack     = { navController.popBackStack() }
            )
        }

        // ── Customer Ledger ────────────────────────────────────────────
        composable(
            route     = Screen.Ledger.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStack ->
            val customerId = backStack.arguments?.getLong("customerId") ?: return@composable
            LedgerScreen(
                customerId     = customerId,
                onBack         = { navController.popBackStack() },
                onEditCustomer = { navController.navigate(Screen.EditCustomer.createRoute(customerId)) },
                onAddTx        = { navController.navigate(Screen.AddTransaction.createRoute(customerId)) }
            )
        }

        // ── Add / Edit Transaction ─────────────────────────────────────
        composable(
            route     = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("customerId")    { type = NavType.LongType },
                navArgument("transactionId") { type = NavType.LongType }
            )
        ) { backStack ->
            val customerId    = backStack.arguments?.getLong("customerId")    ?: return@composable
            val transactionId = backStack.arguments?.getLong("transactionId") ?: -1L
            AddTransactionScreen(
                customerId    = customerId,
                transactionId = if (transactionId == -1L) null else transactionId,
                onBack        = { navController.popBackStack() }
            )
        }

        // ── Daily Summary ──────────────────────────────────────────────
        composable(Screen.DailySummary.route) {
            DailySummaryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}