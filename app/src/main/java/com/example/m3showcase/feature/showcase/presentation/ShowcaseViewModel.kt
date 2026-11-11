package com.example.m3showcase.feature.showcase.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.feature.showcase.domain.usecase.GetShowcaseContentUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.m3showcase.feature.showcase.domain.model.ChatMessage
import com.example.m3showcase.feature.showcase.domain.model.MessageSender
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShowcaseViewModel(
    private val getContent: GetShowcaseContentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ShowcaseState())
    val state: StateFlow<ShowcaseState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ShowcaseEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<ShowcaseEffect> = _effect.asSharedFlow()

    init { load() }

    fun onIntent(intent: ShowcaseIntent) {
        when (intent) {
            is ShowcaseIntent.OnTabSelected -> _state.update { it.copy(selectedTab = intent.tab) }
            is ShowcaseIntent.OnElementSelected -> _state.update { it.copy(selectedElement = intent.element) }
            ShowcaseIntent.OnHeroClicked -> _state.update { it.copy(heroMorphed = !it.heroMorphed) }
            is ShowcaseIntent.OnDemoClicked -> _effect.tryEmit(ShowcaseEffect.ShowMessage(intent.message))
            ShowcaseIntent.OnRetry -> load()
            is ShowcaseIntent.OnPaletteChanged -> _state.update { it.copy(activePalette = intent.palette) }

            // Chat Intents
            is ShowcaseIntent.OnChatInputChanged -> _state.update { it.copy(chatInputText = intent.text) }
            ShowcaseIntent.OnSendChatMessage -> sendChatMessage()
            ShowcaseIntent.OnToggleVoiceRecording -> toggleVoiceRecording()
            ShowcaseIntent.OnTogglePhotoPicker -> _state.update { it.copy(showPhotoPicker = !it.showPhotoPicker, showEmojiPicker = false) }
            is ShowcaseIntent.OnPhotoSelected -> {
                _state.update { it.copy(selectedPhotoUrl = intent.url, showPhotoPicker = false) }
                _effect.tryEmit(ShowcaseEffect.ShowMessage("Photo selected"))
            }
            ShowcaseIntent.OnTakePhoto -> {
                val photoUrl = "https://picsum.photos/400/300?random=${System.currentTimeMillis()}"
                _state.update { it.copy(selectedPhotoUrl = photoUrl, showPhotoPicker = false) }
                _effect.tryEmit(ShowcaseEffect.ShowMessage("Photo captured from camera!"))
            }
            ShowcaseIntent.OnToggleEmojiPicker -> _state.update { it.copy(showEmojiPicker = !it.showEmojiPicker, showPhotoPicker = false) }
            is ShowcaseIntent.OnEmojiSelected -> {
                _state.update { it.copy(chatInputText = it.chatInputText + intent.emoji) }
            }
        }
    }

    private fun sendChatMessage() {
        val s = _state.value
        val text = s.chatInputText.trim()
        val photoUrl = s.selectedPhotoUrl
        if (text.isEmpty() && photoUrl == null) return

        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            text = text,
            sender = MessageSender.User,
            timestamp = now,
            imageUrl = photoUrl
        )

        val updated = s.chatMessages + userMsg
        _state.update {
            it.copy(
                chatMessages = updated,
                chatInputText = "",
                selectedPhotoUrl = null,
                showEmojiPicker = false,
                showPhotoPicker = false
            )
        }

        // Simulate expressive AI assistant response
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val replyText = when {
                photoUrl != null -> "I received your photo! Material 3 Expressive shapes bring media to life."
                text.contains("hello", ignoreCase = true) || text.contains("hi", ignoreCase = true) ->
                    "Hello! How do you like the new Expressive UI?"
                else -> "Expressive response: \"$text\" processed with M3 bouncy spring animations!"
            }
            val replyMsg = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                text = replyText,
                sender = MessageSender.Assistant,
                timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            )
            _state.update { it.copy(chatMessages = it.chatMessages + replyMsg) }
        }
    }

    private fun toggleVoiceRecording() {
        val currentlyRecording = _state.value.isRecordingVoice
        if (currentlyRecording) {
            // Stop recording & send voice note
            val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            val duration = if (_state.value.recordingDurationSec == 0) 4 else _state.value.recordingDurationSec
            val voiceMsg = ChatMessage(
                id = System.currentTimeMillis().toString(),
                text = "Voice note",
                sender = MessageSender.User,
                timestamp = now,
                isVoice = true,
                voiceDurationSec = duration
            )
            _state.update {
                it.copy(
                    isRecordingVoice = false,
                    recordingDurationSec = 0,
                    chatMessages = it.chatMessages + voiceMsg
                )
            }
            _effect.tryEmit(ShowcaseEffect.ShowMessage("Voice note sent!"))
        } else {
            // Start recording
            _state.update { it.copy(isRecordingVoice = true, recordingDurationSec = 3, showPhotoPicker = false, showEmojiPicker = false) }
            _effect.tryEmit(ShowcaseEffect.ShowMessage("Recording voice note..."))
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getContent()
                .onSuccess { c -> _state.update { it.copy(isLoading = false, content = c) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Something went wrong") } }
        }
    }
}
