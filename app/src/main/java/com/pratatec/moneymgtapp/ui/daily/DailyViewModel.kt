package com.pratatec.moneymgtapp.ui.daily

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pratatec.moneymgtapp.data.local.TokenStorage
import com.pratatec.moneymgtapp.data.remote.api.FinanceApi
import com.pratatec.moneymgtapp.data.remote.api.KtorClient
import com.pratatec.moneymgtapp.data.repository.FinanceRepositoryImpl
import com.pratatec.moneymgtapp.domain.model.Categoria
import com.pratatec.moneymgtapp.domain.model.GastoDoDia
import com.pratatec.moneymgtapp.domain.model.Resumo
import com.pratatec.moneymgtapp.domain.repository.FinanceRepository
import com.pratatec.moneymgtapp.domain.usecase.CreateGastoUseCase
import com.pratatec.moneymgtapp.domain.usecase.DeleteGastoUseCase
import com.pratatec.moneymgtapp.domain.usecase.GetCategoriasUseCase
import com.pratatec.moneymgtapp.domain.usecase.GetGastosDoDiaUseCase
import com.pratatec.moneymgtapp.domain.usecase.GetResumoUseCase
import com.pratatec.moneymgtapp.ui.shared.AddGastoSheetState
import kotlinx.coroutines.launch

data class DailyUiState(
    val isLoading: Boolean = false,
    val gastosDoMes: List<GastoDoDia> = emptyList(),
    val resumo: Resumo? = null,
    val categorias: List<Categoria> = emptyList(),
    val error: String? = null,
)

data class DaySheetState(
    val visible: Boolean = false,
    val gastoDoDia: GastoDoDia? = null,
)

class DailyViewModel(app: Application, val periodoId: Int) : AndroidViewModel(app) {

    private val tokenStorage = TokenStorage(app)
    private val httpClient = KtorClient.create(tokenStorage)
    private val api = FinanceApi(httpClient)
    private val repository: FinanceRepository = FinanceRepositoryImpl(api)

    private val getGastosDoDiaUseCase = GetGastosDoDiaUseCase(repository)
    private val getResumoUseCase = GetResumoUseCase(repository)
    private val getCategoriasUseCase = GetCategoriasUseCase(repository)
    private val createGastoUseCase = CreateGastoUseCase(repository)
    private val deleteGastoUseCase = DeleteGastoUseCase(repository)

    var uiState by mutableStateOf(DailyUiState())
        private set

    var daySheet by mutableStateOf(DaySheetState())
        private set

    var addSheet by mutableStateOf(AddGastoSheetState())
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val gastosResult = getGastosDoDiaUseCase(periodoId)
            val resumoResult = getResumoUseCase(periodoId)
            val categoriasResult = getCategoriasUseCase()
            uiState = uiState.copy(
                isLoading = false,
                gastosDoMes = gastosResult.getOrElse { emptyList() },
                resumo = resumoResult.getOrNull(),
                categorias = categoriasResult.getOrElse { emptyList() },
                error = if (gastosResult.isFailure) "Erro ao carregar dados." else null,
            )
        }
    }

    fun openDaySheet(date: String) {
        val gastoDoDia = uiState.gastosDoMes.find { it.data == date }
            ?: GastoDoDia(data = date, gastos = emptyList(), totalDia = 0.0)
        daySheet = DaySheetState(visible = true, gastoDoDia = gastoDoDia)
    }

    fun closeDaySheet() {
        daySheet = DaySheetState()
    }

    fun openAddGasto(date: String) {
        daySheet = DaySheetState()
        addSheet = AddGastoSheetState(visible = true, selectedDate = date)
    }

    fun closeAddGasto() {
        addSheet = AddGastoSheetState()
    }

    fun updateValor(value: String) {
        addSheet = addSheet.copy(valor = value, error = null)
    }

    fun selectCategoria(id: Int) {
        addSheet = addSheet.copy(categoriaId = id, error = null)
    }

    fun updateDescricao(value: String) {
        addSheet = addSheet.copy(descricao = value)
    }

    fun saveGasto() {
        val sheet = addSheet
        val valor = sheet.valor.replace(",", ".").toDoubleOrNull()
        if (valor == null || valor <= 0) {
            addSheet = addSheet.copy(error = "Informe um valor válido.")
            return
        }
        val categoriaId = sheet.categoriaId ?: run {
            addSheet = addSheet.copy(error = "Selecione uma categoria.")
            return
        }
        viewModelScope.launch {
            addSheet = addSheet.copy(isSaving = true)
            createGastoUseCase(periodoId, sheet.selectedDate, valor, categoriaId, sheet.descricao)
                .onSuccess {
                    closeAddGasto()
                    load()
                }
                .onFailure {
                    addSheet = addSheet.copy(isSaving = false, error = "Erro ao salvar gasto.")
                }
        }
    }

    fun deleteGasto(gastoId: Int) {
        viewModelScope.launch {
            deleteGastoUseCase(periodoId, gastoId).onSuccess {
                closeDaySheet()
                load()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}

class DailyViewModelFactory(
    private val app: Application,
    private val periodoId: Int,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DailyViewModel(app, periodoId) as T
}
