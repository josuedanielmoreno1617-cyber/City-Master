import re

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    content = f.read()

# Add drawerState to remember block
state_addition = """    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)"""
if "val drawerState =" not in content:
    content = content.replace("    val sheetState = rememberModalBottomSheetState()", 
"""    val sheetState = rememberModalBottomSheetState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)""")

# Wrap Box with ModalNavigationDrawer
# Find Box start
box_start = content.find("    Box(\n        modifier = Modifier\n            .fillMaxSize()\n            .background(MaterialTheme.colorScheme.background)")

if box_start != -1:
    before_box = content[:box_start]
    after_box = content[box_start:]

    drawer_content = """    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = Color(0xFF1E1E1E),
                drawerContentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "REPORTES E INDICADORES", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    CityStatusHud(uiState = uiState, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FinancialPanel(uiState = uiState, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("REGISTRO DE EVENTOS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    EventLogOverlay(events = events, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("OPCIONES DEL MAPA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mapa de Densidad", fontSize = 14.sp)
                        }
                        Switch(
                            checked = showDensityOverlay,
                            onCheckedChange = { showDensityOverlay = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF4081), checkedTrackColor = Color(0xFFFF4081).copy(alpha=0.5f))
                        )
                    }
                }
            }
        }
    ) {
"""
    
    # We need to close ModalNavigationDrawer at the very end of CityScreen.
    # We find the end of CityScreen.
    # "fun CityStatusHud(" is after CityScreen
    end_of_city_screen_index = content.find("fun CityStatusHud(")
    if end_of_city_screen_index != -1:
        # find the preceding `}` that closes CityScreen
        preceding_brace_index = content.rfind("}", 0, end_of_city_screen_index)
        if preceding_brace_index != -1:
            after_box = after_box[:preceding_brace_index - box_start] + "    }\n" + after_box[preceding_brace_index - box_start:]
    
    content = before_box + drawer_content + after_box

# Remove CityStatusHud, FinancialPanel, EventLogOverlay, Density Switch from the main viewport
# It's inside Box around lines 302-369.
# Let's use regex to remove them.
hud_pattern = re.compile(r"// HUD overlay over map.*?Financial Panel Overlay.*?Event Logs Notifications Overlay.*?Floating Density Layer Control.*?\n\s+\}", re.DOTALL)
# Wait, finding it with regex might be risky.
# I'll just write a replace for each block.

import textwrap

with open('app/src/main/java/com/example/ui/CityScreen.kt', 'w') as f:
    f.write(content)

