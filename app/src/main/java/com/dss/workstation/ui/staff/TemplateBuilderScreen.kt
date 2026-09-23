package com.dss.workstation.ui.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.data.model.TaskTemplateWithDetails
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*

@Composable
fun TemplateBuilderScreen(
    templates: List<TaskTemplateWithDetails>,
    editingTemplate: TaskTemplateWithDetails?,
    onStartNew: () -> Unit,
    onEdit: (TaskTemplateWithDetails) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSaveTemplate: (
        title: String,
        description: String,
        estimatedMinutes: Int,
        helpPhrase: String,
        completionPhrase: String,
        steps: List<com.dss.workstation.data.model.TaskStepEntity>,
        checklist: List<com.dss.workstation.data.model.ChecklistItemEntity>
    ) -> Unit,
    onCancelEditing: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (editingTemplate != null) {
        TemplateEditorScreen(
            templateWithDetails = editingTemplate,
            onSave = onSaveTemplate,
            onCancel = onCancelEditing,
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TASK TEMPLATES",
                        style = DssTypography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = DssTextPrimary
                        )
                    )
                    Text(
                        text = "Build, customize, duplicate, and manage workstation instruction templates",
                        style = DssTypography.bodyMedium.copy(fontSize = 18.sp)
                    )
                }

                DssBigButton(
                    text = "+ CREATE NEW TEMPLATE",
                    onClick = onStartNew,
                    variant = DssButtonVariant.PRIMARY,
                    minHeight = 64.dp,
                    iconResId = R.drawable.ic_add,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(templates) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.template.title,
                                    style = DssTypography.titleLarge.copy(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${item.sortedSteps.size} steps • ${item.sortedChecklistItems.size} checklist items • Est. ${item.template.estimatedMinutes} mins",
                                    style = DssTypography.bodyMedium.copy(fontSize = 16.sp, color = DssTextSecondary)
                                )
                                if (item.template.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.template.description,
                                        style = DssTypography.bodyMedium.copy(fontSize = 16.sp, color = DssTextPrimary)
                                    )
                                }
                            }

                            // Action buttons: Edit, Duplicate, Delete
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onEdit(item) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = DssPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("EDIT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Button(
                                    onClick = { onDuplicate(item.template.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = DssTextPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("COPY", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Button(
                                    onClick = { onDelete(item.template.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = DssDanger),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("DELETE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
