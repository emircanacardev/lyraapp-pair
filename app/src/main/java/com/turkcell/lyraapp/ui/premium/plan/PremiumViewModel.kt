package com.turkcell.lyraapp.ui.premium.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.membership.MembershipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PremiumEffect>(Channel.BUFFERED)
    val effect: Flow<PremiumEffect> = _effect.receiveAsFlow()

    init {
        loadPlans()
    }

    fun onIntent(intent: PremiumIntent) {
        when (intent) {
            is PremiumIntent.PlanSelected -> {
                _uiState.update { it.copy(selectedPlanId = intent.planId) }
            }
            is PremiumIntent.ContinueClicked -> {
                val state = _uiState.value
                if (state.selectedPlanId.isBlank()) return
                val plan = state.plans.find { it.id == state.selectedPlanId } ?: return
                viewModelScope.launch {
                    _effect.send(
                        PremiumEffect.NavigateToPayment(
                            planId = plan.id,
                            planType = plan.type,
                            planName = plan.name,
                            planPrice = plan.price,
                        )
                    )
                }
            }
            is PremiumIntent.BackClicked -> {
                viewModelScope.launch { _effect.send(PremiumEffect.NavigateBack) }
            }
        }
    }

    private fun loadPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            membershipRepository.getPlans()
                .onSuccess { plans ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            plans = plans,
                            selectedPlanId = plans.firstOrNull()?.id ?: "",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }
}
