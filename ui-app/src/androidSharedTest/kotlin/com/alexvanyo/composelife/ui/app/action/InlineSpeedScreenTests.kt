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

package com.alexvanyo.composelife.ui.app.action

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.input.ImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alexvanyo.composelife.ui.app.R
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.math.log2
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class InlineSpeedScreenTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val context: Context get() = composeTestRule.activity

    @Test
    fun target_steps_per_second_is_displayed_correctly() {
        composeTestRule.setContent {
            var targetStepsPerSecond by remember { mutableStateOf(60.0) }
            var generationsPerStep by remember { mutableStateOf(1) }

            InlineSpeedScreen(
                targetStepsPerSecond = targetStepsPerSecond,
                setTargetStepsPerSecond = { targetStepsPerSecond = it },
                generationsPerStep = generationsPerStep,
                setGenerationsPerStep = { generationsPerStep = it },
            )
        }

        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.target_steps_per_second_label)),
            )
            .assertTextContains(context.getString(R.string.target_steps_per_second_value, 60.0))
            .assertIsNotFocused()
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.target_steps_per_second_label_and_value, 60.0),
            )
            .assert(hasProgressBarRangeInfo(ProgressBarRangeInfo(current = log2(60f), range = 0f..8f)))
    }

    @Test
    fun target_steps_per_second_updates_correctly_with_slider() {
        composeTestRule.setContent {
            var targetStepsPerSecond by remember { mutableStateOf(60.0) }
            var generationsPerStep by remember { mutableStateOf(1) }

            InlineSpeedScreen(
                targetStepsPerSecond = targetStepsPerSecond,
                setTargetStepsPerSecond = { targetStepsPerSecond = it },
                generationsPerStep = generationsPerStep,
                setGenerationsPerStep = { generationsPerStep = it },
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.target_steps_per_second_label_and_value, 60.0),
            )
            .performSemanticsAction(SemanticsActions.SetProgress) {
                it(8f)
            }

        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.target_steps_per_second_label)),
            )
            .assertTextContains(context.getString(R.string.target_steps_per_second_value, 256.0))
            .assertIsNotFocused()
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.target_steps_per_second_label_and_value, 256.0),
            )
            .assert(hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 8f, range = 0f..8f)))
    }

    @Test
    fun target_steps_per_second_updates_correctly_with_text() {
        composeTestRule.setContent {
            var targetStepsPerSecond by remember { mutableStateOf(60.0) }
            var generationsPerStep by remember { mutableStateOf(1) }

            InlineSpeedScreen(
                targetStepsPerSecond = targetStepsPerSecond,
                setTargetStepsPerSecond = { targetStepsPerSecond = it },
                generationsPerStep = generationsPerStep,
                setGenerationsPerStep = { generationsPerStep = it },
            )
        }

        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.target_steps_per_second_label)),
            )
            .performTextReplacement("256")
        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.target_steps_per_second_label)),
            )
            .performImeAction()

        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.target_steps_per_second_label)),
            )
            .assertTextContains(context.getString(R.string.target_steps_per_second_value, 256.0))
            .assertIsNotFocused()
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.target_steps_per_second_label_and_value, 256.0),
            )
            .assert(hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 8f, range = 0f..8f)))
    }

    @Test
    fun generations_per_step_is_displayed_correctly() {
        composeTestRule.setContent {
            var targetStepsPerSecond by remember { mutableStateOf(60.0) }
            var generationsPerStep by remember { mutableStateOf(1) }

            InlineSpeedScreen(
                targetStepsPerSecond = targetStepsPerSecond,
                setTargetStepsPerSecond = { targetStepsPerSecond = it },
                generationsPerStep = generationsPerStep,
                setGenerationsPerStep = { generationsPerStep = it },
            )
        }

        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.generations_per_step_label)),
            )
            .assertTextContains(context.getString(R.string.generations_per_step_value, 1))
            .assertIsNotFocused()
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.generations_per_step_label_and_value, 1),
            )
            .assert(hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 0f, range = 0f..8f, steps = 7)))
    }

    @Test
    fun generations_per_step_updates_correctly_with_slider() {
        composeTestRule.setContent {
            var targetStepsPerSecond by remember { mutableStateOf(60.0) }
            var generationsPerStep by remember { mutableStateOf(1) }

            InlineSpeedScreen(
                targetStepsPerSecond = targetStepsPerSecond,
                setTargetStepsPerSecond = { targetStepsPerSecond = it },
                generationsPerStep = generationsPerStep,
                setGenerationsPerStep = { generationsPerStep = it },
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.generations_per_step_label_and_value, 1),
            )
            .performSemanticsAction(SemanticsActions.SetProgress) {
                it(8f)
            }

        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.generations_per_step_label)),
            )
            .assertTextContains(context.getString(R.string.generations_per_step_value, 256))
            .assertIsNotFocused()
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.generations_per_step_label_and_value, 256),
            )
            .assert(hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 8f, range = 0f..8f, steps = 7)))
    }

    @Test
    fun generations_per_step_updates_correctly_with_text() {
        composeTestRule.setContent {
            var targetStepsPerSecond by remember { mutableStateOf(60.0) }
            var generationsPerStep by remember { mutableStateOf(1) }

            InlineSpeedScreen(
                targetStepsPerSecond = targetStepsPerSecond,
                setTargetStepsPerSecond = { targetStepsPerSecond = it },
                generationsPerStep = generationsPerStep,
                setGenerationsPerStep = { generationsPerStep = it },
            )
        }

        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.generations_per_step_label)),
            )
            .performTextReplacement("256")
        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.generations_per_step_label)),
            )
            .performImeAction()

        composeTestRule
            .onNode(
                hasSetTextAction() and hasImeAction(ImeAction.Done) and
                    hasText(context.getString(R.string.generations_per_step_label)),
            )
            .assertTextContains(context.getString(R.string.generations_per_step_value, 256))
            .assertIsNotFocused()
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.generations_per_step_label_and_value, 256),
            )
            .assert(hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 8f, range = 0f..8f, steps = 7)))
    }
}
