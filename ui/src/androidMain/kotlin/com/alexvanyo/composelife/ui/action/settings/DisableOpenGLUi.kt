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
@file:Suppress("MatchingDeclarationName")

package com.alexvanyo.composelife.ui.action.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alexvanyo.composelife.preferences.di.ComposeLifePreferencesProvider
import com.alexvanyo.composelife.preferences.di.LoadedComposeLifePreferencesProvider
import com.alexvanyo.composelife.ui.R
import com.alexvanyo.composelife.ui.component.LabeledSwitch
import com.alexvanyo.composelife.ui.entrypoints.WithPreviewDependencies
import com.alexvanyo.composelife.ui.theme.ComposeLifeTheme
import com.alexvanyo.composelife.ui.util.ThemePreviews
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(ActivityComponent::class)
interface DisableOpenGLUiHiltEntryPoint :
    ComposeLifePreferencesProvider

interface DisableOpenGLUiLocalEntryPoint :
    LoadedComposeLifePreferencesProvider

context(DisableOpenGLUiHiltEntryPoint, DisableOpenGLUiLocalEntryPoint)
@Composable
fun DisableOpenGLUi(
    modifier: Modifier = Modifier,
) {
    DisableOpenGLUi(
        disableOpenGL = preferences.disableOpenGL,
        setDisableOpenGL = composeLifePreferences::setDisableOpenGL,
        modifier = modifier,
    )
}

@Composable
fun DisableOpenGLUi(
    disableOpenGL: Boolean,
    setDisableOpenGL: suspend (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    LabeledSwitch(
        label = stringResource(R.string.disable_opengl),
        checked = disableOpenGL,
        onCheckedChange = { disabled ->
            coroutineScope.launch {
                setDisableOpenGL(disabled)
            }
        },
        modifier = modifier,
    )
}

@ThemePreviews
@Composable
fun DisableOpenGLUiDisabledPreview() {
    WithPreviewDependencies {
        ComposeLifeTheme {
            DisableOpenGLUi(
                disableOpenGL = true,
                setDisableOpenGL = {},
            )
        }
    }
}

@ThemePreviews
@Composable
fun DisableOpenGLUiEnabledPreview() {
    WithPreviewDependencies {
        ComposeLifeTheme {
            DisableOpenGLUi(
                disableOpenGL = false,
                setDisableOpenGL = {},
            )
        }
    }
}
