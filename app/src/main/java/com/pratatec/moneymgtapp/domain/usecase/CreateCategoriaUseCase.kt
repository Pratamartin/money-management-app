package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class CreateCategoriaUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(nome: String) = repository.createCategoria(nome)
}
