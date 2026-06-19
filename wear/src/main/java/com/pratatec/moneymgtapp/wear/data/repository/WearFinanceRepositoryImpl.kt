package com.pratatec.moneymgtapp.wear.data.repository

import com.pratatec.moneymgtapp.wear.data.remote.api.WearFinanceApi
import com.pratatec.moneymgtapp.wear.data.remote.dto.GastoDiarioRequest
import com.pratatec.moneymgtapp.wear.domain.model.Categoria
import com.pratatec.moneymgtapp.wear.domain.model.Periodo
import com.pratatec.moneymgtapp.wear.domain.model.Resumo
import com.pratatec.moneymgtapp.wear.domain.repository.WearFinanceRepository
import java.util.Locale

class WearFinanceRepositoryImpl(private val api: WearFinanceApi) : WearFinanceRepository {

    override suspend fun getPeriodos(): Result<List<Periodo>> =
        api.getPeriodos().map { list ->
            list.map { Periodo(id = it.id, mes = it.mes, ano = it.ano) }
        }

    override suspend fun getResumo(periodoId: Int): Result<Resumo> =
        api.getResumo(periodoId).map {
            Resumo(
                saldoCarteira = it.saldo_carteira,
                saldoDisponivelMes = it.saldo_disponivel_mes,
                totalGastoMes = it.total_gasto_mes,
                limiteHoje = it.limite_hoje,
            )
        }

    override suspend fun getCategorias(): Result<List<Categoria>> =
        api.getCategorias().map { list ->
            list.map { Categoria(id = it.id, nome = it.nome, tipo = it.tipo) }
        }

    override suspend fun createGasto(
        periodoId: Int,
        data: String,
        valor: Double,
        categoriaId: Int,
        descricao: String,
    ): Result<Unit> = api.createGasto(
        periodoId,
        GastoDiarioRequest(
            data = data,
            valor = String.format(Locale.US, "%.2f", valor),
            categoria_id = categoriaId,
            descricao = descricao,
        )
    )
}
