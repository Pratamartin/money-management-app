package com.pratatec.moneymgtapp.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pratatec.moneymgtapp.domain.model.Categoria
import com.pratatec.moneymgtapp.ui.shared.categoriaColor
import com.pratatec.moneymgtapp.ui.shared.categoriaIcon

private val iconOptions: List<Pair<String, ImageVector>> = listOf(
    "Fastfood" to Icons.Default.Fastfood,
    "Restaurant" to Icons.Default.Restaurant,
    "LocalCafe" to Icons.Default.LocalCafe,
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "LocalPharmacy" to Icons.Default.LocalPharmacy,
    "FitnessCenter" to Icons.Default.FitnessCenter,
    "DirectionsTransit" to Icons.Default.DirectionsTransit,
    "SportsEsports" to Icons.Default.SportsEsports,
    "Movie" to Icons.Default.Movie,
    "MusicNote" to Icons.Default.MusicNote,
    "School" to Icons.Default.School,
    "Work" to Icons.Default.Work,
    "Home" to Icons.Default.Home,
    "Pets" to Icons.Default.Pets,
    "LocalGasStation" to Icons.Default.LocalGasStation,
    "FlightTakeoff" to Icons.Default.FlightTakeoff,
    "CardGiftcard" to Icons.Default.CardGiftcard,
    "Build" to Icons.Default.Build,
    "Star" to Icons.Default.Star,
    "DirectionsCar" to Icons.Default.DirectionsCar,
    "Sports" to Icons.Default.Sports,
    "Category" to Icons.Default.Category,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState
    val createSheet = viewModel.createSheet

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
                        text = "Categorias",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = viewModel::openCreateSheet,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nova categoria", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = viewModel::load) { Text("Tentar novamente") }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    // Seção: Padrão
                    item {
                        SectionHeader("PADRÃO")
                    }
                    items(state.defaultCategories) { categoria ->
                        CategoryRow(
                            categoria = categoria,
                            isDefault = true,
                            onDelete = null,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.5.dp)
                    }

                    // Seção: Suas categorias
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader("SUAS CATEGORIAS")
                    }

                    if (state.customCategories.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "Nenhuma categoria personalizada.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    } else {
                        items(state.customCategories) { categoria ->
                            CategoryRow(
                                categoria = categoria,
                                isDefault = false,
                                onDelete = { viewModel.deleteCategoria(categoria.id) },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }

    if (createSheet.visible) {
        CreateCategorySheet(
            state = createSheet,
            onDismiss = viewModel::closeCreateSheet,
            onUpdateNome = viewModel::updateNome,
            onSelectIcon = viewModel::selectIcon,
            onSave = viewModel::saveCategoria,
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun CategoryRow(
    categoria: Categoria,
    isDefault: Boolean,
    onDelete: (() -> Unit)?,
) {
    val color = categoriaColor(categoria.id)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = categoriaIcon(categoria.nome),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = categoria.nome,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        // Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDefault) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = if (isDefault) "PADRÃO" else "CUSTOM",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDefault) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
            )
        }

        if (!isDefault && onDelete != null) {
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCategorySheet(
    state: CreateCategorySheetState,
    onDismiss: () -> Unit,
    onUpdateNome: (String) -> Unit,
    onSelectIcon: (String) -> Unit,
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Criar categoria personalizada",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Nome
            Text("NOME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = state.nome,
                onValueChange = onUpdateNome,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            // Ícone
            Text("ÍCONE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            iconOptions.chunked(5).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { (name, icon) ->
                        val selected = state.selectedIconName == name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                .then(
                                    if (selected) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .clickable { onSelectIcon(name) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    // Preenche colunas vazias na última linha
                    repeat(5 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                )
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Botões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Salvar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
