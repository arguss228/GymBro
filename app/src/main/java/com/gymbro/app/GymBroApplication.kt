package com.obsession.app

import android.app.Application
import com.obsession.app.data.seed.SeedRunner
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltAndroidApp
class ObsessionApplication : Application() {

    @Inject lateinit var seedRunner: SeedRunner

    @Inject lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Заполняет БД при первом запуске. Идемпотентно — можно звать каждый раз.
        seedRunner.runIfNeeded(appScope)
    }
}
