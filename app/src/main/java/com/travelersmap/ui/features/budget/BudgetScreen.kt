package com.travelersmap.ui.features.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelersmap.domain.model.BudgetBreakdown
import com.travelersmap.domain.model.BudgetInput
import com.travelersmap.domain.repository.BudgetRepository
import com.travelersmap.ui.theme.GlassCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BudgetForm(
    val budget: String = "500",
    val days: String = "5",
    val travelers: String = "2"
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepo: BudgetRepository
) : ViewModel() {
    private val form = MutableStateFlow(BudgetForm())
    val formState: StateFlow<BudgetForm> = form.asStateFlow()

    private val _result = MutableStateFlow(budgetRepo.estimate(BudgetInput(500.0, 5, 2)))
    val result: StateFlow<BudgetBreakdown> = _result.asStateFlow()

    fun setBudget(v: String) {
        form.update { it.copy(budget = v.filter { c -> c.isDigit() || c == '.' }) }
        recompute()
    }

    fun setDays(v: String) {
        form.update { it.copy(days = v.filter { c -> c.isDigit() }) }
        recompute()
    }

    fun setTravelers(v: String) {
        form.update { it.copy(travelers = v.filter { c -> c.isDigit() }) }
        recompute()
    }

    private fun recompute() {
        val f = form.value
        _result.value = budgetRepo.estimate(
            BudgetInput(
                totalBudgetUsd = f.budget.toDoubleOrNull() ?: 0.0,
                days = f.days.toIntOrNull() ?: 1,
                travelers = f.travelers.toIntOrNull() ?: 1
            )
        )
    }
}

@Composable
fun BudgetScreen(vm: BudgetViewModel = hiltViewModel()) {
    val form by vm.formState.collectAsStateWithLifecycle()
    val result by vm.result.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Budget planner", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Rough Uzbekistan trip estimates (USD). Adjust freely.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form.budget,
            onValueChange = vm::setBudget,
            label = { Text("Total budget (USD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = form.days,
            onValueChange = vm::setDays,
            label = { Text("Days") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = form.travelers,
            onValueChange = vm::setTravelers,
            label = { Text("Travelers") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Line("Hotels", result.hotels)
                Line("Food", result.food)
                Line("Transportation", result.transportation)
                Line("Tickets / entries", result.tickets)
                Line("Daily budget", result.dailyBudget)
                Line(
                    if (result.remaining >= 0) "Remaining" else "Over budget",
                    result.remaining
                )
            }
        }
    }
}

@Composable
private fun Line(label: String, amount: Double) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$%.0f".format(amount), style = MaterialTheme.typography.titleLarge)
    }
}
