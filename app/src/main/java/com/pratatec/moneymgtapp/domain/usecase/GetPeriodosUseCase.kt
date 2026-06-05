package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class GetPeriodosUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke() = repository.getPeriodos()
}
