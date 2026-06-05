package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.model.GastoDoDia
import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class GetGastosDoDiaUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(periodoId: Int): Result<List<GastoDoDia>> =
        repository.getGastosDiarios(periodoId).map { gastos ->
            gastos
                .groupBy { it.data }
                .map { (data, lista) ->
                    GastoDoDia(
                        data = data,
                        gastos = lista,
                        totalDia = lista.sumOf { it.valor },
                    )
                }
                .sortedBy { it.data }
        }
}
