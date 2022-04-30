package com.alexvanyo.composelife.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.airbnb.android.showkase.models.Showkase
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import com.alexvanyo.composelife.ui.theme.ComposeLifeTheme
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameter.TestParameterValuesProvider
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private val globalPaparazzi = Paparazzi(
    maxPercentDifference = 0.0,
)

class ComponentPreview(
    private val showkaseBrowserComponent: ShowkaseBrowserComponent
) {
    val content: @Composable () -> Unit = showkaseBrowserComponent.component
    override fun toString(): String = showkaseBrowserComponent.componentKey
}

@RunWith(TestParameterInjector::class)
class PaparazziTests {

    object PreviewProvider : TestParameterValuesProvider {
        override fun provideValues(): List<ComponentPreview> =
            Showkase.getMetadata().componentList.map(::ComponentPreview)
    }

    enum class BaseDeviceConfig(
        val deviceConfig: DeviceConfig,
    ) {
        NEXUS_5(DeviceConfig.NEXUS_5),
        PIXEL_5(DeviceConfig.PIXEL_5),
        PIXEL_C(DeviceConfig.PIXEL_C),
    }

    @get:Rule
    val paparazzi = globalPaparazzi

    @Test
    fun `preview tests`(
        @TestParameter(valuesProvider = PreviewProvider::class) componentPreview: ComponentPreview,
        @TestParameter baseDeviceConfig: BaseDeviceConfig,
        @TestParameter nightMode: NightMode,
    ) {
        paparazzi.snapshot(
            deviceConfig = baseDeviceConfig.deviceConfig.copy(
                nightMode = nightMode,
                softButtons = false,
            )
        ) {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ComposeLifeTheme(darkTheme = nightMode == NightMode.NIGHT) {
                    Box {
                        componentPreview.content()
                    }
                }
            }
        }
    }
}
