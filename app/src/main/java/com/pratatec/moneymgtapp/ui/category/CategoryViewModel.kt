package com.pratatec.moneymgtapp.ui.category

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pratatec.moneymgtapp.data.local.TokenStorage
import com.pratatec.moneymgtapp.data.remote.api.FinanceApi
import com.pratatec.moneymgtapp.data.remote.api.KtorClient
import com.pratatec.moneymgtapp.data.repository.FinanceRepositoryImpl
import com.pratatec.moneymgtapp.domain.model.Categoria
import com.pratatec.moneymgtapp.domain.repository.FinanceRepository
import com.pratatec.moneymgtapp.domain.usecase.CreateCategoriaUseCase
import com.pratatec.moneymgtapp.domain.usecase.DeleteCategoriaUseCase
import com.pratatec.moneymgtapp.domain.usecase.GetCategoriasUseCase
import kotlinx.coroutines.launch

data class CategoryUiState(
    val isLoading: Boolean = false,
    val defaultCategories: List<Categoria> = emptyList(),
    val customCategories: List<Categoria> = emptyList(),
    val error: String? = null,
)

data class CreateCategorySheetState(
    val visible: Boolean = false,
    val nome: String = "",
    val selectedIconName: String = "Category",
    val isSaving: Boolean = false,
    val error: String? = null,
)

class CategoryViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenStorage = TokenStorage(app)
    private val httpClient = KtorClient.create(tokenStorage)
    private val api = FinanceApi(httpClient)
    private val repository: FinanceRepository = FinanceRepositoryImpl(api)

    private val getCategoriasUseCase = GetCategoriasUseCase(repository)
    private val createCategoriaUseCase = CreateCategoriaUseCase(repository)
    private val deleteCategoriaUseCase = DeleteCategoriaUseCase(repository)

    var uiState by mutableStateOf(CategoryUiState())
        private set

    var createSheet by mutableStateOf(CreateCategorySheetState())
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            getCategoriasUseCase()
                .onSuccess { categorias ->
                    uiState = uiState.copy(
                        isLoading = false,
                        defaultCategories = categorias.filter { it.tipo == "DEFAULT" },
                        customCategories = categorias.filter { it.tipo == "CUSTOM" },
                    )
                }
                .onFailure {
                    uiState = uiState.copy(isLoading = false, error = "Erro ao carregar categorias.")
                }
        }
    }

    fun openCreateSheet() {
        createSheet = CreateCategorySheetState(visible = true)
    }

    fun closeCreateSheet() {
        createSheet = CreateCategorySheetState()
    }

    fun updateNome(value: String) {
        createSheet = createSheet.copy(nome = value, error = null)
    }

    fun selectIcon(iconName: String) {
        createSheet = createSheet.copy(selectedIconName = iconName)
    }

    fun saveCategoria() {
        val nome = createSheet.nome.trim()
        if (nome.length < 2) {
            createSheet = createSheet.copy(error = "Nome deve ter pelo menos 2 caracteres.")
            return
        }
        viewModelScope.launch {
            createSheet = createSheet.copy(isSaving = true)
            createCategoriaUseCase(nome)
                .onSuccess {
                    closeCreateSheet()
                    load()
                }
                .onFailure {
                    createSheet = createSheet.copy(isSaving = false, error = "Erro ao criar categoria.")
                }
        }
    }

    fun deleteCategoria(id: Int) {
        viewModelScope.launch {
            deleteCategoriaUseCase(id).onSuccess { load() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}
