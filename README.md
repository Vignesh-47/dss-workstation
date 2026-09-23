# DSS Task-Guidance Android Workstation App

A production-grade, 100% offline, kiosk-capable native Android application tailored for a fixed-mount 10–11-inch tablet at an employer packing workstation for the **DSS Innovation Project**.

---

## 1. Project Overview & Architecture

* **Language:** Kotlin 2.0+
* **UI Framework:** 100% Jetpack Compose with Material 3
* **Form Factor:** 10–11-inch tablet locked to Landscape (`screenOrientation = "landscape"`)
* **Architecture:** Clean Architecture / MVVM with unidirectional data flow (`StateFlow`)
* **Local Persistence:** Room Database (SQLite) with auto-seeding callback
* **Staff Security:** Salted SHA-256 PIN hash storage (Default PIN: `1234`)
* **Image Pipeline:** Coil 2.7+ for asynchronous disk caching and loading of local photos
* **Accessibility & TTS:** Native Android `TextToSpeech` tuned for cognitive clarity (0.88x speech rate)
* **Kiosk Control:** Immersive Sticky Mode + Android Screen Pinning (`startLockTask`)
* **Backup & Migration:** ZIP archive `.dssbundle` (JSON data + photo attachments) via Storage Access Framework (SAF)

---

## 2. Accessibility & Workstation Design System

1. **Touch Targets:** All primary learner interactive targets (Buttons, Checkboxes) have a minimum tap area of **72 dp to 96 dp**.
2. **Typography:** Primary instructions **32 sp Bold**, headings **36–44 sp Bold**, button labels **24–28 sp Bold**. High visual contrast (`#111827` on pure white `#FFFFFF` or pale neutral backgrounds `#F8FAFC`).
3. **No Gesture Dependencies:** No pinch-to-zoom, swiping gestures, or double taps required for learners. All transitions rely exclusively on clear, labeled push-buttons.
4. **Distraction-Free:** Immersive Sticky mode (`WindowCompat.getInsetsController.hide(WindowInsetsCompat.Type.systemBars())`).
5. **Assistance Alert:** High-visibility amber visual alert border and TTS "Speak Out Loud" support for non-verbal or quiet learners.

---

## 3. Learner Flow State Machine

The learner workstation experience is governed by the 6-stage linear pipeline:

$$\text{TODAY} \longrightarrow \text{NOW} \longrightarrow \text{HOW} \longrightarrow \text{CHECK} \longrightarrow \text{DONE} \longrightarrow \text{NEXT}$$

* **TODAY:** Renders scheduled tasks in numerical cards. Completed tasks display green check badges.
* **NOW:** Displays large preview illustration of completed outcome and a giant target button: **"START TASK NOW"** (92 dp tall $\times$ 280 dp wide).
* **HOW (Step-by-Step):** Split-view in landscape:
  * **Left (55% width):** High-resolution image card rendered via Coil / vector fallback.
  * **Right (45% width):** Large instruction text ($\ge 30\text{ sp}$ Bold), **"READ INSTRUCTION ALOUD"** TTS button, **"I NEED HELP"** amber button, and **"PREVIOUS"** / **"NEXT STEP"** buttons.
  * **"I Need Help" Modal:** In-place alert modal with amber border, large help phrase, and TTS speech trigger.
* **CHECK:** "CHECK YOUR WORK" checklist with oversized cards ($\ge 80\text{ dp}$ tall). **"CONTINUE TO DONE"** remains strictly disabled until 100% of checklist items are verified.
* **DONE:** Congratulatory checkmark, supervisor handover phrase card with **"READ OUT LOUD"** TTS, and **"CONFIRM & GO TO NEXT"** button.
* **NEXT:** Up next preview card leading to **NOW** of subsequent task, or celebratory shift-completed screen looping back to **TODAY**.

---

## 4. Staff Administration Module (PIN Protected)

Tapping the **STAFF** button in the persistent top bar displays a 4–6 digit numeric keypad dialog (default PIN: `1234`). Once verified, staff access the full administration suite:

1. **Schedule Organizer:** Add task templates to today's shift queue, reorder items with **UP** / **DOWN** buttons, remove items, or clear queue.
2. **Template Builder & Editor:** Create, edit, copy/duplicate, and delete task templates.
   * Add/remove/reorder steps.
   * Photo Handler: **"Camera"** (takes photo via `ActivityResultContracts.TakePicture` and saves to internal `task_photos/`) or **"Gallery"** (`ActivityResultContracts.GetContent`).
   * Checklist Editor: Add/remove verification criteria.
3. **Live Preview Sandbox:** Allows staff to test learner screens (`TODAY` $\to$ `NEXT`) with active TTS in an isolated sandbox without modifying production database records.
4. **Task Recovery & Reset:** If a learner is stuck, staff can reset the active task to Step 1, jump directly to a specific step, or force-mark it complete.
5. **Backup & Restore System (`.dssbundle`):**
   * **Export:** Serializes SQLite data to `data.json` and bundles all referenced photos from `task_photos/` into a single `.dssbundle` ZIP via SAF document picker.
   * **Import:** Validates archive, extracts photos to internal storage, and updates the database records.
6. **Kiosk & Security Settings:**
   * Toggle Android Screen Pinning (`startLockTask()`).
   * Update Staff PIN (salted SHA-256 hash).

---

## 5. Seeded Data Out-of-the-Box

On first launch, the app automatically pre-populates the Room database with the packing workstation task:

* **Title:** *"Pack one parcel"*
* **Steps:**
  1. *"Take the assigned item, box and packing materials."* (bundled vector: `step1_materials.xml`)
  2. *"Check the item and quantity against the packing instruction."* (bundled vector: `step2_check_items.xml`)
  3. *"Place the item in the box. Add the required protective material."* (bundled vector: `step3_place_item.xml`)
  4. *"Close and seal the box."* (bundled vector: `step4_seal_box.xml`)
  5. *"Attach the supplied parcel label in the correct position."* (bundled vector: `step5_attach_label.xml`)
* **Checklist:**
  * "Correct item and quantity"
  * "Item is protected"
  * "Box is sealed"
  * "Correct label is attached"
* **Phrases:**
  * *Help:* "I need help with this step. Please assist me."
  * *Completion:* "I have finished packing this parcel. Please check."
* **Queue:** Pre-scheduled as active in today's queue alongside sample shift tasks.

---

## 6. How to Open and Build in Android Studio

1. Open **Android Studio** (Ladybug / Koala or newer recommended).
2. Select **File $\to$ Open...** and choose this project directory:
   `c:\Users\LENOVO\OneDrive\Desktop\ayush raj project`
3. Allow Gradle to sync dependencies automatically.
4. Select a 10" or 11" tablet emulator (e.g., Pixel Tablet in Landscape) or connect a physical tablet device via USB debugging.
5. Click **Run 'app'** (`Shift + F10`).
