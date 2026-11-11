package com.example.m3showcase.feature.showcase.presentation

import androidx.compose.runtime.Immutable
import com.example.m3showcase.feature.showcase.domain.model.ChatMessage
import com.example.m3showcase.feature.showcase.domain.model.ExpressiveElement
import com.example.m3showcase.feature.showcase.domain.model.MessageSender
import com.example.m3showcase.feature.showcase.domain.model.ShowcaseContent
import com.example.m3showcase.feature.showcase.domain.model.ShowcaseTab

@Immutable
data class ShowcaseState(
    val isLoading: Boolean = true,
    val content: ShowcaseContent? = null,
    val error: String? = null,
    val selectedTab: ShowcaseTab = ShowcaseTab.Tactics,
    val selectedElement: ExpressiveElement = ExpressiveElement.Color,
    val heroMorphed: Boolean = false,
    val activePalette: String = "Vibrant",
    val chatMessages: List<ChatMessage> = initialChatMessages(),
    val chatInputText: String = "",
    val isRecordingVoice: Boolean = false,
    val recordingDurationSec: Int = 0,
    val showPhotoPicker: Boolean = false,
    val showEmojiPicker: Boolean = false,
    val selectedPhotoUrl: String? = null
)

sealed interface ShowcaseIntent {
    data class OnTabSelected(val tab: ShowcaseTab) : ShowcaseIntent
    data class OnElementSelected(val element: ExpressiveElement) : ShowcaseIntent
    data object OnHeroClicked : ShowcaseIntent
    data class OnDemoClicked(val message: String) : ShowcaseIntent
    data object OnRetry : ShowcaseIntent
    data class OnPaletteChanged(val palette: String) : ShowcaseIntent

    // Chat Intents
    data class OnChatInputChanged(val text: String) : ShowcaseIntent
    data object OnSendChatMessage : ShowcaseIntent
    data object OnToggleVoiceRecording : ShowcaseIntent
    data object OnTogglePhotoPicker : ShowcaseIntent
    data class OnPhotoSelected(val url: String) : ShowcaseIntent
    data object OnTakePhoto : ShowcaseIntent
    data object OnToggleEmojiPicker : ShowcaseIntent
    data class OnEmojiSelected(val emoji: String) : ShowcaseIntent
}

sealed interface ShowcaseEffect {
    data class ShowMessage(val text: String) : ShowcaseEffect
}

private fun initialChatMessages(): List<ChatMessage> = listOf(
    ChatMessage(
        id = "1",
        text = "Welcome to Material 3 Expressive Chat! Try taking a photo, picking an image, recording voice, or adding emojis below.",
        sender = MessageSender.Assistant,
        timestamp = "10:30 AM"
    ),
    ChatMessage(
        id = "2",
        text = "The chat bubbles use expressive shapes and vibrant containers!",
        sender = MessageSender.Assistant,
        timestamp = "10:31 AM"
    )
)
