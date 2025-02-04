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

package com.alexvanyo.composelife.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.alexvanyo.composelife.kmpandroidrunner.KmpAndroidJUnit4
import com.alexvanyo.composelife.kmpstaterestorationtester.KmpStateRestorationTester
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.test.Test

@OptIn(ExperimentalAnimationApi::class, ExperimentalTestApi::class)
@RunWith(KmpAndroidJUnit4::class)
class NavigationHostTests {

    @Test
    fun navigation_host_displays_current_entry() = runComposeUiTest {
        val id = UUID.randomUUID()
        val entry = BackstackEntry(
            value = "a",
            previous = null,
            id = id,
        )

        val navigationState = object : NavigationState<BackstackEntry<String>> {
            override val entryMap get() = mapOf(id to entry)
            override val currentEntryId get() = id
        }

        setContent {
            NavigationHost(
                navigationState = navigationState,
            ) { entry ->
                BasicText("value: ${entry.value}, id: ${entry.id}")
            }
        }

        onNodeWithText("value: a, id: $id").assertExists()
    }

    @Test
    fun navigation_host_displays_current_entry_with_multiple_entries() = runComposeUiTest {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val entry1 = BackstackEntry(
            value = "a",
            previous = null,
            id = id1,
        )
        val entry2 = BackstackEntry(
            value = "b",
            previous = entry1,
            id = id2,
        )

        val navigationState = object : BackstackState<String> {
            override val entryMap: Map<UUID, BackstackEntry<String>> = mapOf(
                id1 to entry1,
                id2 to entry2,
            )
            override val currentEntryId: UUID = id2
        }

        setContent {
            NavigationHost(
                navigationState = navigationState,
            ) { entry ->
                BasicText("value: ${entry.value}, id: ${entry.id}")
            }
        }

        onNodeWithText("value: b, id: $id2").assertExists()
    }

    @Test
    fun navigation_host_keeps_state_for_entries_in_map() = runComposeUiTest {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val entry1 = BackstackEntry(
            value = "a",
            previous = null,
            id = id1,
        )
        val entry2 = BackstackEntry(
            value = "b",
            previous = null,
            id = id2,
        )

        val backstackMap = mutableStateMapOf<UUID, BackstackEntry<String>>(
            id1 to entry1,
            id2 to entry2,
        )

        var currentEntryId by mutableStateOf(id1)

        val navigationState = object : BackstackState<String> {
            override val entryMap get() = backstackMap
            override val currentEntryId get() = currentEntryId
        }

        setContent {
            NavigationHost(
                navigationState = navigationState,
            ) { entry ->
                var count by rememberSaveable { mutableStateOf(0) }

                Column {
                    BasicText("value: ${entry.value}, id: ${entry.id}, count: $count")
                    BasicText("+", modifier = Modifier.clickable { count++ })
                }
            }
        }

        onNodeWithText("value: a, id: $id1, count: 0").assertExists()

        onNodeWithText("+").performClick()
        waitForIdle()

        onNodeWithText("value: a, id: $id1, count: 1").assertExists()

        currentEntryId = id2
        waitForIdle()

        onNodeWithText("value: b, id: $id2, count: 0").assertExists()

        currentEntryId = id1
        waitForIdle()

        onNodeWithText("value: a, id: $id1, count: 1").assertExists()
    }

    @Test
    fun navigation_host_does_not_keep_state_for_entries_not_in_map() = runComposeUiTest {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val entry1 = BackstackEntry(
            value = "a",
            previous = null,
            id = id1,
        )
        val entry2 = BackstackEntry(
            value = "b",
            previous = null,
            id = id2,
        )

        val backstackMap = mutableStateMapOf<UUID, BackstackEntry<String>>(
            id1 to entry1,
            id2 to entry2,
        )

        var currentEntryId by mutableStateOf(id1)

        val navigationState = object : BackstackState<String> {
            override val entryMap get() = backstackMap
            override val currentEntryId get() = currentEntryId
        }

        setContent {
            NavigationHost(
                navigationState = navigationState,
            ) { entry ->
                var count by rememberSaveable { mutableStateOf(0) }

                Column {
                    BasicText("value: ${entry.value}, id: ${entry.id}, count: $count")
                    BasicText("+", modifier = Modifier.clickable { count++ })
                }
            }
        }

        onNodeWithText("value: a, id: $id1, count: 0").assertExists()

        onNodeWithText("+").performClick()
        waitForIdle()

        onNodeWithText("value: a, id: $id1, count: 1").assertExists()

        currentEntryId = id2
        backstackMap.remove(id1)
        waitForIdle()

        onNodeWithText("value: b, id: $id2, count: 0").assertExists()

        currentEntryId = id1
        backstackMap[id1] = entry1
        waitForIdle()

        onNodeWithText("value: a, id: $id1, count: 0").assertExists()
    }

    @Test
    fun navigation_host_state_is_preserved_through_recreation() = runComposeUiTest {
        val stateRestorationTester = KmpStateRestorationTester(this)

        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val entry1 = BackstackEntry(
            value = "a",
            previous = null,
            id = id1,
        )
        val entry2 = BackstackEntry(
            value = "b",
            previous = null,
            id = id2,
        )

        val backstackMap = mutableStateMapOf<UUID, BackstackEntry<String>>(
            id1 to entry1,
            id2 to entry2,
        )

        var currentEntryId by mutableStateOf(id1)

        val navigationState = object : BackstackState<String> {
            override val entryMap get() = backstackMap
            override val currentEntryId get() = currentEntryId
        }

        stateRestorationTester.setContent {
            NavigationHost(
                navigationState = navigationState,
            ) { entry ->
                var count by rememberSaveable { mutableStateOf(0) }

                Column {
                    BasicText("value: ${entry.value}, id: ${entry.id}, count: $count")
                    BasicText("+", modifier = Modifier.clickable { count++ })
                }
            }
        }

        onNodeWithText("value: a, id: $id1, count: 0").assertExists()

        onNodeWithText("+").performClick()
        waitForIdle()

        onNodeWithText("value: a, id: $id1, count: 1").assertExists()

        currentEntryId = id2
        waitForIdle()

        onNodeWithText("value: b, id: $id2, count: 0").assertExists()

        stateRestorationTester.emulateSavedInstanceStateRestore()

        currentEntryId = id1
        waitForIdle()

        onNodeWithText("value: a, id: $id1, count: 1").assertExists()
    }
}
