package com.pratatec.moneymgtapp.wear.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAddGasto: () -> Unit,
    onRefresh: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.notAuthenticated -> NotAuthenticatedContent()
            uiState.isLoading -> CircularProgressIndicator()
            uiState.semPeriodo -> SemPeriodoContent()
            uiState.error != null -> ErrorContent(uiState.error, onRefresh)
            uiState.resumo != null -> ResumoContent(uiState, onAddGasto)
        }
    }
}

@Composable
private fun ResumoContent(uiState: HomeUiState, onAddGasto: () -> Unit) {
    val resumo = uiState.resumo!!
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Text(
                text = "Disponível hoje",
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            )
        }
        item {
            Text(
                text = "R$ %.2f".format(resumo.limiteHoje),
                style = MaterialTheme.typography.title1,
                color = if (resumo.limiteHoje >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
            )
        }
        item {
            Text(
                text = "Gasto no mês: R$ %.2f".format(resumo.totalGastoMes),
                style = MaterialTheme.typography.caption1,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        item {
            Button(
                onClick = onAddGasto,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(top = 8.dp),
            ) {
                Text("+ Gasto")
            }
        }
    }
}

@Composable
private fun NotAuthenticatedContent() {
    Text(
        text = "Abra o Money Flow no celular para entrar",
        style = MaterialTheme.typography.body2,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun SemPeriodoContent() {
    Text(
        text = "Nenhum período para este mês.\nCrie um no celular.",
        style = MaterialTheme.typography.body2,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = message,
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
        item {
            Button(onClick = onRetry) { Text("Tentar novamente") }
        }
    }
}
