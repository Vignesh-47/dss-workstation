package com.dss.workstation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.ui.learner.LearnerStage
import com.dss.workstation.ui.theme.*

@Composable
fun DssTopBar(
    currentStage: LearnerStage,
    onStageClick: ((LearnerStage) -> Unit)? = null,
    onStaffClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Station Brand Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(DssPrimary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DSS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PACKING STATION",
                        style = DssTypography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DssTextPrimary
                        )
                    )
                }
            }

            // Center: Stage Pills (TODAY -> NOW -> HOW -> CHECK -> DONE -> NEXT)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LearnerStage.values().forEach { stage ->
                    val isActive = stage == currentStage
                    val isCompleted = stage.ordinal < currentStage.ordinal

                    val pillBackground = when {
                        isActive -> DssPrimary
                        isCompleted -> Color(0xFFDCFCE7) // pale green
                        else -> Color(0xFFF1F5F9) // neutral light grey
                    }
                    val pillTextColor = when {
                        isActive -> Color.White
                        isCompleted -> Color(0xFF15803D)
                        else -> Color(0xFF64748B)
                    }
                    val pillBorder = when {
                        isActive -> DssPrimaryVariant
                        isCompleted -> Color(0xFF86EFAC)
                        else -> Color(0xFFE2E8F0)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(pillBackground)
                            .border(2.dp, pillBorder, RoundedCornerShape(24.dp))
                            .clickable(enabled = onStageClick != null) {
                                onStageClick?.invoke(stage)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stage.name,
                            style = DssTypography.labelLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                color = pillTextColor
                            )
                        )
                    }
                }
            }

            // Right: Staff Gear Button (min 72dp touch target)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                    .clickable { onStaffClick() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Staff Settings",
                        tint = DssTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STAFF",
                        style = DssTypography.labelLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DssTextPrimary
                        )
                    )
                }
            }
        }
    }
}
