package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class UpdatePeriodoUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(periodoId: Int, saldoCarteira: Double? = null, saldoDisponivelMes: Double? = null) =
        repository.updatePeriodo(periodoId, saldoCarteira, saldoDisponivelMes)
}
