package com.pratatec.moneymgtapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratatec.moneymgtapp.ui.shared.AddGastoSheet
import com.pratatec.moneymgtapp.ui.shared.diaDaSemana
import com.pratatec.moneymgtapp.ui.shared.nomeMes
import com.pratatec.moneymgtapp.ui.shared.nomeMesAbrev
import com.pratatec.moneymgtapp.ui.shared.toBRL
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCategories: () -> Unit,
) {
    val state = viewModel.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (state.isLoading && state.resumo == null && !state.semPeriodo) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp),
            ) {
                HomeTopRow(
                    mes = state.mes,
                    ano = state.ano,
                    onPrev = { viewModel.navigateMes(-1) },
                    onNext = { viewModel.navigateMes(1) },
                    onMenu = onNavigateToCategories,
                )

                Spacer(Modifier.height(8.dp))

                if (state.error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = viewModel::load) {
                                Text("Tentar novamente", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                } else if (state.semPeriodo) {
                    SemPeriodoCard(
                        mes = state.mes,
                        ano = state.ano,
                        onCreate = viewModel::openCreatePeriodo,
                    )
                } else {
                    val resumo = state.resumo
                    LimiteHojeCard(
                        limiteHoje = resumo?.limiteHoje ?: 0.0,
                        gastoHoje = state.gastoHoje,
                        mes = state.mes,
                        ano = state.ano,
                    )
                    Spacer(Modifier.height(12.dp))
                    SaldoCarteiraCard(saldoCarteira = resumo?.saldoCarteira ?: 0.0)
                    Spacer(Modifier.height(12.dp))
                    ResumoMesCard(
                        orcamentoTotal = resumo?.saldoDisponivelMes ?: 0.0,
                        disponivel = (resumo?.saldoDisponivelMes ?: 0.0) - (resumo?.totalGastoMes ?: 0.0),
                        gasto = resumo?.totalGastoMes ?: 0.0,
                    )
                }
            }

            if (!state.semPeriodo && state.resumo != null) {
                Button(
                    onClick = viewModel::openAddGasto,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(26.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Registrar gasto",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }

    if (viewModel.createSheet.visible) {
        CreatePeriodoSheet(
            state = viewModel.createSheet,
            mes = state.mes,
            ano = state.ano,
            onDismiss = viewModel::closeCreatePeriodo,
            onUpdateSaldoCarteira = viewModel::updateSaldoCarteira,
            onUpdateSaldoDisponivel = viewModel::updateSaldoDisponivel,
            onSave = viewModel::saveCreatePeriodo,
        )
    }

    if (viewModel.addGastoSheet.visible) {
        AddGastoSheet(
            state = viewModel.addGastoSheet,
            categorias = state.categorias,
            onDismiss = viewModel::closeAddGasto,
            onUpdateValor = viewModel::updateValor,
            onSelectCategoria = viewModel::selectCategoria,
            onUpdateDescricao = viewModel::updateDescricao,
            onSave = viewModel::saveGasto,
        )
    }
}

// ── Top row ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopRow(
    mes: Int,
    ano: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "MÊS ATUAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrev, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Mês anterior",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${nomeMes(mes)} $ano",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Próximo mês",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onMenu, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Menu, contentDescription = "Categorias", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── Cards ────────────────────────────────────────────────────────────────────

@Composable
private fun LimiteHojeCard(
    limiteHoje: Double,
    gastoHoje: Double,
    mes: Int,
    ano: Int,
) {
    val cal = Calendar.getInstance()
    val diaHoje = cal.get(Calendar.DAY_OF_MONTH)
    val excedido = limiteHoje < 0
    val limiteOriginal = limiteHoje + gastoHoje
    val percentual = if (limiteOriginal > 0) (gastoHoje / limiteOriginal).toFloat().coerceIn(0f, 1f) else 0f
    val percentualPct = (percentual * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (excedido) "LIMITE EXCEDIDO" else "LIMITE DE HOJE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (excedido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (excedido) FontWeight.SemiBold else FontWeight.Normal,
                )
                Text(
                    text = "${diaDaSemana(ano, mes, diaHoje)}, $diaHoje ${nomeMesAbrev(mes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = limiteHoje.toBRL(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (excedido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "de ${limiteOriginal.toBRL()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outline),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentual)
                        .fillMaxHeight()
                        .background(if (excedido || percentualPct > 85) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (excedido) {
                    Text(
                        text = "Excedeu em ${(-limiteHoje).toBRL()} hoje",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                    )
                } else {
                    Text(
                        text = "Você gastou ${gastoHoje.toBRL()} hoje",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "$percentualPct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (excedido || percentualPct > 85) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SaldoCarteiraCard(saldoCarteira: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SALDO NA CARTEIRA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = saldoCarteira.toBRL(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ResumoMesCard(
    orcamentoTotal: Double,
    disponivel: Double,
    gasto: Double,
) {
    val percentualUsado = if (orcamentoTotal > 0) (gasto / orcamentoTotal).toFloat().coerceIn(0f, 1f) else 0f
    val percentualUsadoPct = (percentualUsado * 100).toInt()
    val percentualRestante = 100 - percentualUsadoPct

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "RESUMO DO MÊS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${orcamentoTotal.toBRL()} no total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Disponível",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = disponivel.toBRL(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Gasto",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = gasto.toBRL(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outline),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentualUsado)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary),
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "$percentualUsadoPct% usado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$percentualRestante% restante",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SemPeriodoCard(mes: Int, ano: Int, onCreate: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Nenhum período para ${nomeMes(mes)} $ano",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Configure seu orçamento mensal para começar a registrar gastos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onCreate,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Criar período", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Sheet: Criar período ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePeriodoSheet(
    state: CreatePeriodoSheetState,
    mes: Int,
    ano: Int,
    onDismiss: () -> Unit,
    onUpdateSaldoCarteira: (String) -> Unit,
    onUpdateSaldoDisponivel: (String) -> Unit,
    onSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Novo período",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${nomeMes(mes)} $ano",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "SALDO DA CARTEIRA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = state.saldoCarteira,
                onValueChange = onUpdateSaldoCarteira,
                prefix = { Text("R$", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "ORÇAMENTO DISPONÍVEL NO MÊS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = state.saldoDisponivelMes,
                onValueChange = onUpdateSaldoDisponivel,
                prefix = { Text("R$", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.error != null) {
                Spacer(Modifier.height(4.dp))
                Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onSave,
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "Criar período",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
