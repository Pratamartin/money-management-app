package com.pratatec.moneymgtapp.data.repository

import com.pratatec.moneymgtapp.data.remote.api.FinanceApi
import com.pratatec.moneymgtapp.data.remote.dto.GastoDiarioRequest
import java.util.Locale
import com.pratatec.moneymgtapp.data.remote.dto.GastoDiarioResponse
import com.pratatec.moneymgtapp.data.remote.dto.PeriodoRequest
import com.pratatec.moneymgtapp.data.remote.dto.CategoriaResponse
import com.pratatec.moneymgtapp.data.remote.dto.PeriodoResponse
import com.pratatec.moneymgtapp.data.remote.dto.ResumoResponse
import com.pratatec.moneymgtapp.domain.model.Categoria
import com.pratatec.moneymgtapp.domain.model.GastoDiario
import com.pratatec.moneymgtapp.domain.model.Periodo
import com.pratatec.moneymgtapp.domain.model.Resumo
import com.pratatec.moneymgtapp.domain.repository.FinanceRepository

class FinanceRepositoryImpl(private val api: FinanceApi) : FinanceRepository {

    override suspend fun getCategorias() =
        api.getCategorias().map { list -> list.map { it.toDomain() } }

    override suspend fun createCategoria(nome: String) =
        api.createCategoria(nome).map { it.toDomain() }

    override suspend fun deleteCategoria(id: Int) =
        api.deleteCategoria(id)

    override suspend fun getPeriodos() =
        api.getPeriodos().map { list -> list.map { it.toDomain() } }

    override suspend fun createPeriodo(mes: Int, ano: Int, saldoCarteira: Double, saldoDisponivelMes: Double) =
        api.createPeriodo(
            PeriodoRequest(
                mes = mes,
                ano = ano,
                saldo_carteira = saldoCarteira.toBigDecimal().toPlainString(),
                saldo_disponivel_mes = saldoDisponivelMes.toBigDecimal().toPlainString(),
            )
        ).map { it.toDomain() }

    override suspend fun updatePeriodo(periodoId: Int, saldoCarteira: Double?, saldoDisponivelMes: Double?) =
        api.updatePeriodo(
            periodoId = periodoId,
            saldoCarteira = saldoCarteira?.let { String.format(Locale.US, "%.2f", it) },
            saldoDisponivelMes = saldoDisponivelMes?.let { String.format(Locale.US, "%.2f", it) },
        ).map { it.toDomain() }

    override suspend fun getResumo(periodoId: Int) =
        api.getResumo(periodoId).map { it.toDomain() }

    override suspend fun getGastosDiarios(periodoId: Int) =
        api.getGastosDiarios(periodoId).map { list -> list.map { it.toDomain() } }

    override suspend fun createGasto(periodoId: Int, data: String, valor: Double, categoriaId: Int, descricao: String) =
        api.createGasto(
            periodoId,
            GastoDiarioRequest(
                data = data,
                valor = String.format(Locale.US, "%.2f", valor),
                categoria_id = categoriaId,
                descricao = descricao,
            )
        ).map { it.toDomain() }

    override suspend fun updateGasto(periodoId: Int, gastoId: Int, valor: Double?, categoriaId: Int?, descricao: String?) =
        api.updateGasto(
            periodoId = periodoId,
            gastoId = gastoId,
            valor = valor?.let { String.format(Locale.US, "%.2f", it) },
            categoriaId = categoriaId,
            descricao = descricao,
        ).map { it.toDomain() }

    override suspend fun deleteGasto(periodoId: Int, gastoId: Int) =
        api.deleteGasto(periodoId, gastoId)
}

private fun CategoriaResponse.toDomain() = Categoria(id = id, nome = nome, tipo = tipo)

private fun PeriodoResponse.toDomain() = Periodo(
    id = id,
    mes = mes,
    ano = ano,
    saldoCarteira = saldo_carteira.toDouble(),
    saldoDisponivelMes = saldo_disponivel_mes.toDouble(),
)

private fun ResumoResponse.toDomain() = Resumo(
    saldoCarteira = saldo_carteira,
    saldoDisponivelMes = saldo_disponivel_mes,
    totalGastoMes = total_gasto_mes,
    limiteHoje = limite_hoje,
)

private fun GastoDiarioResponse.toDomain() = GastoDiario(
    id = id,
    data = data,
    valor = valor.toDouble(),
    descricao = descricao,
    categoria = categoria.toDomain(),
)
