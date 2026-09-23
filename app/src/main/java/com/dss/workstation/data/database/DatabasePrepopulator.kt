package com.dss.workstation.data.database

import com.dss.workstation.data.dao.ScheduleDao
import com.dss.workstation.data.dao.TaskDao
import com.dss.workstation.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabasePrepopulator {

    const val DEFAULT_TEMPLATE_ID = "template_pack_parcel_001"
    const val SORT_SUPPLIES_TEMPLATE_ID = "template_sort_supplies_002"
    const val CLEAN_AREA_TEMPLATE_ID = "template_clean_area_003"

    fun populateInitialData(taskDao: TaskDao, scheduleDao: ScheduleDao) {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if already populated
            val existing = taskDao.getTemplateWithDetailsSync(DEFAULT_TEMPLATE_ID)
            if (existing != null) return@launch

            // 1. Primary Seed Template: "Pack one parcel"
            val packParcelTemplate = TaskTemplateEntity(
                id = DEFAULT_TEMPLATE_ID,
                title = "Pack one parcel",
                description = "Packing workstation standard single-parcel assembly and labelling.",
                estimatedMinutes = 10,
                helpPhrase = "I need help with this step. Please assist me.",
                completionPhrase = "I have finished packing this parcel. Please check.",
                createdAt = System.currentTimeMillis()
            )

            val packParcelSteps = listOf(
                TaskStepEntity(
                    id = "step_001",
                    templateId = DEFAULT_TEMPLATE_ID,
                    stepOrder = 1,
                    instruction = "Take the assigned item, box and packing materials.",
                    imagePath = "res:drawable/step1_materials"
                ),
                TaskStepEntity(
                    id = "step_002",
                    templateId = DEFAULT_TEMPLATE_ID,
                    stepOrder = 2,
                    instruction = "Check the item and quantity against the packing instruction.",
                    imagePath = "res:drawable/step2_check_items"
                ),
                TaskStepEntity(
                    id = "step_003",
                    templateId = DEFAULT_TEMPLATE_ID,
                    stepOrder = 3,
                    instruction = "Place the item in the box. Add the required protective material.",
                    imagePath = "res:drawable/step3_place_item"
                ),
                TaskStepEntity(
                    id = "step_004",
                    templateId = DEFAULT_TEMPLATE_ID,
                    stepOrder = 4,
                    instruction = "Close and seal the box.",
                    imagePath = "res:drawable/step4_seal_box"
                ),
                TaskStepEntity(
                    id = "step_005",
                    templateId = DEFAULT_TEMPLATE_ID,
                    stepOrder = 5,
                    instruction = "Attach the supplied parcel label in the correct position.",
                    imagePath = "res:drawable/step5_attach_label"
                )
            )

            val packParcelChecklist = listOf(
                ChecklistItemEntity(
                    id = "check_001",
                    templateId = DEFAULT_TEMPLATE_ID,
                    itemOrder = 1,
                    checkPrompt = "Correct item and quantity"
                ),
                ChecklistItemEntity(
                    id = "check_002",
                    templateId = DEFAULT_TEMPLATE_ID,
                    itemOrder = 2,
                    checkPrompt = "Item is protected"
                ),
                ChecklistItemEntity(
                    id = "check_003",
                    templateId = DEFAULT_TEMPLATE_ID,
                    itemOrder = 3,
                    checkPrompt = "Box is sealed"
                ),
                ChecklistItemEntity(
                    id = "check_004",
                    templateId = DEFAULT_TEMPLATE_ID,
                    itemOrder = 4,
                    checkPrompt = "Correct label is attached"
                )
            )

            // 2. Secondary Template: "Sort supplies"
            val sortSuppliesTemplate = TaskTemplateEntity(
                id = SORT_SUPPLIES_TEMPLATE_ID,
                title = "Sort packing supplies",
                description = "Restock cardboard boxes, bubble wrap and tape dispensers.",
                estimatedMinutes = 15,
                helpPhrase = "I need help finding more supplies.",
                completionPhrase = "Packing supplies are sorted and restocked.",
                createdAt = System.currentTimeMillis() - 10000
            )
            val sortSuppliesSteps = listOf(
                TaskStepEntity(
                    id = "step_sort_01",
                    templateId = SORT_SUPPLIES_TEMPLATE_ID,
                    stepOrder = 1,
                    instruction = "Collect flat carton boxes from the replenishment rack.",
                    imagePath = "res:drawable/step1_materials"
                ),
                TaskStepEntity(
                    id = "step_sort_02",
                    templateId = SORT_SUPPLIES_TEMPLATE_ID,
                    stepOrder = 2,
                    instruction = "Stack size A and size B boxes neatly in their holders.",
                    imagePath = "res:drawable/step3_place_item"
                )
            )
            val sortSuppliesChecklist = listOf(
                ChecklistItemEntity(
                    id = "check_sort_01",
                    templateId = SORT_SUPPLIES_TEMPLATE_ID,
                    itemOrder = 1,
                    checkPrompt = "Box racks are fully stocked"
                ),
                ChecklistItemEntity(
                    id = "check_sort_02",
                    templateId = SORT_SUPPLIES_TEMPLATE_ID,
                    itemOrder = 2,
                    checkPrompt = "Tape dispensers are loaded"
                )
            )

            // 3. Third Template: "Clean work area"
            val cleanAreaTemplate = TaskTemplateEntity(
                id = CLEAN_AREA_TEMPLATE_ID,
                title = "Clean and inspect work area",
                description = "Clear cardboard scraps and wipe packing bench.",
                estimatedMinutes = 5,
                helpPhrase = "I need help with waste disposal.",
                completionPhrase = "Work area is clean and tidy.",
                createdAt = System.currentTimeMillis() - 20000
            )
            val cleanAreaSteps = listOf(
                TaskStepEntity(
                    id = "step_clean_01",
                    templateId = CLEAN_AREA_TEMPLATE_ID,
                    stepOrder = 1,
                    instruction = "Discard tape backing and cardboard trimmings into the recycle bin.",
                    imagePath = "res:drawable/step4_seal_box"
                ),
                TaskStepEntity(
                    id = "step_clean_02",
                    templateId = CLEAN_AREA_TEMPLATE_ID,
                    stepOrder = 2,
                    instruction = "Wipe the workbench surface clean with the damp cloth.",
                    imagePath = "res:drawable/step2_check_items"
                )
            )
            val cleanAreaChecklist = listOf(
                ChecklistItemEntity(
                    id = "check_clean_01",
                    templateId = CLEAN_AREA_TEMPLATE_ID,
                    itemOrder = 1,
                    checkPrompt = "Workbench surface is clear and wiped"
                ),
                ChecklistItemEntity(
                    id = "check_clean_02",
                    templateId = CLEAN_AREA_TEMPLATE_ID,
                    itemOrder = 2,
                    checkPrompt = "Recycling bin is not overflowing"
                )
            )

            // Save templates to Room
            taskDao.saveTemplateWithDetails(packParcelTemplate, packParcelSteps, packParcelChecklist)
            taskDao.saveTemplateWithDetails(sortSuppliesTemplate, sortSuppliesSteps, sortSuppliesChecklist)
            taskDao.saveTemplateWithDetails(cleanAreaTemplate, cleanAreaSteps, cleanAreaChecklist)

            // Pre-schedule shift queue as active:
            // 1. Pack one parcel (IN_PROGRESS, currentStepIndex = 0)
            // 2. Sort supplies (PENDING)
            // 3. Clean work area (PENDING)
            val initialSchedules = listOf(
                DailyScheduleEntity(
                    id = "schedule_001",
                    templateId = DEFAULT_TEMPLATE_ID,
                    queueOrder = 1,
                    status = ScheduleStatus.IN_PROGRESS,
                    currentStepIndex = 0
                ),
                DailyScheduleEntity(
                    id = "schedule_002",
                    templateId = SORT_SUPPLIES_TEMPLATE_ID,
                    queueOrder = 2,
                    status = ScheduleStatus.PENDING,
                    currentStepIndex = 0
                ),
                DailyScheduleEntity(
                    id = "schedule_003",
                    templateId = CLEAN_AREA_TEMPLATE_ID,
                    queueOrder = 3,
                    status = ScheduleStatus.PENDING,
                    currentStepIndex = 0
                )
            )

            scheduleDao.insertSchedules(initialSchedules)
        }
    }
}
