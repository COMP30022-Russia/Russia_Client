package com.comp30022.team_russia.assist.features.call

data class JitsiStartArgs (
    val type: JitsiStartType = JitsiStartType.Voice,
    val room: String = ""
)

enum class JitsiStartType {
    Voice,
    VideoBackCamera
}
