// DSS Workstation Interactive Web Simulator
// High-Contrast Workstation FSM & Staff Administration Suite

const SEED_TEMPLATES = [
  {
    id: "template_pack_parcel_001",
    title: "Pack one parcel",
    description: "Standard parcel packing, sealing, and barcode labelling.",
    estimatedMinutes: 10,
    helpPhrase: "I need help with this step. Please assist me.",
    completionPhrase: "I have finished packing this parcel. Please check.",
    steps: [
      { order: 1, instruction: "Take the assigned item, box and packing materials.", image: "assets/step1.svg" },
      { order: 2, instruction: "Check the item and quantity against the packing instruction.", image: "assets/step2.svg" },
      { order: 3, instruction: "Place the item in the box. Add the required protective material.", image: "assets/step3.svg" },
      { order: 4, instruction: "Close and seal the box.", image: "assets/step4.svg" },
      { order: 5, instruction: "Attach the supplied parcel label in the correct position.", image: "assets/step5.svg" }
    ],
    checklist: [
      { id: "c1", prompt: "Correct item and quantity" },
      { id: "c2", prompt: "Item is protected" },
      { id: "c3", prompt: "Box is sealed" },
      { id: "c4", prompt: "Correct label is attached" }
    ]
  },
  {
    id: "template_sort_supplies_002",
    title: "Sort packing supplies",
    description: "Replenish flat cartons and load tape dispensers.",
    estimatedMinutes: 15,
    helpPhrase: "I need help finding replacement stock.",
    completionPhrase: "Packing supplies are sorted and restocked.",
    steps: [
      { order: 1, instruction: "Collect flat cartons from the replenishment rack.", image: "assets/step1.svg" },
      { order: 2, instruction: "Stack boxes neatly in holders and inspect tape roll.", image: "assets/step3.svg" }
    ],
    checklist: [
      { id: "c_s1", prompt: "Box racks are fully stocked" },
      { id: "c_s2", prompt: "Tape dispensers are loaded" }
    ]
  },
  {
    id: "template_clean_area_003",
    title: "Clean and inspect work area",
    description: "Clear cardboard trimmings and wipe packing bench.",
    estimatedMinutes: 5,
    helpPhrase: "I need help with waste disposal.",
    completionPhrase: "Work area is clean and tidy.",
    steps: [
      { order: 1, instruction: "Discard tape backing and cardboard scraps into recycling bin.", image: "assets/step4.svg" },
      { order: 2, instruction: "Wipe workbench clean with the damp cloth.", image: "assets/step2.svg" }
    ],
    checklist: [
      { id: "c_cl1", prompt: "Workbench surface is wiped clean" },
      { id: "c_cl2", prompt: "Recycling bin is not overflowing" }
    ]
  }
];

class WorkstationStore {
  constructor() {
    this.templates = JSON.parse(localStorage.getItem("dss_templates")) || SEED_TEMPLATES;
    this.schedule = JSON.parse(localStorage.getItem("dss_schedule")) || [
      { id: "sch_1", templateId: "template_pack_parcel_001", status: "IN_PROGRESS", currentStep: 0 },
      { id: "sch_2", templateId: "template_sort_supplies_002", status: "PENDING", currentStep: 0 },
      { id: "sch_3", templateId: "template_clean_area_003", status: "PENDING", currentStep: 0 }
    ];
    this.staffPin = localStorage.getItem("dss_staff_pin") || "1234";
  }

  save() {
    localStorage.setItem("dss_templates", JSON.stringify(this.templates));
    localStorage.setItem("dss_schedule", JSON.stringify(this.schedule));
    localStorage.setItem("dss_staff_pin", this.staffPin);
  }

  getActiveScheduleItem() {
    return this.schedule.find(s => s.status === "IN_PROGRESS") || this.schedule[0];
  }

  getTemplate(id) {
    return this.templates.find(t => t.id === id) || this.templates[0];
  }
}

const store = new WorkstationStore();

// App State
let currentStage = "TODAY"; // TODAY, NOW, HOW, CHECK, DONE, NEXT
let activeSchedule = store.getActiveScheduleItem();
let currentStepIndex = 0;
let checkedItems = new Set();
let enteredPin = "";
let isHelpModalOpen = false;
let isPinModalOpen = false;
let isStaffOpen = false;
let currentStaffTab = "SCHEDULE";
let isAudioMuted = false;
let isSpeaking = false;
let editingTemplateId = null;

// Toast Helper
function showToast(msg) {
  const container = document.getElementById("toast-container");
  if (!container) return;
  const toast = document.createElement("div");
  toast.className = "dss-toast";
  toast.innerText = msg;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = "0";
    setTimeout(() => toast.remove(), 250);
  }, 2500);
}

// Web Speech API Engine
function speakText(text) {
  if (isAudioMuted || !('speechSynthesis' in window)) return;
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.rate = 0.88; // Tuned for cognitive accessibility
  utterance.pitch = 1.0;
  utterance.lang = "en-US";

  utterance.onstart = () => {
    isSpeaking = true;
    updateAudioBtnState();
  };
  utterance.onend = () => {
    isSpeaking = false;
    updateAudioBtnState();
  };
  utterance.onerror = () => {
    isSpeaking = false;
    updateAudioBtnState();
  };

  window.speechSynthesis.speak(utterance);
}

function updateAudioBtnState() {
  const btn = document.getElementById("audio-speak-btn");
  if (btn) {
    if (isSpeaking) {
      btn.classList.add("speaking");
      btn.innerHTML = "🔊 SPEAKING OUT LOUD...";
    } else {
      btn.classList.remove("speaking");
      btn.innerHTML = "🔊 READ INSTRUCTION ALOUD";
    }
  }
}

function toggleAudioMute() {
  isAudioMuted = !isAudioMuted;
  if (isAudioMuted && 'speechSynthesis' in window) {
    window.speechSynthesis.cancel();
  }
  const btn = document.getElementById("audio-toggle-btn");
  if (btn) {
    btn.innerHTML = isAudioMuted ? "🔇 Sound: OFF" : "🔊 Sound: ON";
    btn.style.background = isAudioMuted ? "#DC2626" : "#334155";
  }
  showToast(isAudioMuted ? "Audio muted" : "Audio unmuted");
}

// Stage Progress Indicator Bar
function renderTopBar() {
  const stages = ["TODAY", "NOW", "HOW", "CHECK", "DONE", "NEXT"];
  const container = document.getElementById("stage-pills");
  if (!container) return;
  container.innerHTML = stages.map(stage => {
    const isAct = stage === currentStage;
    const isComp = stages.indexOf(stage) < stages.indexOf(currentStage);
    return `<div class="stage-pill ${isAct ? 'active' : ''} ${isComp ? 'completed' : ''}" onclick="goToStage('${stage}')">${stage}</div>`;
  }).join("");
}

function goToStage(stage) {
  currentStage = stage;
  renderApp();
}

// Render Master Screen Views
function renderApp() {
  renderTopBar();
  const main = document.getElementById("main-screen");
  if (!main) return;
  const template = store.getTemplate(activeSchedule?.templateId);

  if (!template && currentStage !== "TODAY") {
    main.innerHTML = `
      <div style="text-align:center; padding:60px 20px; background:#FFF; border-radius:18px; border:2px solid var(--dss-border); margin:auto; max-width:600px;">
        <h2 style="font-size:28px; font-weight:900;">No Active Task Assigned</h2>
        <p style="font-size:18px; color:var(--dss-text-secondary); margin:10px 0 24px;">Please return to the schedule or contact your supervisor.</p>
        <button class="dss-btn dss-btn-primary" onclick="goToStage('TODAY')" style="min-height:56px; font-size:18px; padding:10px 28px;">← VIEW SCHEDULE</button>
      </div>
    `;
    return;
  }

  if (currentStage === "TODAY") {
    main.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:flex-end; flex-shrink:0;">
        <div>
          <h1 style="font-size:36px; font-weight:900;">TODAY'S SHIFT SCHEDULE</h1>
          <p style="font-size:20px; color:var(--dss-text-secondary); margin-top:2px;">Select your assigned workstation task</p>
        </div>
        <div style="background:#EFF6FF; border:2px solid var(--dss-primary); border-radius:12px; padding:8px 18px; font-size:16px; font-weight:800; color:var(--dss-primary);">
          ${store.schedule.filter(s => s.status === 'COMPLETED').length} of ${store.schedule.length} Completed
        </div>
      </div>
      <div class="today-list">
        ${store.schedule.length === 0 ? `
          <div style="background:#FFFFFF; border:2px dashed var(--dss-border); border-radius:18px; padding:48px 24px; text-align:center; margin-top:20px;">
            <div style="font-size:48px; margin-bottom:12px;">📦</div>
            <h2 style="font-size:28px; font-weight:800; color:var(--dss-text-primary);">No Tasks Scheduled For Today</h2>
            <p style="font-size:18px; color:var(--dss-text-secondary); margin-top:6px;">Please ask your supervisor to assign tasks from the Staff menu.</p>
          </div>
        ` : store.schedule.map((item, index) => {
          const t = store.getTemplate(item.templateId);
          const isComp = item.status === "COMPLETED";
          const isAct = item.status === "IN_PROGRESS";
          return `
            <div class="task-card ${isAct ? 'active' : ''} ${isComp ? 'completed' : ''}" onclick="selectTask(${index})">
              <div style="display:flex; align-items:center; gap:18px;">
                <div class="task-order-badge">${isComp ? '✓' : index + 1}</div>
                <div>
                  <h2 style="font-size:26px; font-weight:800;">${t.title}</h2>
                  <p style="font-size:17px; color:var(--dss-text-secondary); margin-top:3px;">${t.steps.length} steps • Est. ${t.estimatedMinutes} mins</p>
                </div>
              </div>
              <div>
                ${isComp ? `
                  <div style="background:#DCFCE7; color:#166534; padding:10px 18px; border-radius:10px; font-weight:800; font-size:16px;">COMPLETED</div>
                ` : `
                  <button class="dss-btn ${isAct ? 'dss-btn-success' : 'dss-btn-primary'}" style="min-height:60px; font-size:18px; padding:10px 22px;">
                    ${isAct ? 'START THIS TASK' : 'SELECT TASK'}
                  </button>
                `}
              </div>
            </div>
          `;
        }).join("")}
      </div>
    `;
  } else if (currentStage === "NOW") {
    main.innerHTML = `
      <div class="now-container">
        <div style="flex-shrink:0;">
          <div style="font-size:22px; font-weight:900; color:var(--dss-primary); letter-spacing:1px;">CURRENT TASK</div>
          <h1 style="font-size:40px; font-weight:900; margin:4px 0 6px;">${template.title}</h1>
          <p style="font-size:20px; color:var(--dss-text-secondary);">${template.steps.length} guided steps • Est. ${template.estimatedMinutes} minutes</p>
        </div>
        <div class="now-preview-card">
          <img src="${template.steps[template.steps.length - 1].image}" alt="Finished Preview" />
        </div>
        <div style="display:flex; gap:20px; width:88%; justify-content:center; flex-shrink:0;">
          <button class="dss-btn dss-btn-secondary" onclick="goToStage('TODAY')" style="flex:0.35; min-height:72px;">
            ← TODAY'S SCHEDULE
          </button>
          <button class="dss-btn dss-btn-success dss-btn-giant" onclick="startActiveTask()" style="flex:0.65; min-height:72px;">
            START TASK NOW ✓
          </button>
        </div>
      </div>
    `;
  } else if (currentStage === "HOW") {
    const step = template.steps[currentStepIndex] || template.steps[0];
    const isLast = currentStepIndex >= template.steps.length - 1;
    main.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; flex-shrink:0;">
        <div style="background:var(--dss-primary); color:#FFFFFF; padding:8px 20px; border-radius:14px; font-size:24px; font-weight:900;">
          STEP ${currentStepIndex + 1} OF ${template.steps.length}
        </div>
        <button class="dss-btn dss-btn-warning" onclick="openHelp()" style="min-height:56px; font-size:19px; padding:10px 22px;">
          ⚠ I NEED HELP
        </button>
      </div>
      <div class="how-container">
        <div class="how-image-col">
          <img src="${step.image}" alt="Step graphic" />
        </div>
        <div class="how-instruction-col">
          <div style="display:flex; flex-direction:column; gap:14px; overflow-y:auto;">
            <div class="instruction-box">${step.instruction}</div>
            <button class="audio-btn" id="audio-speak-btn" onclick="speakCurrentInstruction()">
              🔊 READ INSTRUCTION ALOUD
            </button>
          </div>
          <div style="display:flex; gap:14px; flex-shrink:0;">
            <button class="dss-btn dss-btn-secondary" onclick="prevStep()" style="flex:0.4; font-size:20px; min-height:68px;">
              ← PREVIOUS
            </button>
            <button class="dss-btn ${isLast ? 'dss-btn-success' : 'dss-btn-primary'}" onclick="nextStep()" style="flex:0.6; font-size:22px; min-height:68px;">
              ${isLast ? 'PROCEED TO CHECK ✓' : 'NEXT STEP →'}
            </button>
          </div>
        </div>
      </div>
    `;
  } else if (currentStage === "CHECK") {
    const allChecked = template.checklist.every(item => checkedItems.has(item.id));
    main.innerHTML = `
      <div class="check-container">
        <div style="display:flex; justify-content:space-between; align-items:center; flex-shrink:0;">
          <div>
            <h1 style="font-size:34px; font-weight:900;">CHECK YOUR WORK</h1>
            <p style="font-size:19px; color:var(--dss-text-secondary); margin-top:2px;">Tap each box once you have verified it on your parcel</p>
          </div>
          <div style="background:${allChecked ? '#DCFCE7' : '#EFF6FF'}; color:${allChecked ? '#14532D' : 'var(--dss-primary)'}; padding:10px 20px; border-radius:14px; font-size:20px; font-weight:800;">
            ${checkedItems.size} of ${template.checklist.length} Verified
          </div>
        </div>
        <div class="check-list">
          ${template.checklist.map(item => {
            const isChk = checkedItems.has(item.id);
            return `
              <div class="check-card ${isChk ? 'checked' : ''}" onclick="toggleCheckItem('${item.id}')">
                <div class="check-box-target">${isChk ? '✓' : ''}</div>
                <div class="check-label">${item.prompt}</div>
              </div>
            `;
          }).join("")}
        </div>
        <div style="display:flex; gap:18px; margin-top:16px; flex-shrink:0;">
          <button class="dss-btn dss-btn-secondary" onclick="goToStage('HOW')" style="flex:0.35; min-height:68px;">
            ← REVIEW STEPS
          </button>
          <button class="dss-btn ${allChecked ? 'dss-btn-success' : 'dss-btn-secondary'}" ${allChecked ? '' : 'disabled'} onclick="proceedToDone()" style="flex:0.65; min-height:68px;">
            ${allChecked ? 'CONTINUE TO DONE ✓' : 'CHECK ALL ITEMS TO PROCEED'}
          </button>
        </div>
      </div>
    `;
  } else if (currentStage === "DONE") {
    main.innerHTML = `
      <div class="done-container">
        <div style="flex-shrink:0;">
          <div style="width:80px; height:80px; border-radius:50%; background:var(--dss-success); color:#FFFFFF; font-size:48px; font-weight:900; display:flex; align-items:center; justify-content:center; margin:0 auto 8px;">✓</div>
          <h1 style="font-size:38px; font-weight:900; color:#14532D;">TASK COMPLETE!</h1>
          <p style="font-size:22px; font-weight:700; color:var(--dss-text-primary); margin-top:4px;">Please call your supervisor for inspection and handover.</p>
        </div>
        <div class="completion-phrase-card">
          <div style="font-size:16px; font-weight:900; color:var(--dss-success); letter-spacing:0.5px;">WHAT TO SAY TO YOUR SUPERVISOR:</div>
          <div style="font-size:28px; font-weight:800; color:var(--dss-text-primary); text-align:center;">"${template.completionPhrase}"</div>
          <button class="audio-btn" onclick="speakText('${template.completionPhrase}')" style="min-width:280px;">
            🔊 PLAY REPORTING PHRASE ALOUD
          </button>
        </div>
        <button class="dss-btn dss-btn-success dss-btn-giant" onclick="confirmAndNext()" style="width:82%; min-height:76px; flex-shrink:0;">
          CONFIRM & GO TO NEXT →
        </button>
      </div>
    `;
  } else if (currentStage === "NEXT") {
    const curIdx = store.schedule.findIndex(s => s.id === activeSchedule?.id);
    const nextItem = store.schedule.slice(curIdx + 1).find(s => s.status !== "COMPLETED");
    if (nextItem) {
      const nextTemplate = store.getTemplate(nextItem.templateId);
      main.innerHTML = `
        <div class="now-container">
          <div style="flex-shrink:0;">
            <div style="font-size:24px; font-weight:900; color:var(--dss-primary);">UP NEXT ON YOUR SCHEDULE</div>
            <p style="font-size:20px; color:var(--dss-text-secondary); margin-top:4px;">Ready to proceed to your next assignment?</p>
          </div>
          <div style="width:72%; max-width:620px; background:#FFFFFF; border:3px solid var(--dss-primary); border-radius:20px; padding:28px; box-shadow:0 4px 16px rgba(0,0,0,0.05);">
            <div style="width:60px; height:60px; border-radius:50%; background:#DBEAFE; color:var(--dss-primary); font-size:28px; font-weight:900; display:flex; align-items:center; justify-content:center; margin:0 auto 12px;">→</div>
            <h2 style="font-size:32px; font-weight:900;">${nextTemplate.title}</h2>
            <p style="font-size:18px; color:var(--dss-text-secondary); margin-top:6px;">${nextTemplate.steps.length} steps • Est. ${nextTemplate.estimatedMinutes} mins</p>
          </div>
          <div style="display:flex; gap:18px; width:72%; max-width:620px; flex-shrink:0;">
            <button class="dss-btn dss-btn-secondary" onclick="goToStage('TODAY')" style="flex:0.35; min-height:68px;">
              SCHEDULE
            </button>
            <button class="dss-btn dss-btn-success" onclick="startNextTask('${nextItem.id}')" style="flex:0.65; min-height:68px;">
              START NEXT TASK →
            </button>
          </div>
        </div>
      `;
    } else {
      main.innerHTML = `
        <div class="done-container">
          <div style="margin:auto 0;">
            <div style="width:90px; height:90px; border-radius:50%; background:var(--dss-success); color:#FFFFFF; font-size:52px; font-weight:900; display:flex; align-items:center; justify-content:center; margin:0 auto 16px;">✓</div>
            <h1 style="font-size:42px; font-weight:900; color:#14532D;">ALL TASKS FINISHED!</h1>
            <p style="font-size:24px; font-weight:700; margin-top:8px;">All scheduled workstation tasks are complete.<br>Great work today!</p>
          </div>
          <button class="dss-btn dss-btn-primary dss-btn-giant" onclick="goToStage('TODAY')" style="min-width:380px; min-height:76px; flex-shrink:0;">
            RETURN TO TODAY'S SCHEDULE ↺
          </button>
        </div>
      `;
    }
  }

  // Modals & Overlays
  document.getElementById("help-modal").style.display = isHelpModalOpen ? "flex" : "none";
  document.getElementById("pin-modal").style.display = isPinModalOpen ? "flex" : "none";
  document.getElementById("staff-dashboard").style.display = isStaffOpen ? "flex" : "none";
  if (isStaffOpen) renderStaffContent();
}

// User Actions
function selectTask(index) {
  activeSchedule = store.schedule[index];
  currentStepIndex = 0;
  checkedItems.clear();
  goToStage("NOW");
}

function startActiveTask() {
  activeSchedule.status = "IN_PROGRESS";
  store.save();
  currentStepIndex = 0;
  checkedItems.clear();
  goToStage("HOW");
  speakCurrentInstruction();
}

function speakCurrentInstruction() {
  const template = store.getTemplate(activeSchedule?.templateId);
  const step = template.steps[currentStepIndex];
  if (step) speakText(step.instruction);
}

function nextStep() {
  const template = store.getTemplate(activeSchedule?.templateId);
  if (currentStepIndex < template.steps.length - 1) {
    currentStepIndex++;
    goToStage("HOW");
    speakCurrentInstruction();
  } else {
    goToStage("CHECK");
    speakText("Check your work. Verify all items on the checklist.");
  }
}

function prevStep() {
  if (currentStepIndex > 0) {
    currentStepIndex--;
    goToStage("HOW");
    speakCurrentInstruction();
  } else {
    goToStage("NOW");
  }
}

function toggleCheckItem(id) {
  if (checkedItems.has(id)) checkedItems.delete(id);
  else checkedItems.add(id);
  renderApp();
}

function proceedToDone() {
  goToStage("DONE");
  const template = store.getTemplate(activeSchedule?.templateId);
  speakText("Task complete! " + template.completionPhrase);
}

function confirmAndNext() {
  activeSchedule.status = "COMPLETED";
  store.save();
  goToStage("NEXT");
}

function startNextTask(nextScheduleId) {
  activeSchedule = store.schedule.find(s => s.id === nextScheduleId);
  currentStepIndex = 0;
  checkedItems.clear();
  goToStage("NOW");
}

// Help Modal
function openHelp() {
  isHelpModalOpen = true;
  const template = store.getTemplate(activeSchedule?.templateId);
  document.getElementById("help-text").innerText = `"${template.helpPhrase}"`;
  speakText(template.helpPhrase);
  renderApp();
}

function closeHelp() {
  isHelpModalOpen = false;
  if ('speechSynthesis' in window) window.speechSynthesis.cancel();
  renderApp();
}

function speakHelp() {
  const template = store.getTemplate(activeSchedule?.templateId);
  speakText(template.helpPhrase);
}

// Staff PIN Dialog
function openStaffPin() {
  enteredPin = "";
  isPinModalOpen = true;
  document.getElementById("pin-error").style.display = "none";
  updatePinDots();
  renderApp();
}

function closeStaffPin() {
  isPinModalOpen = false;
  enteredPin = "";
  renderApp();
}

function pinKey(val) {
  if (val === "CLEAR") {
    enteredPin = "";
  } else if (val === "DEL") {
    enteredPin = enteredPin.slice(0, -1);
  } else {
    if (enteredPin.length < 6) enteredPin += val;
  }
  updatePinDots();

  if (enteredPin.length >= 4) {
    if (enteredPin === store.staffPin) {
      isPinModalOpen = false;
      isStaffOpen = true;
      showToast("Staff access authorized");
      renderApp();
    } else if (enteredPin.length === 6) {
      document.getElementById("pin-error").style.display = "block";
      enteredPin = "";
      updatePinDots();
    }
  }
}

function updatePinDots() {
  const dots = document.querySelectorAll(".pin-dot");
  dots.forEach((dot, idx) => {
    if (idx < enteredPin.length) dot.classList.add("filled");
    else dot.classList.remove("filled");
  });
}

// Staff Dashboard Suite
function setStaffTab(tab) {
  currentStaffTab = tab;
  editingTemplateId = null;
  renderStaffContent();
}

function renderStaffContent() {
  const tabs = document.querySelectorAll(".staff-nav-item");
  tabs.forEach(t => t.classList.remove("active"));
  const currentTabEl = document.getElementById("tab-" + currentStaffTab.toLowerCase());
  if (currentTabEl) currentTabEl.classList.add("active");

  const container = document.getElementById("staff-content");
  if (!container) return;

  if (currentStaffTab === "SCHEDULE") {
    container.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px;">
        <div>
          <h2 style="font-size:26px; font-weight:900;">Schedule Organizer</h2>
          <p style="font-size:15px; color:var(--dss-text-secondary); margin-top:2px;">Organize, rename, and assign tasks to today's shift queue.</p>
        </div>
        <div style="display:flex; gap:10px;">
          <button class="dss-btn dss-btn-secondary" onclick="clearScheduleQueue()" style="min-height:48px; font-size:15px; padding:6px 14px;">Clear Queue</button>
          <button class="dss-btn dss-btn-primary" onclick="addScheduleItem()" style="min-height:48px; font-size:16px; padding:8px 18px;">+ Add Task to Today</button>
        </div>
      </div>
      <div style="display:flex; flex-direction:column; gap:12px;">
        ${store.schedule.length === 0 ? `
          <div style="text-align:center; padding:48px 20px; background:#FFFFFF; border:2px dashed var(--dss-border); border-radius:14px; color:var(--dss-text-secondary); font-size:17px;">
            <div style="font-size:36px; margin-bottom:8px;">📋</div>
            No tasks currently scheduled for today.<br><strong style="color:var(--dss-primary);">Click "+ Add Task to Today" above to assign work!</strong>
          </div>
        ` : store.schedule.map((item, idx) => {
          const t = store.getTemplate(item.templateId);
          return `
            <div style="background:#FFFFFF; border:2px solid var(--dss-border); border-radius:14px; padding:14px 18px; display:flex; justify-content:space-between; align-items:center;">
              <div style="display:flex; align-items:center; flex-wrap:wrap; gap:8px;">
                <span style="background:var(--dss-primary); color:#FFF; font-weight:900; padding:4px 10px; border-radius:6px;">#${idx + 1}</span>
                <strong style="font-size:19px;">${t.title}</strong>
                <button class="dss-btn dss-btn-secondary" onclick="renameScheduleTask(${idx})" style="min-height:32px; font-size:13px; padding:2px 10px; border-radius:6px; font-weight:700; background:#F1F5F9;" title="Rename this task">✏ RENAME</button>
                <span style="color:var(--dss-text-secondary); font-size:14px;">(${item.status})</span>
              </div>
              <div style="display:flex; gap:8px;">
                <button class="dss-btn dss-btn-secondary" onclick="moveSchedule(${idx}, -1)" style="min-height:44px; font-size:14px; padding:6px 14px;" ${idx === 0 ? 'disabled' : ''}>▲ UP</button>
                <button class="dss-btn dss-btn-secondary" onclick="moveSchedule(${idx}, 1)" style="min-height:44px; font-size:14px; padding:6px 14px;" ${idx === store.schedule.length - 1 ? 'disabled' : ''}>▼ DOWN</button>
                <button class="dss-btn dss-btn-secondary" onclick="removeSchedule(${idx})" style="min-height:44px; font-size:14px; padding:6px 14px; color:var(--dss-danger);">REMOVE</button>
              </div>
            </div>
          `;
        }).join("")}
      </div>
    `;
  } else if (currentStaffTab === "TEMPLATES") {
    if (editingTemplateId) {
      renderTemplateEditor(container);
    } else {
      container.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px;">
          <h2 style="font-size:26px; font-weight:900;">Task Template Builder</h2>
          <button class="dss-btn dss-btn-primary" onclick="createNewTemplate()" style="min-height:50px; font-size:16px; padding:8px 18px;">+ Create New Template</button>
        </div>
        <div style="display:flex; flex-direction:column; gap:12px;">
          ${store.templates.map((t, idx) => `
            <div style="background:#FFFFFF; border:2px solid var(--dss-border); border-radius:14px; padding:18px; display:flex; justify-content:space-between; align-items:center;">
              <div>
                <h3 style="font-size:20px; font-weight:900;">${t.title}</h3>
                <p style="color:var(--dss-text-secondary); font-size:15px; margin-top:3px;">${t.steps.length} steps • ${t.checklist.length} checklist points • Est. ${t.estimatedMinutes} mins</p>
              </div>
              <div style="display:flex; gap:8px;">
                <button class="dss-btn dss-btn-primary" onclick="editTemplate('${t.id}')" style="min-height:44px; font-size:14px; padding:6px 16px;">EDIT</button>
                <button class="dss-btn dss-btn-secondary" onclick="duplicateTemplate(${idx})" style="min-height:44px; font-size:14px; padding:6px 14px;">COPY</button>
                <button class="dss-btn dss-btn-secondary" onclick="deleteTemplate(${idx})" style="min-height:44px; font-size:14px; padding:6px 14px; color:var(--dss-danger);">DELETE</button>
              </div>
            </div>
          `).join("")}
        </div>
      `;
    }
  } else if (currentStaffTab === "RECOVERY") {
    container.innerHTML = `
      <h2 style="font-size:26px; font-weight:900; margin-bottom:8px;">Task Recovery & Reset</h2>
      <p style="font-size:16px; color:var(--dss-text-secondary); margin-bottom:18px;">Reset learner task state or jump directly to Step 1 if stuck.</p>
      <div style="display:flex; flex-direction:column; gap:12px;">
        ${store.schedule.map((item, idx) => {
          const t = store.getTemplate(item.templateId);
          return `
            <div style="background:#FFFFFF; border:2px solid var(--dss-border); border-radius:14px; padding:16px 20px; display:flex; justify-content:space-between; align-items:center;">
              <div>
                <strong style="font-size:20px;">${t.title}</strong>
                <div style="color:var(--dss-primary); font-weight:700; margin-top:3px; font-size:15px;">Status: ${item.status}</div>
              </div>
              <div style="display:flex; gap:10px;">
                <button class="dss-btn dss-btn-primary" onclick="resetTaskStep(${idx})" style="min-height:46px; font-size:14px; padding:6px 16px;">RESET TO STEP 1</button>
                <button class="dss-btn dss-btn-success" onclick="forceComplete(${idx})" style="min-height:46px; font-size:14px; padding:6px 16px;">FORCE COMPLETE</button>
              </div>
            </div>
          `;
        }).join("")}
      </div>
    `;
  } else if (currentStaffTab === "BACKUP") {
    container.innerHTML = `
      <h2 style="font-size:26px; font-weight:900; margin-bottom:8px;">Backup & Restore (.dssbundle)</h2>
      <p style="font-size:16px; color:var(--dss-text-secondary); margin-bottom:20px;">Export configuration bundle or restore workstation templates.</p>
      <div style="display:flex; gap:18px;">
        <div style="flex:1; background:#FFFFFF; border:2px solid var(--dss-border); border-radius:16px; padding:20px;">
          <h3 style="font-size:20px; font-weight:800; margin-bottom:8px;">Export Data Bundle</h3>
          <p style="color:var(--dss-text-secondary); font-size:15px; margin-bottom:18px;">Downloads all templates, schedules, and step configurations as a JSON backup package.</p>
          <button class="dss-btn dss-btn-primary" onclick="exportData()" style="width:100%; min-height:52px; font-size:16px;">DOWNLOAD .DSSBUNDLE</button>
        </div>
        <div style="flex:1; background:#FFFFFF; border:2px solid var(--dss-border); border-radius:16px; padding:20px;">
          <h3 style="font-size:20px; font-weight:800; margin-bottom:8px;">Restore Factory Defaults</h3>
          <p style="color:var(--dss-text-secondary); font-size:15px; margin-bottom:18px;">Restores initial packing workstation seeded templates and queue.</p>
          <button class="dss-btn dss-btn-secondary" onclick="restoreDefaults()" style="width:100%; min-height:52px; font-size:16px;">RESTORE FACTORY SEED DATA</button>
        </div>
      </div>
    `;
  } else if (currentStaffTab === "SECURITY") {
    container.innerHTML = `
      <h2 style="font-size:26px; font-weight:900; margin-bottom:8px;">Security & Staff PIN</h2>
      <p style="font-size:16px; color:var(--dss-text-secondary); margin-bottom:20px;">Change the security PIN used to protect the administration suite.</p>
      
      <div style="background:#FFFFFF; border:2px solid var(--dss-border); border-radius:16px; padding:24px; max-width:480px;">
        <div style="margin-bottom:14px;">
          <label style="font-weight:700; font-size:14px; display:block; margin-bottom:6px;">Current Staff PIN</label>
          <input type="password" id="pin-current" maxlength="6" placeholder="Enter current PIN" style="width:100%; padding:10px 14px; border-radius:8px; border:2px solid var(--dss-border); font-size:16px; box-sizing:border-box;" />
        </div>
        <div style="margin-bottom:14px;">
          <label style="font-weight:700; font-size:14px; display:block; margin-bottom:6px;">New Staff PIN (4–6 digits)</label>
          <input type="password" id="pin-new" maxlength="6" placeholder="Enter new PIN" style="width:100%; padding:10px 14px; border-radius:8px; border:2px solid var(--dss-border); font-size:16px; box-sizing:border-box;" />
        </div>
        <div style="margin-bottom:20px;">
          <label style="font-weight:700; font-size:14px; display:block; margin-bottom:6px;">Confirm New Staff PIN</label>
          <input type="password" id="pin-confirm" maxlength="6" placeholder="Re-enter new PIN" style="width:100%; padding:10px 14px; border-radius:8px; border:2px solid var(--dss-border); font-size:16px; box-sizing:border-box;" />
        </div>
        <button class="dss-btn dss-btn-primary" onclick="updateStaffPin()" style="width:100%; min-height:50px; font-size:16px;">
          UPDATE STAFF PIN ✓
        </button>
      </div>
    `;
  }
}

// Inline Template Editor
function renderTemplateEditor(container) {
  const t = store.getTemplate(editingTemplateId);
  container.innerHTML = `
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
      <h2 style="font-size:24px; font-weight:900;">Edit Template: ${t.title}</h2>
      <div style="display:flex; gap:10px;">
        <button class="dss-btn dss-btn-secondary" onclick="cancelEditTemplate()" style="min-height:46px; font-size:15px;">CANCEL</button>
        <button class="dss-btn dss-btn-success" onclick="saveTemplateEdit('${t.id}')" style="min-height:46px; font-size:15px;">SAVE CHANGES ✓</button>
      </div>
    </div>
    <div style="display:flex; flex-direction:column; gap:14px; background:#FFF; padding:20px; border-radius:16px; border:2px solid var(--dss-border);">
      <div>
        <label style="font-weight:700; font-size:14px; display:block; margin-bottom:4px;">Task Title</label>
        <input type="text" id="edit-title" value="${t.title}" style="width:100%; padding:10px; border-radius:8px; border:2px solid var(--dss-border); font-size:16px; font-weight:700;" />
      </div>
      <div>
        <label style="font-weight:700; font-size:14px; display:block; margin-bottom:4px;">Help Phrase</label>
        <input type="text" id="edit-help" value="${t.helpPhrase}" style="width:100%; padding:10px; border-radius:8px; border:2px solid var(--dss-border); font-size:15px;" />
      </div>
      <div>
        <label style="font-weight:700; font-size:14px; display:block; margin-bottom:4px;">Completion Reporting Phrase</label>
        <input type="text" id="edit-completion" value="${t.completionPhrase}" style="width:100%; padding:10px; border-radius:8px; border:2px solid var(--dss-border); font-size:15px;" />
      </div>
      <div>
        <label style="font-weight:700; font-size:14px; display:block; margin-bottom:6px;">Step Instructions (${t.steps.length} steps)</label>
        ${t.steps.map((st, i) => `
          <div style="display:flex; gap:10px; margin-bottom:8px; align-items:center;">
            <span style="font-weight:900; background:var(--dss-primary); color:#FFF; padding:6px 10px; border-radius:6px;">${i + 1}</span>
            <input type="text" id="edit-step-${i}" value="${st.instruction}" style="flex:1; padding:8px 12px; border-radius:8px; border:2px solid var(--dss-border); font-size:15px;" />
          </div>
        `).join("")}
      </div>
    </div>
  `;
}

function editTemplate(id) {
  editingTemplateId = id;
  renderStaffContent();
}

function cancelEditTemplate() {
  editingTemplateId = null;
  renderStaffContent();
}

function saveTemplateEdit(id) {
  const t = store.getTemplate(id);
  const title = document.getElementById("edit-title")?.value;
  const help = document.getElementById("edit-help")?.value;
  const completion = document.getElementById("edit-completion")?.value;

  if (title) t.title = title;
  if (help) t.helpPhrase = help;
  if (completion) t.completionPhrase = completion;

  t.steps.forEach((st, i) => {
    const val = document.getElementById(`edit-step-${i}`)?.value;
    if (val) st.instruction = val;
  });

  store.save();
  showToast("Template updated successfully!");
  editingTemplateId = null;
  renderStaffContent();
}

// Staff Actions
function moveSchedule(idx, dir) {
  const item = store.schedule.splice(idx, 1)[0];
  store.schedule.splice(idx + dir, 0, item);
  store.save();
  renderStaffContent();
}

function removeSchedule(idx) {
  store.schedule.splice(idx, 1);
  store.save();
  showToast("Task removed from shift queue");
  renderStaffContent();
}

function clearScheduleQueue() {
  if (confirm("Are you sure you want to clear all tasks from today's schedule?")) {
    store.schedule = [];
    activeSchedule = null;
    store.save();
    showToast("Today's shift queue cleared");
    renderStaffContent();
  }
}

function addScheduleItem() {
  openAddTaskModal();
}

function openAddTaskModal() {
  const modal = document.getElementById("add-task-modal");
  const select = document.getElementById("add-task-template-select");
  const input = document.getElementById("add-task-name-input");
  if (!modal || !select || !input) return;

  // Populate templates dropdown
  select.innerHTML = store.templates.map(t => `<option value="${t.id}">${t.title} (${t.steps.length} steps)</option>`).join("");

  // Default name to first template title
  if (store.templates.length > 0) {
    input.value = store.templates[0].title;
  } else {
    input.value = "New Workstation Task";
  }

  modal.style.display = "flex";
  setTimeout(() => {
    input.focus();
    input.select();
  }, 100);
}

function onTemplateSelectChange() {
  const select = document.getElementById("add-task-template-select");
  const input = document.getElementById("add-task-name-input");
  if (!select || !input) return;
  const t = store.getTemplate(select.value);
  if (t) {
    input.value = t.title;
  }
}

function closeAddTaskModal() {
  const modal = document.getElementById("add-task-modal");
  if (modal) modal.style.display = "none";
}

function confirmAddTaskToToday() {
  const input = document.getElementById("add-task-name-input");
  const select = document.getElementById("add-task-template-select");
  const taskName = input?.value?.trim() || "New Workstation Task";
  const templateId = select?.value || (store.templates[0] && store.templates[0].id);
  const baseTemplate = store.getTemplate(templateId);

  if (!baseTemplate) {
    showToast("Please select a template");
    return;
  }

  let finalTemplateId = baseTemplate.id;

  // If the user customized the title, create a clone with that title
  if (taskName !== baseTemplate.title) {
    const customTemplate = JSON.parse(JSON.stringify(baseTemplate));
    customTemplate.id = "template_" + Date.now();
    customTemplate.title = taskName;
    store.templates.push(customTemplate);
    finalTemplateId = customTemplate.id;
  }

  const newScheduleItem = {
    id: "sch_" + Date.now(),
    templateId: finalTemplateId,
    status: store.schedule.length === 0 ? "IN_PROGRESS" : "PENDING",
    currentStep: 0
  };

  store.schedule.push(newScheduleItem);
  store.save();

  if (!activeSchedule || store.schedule.length === 1) {
    activeSchedule = newScheduleItem;
  }

  closeAddTaskModal();
  showToast(`Added "${taskName}" to today's schedule!`);
  renderStaffContent();
}

function renameScheduleTask(idx) {
  const item = store.schedule[idx];
  if (!item) return;
  const t = store.getTemplate(item.templateId);
  const newName = prompt(`Enter new name for task #${idx + 1}:`, t.title);
  if (newName && newName.trim() && newName.trim() !== t.title) {
    // If template is used across multiple places, create a clone so we don't accidentally rename other items
    const isShared = store.templates.some(other => other.id === t.id && store.schedule.filter(s => s.templateId === t.id).length > 1);
    if (isShared) {
      const cloned = JSON.parse(JSON.stringify(t));
      cloned.id = "template_" + Date.now();
      cloned.title = newName.trim();
      store.templates.push(cloned);
      item.templateId = cloned.id;
    } else {
      t.title = newName.trim();
    }
    store.save();
    showToast(`Task renamed to "${newName.trim()}"`);
    renderStaffContent();
  }
}

function resetTaskStep(idx) {
  store.schedule[idx].status = "IN_PROGRESS";
  store.schedule[idx].currentStep = 0;
  activeSchedule = store.schedule[idx];
  currentStepIndex = 0;
  store.save();
  showToast("Task reset to Step 1!");
  renderStaffContent();
}

function forceComplete(idx) {
  store.schedule[idx].status = "COMPLETED";
  store.save();
  showToast("Task force-marked as COMPLETED");
  renderStaffContent();
}

function duplicateTemplate(idx) {
  const original = store.templates[idx];
  const copy = JSON.parse(JSON.stringify(original));
  copy.id = "template_" + Date.now();
  copy.title = original.title + " (Copy)";
  store.templates.push(copy);
  store.save();
  showToast("Template copied");
  renderStaffContent();
}

function createNewTemplate() {
  const newT = {
    id: "template_" + Date.now(),
    title: "New Packaging Task",
    description: "Custom workstation task",
    estimatedMinutes: 10,
    helpPhrase: "I need help with this step. Please assist me.",
    completionPhrase: "I have finished packing this parcel. Please check.",
    steps: [
      { order: 1, instruction: "Collect the required carton and materials.", image: "assets/step1.svg" },
      { order: 2, instruction: "Place item inside and verify checklist.", image: "assets/step3.svg" }
    ],
    checklist: [
      { id: "c_new1", prompt: "Item verified and packed" },
      { id: "c_new2", prompt: "Box sealed and labelled" }
    ]
  };
  store.templates.push(newT);
  store.save();
  showToast("New template created!");
  editingTemplateId = newT.id;
  renderStaffContent();
}

function deleteTemplate(idx) {
  if (store.templates.length <= 1) {
    showToast("Must keep at least 1 template.");
    return;
  }
  store.templates.splice(idx, 1);
  store.save();
  showToast("Template deleted");
  renderStaffContent();
}

function exportData() {
  const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(store));
  const dlAnchorElem = document.createElement('a');
  dlAnchorElem.setAttribute("href", dataStr);
  dlAnchorElem.setAttribute("download", `dss_workstation_backup_${Date.now()}.dssbundle`);
  dlAnchorElem.click();
  showToast("Backup bundle exported!");
}

function restoreDefaults() {
  store.templates = JSON.parse(JSON.stringify(SEED_TEMPLATES));
  store.schedule = [
    { id: "sch_1", templateId: "template_pack_parcel_001", status: "IN_PROGRESS", currentStep: 0 },
    { id: "sch_2", templateId: "template_sort_supplies_002", status: "PENDING", currentStep: 0 },
    { id: "sch_3", templateId: "template_clean_area_003", status: "PENDING", currentStep: 0 }
  ];
  store.save();
  activeSchedule = store.schedule[0];
  showToast("Default workstation data restored!");
  renderStaffContent();
}

function updateStaffPin() {
  const current = document.getElementById("pin-current")?.value;
  const newPin = document.getElementById("pin-new")?.value;
  const confirmPin = document.getElementById("pin-confirm")?.value;

  if (current !== store.staffPin) {
    showToast("Current PIN is incorrect.");
    return;
  }
  if (!newPin || newPin.length < 4) {
    showToast("New PIN must be at least 4 digits.");
    return;
  }
  if (newPin !== confirmPin) {
    showToast("New PIN and Confirm PIN do not match.");
    return;
  }

  store.staffPin = newPin;
  store.save();
  showToast("Staff PIN updated successfully!");
  renderStaffContent();
}

function exitStaff() {
  isStaffOpen = false;
  renderApp();
}

// Initial render
window.addEventListener("DOMContentLoaded", () => {
  renderApp();
});
