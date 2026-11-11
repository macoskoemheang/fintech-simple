package com.example.m3showcase.feature.showcase.domain.model

data class ResearchStat(val value: String, val label: String)

data class BrandPerception(val label: String, val percent: Int)

/** Main showcase navigation tabs. */
enum class ShowcaseTab(val label: String) {
    Tactics("Tactics & Hero"),
    Components("Components"),
    Chat("Expressive Chat")
}

/** The five building blocks of expressive design. */
enum class ExpressiveElement(val title: String, val tagline: String) {
    Color("Color", "Pulls attention to what matters"),
    Shape("Shape", "Builds identity and recognition"),
    Size("Size", "Creates hierarchy and reach"),
    Motion("Motion", "Springs that feel alive"),
    Containment("Containment", "Groups content with purpose")
}

data class ShowcaseContent(
    val stats: List<ResearchStat>,
    val brandPerception: List<BrandPerception>
)

enum class MessageSender { User, Assistant }

data class ChatMessage(
    val id: String,
    val text: String = "",
    val sender: MessageSender,
    val timestamp: String,
    val imageUrl: String? = null,
    val isVoice: Boolean = false,
    val voiceDurationSec: Int = 0,
    val emojiReaction: String? = null
)
