package com.dss.workstation.ui.learner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.data.model.ChecklistItemEntity
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.components.DssCheckboxCard
import com.dss.workstation.ui.theme.*

@Composable
fun CheckScreen(
    checklistItems: List<ChecklistItemEntity>,
    checkedItemIds: Set<String>,
    onToggleItem: (String) -> Unit,
    onContinueToDone: () -> Unit,
    onBackToSteps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allChecked = checklistItems.isNotEmpty() && checklistItems.all { checkedItemIds.contains(it.id) }
    val checkedCount = checklistItems.count { checkedItemIds.contains(it.id) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DssBackground)
            .padding(horizontal = 40.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CHECK YOUR WORK",
                    style = DssTypography.headlineLarge.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = DssTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap each box once you have verified it on your parcel",
                    style = DssTypography.bodyMedium.copy(
                        fontSize = 22.sp,
                        color = DssTextSecondary
                    )
                )
            }

            // Counter Badge
            Box(
                modifier = Modifier
                    .background(
                        if (allChecked) DssSuccessContainer else Color(0xFFEFF6FF),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "$checkedCount of ${checklistItems.size} Verified",
                    style = DssTypography.labelLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allChecked) Color(0xFF14532D) else DssPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checklist items in oversized touchable cards
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(checklistItems) { item ->
                val isChecked = checkedItemIds.contains(item.id)
                DssCheckboxCard(
                    text = item.checkPrompt,
                    checked = isChecked,
                    onCheckedChange = { onToggleItem(item.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DssBigButton(
                text = "REVIEW STEPS",
                onClick = onBackToSteps,
                variant = DssButtonVariant.SECONDARY,
                minHeight = 84.dp,
                modifier = Modifier.weight(0.35f),
                iconResId = R.drawable.ic_arrow_back,
                fontSize = 22.sp
            )

            // Button strictly disabled until 100% of items are checked
            DssBigButton(
                text = if (allChecked) "CONTINUE TO DONE" else "CHECK ALL ITEMS TO PROCEED",
                onClick = onContinueToDone,
                variant = if (allChecked) DssButtonVariant.SUCCESS else DssButtonVariant.SECONDARY,
                enabled = allChecked,
                minHeight = 84.dp,
                modifier = Modifier.weight(0.65f),
                iconResId = if (allChecked) R.drawable.ic_check else null,
                fontSize = 26.sp
            )
        }
    }
}
