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

package com.alexvanyo.composelife.ui.app.component

import com.alexvanyo.composelife.ui.app.util.BasePaparazziTest
import kotlin.test.Test

class GameOfLifeProgressIndicatorSnapshotTests : BasePaparazziTest() {

    @Test
    fun game_of_life_progress_indicator_blinker_preview() {
        snapshot {
            GameOfLifeProgressIndicatorBlinkerPreview()
        }
    }

    @Test
    fun game_of_life_progress_indicator_toad_preview() {
        snapshot {
            GameOfLifeProgressIndicatorToadPreview()
        }
    }

    @Test
    fun game_of_life_progress_indicator_beacon_preview() {
        snapshot {
            GameOfLifeProgressIndicatorBeaconPreview()
        }
    }

    @Test
    fun game_of_life_progress_indicator_pulsar_preview() {
        snapshot {
            GameOfLifeProgressIndicatorPulsarPreview()
        }
    }
}
