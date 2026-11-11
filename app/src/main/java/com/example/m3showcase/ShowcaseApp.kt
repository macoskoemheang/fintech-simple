package com.example.m3showcase

import android.app.Application
import com.example.m3showcase.core.di.coreModule
import com.example.m3showcase.feature.accounts.di.accountsModule
import com.example.m3showcase.feature.auth.di.authModule
import com.example.m3showcase.feature.home.di.homeModule
import com.example.m3showcase.feature.receipt.di.receiptModule
import com.example.m3showcase.feature.receive.di.receiveModule
import com.example.m3showcase.feature.settings.di.settingsModule
import com.example.m3showcase.feature.showcase.di.showcaseModule
import com.example.m3showcase.feature.transfer.di.transferModule
import com.example.m3showcase.main.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ShowcaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ShowcaseApp)
            modules(
                coreModule,
                mainModule,
                authModule,
                homeModule,
                accountsModule,
                transferModule,
                receiptModule,
                receiveModule,
                settingsModule,
                showcaseModule,
            )
        }
    }
}
