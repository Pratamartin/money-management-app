package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class CreateGastoUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(periodoId: Int, data: String, valor: Double, categoriaId: Int, descricao: String) =
        repository.createGasto(periodoId, data, valor, categoriaId, descricao)
}
