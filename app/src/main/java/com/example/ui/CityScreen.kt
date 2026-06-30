package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
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
    val dayProgress by viewModel.dayTimeProgress.collectAsStateWithLifecycle()
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedCategory by remember { mutableStateOf("Residencial") } // Residencial, Comercial, Industrial, etc
    
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showInfoDialog by remember { mutableStateOf(false) }
    var buildingTooltipDialog by remember { mutableStateOf<BuildingType?>(null) }

    // Dynamic color gradient background based on Time of Day
    // time: 0.0 (Dawn/Morning) -> 0.25 (Midday) -> 0.65 (Sunset) -> 0.8 (Night)
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

    // Background color processing

    val isNightMode = dayProgress >= 0.75f || dayProgress < 0.1f

    var cityName by remember { mutableStateOf("Mi Ciudad de EE.UU.") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawRect(skyBrush)
                if (isNightMode) {
                    drawStars(this)
                }
            }
    ) {
        // Sky Overlay Fixed at Top
        if (uiState.isGraphicsAdvanced) {
            CloudSceneryOverlay(dayProgress)
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Top Bar with Stats Only
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // City Name Editable Area
                BasicTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                // Global stats pills grouped
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloatingStatTicker(
                        icon = Icons.Default.DateRange, 
                        label = "Día ${uiState.day}", 
                        color = Color(0xFFAB47BC)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FloatingStatTicker(
                        icon = Icons.Default.Face, 
                        label = "${uiState.population}", 
                        color = Color(0xFF4FC3F7)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FloatingStatTicker(
                        icon = Icons.Default.ThumbUp, 
                        label = "${uiState.happiness}%", 
                        color = if (uiState.happiness >= 75) Color(0xFF4CAF50) else Color(0xFFFFB300)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FloatingStatTicker(
                        icon = Icons.Default.Star, 
                        label = "Lvl ${uiState.level}", 
                        color = Color(0xFFFFCA28)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FloatingStatTicker(
                        icon = Icons.Default.Warning, 
                        label = "⚡${uiState.power} 💧${uiState.water}", 
                        color = if (uiState.power < 0 || uiState.water < 0) Color.Red else Color.Cyan
                    )
                }
            }

            // Resource Warning Banner
            // Ocultado por solicitud
            
            // The Map viewport (Scrollable container representing the floats)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                if (uiState.isIsometricMode) {
                    // Isometric 3D Viewport - Camara Panoramica
                    var scale by remember { mutableFloatStateOf(0.8f) }
                    var offsetX by remember { mutableFloatStateOf(0f) }
                    var offsetY by remember { mutableFloatStateOf(0f) }
                    
                    val minScale = 0.3f
                    val maxScale = 3f

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(minScale, maxScale)
                                    val limitX = 4000f * scale
                                    val limitY = 4000f * scale

                                    offsetX = (offsetX + pan.x).coerceIn(-limitX, limitX)
                                    offsetY = (offsetY + pan.y).coerceIn(-limitY, limitY)
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Tilted isometric block field for large US map simulation
                            Box(
                                modifier = Modifier
                                .width(1200.dp)
                                .height(800.dp)
                        ) {
                            val gridSize = 14
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
                                // centerOffset = 580.dp, spacingX = 41.5f, spacingY = 23.5f
                                val xDp = ((x - y) * 41.5f) + 580f
                                val yDp = ((x + y) * 23.5f) + 80f

                                val isMyRoad = building?.type in listOf("ROAD", "DIRT_ROAD", "HIGHWAY")
                                val roadConnections = remember(building, uiState.buildings) {
                                    if (isMyRoad) {
                                        val hasTop = uiState.buildings.any { it.x == x && it.y == y - 1 && it.type in listOf("ROAD", "DIRT_ROAD", "HIGHWAY") }
                                        val hasBottom = uiState.buildings.any { it.x == x && it.y == y + 1 && it.type in listOf("ROAD", "DIRT_ROAD", "HIGHWAY") }
                                        val hasLeft = uiState.buildings.any { it.x == x - 1 && it.y == y && it.type in listOf("ROAD", "DIRT_ROAD", "HIGHWAY") }
                                        val hasRight = uiState.buildings.any { it.x == x + 1 && it.y == y && it.type in listOf("ROAD", "DIRT_ROAD", "HIGHWAY") }
                                        intArrayOf(if(hasTop) 1 else 0, if(hasRight) 1 else 0, if(hasBottom) 1 else 0, if(hasLeft) 1 else 0)
                                    } else null
                                }

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
                                        isSelected = selectedCell == Pair(x, y),
                                        showGridOverlay = selectedCategory.isNotEmpty() && selectedCategory != "Ajustes",
                                        roadConnections = roadConnections
                                    )
                                }
                            }
                        }
                        }
                    }

                    // Volumetric Fog Overlay (Gráficos Avanzados - HDRP sim)
                    if (uiState.isGraphicsAdvanced) {
                        val fogColor = when {
                            dayProgress < 0.2f -> Color(0xFFFF9E79).copy(alpha = 0.15f)
                            dayProgress < 0.55f -> Color.White.copy(alpha = 0.05f)
                            dayProgress < 0.75f -> Color(0xFF9C27B0).copy(alpha = 0.15f)
                            else -> Color(0xFF0A0E17).copy(alpha = 0.35f)
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color.Transparent, fogColor),
                                        radius = 600f
                                    )
                                )
                        )
                    }

                } else {
                    // Modern Standard 2D Schematic matrix Map
                    val gridSize = 14
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
                                    // Remove run extension, just apply border modifier directly
                                    .run {
                                        if (selectedCategory.isNotEmpty() && selectedCategory != "Ajustes" && buildingType == null) {
                                            // Fallback dashed border for 2D mode empty cells
                                            this.background(Color.White.copy(alpha=0.1f))
                                        } else this
                                    }
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
                
                // HUD overlay over map
                CityStatusHud(
                    uiState = uiState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                )
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
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val categories = listOf("Residencial", "Comercial", "Industrial", "Carreteras", "Institucional", "Ocio", "Ajustes")
                        categories.forEach { cat ->
                            val isSel = selectedCategory == cat
                            TextButton(
                                onClick = { 
                                    if (selectedCategory == cat) selectedCategory = "" else selectedCategory = cat 
                                }
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Configuración Gráfica y Motor Render", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Modo 3D Isométrico", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = uiState.isIsometricMode,
                                    onCheckedChange = { viewModel.setIsometricMode(it) }
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Calidad de Texturas (ALTA)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = true,
                                    onCheckedChange = { }
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Sombras Dinámicas", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = uiState.isGraphicsAdvanced,
                                    onCheckedChange = { viewModel.setGraphicsAdvanced(it) }
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Efectos de Luz / Iluminación", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = uiState.isGraphicsAdvanced,
                                    onCheckedChange = { viewModel.setGraphicsAdvanced(it) }
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Suavizado de bordes (Antialiasing)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = true,
                                    onCheckedChange = { }
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Resolución de Pantalla (ALTA / ORIGINAL)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = true,
                                    onCheckedChange = { }
                                )
                            }
                        }
                    } else if (selectedCategory.isNotEmpty()) {
                        // Show list of buildable entities
                        val structuresList = when (selectedCategory) {
                            "Residencial" -> listOf(BuildingType.ZONE_RESIDENTIAL, BuildingType.HOUSE, BuildingType.SKYSCRAPER)
                            "Comercial" -> listOf(BuildingType.ZONE_COMMERCIAL, BuildingType.COMMERCE, BuildingType.OFFICE, BuildingType.HOTEL)
                            "Industrial" -> listOf(BuildingType.ZONE_INDUSTRIAL, BuildingType.FACTORY)
                            "Carreteras" -> listOf(BuildingType.DIRT_ROAD, BuildingType.ROAD, BuildingType.HIGHWAY)
                            "Institucional" -> listOf(BuildingType.POLICE, BuildingType.FIRE_STATION, BuildingType.BANK, BuildingType.GOVERNMENT, BuildingType.POWER_PLANT, BuildingType.ECOLOGIC_WATER)
                            "Ocio" -> listOf(BuildingType.STADIUM, BuildingType.COURT, BuildingType.GOLF_COURSE, BuildingType.PARK)
                            else -> emptyList()
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
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (unlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .clickable {
                                            if (unlocked) {
                                                scope.launch {
                                                    val defaultUnbuilt = findFirstUnoccupied(uiState.buildings)
                                                    if (defaultUnbuilt != null) {
                                                        viewModel.build(struct, defaultUnbuilt.first, defaultUnbuilt.second)
                                                    } else {
                                                        selectedCell = Pair(0, 0)
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(
                                            imageVector = get2DIcon(struct),
                                            contentDescription = struct.displayName,
                                            tint = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        if (!unlocked) {
                                            Text(
                                                text = "Lvl ${struct.minLevelRequired}",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    if (unlocked) {
                                        Text(
                                            text = "$${struct.cost}",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .background(Color.Black.copy(0.6f), RoundedCornerShape(bottomStart = 4.dp))
                                                .padding(horizontal = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } // END OF MAIN COLUMN

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

            // Building tooltip info dialog
            buildingTooltipDialog?.let { building ->
                AlertDialog(
                    onDismissRequest = { buildingTooltipDialog = null },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(get2DIcon(building), contentDescription = null, tint = Color(android.graphics.Color.parseColor(building.colorHex)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(building.displayName, fontWeight = FontWeight.Black)
                        }
                    },
                    text = {
                        Column {
                            Text(building.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("🏙️ Costo: $${building.cost}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (building.popProvide > 0) Text("👥 Capacidad: ${building.popProvide} personas", fontSize = 13.sp)
                            if (building.energyProvide > 0) Text("⚡ Proporciona: ${building.energyProvide}kW", fontSize = 13.sp)
                            if (building.waterProvide > 0) Text("💧 Proporciona: ${building.waterProvide}L agua", fontSize = 13.sp)
                            if (building.revenueProvide != 0) Text("💰 Ingresos: ${if(building.revenueProvide > 0) "+" else ""}${building.revenueProvide} al día", fontSize = 13.sp, color = if(building.revenueProvide > 0) Color(0xFF388E3C) else Color(0xFFD32F2F))
                            if (building.pollutionProduce != 0) Text("🏭 Polución: ${if(building.pollutionProduce > 0) "+" else ""}${building.pollutionProduce} smog", fontSize = 13.sp, color = if(building.pollutionProduce > 0) Color(0xFFD32F2F) else Color(0xFF388E3C))
                            if (building.happinessBoost != 0) Text("😊 Felicidad: ${if(building.happinessBoost > 0) "+" else ""}${building.happinessBoost}", fontSize = 13.sp, color = if(building.happinessBoost > 0) Color(0xFF388E3C) else Color(0xFFD32F2F))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nivel requerido: ${building.minLevelRequired}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { buildingTooltipDialog = null }) {
                            Text("Cerrar")
                        }
                    }
                )
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
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = Color(0xFF263238), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
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
    isSelected: Boolean,
    showGridOverlay: Boolean = false,
    roadConnections: IntArray? = null
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

    val citizenTransition = rememberInfiniteTransition(label = "citizens")
    val citizenMoveProgress by citizenTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "walk"
    )

    // Night/Windows/Glow triggers
    val isNightGlow = dayProgress >= 0.7f || dayProgress < 0.15f
    val illuminationCoefficient = when {
        dayProgress < 0.2f -> 0.6f + (dayProgress / 0.2f) * 0.4f // dawning
        dayProgress < 0.55f -> 1.0f // brightly highlighted
        dayProgress < 0.75f -> 1.0f - ((dayProgress - 0.55f) / 0.2f) * 0.5f // sunset dim
        else -> 0.45f // night shade ambient
    }

    val scalePop = if (buildingString != null) {
        0.85f + (0.15f * constructionProgress.value)
    } else 1f

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().graphicsLayer {
        scaleX = scalePop
        scaleY = scalePop
        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.7f)
    }) {
        val w = size.width
        val h = size.height
        
        // Base of coordinate system inside box
        val gY = h * 0.65f
        val rw = w // Make cells perfectly contiguous (width = 83dp)
        val rh = 47f // (height = 47dp based on step 23.5f * 2)
        val gt = 14f // ground thickness (Voxel base depth)

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

        // 2. Draw Side Walls of the Voxel Ground base (Volumen)
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
        drawPath(leftSoilPath, color = Color(0xFFE0E0E0).copy(alpha = 0.8f * illuminationCoefficient))
        drawPath(rightSoilPath, color = Color(0xFF9E9E9E).copy(alpha = 0.8f * illuminationCoefficient))

        // 3. Draw Top Ground Rhombus face (Seamless)
        val topRhombusPath = Path().apply {
            moveTo(w / 2, gY)
            lineTo(w / 2 + rw / 2, gY + rh / 2)
            lineTo(w / 2, gY + rh)
            lineTo(w / 2 - rw / 2, gY + rh / 2)
            close()
        }

        // Surface base colors (Modern)
        val isRoad = buildingString in listOf("ROAD", "DIRT_ROAD", "HIGHWAY")
        val isZone = buildingString in listOf("ZONE_RESIDENTIAL", "ZONE_COMMERCIAL", "ZONE_INDUSTRIAL")
        val surfaceColor = when (buildingString) {
            "ROAD", "DIRT_ROAD", "HIGHWAY" -> Color(0xFFE0E0E0) // Sidewalk/Tiles base for roads
            "FACTORY" -> Color(0xFFCFD8DC)
            "PARK", "GOLF_COURSE" -> Color(0xFF81C784)
            "ZONE_RESIDENTIAL" -> Color(0xFF81C784)
            "ZONE_COMMERCIAL" -> Color(0xFFFFF176)
            "ZONE_INDUSTRIAL" -> Color(0xFFE57373)
            else -> Color(0xFFAED581) // Modern light green meadow
        }
        drawPath(topRhombusPath, color = surfaceColor.copy(alpha = illuminationCoefficient))

        if (showGridOverlay) {
            drawPath(
                path = topRhombusPath,
                color = Color.White.copy(alpha = 0.6f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2f, 
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
            // Draw crosshairs at corners to look more like a structural grid overlay
            listOf(
                Offset(w / 2, gY),
                Offset(w / 2 + rw / 2, gY + rh / 2),
                Offset(w / 2, gY + rh),
                Offset(w / 2 - rw / 2, gY + rh / 2)
            ).forEach { point ->
                drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 2.5f, center = point)
            }
        }

        // Voxel Grass highlights on empty/green tiles
        if (!isRoad && buildingString !in listOf("FACTORY", "ZONE_COMMERCIAL", "ZONE_INDUSTRIAL")) {
            val grassPath = Path().apply {
                moveTo(w / 2 - 12f, gY + rh / 2 + 5f)
                lineTo(w / 2 + 5f, gY + rh / 2 + 12f)
                lineTo(w / 2 + 15f, gY + rh / 2 + 4f)
                lineTo(w / 2 - 2f, gY + rh / 2 - 3f)
                close()
            }
            drawPath(grassPath, color = Color(0xFF81C784).copy(alpha = 0.8f * illuminationCoefficient))
            
            // Draw a few voxel trees
            for (i in listOf(Pair(-15f, -10f), Pair(20f, 0f))) {
                val tX = w / 2 + i.first
                val tY = gY + rh / 2 + i.second
                // trunk
                drawLine(Color(0xFF795548), Offset(tX, tY), Offset(tX, tY - 8f), strokeWidth = 3f)
                // leaves box
                drawRect(Color(0xFF4CAF50).copy(alpha = 0.9f), topLeft = Offset(tX - 6f, tY - 18f), size = androidx.compose.ui.geometry.Size(12f, 12f))
            }
        }

        // Road separator drawing
        if (roadConnections != null) {
            val lineColor = when (buildingString) { "DIRT_ROAD" -> Color(0xFF8D6E63); "HIGHWAY" -> Color(0xFFF1C40F); else -> Color(0xFFD8DEE9) }
            val cX = w / 2
            val cY = gY + rh / 2
            
            // Si hay cruces o conexiones
            val topH = roadConnections[0] == 1
            val rightH = roadConnections[1] == 1
            val bottomH = roadConnections[2] == 1
            val leftH = roadConnections[3] == 1
            
            val totalConns = roadConnections.sum()
            
            // Draw Asphalt inset over sidewalk
            val asphaltInset = 12f
            val asphaltPath = Path().apply {
                moveTo(w / 2, gY + asphaltInset)
                lineTo(w / 2 + rw / 2 - asphaltInset*1.5f, gY + rh / 2)
                lineTo(w / 2, gY + rh - asphaltInset)
                lineTo(w / 2 - rw / 2 + asphaltInset*1.5f, gY + rh / 2)
                close()
            }
            drawPath(asphaltPath, color = Color(0xFF37474F).copy(alpha = 0.95f))

            // Dibujar lineas centrales (Traffic marks / Crosswalks)
            val markColor = Color.White
            if (totalConns <= 1 || (topH && bottomH && !leftH && !rightH)) {
                drawLine(markColor, Offset(cX - rw/5, cY - rh/5), Offset(cX + rw/5, cY + rh/5), strokeWidth = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            } else if (leftH && rightH && !topH && !bottomH) {
                drawLine(markColor, Offset(cX - rw/5, cY + rh/5), Offset(cX + rw/5, cY - rh/5), strokeWidth = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            } else {
                // Intersection crosswalks
                drawRect(markColor.copy(alpha=0.6f), topLeft = Offset(cX - 6f, cY - 2f), size = androidx.compose.ui.geometry.Size(12f, 4f))
                drawRect(markColor.copy(alpha=0.6f), topLeft = Offset(cX - 2f, cY - 6f), size = androidx.compose.ui.geometry.Size(4f, 12f))
            }

            // Draw Traffic Lights / Street lamps at corners
            if (totalConns > 2) {
                val lampX = cX - 10f
                val lampY = cY + 12f
                drawLine(Color.DarkGray, Offset(lampX, lampY), Offset(lampX, lampY - 14f), strokeWidth = 2f)
                drawRect(Color(0xFF424242), topLeft = Offset(lampX - 2f, lampY - 18f), size = androidx.compose.ui.geometry.Size(4f, 6f))
                drawCircle(Color.Green, radius = 1.5f, center = Offset(lampX, lampY - 16f)) // Green light
            }

            // Citizens moving autonomously on sidewalks and crosswalks (Voxel people)
            val p1X = cX - rw/4 + (citizenMoveProgress * rw/2)
            val p1Y = cY - rh/4 + (citizenMoveProgress * rh/2)
            drawRect(Color(0xFFE53935), topLeft = Offset(p1X, p1Y), size = androidx.compose.ui.geometry.Size(4f, 6f)) // body
            drawRect(Color(0xFFFFD54F), topLeft = Offset(p1X + 0.5f, p1Y - 3f), size = androidx.compose.ui.geometry.Size(3f, 3f)) // head
            
            val p2X = cX + rw/4 - (citizenMoveProgress * rw/2)
            val p2Y = cY + rh/4 - (citizenMoveProgress * rh/2)
            drawRect(Color(0xFF1E88E5), topLeft = Offset(p2X - 4f, p2Y), size = androidx.compose.ui.geometry.Size(4f, 6f)) // body
            drawRect(Color(0xFFFFD54F), topLeft = Offset(p2X - 3.5f, p2Y - 3f), size = androidx.compose.ui.geometry.Size(3f, 3f)) // head
        }

        // 4. Draw Rising 3D Building structures
        if (buildingString != null && roadConnections == null && !isZone) {
            val constProgress = constructionProgress.value
            
            // Standard bounding boxes for structures
            val buildType = try { BuildingType.valueOf(buildingString) } catch (e: Exception) { null }
            if (buildType != null) {
                // Dimensions based on building type
                val wallHeight = when (buildType) {
                    BuildingType.HOUSE -> 80f * constProgress // Tall modern apartments
                    BuildingType.SKYSCRAPER -> 240f * constProgress // Huge glass tower
                    BuildingType.FACTORY -> 60f * constProgress
                    BuildingType.COMMERCE -> 70f * constProgress
                    BuildingType.OFFICE -> 140f * constProgress
                    BuildingType.HOTEL -> 160f * constProgress
                    BuildingType.BANK -> 120f * constProgress
                    BuildingType.POLICE -> 50f * constProgress
                    BuildingType.FIRE_STATION -> 45f * constProgress
                    BuildingType.GOVERNMENT -> 90f * constProgress
                    BuildingType.STADIUM -> 40f * constProgress
                    BuildingType.COURT -> 35f * constProgress
                    BuildingType.GOLF_COURSE -> 4f * constProgress
                    BuildingType.POWER_PLANT -> 34f * constProgress
                    BuildingType.ECOLOGIC_WATER -> 15f * constProgress
                    BuildingType.PARK -> 8f * constProgress
                    else -> 40f * constProgress
                }

                // CSS-based grow animation scale for Residential & Commercial
                val isGrowCat = buildingString in listOf("HOUSE", "SKYSCRAPER", "COMMERCE", "OFFICE", "HOTEL")
                val scaleFactor = if (isGrowCat) constProgress else 1f
                val structW = rw * 0.55f * scaleFactor
                val structH = rh * 0.55f * scaleFactor

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

                // Realistic Ray-Traced Shadow (Gráficos Avanzados - HDRP sim)
                if (isGraphicsAdvanced && dayProgress < 0.7f && constProgress > 0f) {
                    val isMorning = dayProgress < 0.35f
                    val shadowAlpha = if (dayProgress < 0.15f || dayProgress > 0.55f) 0.12f * illuminationCoefficient else 0.25f * illuminationCoefficient
                    
                    // Sun vector mapping based on day cycle
                    val sunProgressX = (dayProgress - 0.35f) * 3f 
                    val sDx = 45f * sunProgressX
                    val sDy = Math.abs(sDx) * 0.45f
                    
                    val shadowPathAdvanced = Path().apply {
                        moveTo(bLeft.x, bLeft.y)
                        if (isMorning) {
                            lineTo(bCenter.x, bCenter.y)
                            lineTo(bRight.x, bRight.y)
                            lineTo(tRight.x + sDx, tRight.y + sDy)
                            lineTo(tTop.x + sDx, tTop.y + sDy)
                            lineTo(tLeft.x + sDx, tLeft.y + sDy)
                        } else {
                            lineTo(tLeft.x + sDx, tLeft.y + sDy)
                            lineTo(tTop.x + sDx, tTop.y + sDy)
                            lineTo(tRight.x + sDx, tRight.y + sDy)
                            lineTo(bRight.x, bRight.y)
                            lineTo(bCenter.x, bCenter.y)
                        }
                        close()
                    }
                    drawPath(shadowPathAdvanced, color = Color.Black.copy(alpha = shadowAlpha * constProgress))
                } else if (!isGraphicsAdvanced || dayProgress < 0.7f) {
                    // Primitive simplistic shadow
                    val shadowPath = Path().apply {
                        moveTo(bLeft.x, bLeft.y)
                        lineTo(bCenter.x + (15f + angleOffsetValue), bCenter.y)
                        lineTo(bRight.x + (25f + angleOffsetValue), bRight.y + 12f)
                        lineTo(bRight.x, bRight.y)
                        close()
                    }
                    drawPath(shadowPath, color = Color.Black.copy(alpha = 0.15f * illuminationCoefficient))
                }

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
                    else -> {
                        // Custom US-style styling for specific buildings using colors
                        val baseColor = when (buildType) {
                            BuildingType.SKYSCRAPER -> Color(0xFF1E88E5) // Glass Blue
                            BuildingType.HOUSE -> Color(0xFF64B5F6) // Light Glass Blue
                            BuildingType.OFFICE -> Color(0xFF3949AB) // Dark Glass Blue
                            BuildingType.COMMERCE -> Color(0xFF78909C) // Grey modern structure
                            BuildingType.HOTEL -> Color(0xFFFDD835) // Elegant warm
                            BuildingType.POLICE -> Color(0xFF1565C0) // Police Blue
                            BuildingType.BANK -> Color(0xFF8D6E63) // Classic brick/stone
                            BuildingType.GOVERNMENT -> Color(0xFFECEFF1) // White house style
                            else -> try { Color(android.graphics.Color.parseColor(buildType.colorHex)) } catch (e: Exception) { Color(0xFF9E9E9E) }
                        }
                        
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
                        val roof = Path().apply {
                            moveTo(tLeft.x, tLeft.y)
                            lineTo(tCenter.x, tCenter.y)
                            lineTo(tRight.x, tRight.y)
                            lineTo(tTop.x, tTop.y)
                            close()
                        }
                        
                        // Create variants
                        val leftColor = lerpColor(baseColor, if (buildType == BuildingType.SKYSCRAPER) Color(0xFF64B5F6) else Color.White, 0.2f)
                        val rightColor = lerpColor(baseColor, Color.Black, 0.35f)
                        val topColor = lerpColor(baseColor, Color.White, 0.45f)
                        
                        drawPath(leftWall, color = leftColor.copy(alpha = illuminationCoefficient))
                        drawPath(rightWall, color = rightColor.copy(alpha = illuminationCoefficient))
                        drawPath(roof, color = topColor.copy(alpha = illuminationCoefficient))
                        
                        // Decorate American style details
                        if (buildType == BuildingType.POLICE) {
                            // Police badge / star on roof or red/blue lights
                            drawCircle(color = Color.Red, radius = 2f, center = Offset(tCenter.x - 4f, tCenter.y))
                            drawCircle(color = Color.Blue, radius = 2f, center = Offset(tCenter.x + 4f, tCenter.y))
                        } else if (buildType == BuildingType.HOTEL) {
                            // Hotel marquee
                            val marqueePath = Path().apply {
                                moveTo(tLeft.x + 4f, tLeft.y + 10f)
                                lineTo(tCenter.x - 2f, tCenter.y + 10f)
                                lineTo(tCenter.x - 2f, tCenter.y + 14f)
                                lineTo(tLeft.x + 4f, tLeft.y + 14f)
                                close()
                            }
                            drawPath(marqueePath, color = Color(0xFFD32F2F))
                        } else if (buildType == BuildingType.GOVERNMENT) {
                            // Pillar look
                            for (i in 1..3) {
                                val factor = i / 4f
                                val px = bLeft.x + (bCenter.x - bLeft.x) * factor
                                val py = bLeft.y + (bCenter.y - bLeft.y) * factor
                                drawLine(color = Color.Black.copy(0.3f), start = Offset(px, py), end = Offset(px, py - wallHeight), strokeWidth = 1f)
                            }
                        }

                        // "Anti-aliasing" & Texture Highlighting (Gráficos Avanzados)
                        if (isGraphicsAdvanced && constProgress > 0.5f) {
                            val edgeColor = Color.White.copy(alpha = 0.45f * illuminationCoefficient)
                            val shadeColor = Color.Black.copy(alpha = 0.35f * illuminationCoefficient)
                            
                            drawLine(color = edgeColor, start = tLeft, end = tCenter, strokeWidth = 1.5f)
                            drawLine(color = edgeColor, start = tTop, end = tLeft, strokeWidth = 1.5f)
                            drawLine(color = edgeColor, start = tLeft, end = bLeft, strokeWidth = 1.2f)
                            
                            drawLine(color = shadeColor, start = tCenter, end = tRight, strokeWidth = 1.2f)
                            drawLine(color = shadeColor, start = tRight, end = tTop, strokeWidth = 1.2f)
                            drawLine(color = shadeColor, start = tCenter, end = bCenter, strokeWidth = 1.5f)
                            
                            // US Style windows on office/skyscrapers 
                            if (wallHeight > 30f) {
                                val isGlassBuilding = buildType in listOf(BuildingType.SKYSCRAPER, BuildingType.HOUSE, BuildingType.OFFICE)
                                val winColor = if(isGlassBuilding) Color(0xFF90CAF9).copy(alpha = 0.8f) else Color(0xFFE3F2FD).copy(alpha = 0.6f * illuminationCoefficient)
                                val steps = if(isGlassBuilding) (wallHeight / 12f).toInt() else 4
                                for (i in 1..steps) {
                                    val factor = i.toFloat() / (steps + 1)
                                    val lerpFloat = { start: Float, end: Float, f: Float -> start + (end - start) * f }
                                    // left wall windows
                                    val leftMidStart = Offset(lerpFloat(bLeft.x, tLeft.x, factor), lerpFloat(bLeft.y, tLeft.y, factor))
                                    val leftMidEnd = Offset(lerpFloat(bCenter.x, tCenter.x, factor), lerpFloat(bCenter.y, tCenter.y, factor))
                                    drawLine(color = winColor, start = leftMidStart, end = leftMidEnd, strokeWidth = if(isGlassBuilding) 1.5f else 2.5f)
                                    // right wall windows
                                    val rightMidStart = Offset(lerpFloat(bCenter.x, tCenter.x, factor), lerpFloat(bCenter.y, tCenter.y, factor))
                                    val rightMidEnd = Offset(lerpFloat(bRight.x, tRight.x, factor), lerpFloat(bRight.y, tRight.y, factor))
                                    drawLine(color = winColor, start = rightMidStart, end = rightMidEnd, strokeWidth = if(isGlassBuilding) 1.5f else 2f)
                                }
                            }
                        }
                    }
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
    val gridSize = 14
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
        BuildingType.ZONE_RESIDENTIAL -> Icons.Default.AddHome
        BuildingType.ZONE_COMMERCIAL -> Icons.Default.AddBusiness
        BuildingType.ZONE_INDUSTRIAL -> Icons.Default.Factory
        BuildingType.ROAD -> Icons.Default.AddRoad
        BuildingType.DIRT_ROAD -> Icons.Default.AltRoute
        BuildingType.HIGHWAY -> Icons.Default.DirectionsCar
        BuildingType.HOUSE -> Icons.Default.Home
        BuildingType.SKYSCRAPER -> Icons.Default.LocationCity
        BuildingType.FACTORY -> Icons.Default.Factory
        BuildingType.COMMERCE -> Icons.Default.Storefront
        BuildingType.OFFICE -> Icons.Default.Business
        BuildingType.HOTEL -> Icons.Default.Hotel
        BuildingType.BANK -> Icons.Default.AccountBalance
        BuildingType.POLICE -> Icons.Default.LocalPolice
        BuildingType.FIRE_STATION -> Icons.Default.LocalFireDepartment
        BuildingType.GOVERNMENT -> Icons.Default.AccountBalance
        BuildingType.STADIUM -> Icons.Default.Stadium
        BuildingType.COURT -> Icons.Default.SportsTennis
        BuildingType.GOLF_COURSE -> Icons.Default.GolfCourse
        BuildingType.PARK -> Icons.Default.Park
        BuildingType.POWER_PLANT -> Icons.Default.ElectricMeter
        BuildingType.ECOLOGIC_WATER -> Icons.Default.WaterDrop
    }
}

// Realistic drifting clouds, sun, and moon scenery modifier
@Composable
fun CloudSceneryOverlay(dayProgress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "cloud1"
    )

    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 800f,
        targetValue = -120f,
        animationSpec = infiniteRepeatable(
            animation = tween(29000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "cloud2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Sun logic - pixelated
        val sunVisible = dayProgress in 0.0f..0.8f
        if (sunVisible) {
            // arc from left to right as day progresses from 0 to 0.75
            val sunX = -50f + (dayProgress / 0.8f) * 800f
            val sunY = 200f - Math.sin((dayProgress / 0.8f) * Math.PI) * 150f
            
            androidx.compose.foundation.Canvas(modifier = Modifier.offset(x = sunX.dp, y = sunY.dp).size(60.dp)) {
                // Draw a pixelated circle (sun)
                val blockSize = 10f
                val blocks = listOf(
                    Pair(2, 0), Pair(3, 0), 
                    Pair(1, 1), Pair(2, 1), Pair(3, 1), Pair(4, 1),
                    Pair(0, 2), Pair(1, 2), Pair(2, 2), Pair(3, 2), Pair(4, 2), Pair(5, 2),
                    Pair(0, 3), Pair(1, 3), Pair(2, 3), Pair(3, 3), Pair(4, 3), Pair(5, 3),
                    Pair(1, 4), Pair(2, 4), Pair(3, 4), Pair(4, 4),
                    Pair(2, 5), Pair(3, 5)
                )
                blocks.forEach { (bx, by) ->
                    drawRect(Color(0xFFFFEB3B), topLeft = Offset(bx * blockSize, by * blockSize), size = androidx.compose.ui.geometry.Size(blockSize, blockSize))
                }
            }
        }

        // Moon logic
        val moonVisible = dayProgress > 0.65f || dayProgress < 0.2f
        if (moonVisible) {
            val moonProgress = if (dayProgress > 0.65f) (dayProgress - 0.65f) / 0.55f else (dayProgress + 0.35f) / 0.55f
            val moonX = -50f + moonProgress * 800f
            val moonY = 200f - Math.sin(moonProgress * Math.PI) * 150f
            
            androidx.compose.foundation.Canvas(modifier = Modifier.offset(x = moonX.dp, y = moonY.dp).size(45.dp)) {
                val blockSize = 8f
                val blocks = listOf(
                    Pair(1, 0), Pair(2, 0), Pair(3, 0),
                    Pair(0, 1), Pair(1, 1), Pair(2, 1), Pair(3, 1), Pair(4, 1),
                    Pair(0, 2), Pair(1, 2), Pair(2, 2), Pair(3, 2), Pair(4, 2),
                    Pair(0, 3), Pair(1, 3), Pair(2, 3), Pair(3, 3), Pair(4, 3),
                    Pair(1, 4), Pair(2, 4), Pair(3, 4)
                )
                blocks.forEach { (bx, by) ->
                    drawRect(Color.White.copy(alpha=0.9f), topLeft = Offset(bx * blockSize, by * blockSize), size = androidx.compose.ui.geometry.Size(blockSize, blockSize))
                }
            }
        }

        // Voxel Cloud 1
        Box(modifier = Modifier.offset(x = cloudOffset1.dp, y = 40.dp)) {
            Row {
                Box(modifier = Modifier.size(30.dp, 20.dp).background(Color.White.copy(alpha=0.85f), RoundedCornerShape(2.dp)).align(Alignment.Bottom))
                Box(modifier = Modifier.size(50.dp, 35.dp).background(Color.White.copy(alpha=0.9f), RoundedCornerShape(2.dp)).align(Alignment.Bottom))
                Box(modifier = Modifier.size(40.dp, 25.dp).background(Color.White.copy(alpha=0.85f), RoundedCornerShape(2.dp)).align(Alignment.Bottom))
                Box(modifier = Modifier.size(20.dp, 15.dp).background(Color.White.copy(alpha=0.8f), RoundedCornerShape(2.dp)).align(Alignment.Bottom))
            }
        }
        // Voxel Cloud 2
        Box(modifier = Modifier.offset(x = cloudOffset2.dp, y = 80.dp)) {
            Row {
                Box(modifier = Modifier.size(40.dp, 25.dp).background(Color.White.copy(alpha=0.85f), RoundedCornerShape(2.dp)).align(Alignment.Bottom))
                Box(modifier = Modifier.size(60.dp, 40.dp).background(Color.White.copy(alpha=0.9f), RoundedCornerShape(2.dp)).align(Alignment.Bottom))
                Box(modifier = Modifier.size(45.dp, 30.dp).background(Color.White.copy(alpha=0.85f), RoundedCornerShape(2.dp)).align(Alignment.Bottom))
            }
        }
    }
}

@Composable
fun CityStatusHud(
    uiState: CityUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF263238).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF455A64), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Funds
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Funds",
                tint = Color(0xFF69F0AE),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$${uiState.money}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Ingresos (Taxes): +$${uiState.revenue}", 
                    color = Color(0xFF81C784), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gastos (Construcción): -$${uiState.expenses}", 
                    color = Color(0xFFE57373), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

