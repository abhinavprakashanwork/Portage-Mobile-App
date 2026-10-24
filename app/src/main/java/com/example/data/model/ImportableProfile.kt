package com.example.data.model

data class ImportableProfile(
  val id: String,
  val name: String,
  val type: String,
  val savedDate: String,
  val workflows: List<PresetWorkflowData> = emptyList(),
  val remaps: List<PresetRemapData> = emptyList(),
  val historyTransmissions: List<String> = emptyList()
)

data class PresetWorkflowData(
  val name: String,
  val steps: List<PresetStepData> = emptyList()
)

data class PresetStepData(
  val title: String,
  val actionType: String,
  val badges: String,
  val payload: String
)

data class PresetRemapData(
  val name: String,
  val triggerKey: String,
  val targetKey: String,
  val isActive: Boolean = true
)

val DEFAULT_IMPORTABLE_PROFILES = listOf(
  ImportableProfile(
    id = "profile_jason_laptop",
    name = "Jason's Laptop",
    type = "Laptop",
    savedDate = "28 AUG 2026",
    workflows = listOf(
      PresetWorkflowData(
        name = "Deployment Pipeline",
        steps = listOf(
          PresetStepData(title = "Run Test Suite", actionType = "KEY_SHORTCUT", badges = "Ctrl, Shift, T", payload = "Ctrl+Shift+T"),
          PresetStepData(title = "Wait for Build", actionType = "DELAY", badges = "10s", payload = "10000"),
          PresetStepData(title = "Push to Production", actionType = "TEXT_STREAM", badges = "git push prod", payload = "git push origin main --tags")
        )
      ),
      PresetWorkflowData(
        name = "Dev Environment Setup",
        steps = listOf(
          PresetStepData(title = "Launch Terminal", actionType = "KEY_SHORTCUT", badges = "Ctrl, Alt, T", payload = "Ctrl+Alt+T"),
          PresetStepData(title = "Start Docker Engine", actionType = "TEXT_STREAM", badges = "docker start", payload = "docker compose up -d")
        )
      ),
      PresetWorkflowData(
        name = "Media Automation",
        steps = listOf(
          PresetStepData(title = "Toggle Play / Pause", actionType = "KEY_SHORTCUT", badges = "Media Play", payload = "MediaPlayPause"),
          PresetStepData(title = "Volume Up", actionType = "KEY_SHORTCUT", badges = "Vol +", payload = "AudioVolumeUp")
        )
      )
    ),
    remaps = listOf(
      PresetRemapData(name = "Play / Pause", triggerKey = "Vol +", targetKey = "Media Play"),
      PresetRemapData(name = "Mute Toggle", triggerKey = "Vol -", targetKey = "Mute Audio"),
      PresetRemapData(name = "Take Screenshot", triggerKey = "Vol +", targetKey = "PrintScreen")
    ),
    historyTransmissions = listOf(
      "git push origin main --tags",
      "docker compose -f docker-compose.prod.yml up -d",
      "https://github.com/org/portage/pull/42",
      "npm run build && npm test"
    )
  ),
  ImportableProfile(
    id = "profile_sara_computer",
    name = "Sara's Computer",
    type = "Desktop",
    savedDate = "14 JUL 2026",
    workflows = listOf(
      PresetWorkflowData(
        name = "Design System Sync",
        steps = listOf(
          PresetStepData(title = "Launch Figma", actionType = "KEY_SHORTCUT", badges = "Super, F", payload = "Super+F"),
          PresetStepData(title = "Sync Tokens", actionType = "TEXT_STREAM", badges = "tokens sync", payload = "npm run tokens:export"),
          PresetStepData(title = "Export Assets", actionType = "KEY_SHORTCUT", badges = "Ctrl, Shift, E", payload = "Ctrl+Shift+E")
        )
      ),
      PresetWorkflowData(
        name = "Creative Suite Setup",
        steps = listOf(
          PresetStepData(title = "Start Editor", actionType = "KEY_SHORTCUT", badges = "Ctrl, O", payload = "Ctrl+O"),
          PresetStepData(title = "Focus Workspace", actionType = "DELAY", badges = "5s", payload = "5000")
        )
      )
    ),
    remaps = listOf(
      PresetRemapData(name = "Next Track", triggerKey = "Vol +", targetKey = "Next Track"),
      PresetRemapData(name = "Previous Track", triggerKey = "Vol -", targetKey = "Prev Track")
    ),
    historyTransmissions = listOf(
      "Palette: #0F5BBE, #BC82F3, #F5B9EA, #FF6778",
      "https://dribbble.com/shots/portage-remote-relay",
      "Final deliverables exported to /Portage/Exports/v2"
    )
  )
)
