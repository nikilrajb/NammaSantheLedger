package com.nammasanthe.ledger.ui.navigation

/**
 * Screen — sealed class defining every navigation destination in the app.
 * Using a sealed class prevents typos in route strings.
 *
 * How navigation works:
 *   navController.navigate(Screen.Ledger.createRoute(customerId = 42L))
 */
sealed class Screen(val route: String) {

    // ── Static screens (no parameters) ────────────────────────────────
    object Home           : Screen("home")
    object CustomerList   : Screen("customer_list")
    object AddCustomer    : Screen("add_customer")
    object DailySummary   : Screen("daily_summary")

    // ── Screens with parameters ────────────────────────────────────────
    object Ledger : Screen("ledger/{customerId}") {
        fun createRoute(customerId: Long) = "ledger/$customerId"
    }

    object EditCustomer : Screen("edit_customer/{customerId}") {
        fun createRoute(customerId: Long) = "edit_customer/$customerId"
    }

    object AddTransaction : Screen("add_transaction/{customerId}/{transactionId}") {
        // Use transactionId = -1L for new transaction, or actual ID to edit
        fun createRoute(customerId: Long, transactionId: Long = -1L) =
            "add_transaction/$customerId/$transactionId"
    }
}