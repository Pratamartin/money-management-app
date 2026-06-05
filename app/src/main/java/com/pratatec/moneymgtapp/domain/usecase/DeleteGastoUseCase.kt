package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class DeleteGastoUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(periodoId: Int, gastoId: Int) =
        repository.deleteGasto(periodoId, gastoId)
}
