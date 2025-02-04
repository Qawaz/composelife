/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alexvanyo.composelife

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.DisposableEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.alexvanyo.composelife.resourcestate.isSuccess
import com.alexvanyo.composelife.ui.app.ComposeLifeApp
import com.alexvanyo.composelife.ui.app.theme.ComposeLifeTheme
import com.alexvanyo.composelife.ui.app.theme.shouldUseDarkTheme
import dagger.hilt.EntryPoints
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint(AppCompatActivity::class)
class MainActivity : Hilt_MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val mainActivityEntryPoint = EntryPoints.get(this, MainActivityHiltEntryPoint::class.java)

        // Keep the splash screen on screen until we've loaded preferences
        splashScreen.setKeepOnScreenCondition {
            !mainActivityEntryPoint.composeLifePreferences.loadedPreferencesState.isSuccess()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            with(mainActivityEntryPoint) {
                val darkTheme = shouldUseDarkTheme()
                DisposableEffect(darkTheme) {
                    enableEdgeToEdge()
                    onDispose {}
                }

                ComposeLifeTheme(darkTheme) {
                    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
                    ComposeLifeApp(calculateWindowSizeClass(this@MainActivity))
                }
            }
        }
    }
}
