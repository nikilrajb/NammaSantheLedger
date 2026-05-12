package com.nammasanthe.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.local.entity.TransactionType
import com.nammasanthe.ledger.ui.theme.*
import com.nammasanthe.ledger.utils.FormatUtils

// ─────────────────────────────────────────────────────────────────────────
// SummaryCard — the headline card shown on Home and Summary screens
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun SummaryCard(
    label      : String,
    amount     : Double,
    amountColor: Color    = TextPrimary,
    modifier   : Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(16.dp) // Increased corner radius for professional feel
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text       = FormatUtils.formatCurrency(amount),
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold, // Bolder for emphasis
                color      = amountColor
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// CustomerAvatar — circle with initials (when no photo)
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun CustomerAvatar(name: String, size: Int = 44) {
    val initials = name.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }
        
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Saffron500),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initials,
            color      = SurfaceWhite,
            fontWeight = FontWeight.Bold,
            fontSize   = (size * 0.36f).sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// BalanceChip — coloured chip showing outstanding balance
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun BalanceChip(amount: Double, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when {
        amount > 0.0  -> CreditLight to CreditGreen
        amount < 0.0  -> PaymentLight to PaymentRed
        else          -> Color(0xFFF5F5F5) to NeutralGray
    }
    val label = when {
        amount > 0.0 -> stringResource(R.string.owes_label, FormatUtils.formatCurrency(amount))
        amount < 0.0 -> stringResource(R.string.advance_label, FormatUtils.formatCurrency(-amount))
        else         -> stringResource(R.string.settled)
    }
    Surface(
        modifier  = modifier,
        shape     = RoundedCornerShape(50),
        color     = bgColor
    ) {
        Text(
            text     = label,
            color    = textColor,
            style    = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// TransactionTypeToggle — CREDIT / PAYMENT toggle bar
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun TransactionTypeToggle(
    selected : TransactionType,
    onSelect : (TransactionType) -> Unit,
    modifier : Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F0F0)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TransactionType.entries.forEach { type ->
            val isSelected = selected == type
            val bgColor    = if (isSelected) when(type) {
                TransactionType.CREDIT  -> CreditGreen
                TransactionType.PAYMENT -> PaymentRed
            } else Color.Transparent
            val textColor = if (isSelected) SurfaceWhite else TextSecondary
            val labelRes = if (type == TransactionType.CREDIT) R.string.udari_credit else R.string.payment_received
            Surface(
                onClick    = { onSelect(type) },
                modifier   = Modifier.weight(1f).padding(4.dp),
                shape      = RoundedCornerShape(10.dp),
                color      = bgColor
            ) {
                Text(
                    text       = stringResource(labelRes),
                    color      = textColor,
                    fontWeight = FontWeight.SemiBold,
                    style      = MaterialTheme.typography.bodyMedium,
                    modifier   = Modifier.padding(vertical = 12.dp).wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// EmptyState — shown when a list has no items
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null,
            modifier = Modifier.size(80.dp), tint = DividerColor.copy(alpha = 0.5f))
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium,
            color = NeutralGray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

// ─────────────────────────────────────────────────────────────────────────
// ConfirmDeleteDialog — reusable delete confirmation
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun ConfirmDeleteDialog(
    title   : String,
    message : String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(title, fontWeight = FontWeight.Bold) },
        text    = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = PaymentRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}