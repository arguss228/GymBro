package com.gymbro.app

import android.app.Application
import com.gymbro.app.data.seed.SeedRunner
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltAndroidApp
class GymBroApplication : Application() {

    @Inject lateinit var seedRunner: SeedRunner

    @Inject lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Заполняет БД при первом запуске. Идемпотентно — можно звать каждый раз.
        seedRunner.runIfNeeded(appScope)
    }
}
