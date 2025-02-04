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

package com.alexvanyo.composelife.ui.app.action.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.alexvanyo.composelife.ui.app.R
import com.alexvanyo.composelife.ui.app.action.ActionCardNavigation
import com.alexvanyo.composelife.ui.app.entrypoints.WithPreviewDependencies
import com.alexvanyo.composelife.ui.app.theme.ComposeLifeTheme
import com.alexvanyo.composelife.ui.util.AnimatedContent
import com.alexvanyo.composelife.ui.util.Crossfade
import com.alexvanyo.composelife.ui.util.MobileDevicePreviews
import com.alexvanyo.composelife.ui.util.PredictiveBackState
import com.alexvanyo.composelife.ui.util.TargetState
import com.alexvanyo.composelife.ui.util.predictiveBackHandler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlin.math.roundToInt

@EntryPoint
@InstallIn(ActivityComponent::class)
interface FullscreenSettingsScreenHiltEntryPoint :
    SettingUiHiltEntryPoint

interface FullscreenSettingsScreenLocalEntryPoint :
    SettingUiLocalEntryPoint

context(FullscreenSettingsScreenHiltEntryPoint, FullscreenSettingsScreenLocalEntryPoint)
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun FullscreenSettingsScreen(
    windowSizeClass: WindowSizeClass,
    fullscreen: ActionCardNavigation.FullscreenSettings,
    onBackButtonPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentWindowSizeClass by rememberUpdatedState(windowSizeClass)

    val listScrollState = rememberScrollState()
    val detailScrollStates = SettingsCategory.values.associateWith {
        key(it) { rememberScrollState() }
    }

    fun showList() =
        when (currentWindowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> !fullscreen.showDetails
            else -> true
        }

    fun showDetail() =
        when (currentWindowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> fullscreen.showDetails
            else -> true
        }

    fun showListAndDetail() = showList() && showDetail()

    val predictiveBackState =
        predictiveBackHandler(enabled = showDetail() && !showList()) {
            fullscreen.showDetails = false
        }

    val listContent = remember(fullscreen) {
        movableContentOf {
            SettingsCategoryList(
                currentSettingsCategory = fullscreen.settingsCategory,
                showSelectedSettingsCategory = showListAndDetail(),
                listScrollState = listScrollState,
                setSettingsCategory = {
                    fullscreen.settingsCategory = it
                    fullscreen.showDetails = true
                },
                showFloatingAppBar = showListAndDetail(),
                onBackButtonPressed = onBackButtonPressed,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    val detailContent = remember(fullscreen) {
        movableContentOf { settingsCategory: SettingsCategory ->
            val detailScrollState = detailScrollStates.getValue(settingsCategory)

            SettingsCategoryDetail(
                settingsCategory = settingsCategory,
                detailScrollState = detailScrollState,
                showAppBar = !showListAndDetail(),
                onBackButtonPressed = { fullscreen.showDetails = false },
                settingToScrollTo = fullscreen.settingToScrollTo,
                onFinishedScrollingToSetting = { fullscreen.onFinishedScrollingToSetting() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (showListAndDetail()) {
        Row(modifier = modifier) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.End)),
            ) {
                listContent()
            }

            Column(
                Modifier
                    .weight(1f)
                    .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(
                        top = 4.dp,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 16.dp,
                    ),
            ) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)),
                ) {
                    Crossfade(targetState = TargetState.Single(fullscreen.settingsCategory)) { settingsCategory ->
                        detailContent(settingsCategory)
                    }
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }
    } else {
        AnimatedContent(
            targetState = when (predictiveBackState) {
                PredictiveBackState.NotRunning -> TargetState.Single(showList())
                is PredictiveBackState.Running ->
                    TargetState.InProgress(
                        current = false,
                        provisional = true,
                        progress = predictiveBackState.progress,
                    )
            },
            modifier = modifier,
        ) { showList ->
            if (showList) {
                listContent()
            } else {
                detailContent(fullscreen.settingsCategory)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("LongMethod", "LongParameterList")
@Composable
private fun SettingsCategoryList(
    currentSettingsCategory: SettingsCategory,
    showSelectedSettingsCategory: Boolean,
    listScrollState: ScrollState,
    setSettingsCategory: (SettingsCategory) -> Unit,
    showFloatingAppBar: Boolean,
    onBackButtonPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            val isElevated = listScrollState.canScrollBackward
            val elevation by animateDpAsState(targetValue = if (isElevated) 3.dp else 0.dp)

            Surface(
                tonalElevation = elevation,
                shape = RoundedCornerShape(if (showFloatingAppBar) 16.dp else 0.dp),
                modifier = Modifier
                    .then(
                        if (showFloatingAppBar) {
                            Modifier
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(
                                        WindowInsetsSides.Horizontal + WindowInsetsSides.Top,
                                    ),
                                )
                                .padding(4.dp)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (showFloatingAppBar) {
                                Modifier
                            } else {
                                Modifier.windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(
                                        WindowInsetsSides.Horizontal + WindowInsetsSides.Top,
                                    ),
                                )
                            },
                        )
                        .height(64.dp),
                ) {
                    Box(
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        PlainTooltipBox(
                            tooltip = {
                                Text(stringResource(id = R.string.back))
                            },
                        ) {
                            IconButton(
                                onClick = onBackButtonPressed,
                                modifier = Modifier.tooltipTrigger(),
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = stringResource(id = R.string.back),
                                )
                            }
                        }
                    }

                    Text(
                        stringResource(id = R.string.settings),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(listScrollState)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = 8.dp)
                .safeDrawingPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SettingsCategory.values.forEach { settingsCategory ->
                SettingsCategoryButton(
                    settingsCategory = settingsCategory,
                    showSelectedSettingsCategory = showSelectedSettingsCategory,
                    isCurrentSettingsCategory = settingsCategory == currentSettingsCategory,
                    onClick = { setSettingsCategory(settingsCategory) },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsCategoryButton(
    settingsCategory: SettingsCategory,
    showSelectedSettingsCategory: Boolean,
    isCurrentSettingsCategory: Boolean,
    onClick: () -> Unit,
) {
    val title = settingsCategory.title
    val outlinedIcon = settingsCategory.outlinedIcon
    val filledIcon = settingsCategory.filledIcon

    val isVisuallySelected = showSelectedSettingsCategory && isCurrentSettingsCategory
    val icon = if (isVisuallySelected) filledIcon else outlinedIcon

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isVisuallySelected) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        onClick = onClick,
        modifier = Modifier.semantics {
            if (showSelectedSettingsCategory) {
                selected = isCurrentSettingsCategory
            }
        },
    ) {
        Row(
            modifier = Modifier
                .sizeIn(minHeight = 64.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = null)
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

context(SettingUiHiltEntryPoint, SettingUiLocalEntryPoint)
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "LongParameterList")
@Composable
private fun SettingsCategoryDetail(
    settingsCategory: SettingsCategory,
    detailScrollState: ScrollState,
    showAppBar: Boolean,
    onBackButtonPressed: () -> Unit,
    settingToScrollTo: Setting?,
    onFinishedScrollingToSetting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        if (showAppBar) {
            val isElevated = detailScrollState.canScrollBackward
            val elevation by animateDpAsState(targetValue = if (isElevated) 3.dp else 0.dp)

            Surface(
                tonalElevation = elevation,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Top,
                            ),
                        )
                        .height(64.dp),
                ) {
                    Box(
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        PlainTooltipBox(
                            tooltip = {
                                Text(stringResource(id = R.string.back))
                            },
                        ) {
                            IconButton(
                                onClick = onBackButtonPressed,
                                modifier = Modifier.tooltipTrigger(),
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = stringResource(id = R.string.back),
                                )
                            }
                        }
                    }

                    Text(
                        text = settingsCategory.title,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .then(
                    if (showAppBar) {
                        Modifier.consumeWindowInsets(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Top,
                            ),
                        )
                    } else {
                        Modifier
                    },
                )
                .safeDrawingPadding()
                .verticalScroll(detailScrollState)
                .padding(vertical = 16.dp),
        ) {
            settingsCategory.settings.forEach { setting ->
                var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }

                SettingUi(
                    setting = setting,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .onPlaced {
                            layoutCoordinates = it
                        },
                )

                val currentOnFinishedScrollingToSetting by rememberUpdatedState(onFinishedScrollingToSetting)

                LaunchedEffect(settingToScrollTo, layoutCoordinates) {
                    val currentLayoutCoordinates = layoutCoordinates
                    if (currentLayoutCoordinates != null && settingToScrollTo == setting) {
                        detailScrollState.animateScrollTo(currentLayoutCoordinates.boundsInParent().top.roundToInt())
                        currentOnFinishedScrollingToSetting()
                    }
                }
            }
        }
    }
}

private val SettingsCategory.title: String
    @Composable
    get() = when (this) {
        SettingsCategory.Algorithm -> stringResource(id = R.string.algorithm)
        SettingsCategory.FeatureFlags -> stringResource(id = R.string.feature_flags)
        SettingsCategory.Visual -> stringResource(id = R.string.visual)
    }

private val SettingsCategory.filledIcon: ImageVector
    @Composable
    get() = when (this) {
        SettingsCategory.Algorithm -> Icons.Filled.Analytics
        SettingsCategory.FeatureFlags -> Icons.Filled.Flag
        SettingsCategory.Visual -> Icons.Filled.Palette
    }

private val SettingsCategory.outlinedIcon: ImageVector
    @Composable
    get() = when (this) {
        SettingsCategory.Algorithm -> Icons.Outlined.Analytics
        SettingsCategory.FeatureFlags -> Icons.Outlined.Flag
        SettingsCategory.Visual -> Icons.Outlined.Palette
    }

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@MobileDevicePreviews
@Composable
fun FullscreenSettingsScreenListPreview() {
    WithPreviewDependencies {
        ComposeLifeTheme {
            BoxWithConstraints {
                val size = DpSize(maxWidth, maxHeight)
                Surface {
                    FullscreenSettingsScreen(
                        windowSizeClass = WindowSizeClass.calculateFromSize(size),
                        fullscreen = ActionCardNavigation.FullscreenSettings(
                            initialSettingsCategory = SettingsCategory.Algorithm,
                            initialShowDetails = false,
                            initialSettingToScrollTo = null,
                        ),
                        onBackButtonPressed = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@MobileDevicePreviews
@Composable
fun FullscreenSettingsScreenAlgorithmPreview() {
    WithPreviewDependencies {
        ComposeLifeTheme {
            BoxWithConstraints {
                val size = DpSize(maxWidth, maxHeight)
                Surface {
                    FullscreenSettingsScreen(
                        windowSizeClass = WindowSizeClass.calculateFromSize(size),
                        fullscreen = ActionCardNavigation.FullscreenSettings(
                            initialSettingsCategory = SettingsCategory.Algorithm,
                            initialShowDetails = true,
                            initialSettingToScrollTo = null,
                        ),
                        onBackButtonPressed = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@MobileDevicePreviews
@Composable
fun FullscreenSettingsScreenVisualPreview() {
    WithPreviewDependencies {
        ComposeLifeTheme {
            BoxWithConstraints {
                val size = DpSize(maxWidth, maxHeight)
                Surface {
                    FullscreenSettingsScreen(
                        windowSizeClass = WindowSizeClass.calculateFromSize(size),
                        fullscreen = ActionCardNavigation.FullscreenSettings(
                            initialSettingsCategory = SettingsCategory.Visual,
                            initialShowDetails = true,
                            initialSettingToScrollTo = null,
                        ),
                        onBackButtonPressed = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@MobileDevicePreviews
@Composable
fun FullscreenSettingsScreenFeatureFlagsPreview() {
    WithPreviewDependencies {
        ComposeLifeTheme {
            BoxWithConstraints {
                val size = DpSize(maxWidth, maxHeight)
                Surface {
                    FullscreenSettingsScreen(
                        windowSizeClass = WindowSizeClass.calculateFromSize(size),
                        fullscreen = ActionCardNavigation.FullscreenSettings(
                            initialSettingsCategory = SettingsCategory.FeatureFlags,
                            initialShowDetails = true,
                            initialSettingToScrollTo = null,
                        ),
                        onBackButtonPressed = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
