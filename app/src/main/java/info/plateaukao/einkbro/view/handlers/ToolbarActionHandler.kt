package info.plateaukao.einkbro.view.handlers

import androidx.fragment.app.FragmentActivity
import info.plateaukao.einkbro.browser.BrowserController
import info.plateaukao.einkbro.preference.ConfigManager
import info.plateaukao.einkbro.preference.TranslationMode
import info.plateaukao.einkbro.preference.toggle
import info.plateaukao.einkbro.unit.IntentUnit
import info.plateaukao.einkbro.view.dialog.compose.ToolbarConfigDialogFragment
import info.plateaukao.einkbro.view.dialog.compose.TouchAreaDialogFragment
import info.plateaukao.einkbro.view.dialog.compose.TtsSettingDialogFragment
import info.plateaukao.einkbro.view.toolbaricons.ToolbarAction
import info.plateaukao.einkbro.viewmodel.TRANSLATE_API
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ToolbarActionHandler(
    private val activity: FragmentActivity
) : KoinComponent {
    private val config: ConfigManager by inject()
    private val browserController = activity as BrowserController

    fun handleLongClick(toolbarAction: ToolbarAction) = when (toolbarAction) {
        ToolbarAction.Back -> browserController.openHistoryPage(5)
        ToolbarAction.Refresh -> browserController.fullscreen()
        ToolbarAction.Touch -> TouchAreaDialogFragment().show(
            activity.supportFragmentManager,
            "TouchAreaDialog"
        )

        ToolbarAction.PageUp -> browserController.jumpToTop()
        ToolbarAction.PageDown -> browserController.jumpToBottom()
        ToolbarAction.TabCount -> config::isIncognitoMode.toggle()
        ToolbarAction.Settings -> browserController.showFastToggleDialog()
        ToolbarAction.Bookmark -> browserController.saveBookmark()
        ToolbarAction.Translation -> browserController.showTranslationConfigDialog()
        ToolbarAction.NewTab -> IntentUnit.launchNewBrowser(activity, config.favoriteUrl)
        ToolbarAction.Tts ->
            TtsSettingDialogFragment { IntentUnit.gotoSystemTtsSettings(activity) }
                .show(activity.supportFragmentManager, "TtsSettingDialog")

        ToolbarAction.Font -> browserController.toggleReaderMode()
        ToolbarAction.InputUrl -> {
            // toggle papago translate
            if (config.papagoApiSecret.isBlank()) {
                config.papagoApiSecret = "123"
            } else {
                config.papagoApiSecret = ""
            }
        }

        ToolbarAction.TranslateByParagraph -> browserController.configureTranslationLanguage(TRANSLATE_API.GOOGLE)

        ToolbarAction.PapagoByParagraph -> browserController.configureTranslationLanguage(TRANSLATE_API.PAPAGO)

        else -> {}
    }

    fun handleClick(toolbarAction: ToolbarAction) = when (toolbarAction) {
        ToolbarAction.Title -> browserController.focusOnInput()
        ToolbarAction.Back -> browserController.handleBackKey()
        ToolbarAction.Refresh -> browserController.refreshAction()
        ToolbarAction.Touch -> browserController.toggleTouchTurnPageFeature()
        ToolbarAction.PageUp -> browserController.pageUp()
        ToolbarAction.PageDown -> browserController.pageDown()
        ToolbarAction.TabCount -> browserController.showOverview()
        ToolbarAction.Font -> browserController.showFontSizeChangeDialog()
        ToolbarAction.Settings -> browserController.showMenuDialog()
        ToolbarAction.Bookmark -> browserController.openBookmarkPage()
        ToolbarAction.IconSetting -> ToolbarConfigDialogFragment().show(
            activity.supportFragmentManager,
            "toolbar_config"
        )

        ToolbarAction.VerticalLayout -> browserController.toggleVerticalRead()
        ToolbarAction.ReaderMode -> browserController.toggleReaderMode()
        ToolbarAction.BoldFont -> config::boldFontStyle.toggle()
        ToolbarAction.IncreaseFont -> browserController.increaseFontSize()
        ToolbarAction.DecreaseFont -> browserController.decreaseFontSize()
        ToolbarAction.FullScreen -> browserController.fullscreen()
        ToolbarAction.Forward -> browserController.goForward()
        ToolbarAction.RotateScreen -> browserController.rotateScreen()
        ToolbarAction.Translation -> browserController.showTranslation()
        ToolbarAction.CloseTab -> browserController.removeAlbum()
        ToolbarAction.InputUrl -> browserController.focusOnInput()
        ToolbarAction.NewTab -> browserController.newATab()
        ToolbarAction.Desktop -> config::desktop.toggle()
        ToolbarAction.Search -> browserController.showSearchPanel()
        ToolbarAction.DuplicateTab -> browserController.duplicateTab()
        ToolbarAction.Tts -> browserController.toggleTtsRead()
        ToolbarAction.TOC -> browserController.showTocDialog()
        ToolbarAction.PageInfo -> Unit
        ToolbarAction.GoogleInPlace -> browserController.translate(TranslationMode.GOOGLE_IN_PLACE)
        ToolbarAction.TranslateByParagraph -> browserController.translate(TranslationMode.TRANSLATE_BY_PARAGRAPH)
        ToolbarAction.PapagoByParagraph -> browserController.translate(TranslationMode.PAPAGO_TRANSLATE_BY_PARAGRAPH)
    }
}