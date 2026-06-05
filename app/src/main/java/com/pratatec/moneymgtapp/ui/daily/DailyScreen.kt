package com.pratatec.moneymgtapp.ui.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratatec.moneymgtapp.domain.model.GastoDiario
import com.pratatec.moneymgtapp.domain.model.GastoDoDia
import com.pratatec.moneymgtapp.ui.shared.AddGastoSheet
import com.pratatec.moneymgtapp.ui.shared.categoriaColor
import com.pratatec.moneymgtapp.ui.shared.categoriaIcon
import com.pratatec.moneymgtapp.ui.shared.diaDaSemana
import com.pratatec.moneymgtapp.ui.shared.diasNoMes
import com.pratatec.moneymgtapp.ui.shared.nomeMes
import com.pratatec.moneymgtapp.ui.shared.nomeMesAbrev
import com.pratatec.moneymgtapp.ui.shared.toBRL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    viewModel: DailyViewModel,
    mes: Int,
    ano: Int,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState
    val daySheet = viewModel.daySheet
    val addSheet = viewModel.addSheet

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                title = {
                    Text(
                        text = "CONTROLE DIÁRIO",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                },
                actions = { Spacer(Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Month header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${nomeMes(mes)} $ano",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // Summary row
            state.resumo?.let { resumo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("GASTO NO MÊS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(resumo.totalGastoMes.toBRL(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("DISPONÍVEL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(resumo.saldoDisponivelMes.toBRL(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = viewModel::load) { Text("Tentar novamente") }
                    }
                }
            } else {
                val numDias = diasNoMes(mes, ano)
                LazyColumn {
                    items(numDias) { i ->
                        val dia = i + 1
                        val dateStr = "%04d-%02d-%02d".format(ano, mes, dia)
                        val gastoDoDia = state.gastosDoMes.find { it.data == dateStr }
                        DayRow(
                            dia = dia,
                            diaDaSemana = diaDaSemana(ano, mes, dia),
                            gastoDoDia = gastoDoDia,
                            onClick = { viewModel.openDaySheet(dateStr) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    if (daySheet.visible && daySheet.gastoDoDia != null) {
        DayDetailSheet(
            gastoDoDia = daySheet.gastoDoDia,
            onDismiss = viewModel::closeDaySheet,
            onAddGasto = { viewModel.openAddGasto(daySheet.gastoDoDia.data) },
            onDeleteGasto = viewModel::deleteGasto,
        )
    }

    if (addSheet.visible) {
        AddGastoSheet(
            state = addSheet,
            categorias = state.categorias,
            onDismiss = viewModel::closeAddGasto,
            onUpdateValor = viewModel::updateValor,
            onSelectCategoria = viewModel::selectCategoria,
            onUpdateDescricao = viewModel::updateDescricao,
            onSave = viewModel::saveGasto,
        )
    }
}
// ── Suppressed: composables and helpers moved to ui/shared/ ─────────────────

@Composable
private fun DayRow(
    dia: Int,
    diaDaSemana: String,
    gastoDoDia: GastoDoDia?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Dia + dia da semana
        Column(modifier = Modifier.width(36.dp)) {
            Text(
                text = dia.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = diaDaSemana,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(12.dp))

        // Category chips
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (gastoDoDia != null && gastoDoDia.gastos.isNotEmpty()) {
                val visible = gastoDoDia.gastos.take(2)
                val extra = gastoDoDia.gastos.size - visible.size
                visible.forEach { gasto ->
                    GastoChip(gasto = gasto)
                }
                if (extra > 0) {
                    Text(
                        text = "+$extra",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Total do dia
        if (gastoDoDia != null && gastoDoDia.totalDia > 0) {
            Text(
                text = gastoDoDia.totalDia.toBRL(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun GastoChip(gasto: GastoDiario) {
    val color = categoriaColor(gasto.categoria.id)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = categoriaIcon(gasto.categoria.nome),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(10.dp),
        )
        Text(
            text = "%.2f".format(gasto.valor).replace(".", ","),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 10.sp,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailSheet(
    gastoDoDia: GastoDoDia,
    onDismiss: () -> Unit,
    onAddGasto: () -> Unit,
    onDeleteGasto: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val parts = gastoDoDia.data.split("-")
    val ano = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val mes = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val dia = parts.getOrNull(2)?.toIntOrNull() ?: 0

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "${diaDaSemana(ano, mes, dia).uppercase()} $dia ${nomeMesAbrev(mes).uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Gastos do dia $dia",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (gastoDoDia.totalDia > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = gastoDoDia.totalDia.toBRL(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))

            if (gastoDoDia.gastos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Nenhum gasto neste dia.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                gastoDoDia.gastos.forEach { gasto ->
                    GastoRowItem(gasto = gasto, onDelete = { onDeleteGasto(gasto.id) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onAddGasto,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                Text("Adicionar gasto", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GastoRowItem(gasto: GastoDiario, onDelete: () -> Unit) {
    val color = categoriaColor(gasto.categoria.id)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = categoriaIcon(gasto.categoria.nome),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = gasto.descricao.ifBlank { gasto.categoria.nome },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = gasto.categoria.nome,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = gasto.valor.toBRL(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}


