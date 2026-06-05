package com.pratatec.moneymgtapp.ui.home

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
import com.pratatec.moneymgtapp.domain.model.Periodo
import com.pratatec.moneymgtapp.domain.model.Resumo
import com.pratatec.moneymgtapp.domain.repository.FinanceRepository
import com.pratatec.moneymgtapp.domain.usecase.CreateGastoUseCase
import com.pratatec.moneymgtapp.domain.usecase.CreatePeriodoUseCase
import com.pratatec.moneymgtapp.domain.usecase.GetCategoriasUseCase
import com.pratatec.moneymgtapp.domain.usecase.GetGastosDoDiaUseCase
import com.pratatec.moneymgtapp.domain.usecase.GetPeriodosUseCase
import com.pratatec.moneymgtapp.domain.usecase.GetResumoUseCase
import android.util.Log
import com.pratatec.moneymgtapp.ui.shared.AddGastoSheetState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val isLoading: Boolean = false,
    val semPeriodo: Boolean = false,
    val periodo: Periodo? = null,
    val resumo: Resumo? = null,
    val gastoHoje: Double = 0.0,
    val categorias: List<Categoria> = emptyList(),
    val error: String? = null,
    val mes: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val ano: Int = Calendar.getInstance().get(Calendar.YEAR),
)

data class CreatePeriodoSheetState(
    val visible: Boolean = false,
    val saldoCarteira: String = "",
    val saldoDisponivelMes: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

sealed class HomeEvent {
    object SessionExpired : HomeEvent()
}

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val tokenStorage = TokenStorage(app)
    private val httpClient = KtorClient.create(tokenStorage)
    private val api = FinanceApi(httpClient)
    private val repository: FinanceRepository = FinanceRepositoryImpl(api)

    private val getPeriodosUseCase = GetPeriodosUseCase(repository)
    private val createPeriodoUseCase = CreatePeriodoUseCase(repository)
    private val getResumoUseCase = GetResumoUseCase(repository)
    private val getGastosDoDiaUseCase = GetGastosDoDiaUseCase(repository)
    private val getCategoriasUseCase = GetCategoriasUseCase(repository)
    private val createGastoUseCase = CreateGastoUseCase(repository)

    var uiState by mutableStateOf(HomeUiState())
        private set

    var createSheet by mutableStateOf(CreatePeriodoSheetState())
        private set

    var addGastoSheet by mutableStateOf(AddGastoSheetState())
        private set

    init {
        load()
    }

    fun load() {
        val mes = uiState.mes
        val ano = uiState.ano
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, semPeriodo = false)

            val periodosResult = getPeriodosUseCase()
            Log.d("HomeViewModel", "load mes=$mes ano=$ano | periodos isFailure=${periodosResult.isFailure} err=${periodosResult.exceptionOrNull()?.message}")
            if (periodosResult.isFailure) {
                val msg = periodosResult.exceptionOrNull()?.message.orEmpty()
                if ("user_not_found" in msg || "token" in msg.lowercase() || "authentication" in msg.lowercase()) {
                    tokenStorage.clear()
                    _events.send(HomeEvent.SessionExpired)
                    return@launch
                }
                uiState = uiState.copy(isLoading = false, error = "Erro ao carregar dados.")
                return@launch
            }

            val periodos = periodosResult.getOrElse { emptyList() }
            Log.d("HomeViewModel", "periodos count=${periodos.size} ids=${periodos.map { it.id }}")
            val periodo = periodos.find { it.mes == mes && it.ano == ano }
            Log.d("HomeViewModel", "periodo encontrado: $periodo")
            if (periodo == null) {
                uiState = uiState.copy(isLoading = false, semPeriodo = true, periodo = null, resumo = null, gastoHoje = 0.0)
                return@launch
            }

            val resumoResult = getResumoUseCase(periodo.id)
            Log.d("HomeViewModel", "resumo isFailure=${resumoResult.isFailure} err=${resumoResult.exceptionOrNull()?.message} value=${resumoResult.getOrNull()}")
            val gastosResult = getGastosDoDiaUseCase(periodo.id)
            Log.d("HomeViewModel", "gastos isFailure=${gastosResult.isFailure} count=${gastosResult.getOrElse{emptyList()}.size}")
            val categoriasResult = getCategoriasUseCase()

            val cal = Calendar.getInstance()
            val hoje = "%04d-%02d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
            )
            val gastoHoje = gastosResult.getOrElse { emptyList() }
                .find { it.data == hoje }?.totalDia ?: 0.0

            uiState = uiState.copy(
                isLoading = false,
                semPeriodo = false,
                periodo = periodo,
                resumo = resumoResult.getOrNull(),
                gastoHoje = gastoHoje,
                categorias = categoriasResult.getOrElse { emptyList() },
                error = if (resumoResult.isFailure) "Erro ao carregar resumo do período." else null,
            )
        }
    }

    fun navigateMes(delta: Int) {
        var mes = uiState.mes + delta
        var ano = uiState.ano
        if (mes > 12) { mes = 1; ano++ }
        if (mes < 1) { mes = 12; ano-- }
        uiState = uiState.copy(mes = mes, ano = ano, resumo = null, periodo = null, semPeriodo = false, gastoHoje = 0.0)
        load()
    }

    // ── Criar período ────────────────────────────────────────────────────────

    fun openCreatePeriodo() { createSheet = CreatePeriodoSheetState(visible = true) }
    fun closeCreatePeriodo() { createSheet = CreatePeriodoSheetState() }
    fun updateSaldoCarteira(v: String) { createSheet = createSheet.copy(saldoCarteira = v, error = null) }
    fun updateSaldoDisponivel(v: String) { createSheet = createSheet.copy(saldoDisponivelMes = v, error = null) }

    fun saveCreatePeriodo() {
        val carteira = createSheet.saldoCarteira.replace(",", ".").toDoubleOrNull()
        val disponivel = createSheet.saldoDisponivelMes.replace(",", ".").toDoubleOrNull()
        if (carteira == null || carteira < 0) {
            createSheet = createSheet.copy(error = "Informe um saldo de carteira válido.")
            return
        }
        if (disponivel == null || disponivel < 0) {
            createSheet = createSheet.copy(error = "Informe um orçamento disponível válido.")
            return
        }
        viewModelScope.launch {
            createSheet = createSheet.copy(isSaving = true)
            createPeriodoUseCase(uiState.mes, uiState.ano, carteira, disponivel)
                .onSuccess {
                    closeCreatePeriodo()
                    load()
                }
                .onFailure {
                    createSheet = createSheet.copy(isSaving = false, error = "Erro ao criar período.")
                }
        }
    }

    // ── Registrar gasto ──────────────────────────────────────────────────────

    fun openAddGasto() {
        val cal = Calendar.getInstance()
        val hoje = "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
        )
        addGastoSheet = AddGastoSheetState(visible = true, selectedDate = hoje)
    }

    fun closeAddGasto() { addGastoSheet = AddGastoSheetState() }
    fun updateValor(v: String) { addGastoSheet = addGastoSheet.copy(valor = v, error = null) }
    fun selectCategoria(id: Int) { addGastoSheet = addGastoSheet.copy(categoriaId = id, error = null) }
    fun updateDescricao(v: String) { addGastoSheet = addGastoSheet.copy(descricao = v) }

    fun saveGasto() {
        val sheet = addGastoSheet
        val periodoId = uiState.periodo?.id ?: return
        val valor = sheet.valor.replace(",", ".").toDoubleOrNull()
        if (valor == null || valor <= 0) {
            addGastoSheet = addGastoSheet.copy(error = "Informe um valor válido.")
            return
        }
        val categoriaId = sheet.categoriaId ?: run {
            addGastoSheet = addGastoSheet.copy(error = "Selecione uma categoria.")
            return
        }
        viewModelScope.launch {
            addGastoSheet = addGastoSheet.copy(isSaving = true)
            createGastoUseCase(periodoId, sheet.selectedDate, valor, categoriaId, sheet.descricao)
                .onSuccess {
                    closeAddGasto()
                    load()
                }
                .onFailure {
                    addGastoSheet = addGastoSheet.copy(isSaving = false, error = "Erro ao salvar gasto.")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}

class HomeViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(app) as T
}
