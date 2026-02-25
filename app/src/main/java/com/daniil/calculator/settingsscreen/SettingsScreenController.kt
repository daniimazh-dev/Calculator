import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.daniil.calculator.settingsscreen.RegisterCustomSettings
import com.daniil.calculator.settingsscreen.SettingsScreenModel
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import com.daniil.calculator.settingsscreen.screen.SettingsCustomScreen
import com.daniil.calculator.settingsscreen.screen.SettingsHomeScreen


@Composable
fun SettingsScreenController(
    settingsScreenModel: SettingsScreenModel,

) {
    RegisterCustomSettings(
        settingsScreenModel = settingsScreenModel
    )
    val customScreenStack by settingsScreenModel.customScreenStack.collectAsState()


    AnimatedContent(
        targetState = customScreenStack.lastOrNull(),
        transitionSpec = {
            if (!settingsScreenModel.lastActive) {
                fadeIn(tween(300))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { it / 1 })

            } else {
                slideInHorizontally(animationSpec = tween(300)) { it / 1 }
                    .togetherWith(fadeOut(tween(300)))
            }

        },
    ) { customScreen ->


        if (customScreen == null) {
            SettingsHomeScreen(settingsScreenModel)
        } else {

            val listSize =  customScreenStack.size

            val setting = remember(listSize) {
                DynamicSettingsManager.getSetting(
                    customScreen
                )
            } ?: run {
                settingsScreenModel.backStack()
                return@AnimatedContent
            }

            val render = remember(listSize) {
                DynamicSettingRenderManager
                    .getRendererById((setting.value ?: setting.defaultValue).toString())
            }

            SettingsCustomScreen(
                settingsScreenModel = settingsScreenModel,
                setting = setting,
                content = { render?.invoke(setting) }
            )
        }
    }

}