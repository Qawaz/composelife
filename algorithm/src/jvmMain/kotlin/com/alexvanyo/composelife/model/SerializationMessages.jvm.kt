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

package com.alexvanyo.composelife.model

import androidx.compose.ui.unit.IntOffset
import com.alexvanyo.composelife.parameterizedstring.ParameterizedString

actual fun UnexpectedInputMessage(
    input: String,
    lineIndex: Int,
    characterIndex: Int,
): ParameterizedString =
    ParameterizedString("Unexpected input \"$input\" starting on line $lineIndex, character $characterIndex")

actual fun UnexpectedCharacterMessage(
    character: Char,
    lineIndex: Int,
    characterIndex: Int,
): ParameterizedString =
    ParameterizedString("Unexpected character $character on line $lineIndex, offset $characterIndex interpreted as on")

actual fun UnexpectedHeaderMessage(
    header: String,
): ParameterizedString =
    ParameterizedString("Unexpected header, found $header")

actual fun UnexpectedShortLineMessage(
    lineIndex: Int,
): ParameterizedString =
    ParameterizedString("Line $lineIndex is unexpectedly short")

actual fun UnexpectedBlankLineMessage(
    lineIndex: Int,
): ParameterizedString =
    ParameterizedString("Unexpected blank line at line $lineIndex")

actual fun UnexpectedEmptyFileMessage(): ParameterizedString =
    ParameterizedString("Unexpected empty file, assuming blank pattern")

actual fun RuleNotSupportedMessage(): ParameterizedString =
    ParameterizedString("Ruleset not supported")

actual fun DuplicateTopLeftCoordinate(
    overwritingOffset: IntOffset,
): ParameterizedString =
    ParameterizedString(
        "Duplicate top-left coordinate instruction, overwriting with (${
            overwritingOffset.x
        }, ${
            overwritingOffset.y
        })",
    )
