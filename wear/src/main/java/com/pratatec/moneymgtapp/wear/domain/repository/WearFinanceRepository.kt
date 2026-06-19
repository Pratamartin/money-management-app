package com.pratatec.moneymgtapp.wear.domain.repository

import com.pratatec.moneymgtapp.wear.domain.model.Categoria
import com.pratatec.moneymgtapp.wear.domain.model.Periodo
import com.pratatec.moneymgtapp.wear.domain.model.Resumo

interface WearFinanceRepository {
    suspend fun getPeriodos(): Result<List<Periodo>>
    suspend fun getResumo(periodoId: Int): Result<Resumo>
    suspend fun getCategorias(): Result<List<Categoria>>
    suspend fun createGasto(periodoId: Int, data: String, valor: Double, categoriaId: Int, descricao: String): Result<Unit>
}
