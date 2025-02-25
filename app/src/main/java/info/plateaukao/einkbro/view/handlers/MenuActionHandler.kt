package info.plateaukao.einkbro.view.handlers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import info.plateaukao.einkbro.R
import info.plateaukao.einkbro.browser.BrowserController
import info.plateaukao.einkbro.preference.ConfigManager
import info.plateaukao.einkbro.preference.toggle
import info.plateaukao.einkbro.unit.BrowserUnit
import info.plateaukao.einkbro.unit.HelperUnit
import info.plateaukao.einkbro.unit.IntentUnit
import info.plateaukao.einkbro.unit.ShareUtil
import info.plateaukao.einkbro.view.NinjaToast
import info.plateaukao.einkbro.view.NinjaWebView
import info.plateaukao.einkbro.view.dialog.DialogManager
import info.plateaukao.einkbro.view.dialog.ReceiveDataDialog
import info.plateaukao.einkbro.view.dialog.SendLinkDialog
import info.plateaukao.einkbro.view.dialog.compose.MenuItemType
import info.plateaukao.einkbro.view.dialog.compose.ToolbarConfigDialogFragment
import info.plateaukao.einkbro.view.dialog.compose.TouchAreaDialogFragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MenuActionHandler(
    private val activity: FragmentActivity,
) : KoinComponent {
    private val config: ConfigManager by inject()
    private val browserController = activity as BrowserController
    private val dialogManager by lazy { DialogManager(activity) }

    fun handle(menuItemType: MenuItemType, ninjaWebView: NinjaWebView) = when (menuItemType) {
        MenuItemType.Tts -> browserController.toggleTtsRead()
        MenuItemType.QuickToggle -> browserController.showFastToggleDialog()
        MenuItemType.OpenHome -> browserController.updateAlbum(config.favoriteUrl)
        MenuItemType.CloseTab -> browserController.removeAlbum()
        MenuItemType.Quit -> activity.finishAndRemoveTask()

        MenuItemType.SplitScreen -> browserController.toggleSplitScreen()
        MenuItemType.Translate -> browserController.showTranslation()
        MenuItemType.VerticalRead -> browserController.toggleVerticalRead()
        MenuItemType.ReaderMode -> browserController.toggleReaderMode()
        MenuItemType.TouchSetting -> TouchAreaDialogFragment().show(
            activity.supportFragmentManager,
            "TouchAreaDialog"
        )

        MenuItemType.ToolbarSetting -> ToolbarConfigDialogFragment().show(
            activity.supportFragmentManager,
            "toolbar_config"
        )

        MenuItemType.ReceiveData -> showReceiveDataDialog(ninjaWebView)
        MenuItemType.SendLink ->
            SendLinkDialog(activity, activity.lifecycleScope).show(ninjaWebView.url.orEmpty())

        MenuItemType.ShareLink ->
            IntentUnit.share(activity, ninjaWebView.title, ninjaWebView.url)

        MenuItemType.OpenWith -> HelperUnit.showBrowserChooser(
            activity,
            ninjaWebView.url,
            activity.getString(R.string.menu_open_with)
        )

        MenuItemType.CopyLink -> ShareUtil.copyToClipboard(
            activity,
            BrowserUnit.stripUrlQuery(ninjaWebView.url ?: "")
        )

        MenuItemType.Shortcut -> HelperUnit.createShortcut(
            activity,
            ninjaWebView.title,
            ninjaWebView.url,
            ninjaWebView.favicon
        )

        MenuItemType.SetHome -> config.favoriteUrl = ninjaWebView.url.orEmpty()
        MenuItemType.SaveBookmark -> browserController.saveBookmark()
        MenuItemType.OpenEpub -> openSavedEpub()
        MenuItemType.SaveEpub -> browserController.showSaveEpubDialog()
        MenuItemType.SavePdf -> printPDF(ninjaWebView)

        MenuItemType.FontSize -> browserController.showFontSizeChangeDialog()
        MenuItemType.WhiteBknd -> config::whiteBackground.toggle()
        MenuItemType.BoldFont -> config::boldFontStyle.toggle()
        MenuItemType.BlackFont -> config::blackFontStyle.toggle()
        MenuItemType.Search -> browserController.showSearchPanel()
        MenuItemType.Download -> BrowserUnit.openDownloadFolder(activity)
        MenuItemType.SaveArchive -> browserController.showWebArchiveFilePicker()
        MenuItemType.Settings -> IntentUnit.gotoSettings(activity)

        MenuItemType.AddToPocket -> ninjaWebView.url?.let { browserController.addToPocket(it) }
    }


    private fun showReceiveDataDialog(ninjaWebView: NinjaWebView) {
        ReceiveDataDialog(activity, activity.lifecycleScope).show { text ->
            if (text.startsWith("http")) ninjaWebView.loadUrl(text)
            else {
                val clip = ClipData.newPlainText("Copied Text", text)
                (activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                    .setPrimaryClip(clip)
                NinjaToast.show(activity, "String is Copied!")
            }
        }
    }

    private fun openSavedEpub() = if (config.savedEpubFileInfos.isEmpty()) {
        NinjaToast.show(activity, "no saved epub!")
    } else {
        dialogManager.showSaveEpubDialog(shouldAddNewEpub = false) { uri ->
            HelperUnit.openFile(activity, uri ?: return@showSaveEpubDialog)
        }
    }

    private fun printPDF(ninjaWebView: NinjaWebView) {
        try {
            val title = HelperUnit.fileName(ninjaWebView.url)
            val printManager =
                activity.getSystemService(FragmentActivity.PRINT_SERVICE) as PrintManager
            val printAdapter = ninjaWebView.createPrintDocumentAdapter(title) {
                showFileListConfirmDialog()
            }
            printManager.print(title, printAdapter, PrintAttributes.Builder().build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showFileListConfirmDialog() {
        dialogManager.showOkCancelDialog(
            messageResId = R.string.toast_downloadComplete,
            okAction = { BrowserUnit.openDownloadFolder(activity) }
        )
    }
}