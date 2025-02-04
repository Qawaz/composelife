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

package com.alexvanyo.composelife.wear.watchface

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.text.format.DateFormat
import android.view.SurfaceHolder
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.WatchFaceLayer
import com.alexvanyo.composelife.geometry.containedPoints
import com.alexvanyo.composelife.model.CellState
import com.alexvanyo.composelife.model.TemporalGameOfLifeState
import com.alexvanyo.composelife.model.emptyCellState
import com.alexvanyo.composelife.model.toCellState
import com.alexvanyo.composelife.openglrenderer.GameOfLifeShape
import com.alexvanyo.composelife.openglrenderer.GameOfLifeShapeParameters
import com.alexvanyo.composelife.preferences.CurrentShape
import com.alexvanyo.composelife.wear.watchface.configuration.getGameOfLifeColor
import com.alexvanyo.composelife.wear.watchface.configuration.getShowComplicationsInAmbient
import java.nio.IntBuffer
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.properties.Delegates
import kotlin.random.Random

class GameOfLifeRenderer(
    context: Context,
    surfaceHolder: SurfaceHolder,
    private val currentUserStyleRepository: CurrentUserStyleRepository,
    private val complicationSlotsManager: ComplicationSlotsManager,
    private val watchState: WatchState,
    private val temporalGameOfLifeState: TemporalGameOfLifeState,
) : Renderer.GlesRenderer2<Renderer.SharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    interactiveDrawModeUpdateDelayMillis = 50,
) {
    private val cellWindow = IntRect(IntOffset(0, 0), IntSize(69, 69))

    private val use24HourFormat = DateFormat.is24HourFormat(context)

    private val isRound = context.resources.configuration.isScreenRound

    private var previousLocalTime = LocalTime.MIN

    private val shape: CurrentShape = CurrentShape.RoundRectangle(
        sizeFraction = 1f,
        cornerFraction = 0f,
    )

    private var cellSize by Delegates.notNull<Float>()

    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    init {
        Matrix.orthoM(projectionMatrix, 0, 0f, 1f, 0f, 1f, 0.5f, 2f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    private lateinit var gameOfLifeShape: GameOfLifeShape
    private lateinit var complicationShapes: List<ComplicationShape>

    override suspend fun onUiThreadGlSurfaceCreated(width: Int, height: Int) {
        cellSize = width.toFloat() / (cellWindow.width + 1)

        GLES20.glClearColor(0f, 0f, 0f, 0f)

        gameOfLifeShape = GameOfLifeShape(
            texture = 10,
        )
        gameOfLifeShape.setSize(width, height)
        complicationShapes = complicationSlotsManager.complicationSlots.values.mapIndexed { index, complicationSlot ->
            ComplicationShape(
                screenSize = IntSize(width, height),
                complicationSlot = complicationSlot,
                texture = 11 + index,
            )
        }
    }

    @Suppress("LongMethod", "NestedBlockDepth", "CyclomaticComplexMethod")
    override fun render(zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        val previousSeedCellState = temporalGameOfLifeState.seedCellState

        val localTime = zonedDateTime.toLocalTime()

        if (previousLocalTime.hour != localTime.hour || previousLocalTime.minute != localTime.minute) {
            Snapshot.withMutableSnapshot {
                temporalGameOfLifeState.cellState = createTimeCellState(
                    isRound = isRound,
                    timeDigits = createTimeDigits(localTime, use24HourFormat),
                )
            }
        }

        val cellState = temporalGameOfLifeState.cellState

        val cellWindowSize = IntSize(cellWindow.width + 1, cellWindow.height + 1)
        val cellsBuffer = IntBuffer.allocate(cellWindowSize.width * cellWindowSize.height)

        cellState.getAliveCellsInWindow(cellWindow).forEach { cell ->
            val index = (cellWindow.bottom - cell.y) * cellWindowSize.width + cell.x - cellWindow.left
            cellsBuffer.put(index, android.graphics.Color.WHITE)
        }

        val gameOfLifeColor = with(currentUserStyleRepository.schema) {
            currentUserStyleRepository.userStyle.value.getGameOfLifeColor()
        }

        val screenShapeParameters = when (shape) {
            is CurrentShape.RoundRectangle -> {
                GameOfLifeShapeParameters.RoundRectangle(
                    cells = cellsBuffer,
                    aliveColor = gameOfLifeColor,
                    deadColor = Color.Black,
                    cellWindowSize = IntSize(cellWindow.width + 1, cellWindow.height + 1),
                    scaledCellPixelSize = cellSize,
                    pixelOffsetFromCenter = Offset.Zero,
                    sizeFraction = shape.sizeFraction,
                    cornerFraction = shape.cornerFraction,
                )
            }
        }

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        if (WatchFaceLayer.BASE in renderParameters.watchFaceLayers) {
            gameOfLifeShape.setScreenShapeParameters(screenShapeParameters)
            gameOfLifeShape.draw(mvpMatrix)
        }

        if (
            renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS) &&
            (
                renderParameters.drawMode != DrawMode.AMBIENT ||
                    with(currentUserStyleRepository.schema) {
                        currentUserStyleRepository.userStyle.value.getShowComplicationsInAmbient()
                    }
                )
        ) {
            complicationSlotsManager.complicationSlots.values.forEach {
                (it.renderer as? CanvasComplicationDrawable)?.drawable?.apply {
                    activeStyle.apply {
                        borderColor = gameOfLifeColor.copy(alpha = 0.9f).toArgb()
                        highlightColor = gameOfLifeColor.copy(alpha = 0.3f).toArgb()
                        iconColor = gameOfLifeColor.toArgb()
                        rangedValuePrimaryColor = gameOfLifeColor.copy(alpha = 0.9f).toArgb()
                        rangedValueSecondaryColor = gameOfLifeColor.copy(alpha = 0.7f).toArgb()
                        textColor = gameOfLifeColor.copy(alpha = 0.9f).toArgb()
                        titleColor = gameOfLifeColor.copy(alpha = 0.9f).toArgb()
                    }
                    ambientStyle.apply {
                        borderColor = gameOfLifeColor.copy(alpha = 0.5f).toArgb()
                        iconColor = gameOfLifeColor.toArgb()
                        textColor = gameOfLifeColor.toArgb()
                        titleColor = gameOfLifeColor.copy(alpha = 0.7f).toArgb()
                        rangedValuePrimaryColor = gameOfLifeColor.toArgb()
                    }
                }
            }
            complicationShapes.forEach { complicationShape ->
                complicationShape.draw(zonedDateTime, renderParameters, mvpMatrix, false)
            }
        }

        if (zonedDateTime.toEpochSecond() * 1000 == watchState.digitalPreviewReferenceTimeMillis) {
            Snapshot.withMutableSnapshot {
                temporalGameOfLifeState.cellState = previousSeedCellState
            }
        } else {
            previousLocalTime = localTime
        }
    }

    override fun renderHighlightLayer(zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        val highlightLayer = requireNotNull(renderParameters.highlightLayer) {
            "Trying to render highlight layer, but it was null!"
        }
        val highlightedElement = highlightLayer.highlightedElement
        val backgroundTint = Color(highlightLayer.backgroundTint)
        GLES20.glClearColor(
            backgroundTint.red,
            backgroundTint.green,
            backgroundTint.blue,
            backgroundTint.alpha,
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (WatchFaceLayer.COMPLICATIONS in renderParameters.watchFaceLayers) {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO)

            complicationShapes.forEach { complicationShape ->
                val shouldDrawComplicationHighlight = when (highlightedElement) {
                    RenderParameters.HighlightedElement.AllComplicationSlots -> true
                    is RenderParameters.HighlightedElement.ComplicationSlot ->
                        highlightedElement.id == complicationShape.id
                    is RenderParameters.HighlightedElement.UserStyle -> false
                }
                if (shouldDrawComplicationHighlight) {
                    complicationShape.draw(zonedDateTime, renderParameters, mvpMatrix, true)
                }
            }
        }
    }

    override suspend fun createSharedAssets(): SharedAssets = object : SharedAssets {
        override fun onDestroy() = Unit
    }
}

private fun createTimeCellState(
    isRound: Boolean,
    timeDigits: TimeDigits,
): CellState {
    val timeCellState = timeDigits.firstDigit.cellState
        .union(timeDigits.secondDigit.cellState.offsetBy(IntOffset(14, 0)))
        .union(timeDigits.thirdDigit.cellState.offsetBy(IntOffset(32, 0)))
        .union(timeDigits.fourthDigit.cellState.offsetBy(IntOffset(46, 0)))
        .union(
            """
                |OO
                |OO
                |..
                |..
                |OO
                |OO
            """.toCellState(IntOffset(27, 6)),
        )
        .offsetBy(IntOffset(8, 26))

    val randomPointPool = if (isRound) {
        roundRandomPointPool
    } else {
        notRoundRandomPointPool
    }

    val randomPoints = CellState(
        randomPointPool.filter { Random.nextFloat() < 0.2 }.toSet(),
    )

    return timeCellState.union(randomPoints)
}

val roundRandomPointPool =
    IntRect(
        IntOffset(-30, -30),
        IntOffset(100, 100),
    )
        .containedPoints()
        .filter {
            (it.toOffset() - Offset(34.5f, 34.5f)).getDistance() in 36f..46f
        }

val notRoundRandomPointPool =
    IntRect(
        IntOffset(-10, -10),
        IntOffset(80, 80),
    )
        .containedPoints()
        .filter {
            it.x !in 0..69 && it.y !in 0..69
        }

fun createTimeDigits(localTime: LocalTime, use24HourFormat: Boolean): TimeDigits {
    val clockHour = localTime.hour.rem(12)
    val displayHour = if (use24HourFormat) {
        localTime.hour
    } else if (clockHour == 0) {
        12
    } else {
        clockHour
    }

    val hourTensPlace = displayHour / 10
    val firstDigit = if (hourTensPlace == 0 && !use24HourFormat) {
        GameOfLifeSegmentChar.Blank
    } else {
        GameOfLifeSegmentChar.fromChar(hourTensPlace)
    }
    val secondDigit = GameOfLifeSegmentChar.fromChar(displayHour.rem(10))
    val thirdDigit = GameOfLifeSegmentChar.fromChar(localTime.minute / 10)
    val fourthDigit = GameOfLifeSegmentChar.fromChar(localTime.minute.rem(10))

    return TimeDigits(
        firstDigit = firstDigit,
        secondDigit = secondDigit,
        thirdDigit = thirdDigit,
        fourthDigit = fourthDigit,
    )
}

data class TimeDigits(
    val firstDigit: GameOfLifeSegmentChar,
    val secondDigit: GameOfLifeSegmentChar,
    val thirdDigit: GameOfLifeSegmentChar,
    val fourthDigit: GameOfLifeSegmentChar,
)

sealed class GameOfLifeSegmentChar(
    val cellState: CellState,
) {
    object Zero : GameOfLifeSegmentChar(segA.union(segB).union(segC).union(segD).union(segE).union(segF))
    object One : GameOfLifeSegmentChar(segB.union(segC))
    object Two : GameOfLifeSegmentChar(segA.union(segB).union(segD).union(segE).union(segG))
    object Three : GameOfLifeSegmentChar(segA.union(segB).union(segC).union(segD).union(segG))
    object Four : GameOfLifeSegmentChar(segB.union(segC).union(segF).union(segG))
    object Five : GameOfLifeSegmentChar(segA.union(segC).union(segD).union(segF).union(segG))
    object Six : GameOfLifeSegmentChar(segA.union(segC).union(segD).union(segE).union(segF).union(segG))
    object Seven : GameOfLifeSegmentChar(segA.union(segB).union(segC))
    object Eight : GameOfLifeSegmentChar(segA.union(segB).union(segC).union(segD).union(segE).union(segF).union(segG))
    object Nine : GameOfLifeSegmentChar(segA.union(segB).union(segC).union(segD).union(segF).union(segG))
    object Blank : GameOfLifeSegmentChar(emptyCellState())

    companion object {
        fun fromChar(digit: Int): GameOfLifeSegmentChar {
            return when (digit) {
                0 -> Zero
                1 -> One
                2 -> Two
                3 -> Three
                4 -> Four
                5 -> Five
                6 -> Six
                7 -> Seven
                8 -> Eight
                9 -> Nine
                else -> throw IllegalArgumentException("input wasn't a digit!")
            }
        }
    }
}

private val segA = """
    |OO.OO.O.OO
    |OO.O.OO.OO
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
""".toCellState()

private val segB = """
    |........OO
    |........OO
    |..........
    |..........
    |........OO
    |........OO
    |..........
    |..........
    |........OO
    |........OO
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
""".toCellState()

private val segC = """
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |........OO
    |........OO
    |..........
    |..........
    |........OO
    |........OO
    |..........
    |..........
    |........OO
    |........OO
""".toCellState()

private val segD = """
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |OO.OO.O.OO
    |OO.O.OO.OO
""".toCellState()

private val segE = """
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |OO........
    |OO........
    |..........
    |..........
    |OO........
    |OO........
    |..........
    |..........
    |OO........
    |OO........
""".toCellState()

private val segF = """
    |OO........
    |OO........
    |..........
    |..........
    |OO........
    |OO........
    |..........
    |..........
    |OO........
    |OO........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
""".toCellState()

private val segG = """
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |OO.O.OO.OO
    |OO.OO.O.OO
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
    |..........
""".toCellState()
