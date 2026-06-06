package com.pratatec.moneymgtapp.domain.repository

import com.pratatec.moneymgtapp.domain.model.Categoria
import com.pratatec.moneymgtapp.domain.model.GastoDiario
import com.pratatec.moneymgtapp.domain.model.Periodo
import com.pratatec.moneymgtapp.domain.model.Resumo

interface FinanceRepository {

    suspend fun getCategorias(): Result<List<Categoria>>
    suspend fun createCategoria(nome: String): Result<Categoria>
    suspend fun deleteCategoria(id: Int): Result<Unit>

    suspend fun getPeriodos(): Result<List<Periodo>>
    suspend fun createPeriodo(mes: Int, ano: Int, saldoCarteira: Double, saldoDisponivelMes: Double): Result<Periodo>
    suspend fun updatePeriodo(periodoId: Int, saldoCarteira: Double? = null, saldoDisponivelMes: Double? = null): Result<Periodo>
    suspend fun getResumo(periodoId: Int): Result<Resumo>

    suspend fun getGastosDiarios(periodoId: Int): Result<List<GastoDiario>>
    suspend fun createGasto(periodoId: Int, data: String, valor: Double, categoriaId: Int, descricao: String): Result<GastoDiario>
    suspend fun updateGasto(periodoId: Int, gastoId: Int, valor: Double? = null, categoriaId: Int? = null, descricao: String? = null): Result<GastoDiario>
    suspend fun deleteGasto(periodoId: Int, gastoId: Int): Result<Unit>
}
