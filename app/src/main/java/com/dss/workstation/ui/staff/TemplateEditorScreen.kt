package com.dss.workstation.ui.staff

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dss.workstation.R
import com.dss.workstation.data.model.ChecklistItemEntity
import com.dss.workstation.data.model.TaskStepEntity
import com.dss.workstation.data.model.TaskTemplateWithDetails
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*
import com.dss.workstation.util.PhotoStorageManager
import java.io.File
import java.util.UUID

@Composable
fun TemplateEditorScreen(
    templateWithDetails: TaskTemplateWithDetails,
    onSave: (
        title: String,
        description: String,
        estimatedMinutes: Int,
        helpPhrase: String,
        completionPhrase: String,
        steps: List<TaskStepEntity>,
        checklist: List<ChecklistItemEntity>
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoStorage = remember { PhotoStorageManager(context) }

    var title by remember { mutableStateOf(templateWithDetails.template.title) }
    var description by remember { mutableStateOf(templateWithDetails.template.description) }
    var estimatedMinutes by remember { mutableStateOf(templateWithDetails.template.estimatedMinutes.toString()) }
    var helpPhrase by remember { mutableStateOf(templateWithDetails.template.helpPhrase) }
    var completionPhrase by remember { mutableStateOf(templateWithDetails.template.completionPhrase) }

    var steps by remember { mutableStateOf(templateWithDetails.sortedSteps) }
    var checklist by remember { mutableStateOf(templateWithDetails.sortedChecklistItems) }

    // Active step being photographed
    var activePhotoStepIndex by remember { mutableStateOf<Int?>(null) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var cameraPhotoFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && activePhotoStepIndex != null && cameraPhotoFile != null) {
            val relativePath = "task_photos/${cameraPhotoFile?.name}"
            val updated = steps.toMutableList()
            updated[activePhotoStepIndex!!] = updated[activePhotoStepIndex!!].copy(imagePath = relativePath)
            steps = updated
        }
        activePhotoStepIndex = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && activePhotoStepIndex != null) {
            val savedPath = photoStorage.copyUriToInternalStorage(uri)
            if (savedPath != null) {
                val updated = steps.toMutableList()
                updated[activePhotoStepIndex!!] = updated[activePhotoStepIndex!!].copy(imagePath = savedPath)
                steps = updated
            }
        }
        activePhotoStepIndex = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TEMPLATE BUILDER & EDITOR",
                style = DssTypography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = DssTextPrimary
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DssBigButton(
                    text = "CANCEL",
                    onClick = onCancel,
                    variant = DssButtonVariant.SECONDARY,
                    minHeight = 56.dp,
                    fontSize = 18.sp
                )
                DssBigButton(
                    text = "SAVE TEMPLATE",
                    onClick = {
                        val mins = estimatedMinutes.toIntOrNull() ?: 10
                        onSave(title, description, mins, helpPhrase, completionPhrase, steps, checklist)
                    },
                    variant = DssButtonVariant.SUCCESS,
                    minHeight = 56.dp,
                    iconResId = R.drawable.ic_check,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // General Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color(0xFFCBD5E1))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "General Details",
                            style = DssTypography.titleMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Task Title") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Short Description") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = estimatedMinutes,
                                onValueChange = { estimatedMinutes = it },
                                label = { Text("Est. Minutes") },
                                modifier = Modifier.weight(0.3f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                            )
                            OutlinedTextField(
                                value = helpPhrase,
                                onValueChange = { helpPhrase = it },
                                label = { Text("Learner Help Phrase") },
                                modifier = Modifier.weight(0.7f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                            )
                        }

                        OutlinedTextField(
                            value = completionPhrase,
                            onValueChange = { completionPhrase = it },
                            label = { Text("Completion Handover Phrase") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                        )
                    }
                }
            }

            // Steps Editor Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color(0xFFCBD5E1))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Task Steps (${steps.size})",
                                style = DssTypography.titleMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            )

                            Button(
                                onClick = {
                                    val newStep = TaskStepEntity(
                                        id = UUID.randomUUID().toString(),
                                        templateId = templateWithDetails.template.id,
                                        stepOrder = steps.size + 1,
                                        instruction = "New step instruction..."
                                    )
                                    steps = steps + newStep
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DssPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+ ADD STEP", fontWeight = FontWeight.Bold)
                            }
                        }

                        steps.forEachIndexed { index, step ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Step Index badge
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(DssPrimary, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }

                                    // Instruction TextField
                                    OutlinedTextField(
                                        value = step.instruction,
                                        onValueChange = { newInstruction ->
                                            val updated = steps.toMutableList()
                                            updated[index] = step.copy(instruction = newInstruction)
                                            steps = updated
                                        },
                                        label = { Text("Step Instruction Sentence") },
                                        modifier = Modifier.weight(1f),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                                    )

                                    // Photo Preview & Buttons
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val resId = photoStorage.resolveDrawableResId(step.imagePath)
                                            val file = if (step.imagePath != null) photoStorage.getFileFromRelativePath(step.imagePath) else null

                                            if (resId != null) {
                                                Image(
                                                    painter = painterResource(id = resId),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else if (file != null) {
                                                AsyncImage(
                                                    model = file,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text("No Photo", fontSize = 12.sp, color = DssTextSecondary)
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Camera Capture Button
                                            Button(
                                                onClick = {
                                                    activePhotoStepIndex = index
                                                    val (uri, file) = photoStorage.createTempCameraPhotoUri()
                                                    cameraPhotoUri = uri
                                                    cameraPhotoFile = file
                                                    cameraLauncher.launch(uri)
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = DssPrimary)
                                            ) {
                                                Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Gallery Pick Button
                                            Button(
                                                onClick = {
                                                    activePhotoStepIndex = index
                                                    galleryLauncher.launch("image/*")
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = DssPrimary)
                                            ) {
                                                Text("Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Delete step
                                    IconButton(
                                        onClick = {
                                            val updated = steps.toMutableList()
                                            updated.removeAt(index)
                                            steps = updated.mapIndexed { idx, s -> s.copy(stepOrder = idx + 1) }
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_delete),
                                            contentDescription = "Delete Step",
                                            tint = DssDanger
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Checklist Editor Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color(0xFFCBD5E1))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verification Checklist (${checklist.size})",
                                style = DssTypography.titleMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            )

                            Button(
                                onClick = {
                                    val newItem = ChecklistItemEntity(
                                        id = UUID.randomUUID().toString(),
                                        templateId = templateWithDetails.template.id,
                                        itemOrder = checklist.size + 1,
                                        checkPrompt = "Item is verified"
                                    )
                                    checklist = checklist + newItem
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DssSuccess),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+ ADD CHECKPOINT", fontWeight = FontWeight.Bold)
                            }
                        }

                        checklist.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = item.checkPrompt,
                                    onValueChange = { newPrompt ->
                                        val updated = checklist.toMutableList()
                                        updated[index] = item.copy(checkPrompt = newPrompt)
                                        checklist = updated
                                    },
                                    label = { Text("Checklist Item #${index + 1}") },
                                    modifier = Modifier.weight(1f),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                                )

                                IconButton(
                                    onClick = {
                                        val updated = checklist.toMutableList()
                                        updated.removeAt(index)
                                        checklist = updated.mapIndexed { idx, itm -> itm.copy(itemOrder = idx + 1) }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_delete),
                                        contentDescription = "Delete checklist item",
                                        tint = DssDanger
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
