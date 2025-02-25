package info.plateaukao.einkbro.view.dialog.compose

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import info.plateaukao.einkbro.R
import info.plateaukao.einkbro.view.compose.MyTheme
import info.plateaukao.einkbro.viewmodel.GptViewModel


class GPTDialogFragment(
    private val gptViewModel: GptViewModel,
    private val anchorPoint: Point,
    private val hasBackgroundColor: Boolean = false,
    private val onDismissed: () -> Unit = {}
) : DraggableComposeDialogFragment() {

    override fun setupComposeView() = composeView.setContent {
        MyTheme {
            GptResponse(gptViewModel, hasBackgroundColor)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setupDialogPosition(anchorPoint)

        gptViewModel.query()
        if (hasBackgroundColor) {
            dialog?.window?.setBackgroundDrawableResource(R.drawable.white_bgd_with_border_margin)
        }
        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        onDismissed()
    }
}

@Composable
private fun GptResponse(gptViewModel: GptViewModel, hasBackgroundColor: Boolean) {
    val requestMessage by gptViewModel.inputMessage.collectAsState()
    val responseMessage by gptViewModel.responseMessage.collectAsState()
    val showRequest = remember { mutableStateOf(false) }

    Box {
        Column(
            modifier = Modifier
                .defaultMinSize(minWidth = 200.dp)
                .wrapContentHeight()
                .wrapContentWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showRequest.value) {
                Text(
                    text = requestMessage,
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier.padding(10.dp),
                )
                Divider()
            }
            Text(
                text = responseMessage,
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier.padding(
                    top = if (!showRequest.value) 25.dp else 10.dp,
                    bottom = 10.dp,
                    start = 10.dp,
                    end = 10.dp
                ),
            )
        }
        if (!showRequest.value) {
            Icon(
                painter = painterResource(id = R.drawable.icon_arrow_down_gest),
                tint = MaterialTheme.colors.onBackground,
                contentDescription = "Info Icon",
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .clickable { showRequest.value = true }
            )
        }
    }
}

