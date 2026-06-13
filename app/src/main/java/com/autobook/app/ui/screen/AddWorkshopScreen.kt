package com.autobook.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.autobook.app.R
import com.autobook.app.ui.component.WorkshopForm
import com.autobook.app.ui.viewmodel.WorkshopViewModel

@Composable
fun AddWorkshopScreen(
    viewModel: WorkshopViewModel,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val savedMsg = stringResource(R.string.workshop_saved)
    WorkshopForm(
        title = stringResource(R.string.add_workshop_title),
        initial = null,
        onBack = onBack,
        onSave = { workshop ->
            viewModel.insertWorkshop(workshop) {
                onShowMessage(savedMsg)
                onBack()
            }
        }
    )
}
