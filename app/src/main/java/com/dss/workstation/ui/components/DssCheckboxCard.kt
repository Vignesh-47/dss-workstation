package com.dss.workstation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.ui.theme.*

@Composable
fun DssCheckboxCard(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (checked) Color(0xFFF0FDF4) else Color.White
    val borderColor = if (checked) DssSuccess else Color(0xFFCBD5E1)
    val borderWidth = if (checked) 3.dp else 2.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = DssDimens.ChecklistCardMinHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = DssPrimary)
            ) {
                onCheckedChange(!checked)
            },
        shape = RoundedCornerShape(DssDimens.CardCornerRadius),
        border = BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (checked) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Oversized accessible custom checkbox
            Box(
                modifier = Modifier
                    .size(DssDimens.CheckboxSize)
                    .background(
                        color = if (checked) DssSuccess else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 3.dp,
                        color = if (checked) DssSuccess else Color(0xFF64748B),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Checked",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = text,
                style = DssTypography.bodyLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (checked) Color(0xFF14532D) else DssTextPrimary
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
