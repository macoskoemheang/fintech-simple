package com.example.m3showcase.feature.settings.di

import com.example.m3showcase.feature.settings.presentation.SettingsViewModel
import com.example.m3showcase.feature.settings.presentation.theme.ThemeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ThemeViewModel)
}
