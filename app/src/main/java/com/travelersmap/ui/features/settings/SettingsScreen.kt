package com.travelersmap.ui.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.travelersmap.domain.model.AppLanguage
import com.travelersmap.domain.model.AppSettings
import com.travelersmap.domain.repository.SettingsRepository
import com.travelersmap.ui.components.SectionLabel
import com.travelersmap.ui.theme.GlassCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setDark(enabled: Boolean) = viewModelScope.launch { settingsRepo.setDarkMode(enabled) }
    fun setLang(lang: AppLanguage) = viewModelScope.launch { settingsRepo.setLanguage(lang) }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        GlassCard(modifier = Modifier.fillMaxWidth(), corner = 18.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Dark mode", style = MaterialTheme.typography.titleLarge)
                    Text("Default for MVP", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.darkMode, onCheckedChange = vm::setDark)
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("Language")
        Row {
            listOf(
                AppLanguage.ENGLISH to "English",
                AppLanguage.UZBEK to "Oʻzbek",
                AppLanguage.RUSSIAN to "Русский"
            ).forEach { (lang, label) ->
                FilterChip(
                    selected = settings.language == lang,
                    onClick = { vm.setLang(lang) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        Text(
            "Language preference is stored locally. Full UI strings can be expanded later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(24.dp))
        SectionLabel("About")
        GlassCard(modifier = Modifier.fillMaxWidth(), corner = 18.dp) {
            Column(Modifier.padding(16.dp)) {
                Text("Traveler's Map", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Premium Uzbekistan travel map MVP. No accounts. Offline-first catalog of tourist places.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text("Version 1.0.0-mvp", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionLabel("Privacy")
        GlassCard(modifier = Modifier.fillMaxWidth(), corner = 18.dp) {
            Text(
                "Favorites and settings stay on device. Map tiles and images may use the network when available. No login, no analytics in this MVP.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
