package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(viewModel: CityViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedCategory by remember { mutableStateOf("Zonas") } // Zonas, Servicios, Espacios, Opciones
    
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showInfoDialog by remember { mutableStateOf(false) }

    // Dynamic color gradient background based on Time of Day
    // time: 0.0 (Dawn/Morning) -> 0.25 (Midday) -> 0.65 (Sunset) -> 0.8 (Night)
    val dayProgress = uiState.dayTimeProgress
    val skyBrush = remember(dayProgress) {
        val color1: Color
        val color2: Color
        when {
            dayProgress < 0.2f -> { // Dawn transition
                val ratio = dayProgress / 0.2f
                color1 = lerpColor(Color(0xFF0F1E36), Color(0xFFFF9E79), ratio)
                color2 = lerpColor(Color(0xFF233A5F), Color(0xFF70A1FF), ratio)
            }
            dayProgress < 0.55f -> { // Sunny Day
                val ratio = (dayProgress - 0.2f) / 0.35f
                color1 = lerpColor(Color(0xFFFF9E79), Color(0xFF4AC2E2), ratio)
                color2 = lerpColor(Color(0xFF70A1FF), Color(0xFF87CEEB), ratio)
            }
            dayProgress < 0.75f -> { // Crimson Sunset
                val ratio = (dayProgress - 0.55f) / 0.20f
                color1 = lerpColor(Color(0xFF4AC2E2), Color(0xFFE65C00), ratio)
                color2 = lerpColor(Color(0xFF87CEEB), Color(0xFF3B1C63), ratio)
            }
            else -> { // Moonlight starry night
                val ratio = (dayProgress - 0.75f) / 0.25f
                color1 = lerpColor(Color(0xFFE65C00), Color(0xFF060D1A), ratio)
                color2 = lerpColor(Color(0xFF3B1C63), Color(0xFF111927), ratio)
            }
        }
        Brush.verticalGradient(listOf(color1, color2))
    }

    // Interactive time of day label
    val dayTimeLabel = when {
        dayProgress < 0.2f -> "Amanecer 🌅"
        dayProgress < 0.55f -> "Día Soleado ☀️"
        dayProgress < 0.75f -> "Atardecer 🌇"
        else -> "Noche Estrellada 🌙"
    }

    val isNightMode = dayProgress >= 0.75f || dayProgress < 0.1f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text("Constructor de Ciudades", fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text("Simulador de Estrategia 3D", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Manual de Juego")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Dynamic Sky and Day-Cycle Background Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .drawBehind {
                        drawRect(skyBrush)
                        // Starry night sparkles
                        if (isNightMode) {
                            drawStars(this)
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "AÑO LUZ • DÍA ${uiState.day}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayTimeLabel,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Global stats pills
                    Row {
                        FloatingStatTicker(
                            icon = Icons.Default.Star, 
                            label = "Lvl ${uiState.level}", 
                            color = Color(0xFFFFCA28)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FloatingStatTicker(
                            icon = Icons.Default.Favorite, 
                            label = "${uiState.happiness}%", 
                            color = if (uiState.happiness >= 75) Color(0xFF4CAF50) else if (uiState.happiness >= 45) Color(0xFFFFB300) else Color(0xFFF44336)
                        )
                    }
                }
            }

            // Stats row (Money, Population, Happiness metrics)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatMetricColumn(
                        icon = Icons.Default.PlayArrow,
                        title = "Tesorería",
                        value = "$${uiState.money}",
                        trend = if (uiState.happiness >= 60) "+${(uiState.population * (uiState.happiness / 100f) * 1.5f).toInt()} tazas" else "Baja",
                        goodTrend = uiState.happiness >= 60
                    )
                    VerticalDivider(modifier = Modifier.height(35.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    StatMetricColumn(
                        icon = Icons.Default.Face,
                        title = "Ciudadanos",
                        value = "${uiState.population} habs",
                        trend = "Límite: ${uiState.level * 40}",
                        goodTrend = uiState.population < uiState.level * 40
                    )
                    VerticalDivider(modifier = Modifier.height(35.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    StatMetricColumn(
                        icon = Icons.Default.Warning,
                        title = "Redes (E/A)",
                        value = "⚡${uiState.power} 💧${uiState.water}",
                        trend = if (uiState.power < 0 || uiState.water < 0) "Crítico" else "Estable",
                        goodTrend = uiState.power >= 0 && uiState.water >= 0
                    )
                }
            }

            // Resource Warning Banner
            if (uiState.power < 0 || uiState.water < 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.power < 0 && uiState.water < 0) {
                                "⚠️ Déficit de Luz y Agua: Economía al 40% y felicidad reducida."
                            } else if (uiState.power < 0) {
                                "⚡ Falta Energía Eléctrica: Fábricas y comercios frenados."
                            } else {
                                "💧 Sequía en Acueductos: Residentes insatisfechos abandonando casas."
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // The Map viewport (Scrollable container representing the floats)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            ) {
                // Background cloud assets animation (advanced graphics enabled)
                if (uiState.isGraphicsAdvanced) {
                    CloudSceneryOverlay()
                }

                if (uiState.isIsometricMode) {
                    // Isometric 3D Viewport
                    val scrollStateX = rememberScrollState(220)
                    val scrollStateY = rememberScrollState(50)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollStateX)
                            .verticalScroll(scrollStateY)
                    ) {
                        // Tilted isometric block field (width: 520.dp, height: 420.dp)
                        Box(
                            modifier = Modifier
                                .width(520.dp)
                                .height(400.dp)
                        ) {
                            val gridSize = 6
                            // Draw back-to-front by x+y index so foreground shadows overlay background units perfectly
                            val tileSequence = remember {
                                mutableListOf<Pair<Int, Int>>().apply {
                                    for (diagonalSum in 0..(2 * (gridSize - 1))) {
                                        for (x in 0 until gridSize) {
                                            val y = diagonalSum - x
                                            if (y in 0 until gridSize) {
                                                add(Pair(x, y))
                                            }
                                        }
                                    }
                                }
                            }

                            tileSequence.forEach { (x, y) ->
                                val building = uiState.buildings.find { it.x == x && it.y == y }
                                
                                // Coordinates mapping of isometric projections
                                // centerOffset = 220.dp, spacingX = 42.1f, spacingY = 22.8f
                                val xDp = ((x - y) * 41.5f) + 220f
                                val yDp = ((x + y) * 23.5f) + 40f

                                Box(
                                    modifier = Modifier
                                        .offset(x = xDp.dp, y = yDp.dp)
                                        .size(width = 83.dp, height = 115.dp)
                                        .clickable {
                                            selectedCell = Pair(x, y)
                                        }
                                ) {
                                    Isometric3DTile(
                                        buildingString = building?.type,
                                        dayProgress = dayProgress,
                                        isGraphicsAdvanced = uiState.isGraphicsAdvanced,
                                        isSelected = selectedCell == Pair(x, y)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Modern Standard 2D Schematic matrix Map
                    val gridSize = 6
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridSize),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    ) {
                        items(gridSize * gridSize) { index ->
                            val x = index % gridSize
                            val y = index / gridSize
                            val building = uiState.buildings.find { it.x == x && it.y == y }
                            val buildingType = building?.let {
                                try { BuildingType.valueOf(it.type) } catch (e: Exception) { null }
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (buildingType != null) {
                                            Color(android.graphics.Color.parseColor(buildingType.colorHex))
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .border(
                                        width = if (selectedCell == Pair(x, y)) 3.dp else 1.dp,
                                        color = if (selectedCell == Pair(x, y)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCell = Pair(x, y) }
                            ) {
                                if (buildingType != null) {
                                    Icon(
                                        imageVector = get2DIcon(buildingType),
                                        contentDescription = buildingType.displayName,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        "${x},${y}", 
                                        fontSize = 8.sp, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lower category tabs and construction tools
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // Category Select Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val categories = listOf("Zonas", "Servicios", "Espacios", "Ajustes")
                        categories.forEach { cat ->
                            val isSel = selectedCategory == cat
                            TextButton(
                                onClick = { selectedCategory = cat },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = cat,
                                        fontWeight = if (isSel) FontWeight.Black else FontWeight.Normal,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        fontSize = 13.sp
                                    )
                                    if (isSel) {
                                        Box(
                                            modifier = Modifier
                                                .width(20.dp)
                                                .height(3.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Content panel according to filter
                    if (selectedCategory == "Ajustes") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Modo 3D Isométrico", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = uiState.isIsometricMode,
                                    onCheckedChange = { viewModel.setIsometricMode(it) },
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (uiState.isIsometricMode) Icons.Default.Add else Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Gráficos Avanzados", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = uiState.isGraphicsAdvanced,
                                    onCheckedChange = { viewModel.setGraphicsAdvanced(it) }
                                )
                            }
                        }
                    } else {
                        // Show list of buildable entities
                        val structuresList = when (selectedCategory) {
                            "Zonas" -> listOf(BuildingType.HOUSE, BuildingType.FACTORY, BuildingType.COMMERCE)
                            else -> listOf(BuildingType.ROAD, BuildingType.PARK, BuildingType.POWER_PLANT, BuildingType.ECOLOGIC_WATER)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            structuresList.forEach { struct ->
                                val unlocked = uiState.level >= struct.minLevelRequired
                                Card(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .clickable {
                                            if (unlocked) {
                                                // prompt placement instruction tips
                                                scope.launch {
                                                    val defaultUnbuilt = findFirstUnoccupied(uiState.buildings)
                                                    if (defaultUnbuilt != null) {
                                                        viewModel.build(struct, defaultUnbuilt.first, defaultUnbuilt.second)
                                                    } else {
                                                        // Fallback is selecting a random spot, otherwise click tile then build!
                                                        selectedCell = Pair(0, 0)
                                                    }
                                                }
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (unlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) 
                                                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(1.dp, if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                struct.displayName, 
                                                fontSize = 11.sp, 
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                color = if (unlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(Color(android.graphics.Color.parseColor(struct.colorHex)), CircleShape)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(struct.description, fontSize = 9.sp, lineHeight = 11.sp, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("S${struct.cost}", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                            if (!unlocked) {
                                                Text("Lvl ${struct.minLevelRequired} 🔒", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                            } else {
                                                Text("Construir", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sheet Menu when cell click
            if (selectedCell != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedCell = null },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ) {
                    val tileX = selectedCell!!.first
                    val tileY = selectedCell!!.second
                    val activeB = uiState.buildings.find { it.x == tileX && it.y == tileY }
                    val activeBType = activeB?.let {
                        try { BuildingType.valueOf(it.type) } catch (e: Exception) { null }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp, top = 8.dp)
                    ) {
                        Text(
                            text = "Propuesta Parcela ($tileX, $tileY)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (activeBType != null) "Ocupada por: ${activeBType.displayName}" else "Estado: Parcela vacía (Hierba)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text("Selecciona una estructura para construir:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid options inside sheet
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Rows of structures
                            BuildingType.entries.chunked(3).forEach { group ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    group.forEach { type ->
                                        val affordable = uiState.money >= type.cost
                                        val unlocked = uiState.level >= type.minLevelRequired
                                        val isEquipped = activeBType == type

                                        Button(
                                            onClick = {
                                                viewModel.build(type, tileX, tileY)
                                                selectedCell = null
                                            },
                                            enabled = affordable && unlocked && !isEquipped,
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(android.graphics.Color.parseColor(type.colorHex)),
                                                disabledContainerColor = Color(android.graphics.Color.parseColor(type.colorHex)).copy(alpha = 0.25f)
                                            ),
                                            contentPadding = PaddingValues(2.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    type.displayName, 
                                                    style = MaterialTheme.typography.labelSmall, 
                                                    maxLines = 1,
                                                    color = if (affordable && unlocked) Color.White else Color.White.copy(alpha = 0.5f)
                                                )
                                                Text("-$${type.cost}", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                                            }
                                        }
                                    }
                                    if (group.size < 3) {
                                        Spacer(modifier = Modifier.weight((3 - group.size).toFloat()))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.demolish(tileX, tileY)
                                    selectedCell = null
                                },
                                enabled = activeB != null,
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Demoler Edificio")
                            }

                            Button(
                                onClick = { selectedCell = null },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
                            ) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
            }

            // Info guide dialog
            if (showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    title = { Text("Manual de Estrategia 🏢", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                "¡Bienvenido a Constructor de Ciudades! Diseña tu metrópolis flotante con estas reglas estratégicas:\n",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RuleBullet("1. Viviendas y Servicios Balanceados:", "Para crecer necesitas residencias (Casa), pero éstas consumen electricidad (Térmica Solar) e inmensas cantidades de Agua (Acueducto). Mantén valores positivos para atraer gente.")
                            RuleBullet("2. Fábrica vs Parques:", "Las Fábricas dan altísima recolección tributaria diaria (+$40 monedas c/u!) pero contaminan mucho y bajan la Felicidad de golpe. Compensa colocando Parques Sostenibles para limpiar el aire y subir el ánimo.")
                            RuleBullet("3. Comercio local:", "Las zonas comerciales producen ganancias y son el servicio necesario que eleva la felicidad general.")
                            RuleBullet("4. Avanza Días y Niveles:", "A medida que tu población aumenta, desbloqueas nuevos niveles con estructuras indispensables: Nivel 2 (Fábricas/Comercios), Nivel 3 (Térmica Solar/ParqueS), Nivel 4 (Acueductos).")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Consejo: ¡Toca cualquier casilla para construir o demoler parcelas!", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showInfoDialog = false }) {
                            Text("¡Entendido!")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RuleBullet(boldText: String, normalText: String) {
    Row(modifier = Modifier.padding(bottom = 6.dp)) {
        Text("• ", fontWeight = FontWeight.Bold)
        Column {
            Text(boldText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(normalText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FloatingStatTicker(icon: ImageVector, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun StatMetricColumn(
    icon: ImageVector,
    title: String,
    value: String,
    trend: String,
    goodTrend: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Text(trend, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = if (goodTrend) Color(0xFF388E3C) else Color(0xFFD32F2F))
    }
}

@Composable
fun Isometric3DTile(
    buildingString: String?,
    dayProgress: Float,
    isGraphicsAdvanced: Boolean,
    isSelected: Boolean
) {
    // Construction Animation factor (0f rising to 1f)
    var key by remember(buildingString) { mutableStateOf(0) }
    val constructionProgress = remember(buildingString) { Animatable(0f) }
    
    LaunchedEffect(buildingString) {
        constructionProgress.snapTo(0f)
        constructionProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
        )
    }

    val angleOffsetValue = if (isGraphicsAdvanced) {
        val infiniteTransition = rememberInfiniteTransition(label = "shadowLift")
        val valOffset by infiniteTransition.animateFloat(
            initialValue = -1.2f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "shadowAngle"
        )
        valOffset
    } else 0f

    // Night/Windows/Glow triggers
    val isNightGlow = dayProgress >= 0.7f || dayProgress < 0.15f
    val illuminationCoefficient = when {
        dayProgress < 0.2f -> 0.6f + (dayProgress / 0.2f) * 0.4f // dawning
        dayProgress < 0.55f -> 1.0f // brightly highlighted
        dayProgress < 0.75f -> 1.0f - ((dayProgress - 0.55f) / 0.2f) * 0.5f // sunset dim
        else -> 0.45f // night shade ambient
    }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Base of coordinate system inside box
        val gY = h * 0.65f
        val gt = h * 0.08f // earth slab thickness
        val rw = w * 0.95f // rhombus width
        val rh = h * 0.28f // rhombus height

        // 1. Draw selections / Tile Highlight glow
        if (isSelected) {
            val hPath = Path().apply {
                moveTo(w / 2, gY - 4)
                lineTo(w / 2 + rw / 2 + 4, gY + rh / 2)
                lineTo(w / 2, gY + rh + 4)
                lineTo(w / 2 - rw / 2 - 4, gY + rh / 2)
                close()
            }
            drawPath(hPath, color = Color(0xFF64B5F6).copy(alpha = 0.35f))
        }

        // 2. Draw Soil/Earth 3D side foundations (Left side facet + Right side facet)
        val leftSoilPath = Path().apply {
            moveTo(w / 2 - rw / 2, gY + rh / 2)
            lineTo(w / 2, gY + rh)
            lineTo(w / 2, gY + rh + gt)
            lineTo(w / 2 - rw / 2, gY + rh / 2 + gt)
            close()
        }
        val rightSoilPath = Path().apply {
            moveTo(w / 2, gY + rh)
            lineTo(w / 2 + rw / 2, gY + rh / 2)
            lineTo(w / 2 + rw / 2, gY + rh / 2 + gt)
            lineTo(w / 2, gY + rh + gt)
            close()
        }

        // Deep rich chocolate shading for concrete/soil 3D depth
        drawPath(leftSoilPath, color = Color(0xFF5D4037))
        drawPath(rightSoilPath, color = Color(0xFF4E342E))

        // 3. Draw Top Ground Rhombus face
        val topRhombusPath = Path().apply {
            moveTo(w / 2, gY)
            lineTo(w / 2 + rw / 2, gY + rh / 2)
            lineTo(w / 2, gY + rh)
            lineTo(w / 2 - rw / 2, gY + rh / 2)
            close()
        }

        // Grass green or road asphalt gray base
        val surfaceColor = if (buildingString == "ROAD") {
            Color(0xFF2E3440)
        } else if (buildingString == "FACTORY") {
            Color(0xFF4C566A)
        } else {
            // green meadow layout
            Color(0xFF48BB78)
        }
        drawPath(topRhombusPath, color = surfaceColor.copy(alpha = illuminationCoefficient))

        // Road separator drawing
        if (buildingString == "ROAD") {
            drawLine(
                color = Color(0xFFD8DEE9),
                start = Offset(w / 2 - rw / 4, gY + rh / 2 - rh / 8),
                end = Offset(w / 2 + rw / 4, gY + rh / 2 + rh / 8),
                strokeWidth = 2f
            )
        }

        // 4. Draw Rising 3D Building structures
        if (buildingString != null && buildingString != "ROAD") {
            val constProgress = constructionProgress.value
            
            // Standard bounding boxes for structures
            val buildType = try { BuildingType.valueOf(buildingString) } catch (e: Exception) { null }
            if (buildType != null) {
                // Dimensions based on building type
                val wallHeight = when (buildType) {
                    BuildingType.HOUSE -> 32f * constProgress
                    BuildingType.FACTORY -> 44f * constProgress
                    BuildingType.COMMERCE -> 38f * constProgress
                    BuildingType.POWER_PLANT -> 24f * constProgress
                    BuildingType.ECOLOGIC_WATER -> 15f * constProgress
                    BuildingType.PARK -> 8f * constProgress
                    else -> 20f
                }

                val structW = rw * 0.55f
                val structH = rh * 0.55f

                // Base coordinates of structure centered within tile top rhombus
                val sbX = w / 2
                val sbY = gY + rh / 2 + (rh * 0.08f)

                // Vertices
                val bCenter = Offset(sbX, sbY + structH / 2)
                val bLeft = Offset(sbX - structW / 2, sbY)
                val bRight = Offset(sbX + structW / 2, sbY)
                val bTop = Offset(sbX, sbY - structH / 2)

                // Tall extruded coordinates
                val tCenter = Offset(bCenter.x, bCenter.y - wallHeight)
                val tLeft = Offset(bLeft.x, bLeft.y - wallHeight)
                val tRight = Offset(bRight.x, bRight.y - wallHeight)
                val tTop = Offset(bTop.x, bTop.y - wallHeight)

                // Shadow coordinates
                val shadowPath = Path().apply {
                    moveTo(bLeft.x, bLeft.y)
                    lineTo(bCenter.x + (15f + angleOffsetValue), bCenter.y)
                    lineTo(bRight.x + (25f + angleOffsetValue), bRight.y + 12f)
                    lineTo(bRight.x, bRight.y)
                    close()
                }
                drawPath(shadowPath, color = Color.Black.copy(alpha = 0.22f))

                when (buildType) {
                    BuildingType.HOUSE -> {
                        // Drawing cozy residencies
                        val leftWallHouse = Path().apply {
                            moveTo(bLeft.x, bLeft.y)
                            lineTo(bCenter.x, bCenter.y)
                            lineTo(tCenter.x, tCenter.y)
                            lineTo(tLeft.x, tLeft.y)
                            close()
                        }
                        val rightWallHouse = Path().apply {
                            moveTo(bCenter.x, bCenter.y)
                            lineTo(bRight.x, bRight.y)
                            lineTo(tRight.x, tRight.y)
                            lineTo(tCenter.x, tCenter.y)
                            close()
                        }
                        // Left plaster wall yellow/cremish, shaded right
                        drawPath(leftWallHouse, color = Color(0xFFF7F1E3).copy(alpha = illuminationCoefficient))
                        drawPath(rightWallHouse, color = Color(0xFFD1CCC0).copy(alpha = illuminationCoefficient))

                        // Triangular Roof Gable
                        val tApex = Offset(sbX, tTop.y - (16f * constProgress))
                        val leftRoof = Path().apply {
                            moveTo(tLeft.x, tLeft.y)
                            lineTo(tCenter.x, tCenter.y)
                            lineTo(tApex.x, tApex.y)
                            close()
                        }
                        val rightRoof = Path().apply {
                            moveTo(tCenter.x, tCenter.y)
                            lineTo(tRight.x, tRight.y)
                            lineTo(tApex.x, tApex.y)
                            close()
                        }
                        drawPath(leftRoof, color = Color(0xFFFF5252).copy(alpha = illuminationCoefficient))
                        drawPath(rightRoof, color = Color(0xFFFF3838).copy(alpha = illuminationCoefficient))

                        // Windows cozy lighting glows
                        val winColor = if (isNightGlow) Color(0xFFFFCA28) else Color(0xFF2C3E50)
                        drawCircle(color = winColor, radius = 2.2f, center = Offset(tLeft.x + (tCenter.x - tLeft.x) * 0.4f, tLeft.y + 10f))
                        drawCircle(color = winColor, radius = 2.2f, center = Offset(tCenter.x + (tRight.x - tCenter.x) * 0.6f, tCenter.y + 10f))
                    }
                    BuildingType.FACTORY -> {
                        // High industrial metallic block
                        val leftWall = Path().apply {
                            moveTo(bLeft.x, bLeft.y)
                            lineTo(bCenter.x, bCenter.y)
                            lineTo(tCenter.x, tCenter.y)
                            lineTo(tLeft.x, tLeft.y)
                            close()
                        }
                        val rightWall = Path().apply {
                            moveTo(bCenter.x, bCenter.y)
                            lineTo(bRight.x, bRight.y)
                            lineTo(tRight.x, tRight.y)
                            lineTo(tCenter.x, tCenter.y)
                            close()
                        }
                        drawPath(leftWall, color = Color(0xFF90A4AE).copy(alpha = illuminationCoefficient))
                        drawPath(rightWall, color = Color(0xFF78909C).copy(alpha = illuminationCoefficient))

                        // Flat roof panel
                        val roof = Path().apply {
                            moveTo(tLeft.x, tLeft.y)
                            lineTo(tCenter.x, tCenter.y)
                            lineTo(tRight.x, tRight.y)
                            lineTo(tTop.x, tTop.y)
                            close()
                        }
                        drawPath(roof, color = Color(0xFF546E7A).copy(alpha = illuminationCoefficient))

                        // Smoke stacks & dynamic thermal emission clouds (Factory output)
                        val chimneyX = tTop.x
                        val chimneyY = tTop.y
                        drawLine(
                            color = Color(0xFFB0BEC5), 
                            start = Offset(chimneyX, chimneyY), 
                            end = Offset(chimneyX, chimneyY - 22f), 
                            strokeWidth = 4f
                        )
                        drawLine(
                            color = Color(0xFFE53935), 
                            start = Offset(chimneyX, chimneyY - 14f), 
                            end = Offset(chimneyX, chimneyY - 22f), 
                            strokeWidth = 4f
                        )

                        // Sparkles / Smoke puff (Gráficos Avanzados)
                        if (isGraphicsAdvanced) {
                            drawCircle(color = Color.LightGray.copy(alpha = 0.55f), radius = 6f, center = Offset(chimneyX + angleOffsetValue * 1.5f, chimneyY - (30f + angleOffsetValue)))
                            drawCircle(color = Color.LightGray.copy(alpha = 0.35f), radius = 4f, center = Offset(chimneyX + angleOffsetValue * 2f, chimneyY - (38f + angleOffsetValue)))
                        }
                    }
                    BuildingType.COMMERCE -> {
                        // Modern block with reflective cyan visual windows
                        val leftWall = Path().apply {
                            moveTo(bLeft.x, bLeft.y)
                            lineTo(bCenter.x, bCenter.y)
                            lineTo(tCenter.x, tCenter.y)
                            lineTo(tLeft.x, tLeft.y)
                            close()
                        }
                        val rightWall = Path().apply {
                            moveTo(bCenter.x, bCenter.y)
                            lineTo(bRight.x, bRight.y)
                            lineTo(tRight.x, tRight.y)
                            lineTo(tCenter.x, tCenter.y)
                            close()
                        }
                        drawPath(leftWall, color = Color(0xFF26C6DA).copy(alpha = illuminationCoefficient))
                        drawPath(rightWall, color = Color(0xFF00ACC1).copy(alpha = illuminationCoefficient))

                        val roof = Path().apply {
                            moveTo(tLeft.x, tLeft.y)
                            lineTo(tCenter.x, tCenter.y)
                            lineTo(tRight.x, tRight.y)
                            lineTo(tTop.x, tTop.y)
                            close()
                        }
                        drawPath(roof, color = Color(0xFF00838F).copy(alpha = illuminationCoefficient))

                        // Glowing Neon billboard on the left wall
                        val neonBillboard = Path().apply {
                            moveTo(tLeft.x + 4f, tLeft.y + 12f)
                            lineTo(tCenter.x - 4f, tCenter.y + 12f)
                            lineTo(tCenter.x - 4f, tCenter.y + 18f)
                            lineTo(tLeft.x + 4f, tLeft.y + 18f)
                            close()
                        }
                        drawPath(neonBillboard, color = if (isNightGlow) Color(0xFFFF4081) else Color(0xFFAD1457))
                    }
                    BuildingType.POWER_PLANT -> {
                        // High-tech concrete core generator box
                        val generatorBoxLeft = Path().apply {
                            moveTo(bLeft.x + 4f, bLeft.y)
                            lineTo(bCenter.x, bCenter.y)
                            lineTo(tCenter.x, tCenter.y)
                            lineTo(tLeft.x + 4f, tLeft.y)
                            close()
                        }
                        drawPath(generatorBoxLeft, color = Color(0xFFFFA726).copy(alpha = illuminationCoefficient))

                        val generatorBoxRight = Path().apply {
                            moveTo(bCenter.x, bCenter.y)
                            lineTo(bRight.x - 4f, bRight.y)
                            lineTo(tRight.x - 4f, tRight.y)
                            lineTo(tCenter.x, tCenter.y)
                            close()
                        }
                        drawPath(generatorBoxRight, color = Color(0xFFFB8C00).copy(alpha = illuminationCoefficient))

                        // Large tilted photovoltaic blue solar panel matrixes
                        val panelPath = Path().apply {
                            moveTo(tLeft.x - 8f, tLeft.y + 4f)
                            lineTo(tTop.x, tTop.y - 10f)
                            lineTo(tRight.x + 8f, tRight.y + 4f)
                            lineTo(tCenter.x, tCenter.y + 12f)
                            close()
                        }
                        drawPath(panelPath, color = Color(0xFF1565C0).copy(alpha = illuminationCoefficient))

                        // Blinking power core bulb
                        val blinkingGlow = if (isNightGlow) {
                            if (angleOffsetValue > 0) Color(0xFF00E676) else Color(0xFF00701A)
                        } else Color(0xFF00E676)
                        drawCircle(color = blinkingGlow, radius = 3.5f, center = Offset(tTop.x, tTop.y + 2f))
                    }
                    BuildingType.ECOLOGIC_WATER -> {
                        // Solid stone water reservoir basin
                        val leftWall = Path().apply {
                            moveTo(bLeft.x, bLeft.y)
                            lineTo(bCenter.x, bCenter.y)
                            lineTo(tCenter.x, tCenter.y)
                            lineTo(tLeft.x, tLeft.y)
                            close()
                        }
                        val rightWall = Path().apply {
                            moveTo(bCenter.x, bCenter.y)
                            lineTo(bRight.x, bRight.y)
                            lineTo(tRight.x, tRight.y)
                            lineTo(tCenter.x, tCenter.y)
                            close()
                        }
                        drawPath(leftWall, color = Color(0xFF78909C))
                        drawPath(rightWall, color = Color(0xFF607D8B))

                        // Water surface element (animated moving ripples)
                        val ripplingOffset = if (isGraphicsAdvanced) angleOffsetValue * 1.2f else 0f
                        val waterSurface = Path().apply {
                            moveTo(tLeft.x + 2f, tLeft.y + 1f)
                            lineTo(tCenter.x, tCenter.y + 2f)
                            lineTo(tRight.x - 2f, tRight.y + 1f)
                            lineTo(tTop.x, tTop.y)
                            close()
                        }
                        drawPath(waterSurface, color = Color(0xFF1E88E5).copy(alpha = illuminationCoefficient))
                        
                        // Small fountain ripples
                        drawCircle(
                            color = Color(0xFFBBDEFB).copy(alpha = 0.5f), 
                            radius = (5f + kotlin.math.abs(ripplingOffset)).coerceIn(1f, 15f), 
                            center = tCenter
                        )
                    }
                    BuildingType.PARK -> {
                        // Natural 3D bushy pine trees
                        drawCircle(color = Color(0xFF2E7D32).copy(alpha = illuminationCoefficient), radius = 10f, center = Offset(bCenter.x - 8f, bCenter.y - 12f * constProgress))
                        drawCircle(color = Color(0xFF1B5E20).copy(alpha = illuminationCoefficient), radius = 7f, center = Offset(bCenter.x + 10f, bCenter.y - 8f * constProgress))
                        
                        // trunk stems
                        drawLine(color = Color(0xFF5D4037), start = Offset(bCenter.x - 8f, bCenter.y), end = Offset(bCenter.x - 8f, bCenter.y - 10f), strokeWidth = 2.5f)
                        drawLine(color = Color(0xFF5D4037), start = Offset(bCenter.x + 10f, bCenter.y), end = Offset(bCenter.x + 10f, bCenter.y - 6f), strokeWidth = 2f)
                    }
                    else -> {}
                }
            }
        }
    }
}

// Lerp color helper for beautiful smooth background sky conversions
fun lerpColor(c1: Color, c2: Color, f: Float): Color {
    val r = c1.red + (c2.red - c1.red) * f
    val g = c1.green + (c2.green - c1.green) * f
    val b = c1.blue + (c2.blue - c1.blue) * f
    val a = c1.alpha + (c2.alpha - c1.alpha) * f
    return Color(r, g, b, a)
}

// Custom star drawings inside night canvas
fun drawStars(drawScope: DrawScope) {
    val starCoords = listOf(
        Offset(50f, 30f), Offset(180f, 15f), Offset(320f, 45f), Offset(460f, 25f), Offset(600f, 40f),
        Offset(720f, 18f), Offset(850f, 50f), Offset(960f, 12f), Offset(1050f, 38f), Offset(250f, 75f),
        Offset(380f, 65f), Offset(540f, 80f), Offset(680f, 60f), Offset(810f, 75f), Offset(1000f, 85f)
    )
    starCoords.forEach { offset ->
        drawScope.drawCircle(
            color = Color.White.copy(alpha = (0.35f..0.85f).randomSafeValue()),
            radius = 1.8f,
            center = offset
        )
    }
}

// Safely generate float ratios
fun ClosedRange<Float>.randomSafeValue(): Float {
    return this.start + (this.endInclusive - this.start) * (0..100).random() / 100f
}

// Find empty tile on map
fun findFirstUnoccupied(buildings: List<com.example.data.BuildingEntity>): Pair<Int, Int>? {
    val gridSize = 6
    for (x in 0 until gridSize) {
        for (y in 0 until gridSize) {
            val empty = buildings.none { it.x == x && it.y == y }
            if (empty) return Pair(x, y)
        }
    }
    return null
}

// Helper to direct flat icons based on item category
@Composable
fun get2DIcon(type: BuildingType): ImageVector {
    return when (type) {
        BuildingType.ROAD -> Icons.Default.Place
        BuildingType.HOUSE -> Icons.Default.Home
        BuildingType.FACTORY -> Icons.Default.Build
        BuildingType.COMMERCE -> Icons.Default.ShoppingCart
        BuildingType.PARK -> Icons.Default.Favorite
        BuildingType.POWER_PLANT -> Icons.Default.PlayArrow
        BuildingType.ECOLOGIC_WATER -> Icons.Default.Share
    }
}

// Cartoon drifting clouds scenery modifier
@Composable
fun CloudSceneryOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "cloud1"
    )

    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 600f,
        targetValue = -120f,
        animationSpec = infiniteRepeatable(
            animation = tween(29000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "cloud2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Cloud 1
        Box(
            modifier = Modifier
                .offset(x = cloudOffset1.dp, y = 15.dp)
                .size(width = 65.dp, height = 28.dp)
                .background(Color.White.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
        )
        // Cloud 2
        Box(
            modifier = Modifier
                .offset(x = cloudOffset2.dp, y = 55.dp)
                .size(width = 50.dp, height = 22.dp)
                .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
        )
    }
}
