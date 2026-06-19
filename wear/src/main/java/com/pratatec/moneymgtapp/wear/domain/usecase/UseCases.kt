package com.pratatec.moneymgtapp.wear.domain.usecase

import com.pratatec.moneymgtapp.wear.domain.repository.WearFinanceRepository

class GetPeriodosUseCase(private val repo: WearFinanceRepository) {
    suspend operator fun invoke() = repo.getPeriodos()
}

class GetResumoUseCase(private val repo: WearFinanceRepository) {
    suspend operator fun invoke(periodoId: Int) = repo.getResumo(periodoId)
}

class GetCategoriasUseCase(private val repo: WearFinanceRepository) {
    suspend operator fun invoke() = repo.getCategorias()
}

class CreateGastoUseCase(private val repo: WearFinanceRepository) {
    suspend operator fun invoke(
        periodoId: Int,
        data: String,
        valor: Double,
        categoriaId: Int,
        descricao: String,
    ) = repo.createGasto(periodoId, data, valor, categoriaId, descricao)
}
