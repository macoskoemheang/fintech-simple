package com.example.m3showcase.feature.showcase.data

import com.example.m3showcase.feature.showcase.domain.model.BrandPerception
import com.example.m3showcase.feature.showcase.domain.model.ResearchStat
import com.example.m3showcase.feature.showcase.domain.model.ShowcaseContent
import com.example.m3showcase.feature.showcase.domain.repository.ShowcaseRepository
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** Static content for the showcase. Replace with Retrofit/Room later without touching the UI. */
class ShowcaseRepositoryImpl : ShowcaseRepository {
    override suspend fun getContent(): Result<ShowcaseContent> = runCatching {
        delay(900.milliseconds) // lets the expressive LoadingIndicator be seen
        ShowcaseContent(
            stats = listOf(
                ResearchStat("46", "research studies"),
                ResearchStat("18,000+", "participants worldwide"),
                ResearchStat("4×", "faster to spot the key button"),
                ResearchStat("Equal", "older users as fast as younger ones")
            ),
            brandPerception = listOf(
                BrandPerception("Feels more in-the-know", 32),
                BrandPerception("Feels more modern", 34),
                BrandPerception("Feels bolder", 30)
            )
        )
    }
}
