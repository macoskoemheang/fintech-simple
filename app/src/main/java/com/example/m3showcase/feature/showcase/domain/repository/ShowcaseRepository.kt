package com.example.m3showcase.feature.showcase.domain.repository

import com.example.m3showcase.feature.showcase.domain.model.ShowcaseContent

interface ShowcaseRepository {
    suspend fun getContent(): Result<ShowcaseContent>
}
