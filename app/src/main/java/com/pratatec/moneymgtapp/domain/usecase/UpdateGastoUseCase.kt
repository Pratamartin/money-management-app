package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class UpdateGastoUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(periodoId: Int, gastoId: Int, valor: Double? = null, categoriaId: Int? = null, descricao: String? = null) =
        repository.updateGasto(periodoId, gastoId, valor, categoriaId, descricao)
}
