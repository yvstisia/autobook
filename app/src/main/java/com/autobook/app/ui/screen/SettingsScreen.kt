package com.autobook.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobook.app.BuildConfig
import com.autobook.app.R
import com.autobook.app.data.preferences.AppLanguage
import com.autobook.app.data.preferences.ThemeMode
import com.autobook.app.ui.component.AutoBookCard
import com.autobook.app.ui.component.CurrencyDropdown
import com.autobook.app.ui.component.FormTextField
import com.autobook.app.ui.component.SegmentedChips
import com.autobook.app.ui.theme.ScreenPaddingH
import com.autobook.app.ui.theme.SectionGap
import com.autobook.app.ui.theme.autoBookColors
import com.autobook.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val persistedName by viewModel.userName.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    // Local editable copy so typing stays responsive; re-seeded if the stored value
    // changes from elsewhere (e.g. first load).
    var name by remember { mutableStateOf(persistedName) }
    LaunchedEffect(persistedName) { if (persistedName != name) name = persistedName }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPaddingH, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(SectionGap)
        ) {
            // Profile
            SettingsSection(title = stringResource(R.string.settings_section_profile)) {
                FormTextField(
                    label = stringResource(R.string.settings_name_label),
                    value = name,
                    onValueChange = {
                        name = it
                        viewModel.setUserName(it)
                    }
                )
            }

            // Appearance / theme
            SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                SegmentedChips(
                    options = listOf(
                        ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                        ThemeMode.LIGHT to stringResource(R.string.theme_light),
                        ThemeMode.DARK to stringResource(R.string.theme_dark)
                    ),
                    selected = themeMode,
                    onSelect = viewModel::setThemeMode
                )
            }

            // Language
            SettingsSection(title = stringResource(R.string.settings_section_language)) {
                SegmentedChips(
                    options = listOf(
                        AppLanguage.SYSTEM to stringResource(R.string.lang_system),
                        AppLanguage.INDONESIAN to stringResource(R.string.lang_indonesian),
                        AppLanguage.ENGLISH to stringResource(R.string.lang_english)
                    ),
                    selected = language,
                    onSelect = viewModel::setLanguage
                )
            }

            // Currency
            SettingsSection(title = stringResource(R.string.settings_section_currency)) {
                CurrencyDropdown(selected = currency, onSelect = viewModel::setCurrency)
                Text(
                    text = stringResource(R.string.currency_change_note),
                    style = MaterialTheme.typography.labelMedium,
                    color = autoBookColors.textTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // About
            SettingsSection(title = stringResource(R.string.settings_section_about)) {
                Text(
                    text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                    color = autoBookColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = autoBookColors.textSecondary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        AutoBookCard { content() }
    }
}
