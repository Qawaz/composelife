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

package com.alexvanyo.composelife.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import kotlinx.coroutines.launch

/**
 * A [Modifier] that animates placement using the given [animationSpec].
 */
fun Modifier.animatePlacement(
    animationSpec: AnimationSpec<IntOffset> = spring(stiffness = Spring.StiffnessMedium),
    fixedPoint: (layoutCoordinates: LayoutCoordinates, layoutDirection: LayoutDirection) -> IntOffset =
        { layoutCoordinates, layoutDirection ->
            with(layoutDirection) {
                layoutCoordinates.boundsInParent().topStart.round()
            }
        },
    parentFixedPoint: (parentLayoutCoordinates: LayoutCoordinates, layoutDirection: LayoutDirection) -> IntOffset =
        { parentLayoutCoordinates, layoutDirection ->
            with(layoutDirection) {
                parentLayoutCoordinates.size.toIntRect().topStart
            }
        }
): Modifier = composed {
    val scope = rememberCoroutineScope()
    var targetOffset by remember { mutableStateOf(IntOffset.Zero) }
    var animatable by remember {
        mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null)
    }
    val layoutDirection = LocalLayoutDirection.current
    this
        .onPlaced { layoutCoordinates ->
            // Calculate the alignment coordinate of this node in the parent coordinates, and calculate the offset from
            // that to the fixed point in the parent
            val currentFixedPoint = fixedPoint(
                layoutCoordinates,
                layoutDirection
            )
            val currentParentFixedPoint = parentFixedPoint(
                requireNotNull(layoutCoordinates.parentLayoutCoordinates),
                layoutDirection
            )
            targetOffset = currentFixedPoint - currentParentFixedPoint
        }
        .offset {
            // Animate to the new target offset when alignment changes.
            val anim = animatable ?: Animatable(targetOffset, IntOffset.VectorConverter)
                .also { animatable = it }
            if (anim.targetValue != targetOffset) {
                scope.launch {
                    anim.animateTo(targetOffset, animationSpec)
                }
            }
            // Offset the child in the opposite direction to the targetOffset, and slowly catch
            // up to zero offset via an animation to achieve an overall animated movement.
            animatable?.let { it.value - targetOffset } ?: IntOffset.Zero
        }
}
