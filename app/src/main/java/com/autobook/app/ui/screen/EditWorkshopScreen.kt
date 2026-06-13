package com.autobook.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.autobook.app.R
import com.autobook.app.data.local.entity.Workshop
import com.autobook.app.ui.component.WorkshopForm
import com.autobook.app.ui.viewmodel.WorkshopViewModel

@Composable
fun EditWorkshopScreen(
    viewModel: WorkshopViewModel,
    workshopId: Int,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    var workshop by remember { mutableStateOf<Workshop?>(null) }

    LaunchedEffect(workshopId) {
        viewModel.loadWorkshop(workshopId) { workshop = it }
    }

    val savedMsg = stringResource(R.string.workshop_saved)
    val deletedMsg = stringResource(R.string.workshop_deleted)

    val loaded = workshop
    if (loaded == null) {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    } else {
        WorkshopForm(
            title = stringResource(R.string.edit_workshop_title),
            initial = loaded,
            onBack = onBack,
            onSave = { updated ->
                viewModel.updateWorkshop(updated) {
                    onShowMessage(savedMsg)
                    onBack()
                }
            },
            onDelete = {
                viewModel.deleteWorkshop(loaded) {
                    onShowMessage(deletedMsg)
                    onBack()
                }
            }
        )
    }
}
