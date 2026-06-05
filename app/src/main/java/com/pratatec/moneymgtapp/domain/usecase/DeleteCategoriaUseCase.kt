package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class DeleteCategoriaUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(id: Int) = repository.deleteCategoria(id)
}
