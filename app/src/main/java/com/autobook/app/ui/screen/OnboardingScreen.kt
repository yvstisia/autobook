package com.autobook.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobook.app.R
import com.autobook.app.data.preferences.AppLanguage
import com.autobook.app.ui.component.CurrencyDropdown
import com.autobook.app.ui.component.SegmentedChips
import com.autobook.app.ui.theme.RadiusButton
import com.autobook.app.ui.theme.ScreenPaddingH
import com.autobook.app.ui.theme.SectionGap
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.viewmodel.SettingsViewModel

/**
 * First-run screen: pick language and currency before entering the app. Language changes
 * apply the locale live (the screen re-renders translated); currency is the currency the
 * user will record amounts in. "Continue" marks onboarding complete and proceeds.
 */
@Composable
fun OnboardingScreen(
    viewModel: SettingsViewModel,
    onDone: () -> Unit
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenPaddingH)
            .padding(top = 48.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(SectionGap)
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineLarge,
            color = autoBookColors.textPrimary
        )
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = autoBookColors.textSecondary
        )

        Column {
            Text(
                text = stringResource(R.string.settings_section_language),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // SYSTEM is omitted here so the choice is explicit on first run.
            SegmentedChips(
                options = listOf(
                    AppLanguage.INDONESIAN to stringResource(R.string.lang_indonesian),
                    AppLanguage.ENGLISH to stringResource(R.string.lang_english)
                ),
                selected = language,
                onSelect = viewModel::setLanguage
            )
        }

        Column {
            Text(
                text = stringResource(R.string.settings_section_currency),
                style = MaterialTheme.typography.labelMedium,
                color = autoBookColors.textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CurrencyDropdown(selected = currency, onSelect = viewModel::setCurrency)
        }

        Button(
            onClick = {
                viewModel.completeOnboarding()
                onDone()
            },
            shape = RadiusButton,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                stringResource(R.string.onboarding_continue),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
