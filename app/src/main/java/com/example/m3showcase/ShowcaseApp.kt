package com.example.m3showcase

import android.app.Application
import com.example.m3showcase.core.di.coreModule
import com.example.m3showcase.feature.accounts.di.accountsModule
import com.example.m3showcase.feature.auth.di.authModule
import com.example.m3showcase.feature.home.di.homeModule
import com.example.m3showcase.feature.notifications.di.notificationsModule
import com.example.m3showcase.feature.receipt.di.receiptModule
import com.example.m3showcase.feature.receive.di.receiveModule
import com.example.m3showcase.feature.settings.di.settingsModule
import com.example.m3showcase.feature.showcase.di.showcaseModule
import com.example.m3showcase.feature.transfer.di.transferModule
import com.example.m3showcase.main.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.example.m3showcase.feature.payments.di.paymentsModule
import com.example.m3showcase.feature.deposits.di.depositsModule
import com.example.m3showcase.feature.loans.di.loansModule
import com.example.m3showcase.feature.report.di.reportModule
import com.example.m3showcase.feature.locator.di.locatorModule

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
                notificationsModule,
                paymentsModule,
                depositsModule,
                loansModule,
                reportModule,
                locatorModule,
            )
        }
    }
}
