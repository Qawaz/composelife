/*
 * Copyright 2023 The Android Open Source Project
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

package com.alexvanyo.composelife.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alexvanyo.composelife.updatable.Updatable
import dagger.hilt.android.testing.HiltAndroidRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A base class for testing components that depend on Hilt injected classes.
 *
 * Subclasses must call [runAppTest] instead of [runTest] to properly initialize dependencies.
 */
@Suppress("UnnecessaryAbstractClass")
@RunWith(AndroidJUnit4::class)
abstract class BaseHiltTest {

    @get:Rule(order = 1)
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var updatables: Set<@JvmSuppressWildcards Updatable>

    @BeforeTest
    fun baseHiltTestSetup() {
        hiltAndroidRule.inject()
    }

    fun runAppTest(
        context: CoroutineContext = EmptyCoroutineContext,
        timeout: Duration = 60.seconds,
        testBody: suspend TestScope.() -> Unit,
    ): TestResult = runTest(
        context = context,
        timeout = timeout,
    ) {
        updatables.forEach { updatable ->
            backgroundScope.launch {
                updatable.update()
            }
        }
        testBody()
    }
}
