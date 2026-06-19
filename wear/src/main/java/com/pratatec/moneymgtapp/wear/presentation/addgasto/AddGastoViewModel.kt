package com.pratatec.moneymgtapp.wear.presentation.addgasto

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
import com.pratatec.moneymgtapp.wear.domain.model.Categoria
import com.pratatec.moneymgtapp.wear.domain.usecase.CreateGastoUseCase
import com.pratatec.moneymgtapp.wear.domain.usecase.GetCategoriasUseCase
import com.pratatec.moneymgtapp.wear.domain.usecase.GetPeriodosUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddGastoUiState(
    val isLoading: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val selectedCategoriaIndex: Int = 0,
    val valorIndex: Int = 1,
    val isSaving: Boolean = false,
    val error: String? = null,
)

sealed class AddGastoEvent {
    object Success : AddGastoEvent()
}

class AddGastoViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenStorage = WearTokenStorage(app)
    private val httpClient = WearKtorClient.create(tokenStorage)
    private val api = WearFinanceApi(httpClient)
    private val repository = WearFinanceRepositoryImpl(api)
    private val getCategoriasUseCase = GetCategoriasUseCase(repository)
    private val getPeriodosUseCase = GetPeriodosUseCase(repository)
    private val createGastoUseCase = CreateGastoUseCase(repository)

    var uiState by mutableStateOf(AddGastoUiState())
        private set

    private val _events = Channel<AddGastoEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadCategorias()
    }

    private fun loadCategorias() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val result = getCategoriasUseCase()
            uiState = uiState.copy(
                isLoading = false,
                categorias = result.getOrElse { emptyList() },
            )
        }
    }

    fun selectCategoria(index: Int) {
        uiState = uiState.copy(selectedCategoriaIndex = index, error = null)
    }

    fun selectValorIndex(index: Int) {
        uiState = uiState.copy(valorIndex = index, error = null)
    }

    fun save() {
        val categorias = uiState.categorias
        if (categorias.isEmpty()) {
            uiState = uiState.copy(error = "Sem categorias disponíveis.")
            return
        }
        val categoria = categorias[uiState.selectedCategoriaIndex]
        val valor = (uiState.valorIndex + 1) * 5.0

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true)

            val cal = Calendar.getInstance()
            val mes = cal.get(Calendar.MONTH) + 1
            val ano = cal.get(Calendar.YEAR)
            val hoje = "%04d-%02d-%02d".format(ano, mes, cal.get(Calendar.DAY_OF_MONTH))

            val periodos = getPeriodosUseCase().getOrElse { emptyList() }
            val periodo = periodos.find { it.mes == mes && it.ano == ano }

            if (periodo == null) {
                uiState = uiState.copy(isSaving = false, error = "Sem período ativo.")
                return@launch
            }

            createGastoUseCase(periodo.id, hoje, valor, categoria.id, "")
                .onSuccess { _events.send(AddGastoEvent.Success) }
                .onFailure { uiState = uiState.copy(isSaving = false, error = "Erro ao salvar.") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}

class AddGastoViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        AddGastoViewModel(app) as T
}
