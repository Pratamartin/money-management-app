package com.pratatec.moneymgtapp.wear.presentation.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pratatec.moneymgtapp.wear.data.local.WearTokenStorage
import com.pratatec.moneymgtapp.wear.data.remote.api.WearFinanceApi
import com.pratatec.moneymgtapp.wear.data.remote.api.WearKtorClient
import com.pratatec.moneymgtapp.wear.data.repository.WearFinanceRepositoryImpl
import com.pratatec.moneymgtapp.wear.domain.model.Resumo
import com.pratatec.moneymgtapp.wear.domain.usecase.GetPeriodosUseCase
import com.pratatec.moneymgtapp.wear.domain.usecase.GetResumoUseCase
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val isLoading: Boolean = false,
    val notAuthenticated: Boolean = false,
    val semPeriodo: Boolean = false,
    val resumo: Resumo? = null,
    val error: String? = null,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenStorage = WearTokenStorage(app)
    private val httpClient = WearKtorClient.create(tokenStorage)
    private val api = WearFinanceApi(httpClient)
    private val repository = WearFinanceRepositoryImpl(api)
    private val getPeriodosUseCase = GetPeriodosUseCase(repository)
    private val getResumoUseCase = GetResumoUseCase(repository)

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        load()
    }

    fun load() {
        if (!tokenStorage.hasTokens()) return
        val cal = Calendar.getInstance()
        val mes = cal.get(Calendar.MONTH) + 1
        val ano = cal.get(Calendar.YEAR)

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val periodosResult = getPeriodosUseCase()
            if (periodosResult.isFailure) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao carregar.")
                return@launch
            }

            val periodo = periodosResult.getOrElse { emptyList() }
                .find { it.mes == mes && it.ano == ano }

            if (periodo == null) {
                uiState = uiState.copy(isLoading = false, semPeriodo = true)
                return@launch
            }

            val resumoResult = getResumoUseCase(periodo.id)
            uiState = uiState.copy(
                isLoading = false,
                semPeriodo = false,
                resumo = resumoResult.getOrNull(),
                error = if (resumoResult.isFailure) "Erro ao carregar resumo." else null,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}

class HomeViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(app) as T
}
