package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class GetCategoriasUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke() = repository.getCategorias()
}
