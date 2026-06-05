package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class CreatePeriodoUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(mes: Int, ano: Int, saldoCarteira: Double, saldoDisponivelMes: Double) =
        repository.createPeriodo(mes, ano, saldoCarteira, saldoDisponivelMes)
}
