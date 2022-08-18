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

package com.alexvanyo.composelife.ui.entrypoints

import androidx.compose.runtime.Composable
import com.alexvanyo.composelife.algorithm.GameOfLifeAlgorithm
import com.alexvanyo.composelife.algorithm.NaiveGameOfLifeAlgorithm
import com.alexvanyo.composelife.algorithm.di.GameOfLifeAlgorithmProvider
import com.alexvanyo.composelife.dispatchers.ComposeLifeDispatchers
import com.alexvanyo.composelife.dispatchers.DefaultComposeLifeDispatchers
import com.alexvanyo.composelife.dispatchers.di.ComposeLifeDispatchersProvider
import com.alexvanyo.composelife.preferences.ComposeLifePreferences
import com.alexvanyo.composelife.preferences.CurrentShape
import com.alexvanyo.composelife.preferences.LoadedComposeLifePreferences
import com.alexvanyo.composelife.preferences.TestComposeLifePreferences
import com.alexvanyo.composelife.preferences.di.ComposeLifePreferencesProvider
import com.alexvanyo.composelife.preferences.di.LoadedComposeLifePreferencesProvider
import com.alexvanyo.composelife.random.di.RandomProvider
import com.alexvanyo.composelife.ui.InteractiveCellUniverseHiltEntryPoint
import com.alexvanyo.composelife.ui.InteractiveCellUniverseOverlayHiltEntryPoint
import com.alexvanyo.composelife.ui.InteractiveCellUniverseOverlayLocalEntryPoint
import com.alexvanyo.composelife.ui.action.CellUniverseActionCardHiltEntryPoint
import com.alexvanyo.composelife.ui.action.settings.AlgorithmImplementationUiHiltEntryPoint
import com.alexvanyo.composelife.ui.action.settings.AlgorithmImplementationUiLocalEntryPoint
import com.alexvanyo.composelife.ui.action.settings.CellShapeConfigUiHiltEntryPoint
import com.alexvanyo.composelife.ui.action.settings.CellShapeConfigUiLocalEntryPoint
import com.alexvanyo.composelife.ui.action.settings.CellStatePreviewUiLocalEntryPoint
import com.alexvanyo.composelife.ui.action.settings.DarkThemeConfigUiHiltEntryPoint
import com.alexvanyo.composelife.ui.action.settings.DarkThemeConfigUiLocalEntryPoint
import com.alexvanyo.composelife.ui.action.settings.DisableAGSLUiHiltEntryPoint
import com.alexvanyo.composelife.ui.action.settings.DisableAGSLUiLocalEntryPoint
import com.alexvanyo.composelife.ui.action.settings.DisableOpenGLUiHiltEntryPoint
import com.alexvanyo.composelife.ui.action.settings.DisableOpenGLUiLocalEntryPoint
import com.alexvanyo.composelife.ui.action.settings.FullscreenSettingsScreenHiltEntryPoint
import com.alexvanyo.composelife.ui.action.settings.FullscreenSettingsScreenLocalEntryPoint
import com.alexvanyo.composelife.ui.action.settings.InlineSettingsScreenHiltEntryPoint
import com.alexvanyo.composelife.ui.action.settings.InlineSettingsScreenLocalEntryPoint
import com.alexvanyo.composelife.ui.action.settings.SettingUiHiltEntryPoint
import com.alexvanyo.composelife.ui.action.settings.SettingUiLocalEntryPoint
import com.alexvanyo.composelife.ui.cells.CellWindowLocalEntryPoint
import com.alexvanyo.composelife.ui.cells.InteractableCellsLocalEntryPoint
import com.alexvanyo.composelife.ui.cells.NonInteractableCellsLocalEntryPoint
import com.alexvanyo.composelife.ui.component.GameOfLifeProgressIndicatorHiltEntryPoint
import com.alexvanyo.composelife.ui.component.GameOfLifeProgressIndicatorLocalEntryPoint
import kotlin.random.Random

interface PreviewEntryPoint :
    AlgorithmImplementationUiHiltEntryPoint,
    AlgorithmImplementationUiLocalEntryPoint,
    CellShapeConfigUiHiltEntryPoint,
    CellShapeConfigUiLocalEntryPoint,
    CellStatePreviewUiLocalEntryPoint,
    CellWindowLocalEntryPoint,
    CellUniverseActionCardHiltEntryPoint,
    DarkThemeConfigUiHiltEntryPoint,
    DarkThemeConfigUiLocalEntryPoint,
    DisableAGSLUiHiltEntryPoint,
    DisableAGSLUiLocalEntryPoint,
    DisableOpenGLUiHiltEntryPoint,
    DisableOpenGLUiLocalEntryPoint,
    FullscreenSettingsScreenHiltEntryPoint,
    FullscreenSettingsScreenLocalEntryPoint,
    GameOfLifeProgressIndicatorHiltEntryPoint,
    GameOfLifeProgressIndicatorLocalEntryPoint,
    InlineSettingsScreenHiltEntryPoint,
    InlineSettingsScreenLocalEntryPoint,
    InteractableCellsLocalEntryPoint,
    InteractiveCellUniverseHiltEntryPoint,
    InteractiveCellUniverseOverlayHiltEntryPoint,
    InteractiveCellUniverseOverlayLocalEntryPoint,
    NonInteractableCellsLocalEntryPoint,
    SettingUiHiltEntryPoint,
    SettingUiLocalEntryPoint

/**
 * Provides fake implementations for the entry points passed to [content] as context receivers.
 *
 * This is useful for providing dependencies to previews where the full dependency graph isn't available.
 */
@Suppress("LongParameterList")
@Composable
fun WithPreviewDependencies(
    dispatchers: ComposeLifeDispatchers = DefaultComposeLifeDispatchers(),
    gameOfLifeAlgorithm: GameOfLifeAlgorithm = NaiveGameOfLifeAlgorithm(dispatchers),
    loadedComposeLifePreferences: LoadedComposeLifePreferences = LoadedComposeLifePreferences.Defaults,
    composeLifePreferences: ComposeLifePreferences = TestComposeLifePreferences.Loaded(
        algorithmChoice = loadedComposeLifePreferences.algorithmChoice,
        currentShapeType = loadedComposeLifePreferences.currentShape.type,
        roundRectangleConfig = when (loadedComposeLifePreferences.currentShape) {
            is CurrentShape.RoundRectangle -> loadedComposeLifePreferences.currentShape as CurrentShape.RoundRectangle
        },
        darkThemeConfig = loadedComposeLifePreferences.darkThemeConfig,
        disableAGSL = loadedComposeLifePreferences.disableAGSL,
        disableOpenGL = loadedComposeLifePreferences.disableOpenGL,
    ),
    random: Random = Random(0),
    content: @Composable context(PreviewEntryPoint) () -> Unit,
) {
    val dispatchersProvider = object : ComposeLifeDispatchersProvider {
        override val dispatchers = dispatchers
    }
    val algorithmProvider = object : GameOfLifeAlgorithmProvider {
        override val gameOfLifeAlgorithm = gameOfLifeAlgorithm
    }
    val preferencesProvider = object : ComposeLifePreferencesProvider {
        override val composeLifePreferences = composeLifePreferences
    }
    val loadedPreferencesProvider = object : LoadedComposeLifePreferencesProvider {
        override val preferences: LoadedComposeLifePreferences = loadedComposeLifePreferences
    }
    val randomProvider = object : RandomProvider {
        override val random = random
    }

    val entryPoint = object :
        PreviewEntryPoint,
        ComposeLifeDispatchersProvider by dispatchersProvider,
        GameOfLifeAlgorithmProvider by algorithmProvider,
        ComposeLifePreferencesProvider by preferencesProvider,
        LoadedComposeLifePreferencesProvider by loadedPreferencesProvider,
        RandomProvider by randomProvider {}

    content(entryPoint)
}
