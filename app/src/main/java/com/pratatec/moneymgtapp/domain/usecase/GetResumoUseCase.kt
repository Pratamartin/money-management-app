package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class GetResumoUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(periodoId: Int) = repository.getResumo(periodoId)
}
