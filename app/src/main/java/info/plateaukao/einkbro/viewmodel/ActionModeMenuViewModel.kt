package info.plateaukao.einkbro.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import info.plateaukao.einkbro.R
import info.plateaukao.einkbro.preference.ConfigManager
import info.plateaukao.einkbro.unit.ShareUtil
import info.plateaukao.einkbro.view.data.MenuInfo
import info.plateaukao.einkbro.view.data.toMenuInfo
import info.plateaukao.einkbro.view.dialog.compose.ActionModeDialogFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ActionModeMenuViewModel : ViewModel(), KoinComponent {
    private val configManager: ConfigManager by inject()

    private var actionMode: ActionMode? = null
    private val _clickedPoint = MutableStateFlow(Point(0, 0))
    val clickedPoint: StateFlow<Point> = _clickedPoint.asStateFlow()

    private val _selectedText = MutableStateFlow("")
    val selectedText: StateFlow<String> = _selectedText.asStateFlow()

    private val _actionModeMenuState =
        MutableStateFlow(ActionModeMenuState.Idle as ActionModeMenuState)
    val actionModeMenuState: StateFlow<ActionModeMenuState> = _actionModeMenuState.asStateFlow()

    fun isInActionMode(): Boolean = actionMode != null

    fun updateActionMode(actionMode: ActionMode?) {
        this.actionMode = actionMode
    }

    private var fragment: DialogFragment? = null
    fun showActionModeDialogFragment(
        context: Context,
        supportFragmentManager: FragmentManager,
        packageManager: PackageManager
    ) {
        if (fragment != null && fragment?.isAdded == true) {
            return
        }
        fragment = ActionModeDialogFragment(
            this,
            getAllProcessTextMenuInfos(context, packageManager)
        ) {
            finishActionMode()
        }.apply {
            show(supportFragmentManager, "action_mode_dialog")
        }
    }

    fun finishActionMode() {
        actionMode?.finish()
        actionMode = null
        fragment?.dismiss()
        fragment = null
    }

    fun updateSelectedText(text: String) {
        _selectedText.value = text
    }

    fun updateClickedPoint(point: Point) {
        _clickedPoint.value = point
    }

    private fun getAllProcessTextMenuInfos(
        context: Context,
        packageManager: PackageManager
    ): List<MenuInfo> {
        val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)

        val menuInfos = resolveInfos.map { it.toMenuInfo(packageManager) }.toMutableList()

        if (configManager.papagoApiSecret.isNotEmpty()) {
            menuInfos.add(
                0,
                MenuInfo(
                    context.getString(R.string.papago),
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_papago),
                    action = {
                        _actionModeMenuState.value = ActionModeMenuState.Papago
                    }
                )
            )
        }
        menuInfos.add(
            0,
            MenuInfo(
                context.getString(R.string.google_translate),
                icon = ContextCompat.getDrawable(context, R.drawable.ic_translate),
                action = {
                    _actionModeMenuState.value = ActionModeMenuState.GoogleTranslate
                }
            )
        )

        if (configManager.gptApiKey.isNotEmpty()) {
            menuInfos.add(
                0,
                MenuInfo(
                    context.getString(R.string.menu_gpt),
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_chat_gpt),
                    action = {
                        _actionModeMenuState.value = ActionModeMenuState.Gpt
                    }
                )
            )
        }

        menuInfos.add(
            0,
            MenuInfo(
                context.getString(android.R.string.copy),
                icon = ContextCompat.getDrawable(context, R.drawable.ic_copy),
                action = {
                    ShareUtil.copyToClipboard(context, selectedText.value)
                }
            )
        )

        return menuInfos
    }

    fun resetActionModeMenuState() {
        _actionModeMenuState.value = ActionModeMenuState.Idle
    }

}

sealed class ActionModeMenuState {
    object Idle : ActionModeMenuState()
    object Gpt : ActionModeMenuState()
    object GoogleTranslate : ActionModeMenuState()
    object Papago : ActionModeMenuState()
}