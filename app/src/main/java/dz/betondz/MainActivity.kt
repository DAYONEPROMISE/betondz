package dz.betondz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(applicationContext, AppDb::class.java, "betondz.db").build()
        setContent { BetonDzApp(db) }
    }
}

private data class BetonTab(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetonDzApp(db: AppDb) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        BetonTab("B\u00e9ton", Icons.Filled.Calculate),
        BetonTab("Mortier", Icons.Filled.List),
        BetonTab("Co\u00fbt", Icons.Filled.AttachMoney),
        BetonTab("Carnet", Icons.Filled.Book)
    )
    DzTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("BetonDZ \u2014 \u0645\u0642\u062f\u0651\u0631 \u0627\u0644\u062e\u0631\u0633\u0627\u0646\u0629") }) },
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { i, t ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            label = { Text(t.label) },
                            icon = { Icon(t.icon, contentDescription = t.label) }
                        )
                    }
                }
            }) { p ->
            Box(Modifier.padding(p).padding(16.dp)) {
                when (tab) {
                    0 -> ConcreteScreen()
                    1 -> MortarScreen()
                    2 -> CostScreen()
                    else -> LogbookScreen(db)
                }
            }
        }
    }
}

@Composable
fun ConcreteScreen() {
    var l by remember { mutableStateOf("5") }
    var w by remember { mutableStateOf("4") }
    var t by remember { mutableStateOf("15") }
    var idx by remember { mutableIntStateOf(3) }
    var loss by remember { mutableStateOf("5") }
    val dosage = Dosages.concretes[idx]
    val vol = l.toDoubleOrNull()?.let { L -> w.toDoubleOrNull()?.let { W -> t.toDoubleOrNull()?.let { T ->
        runCatching { Calculator.volumeFromDims(L, W, T) }.getOrNull() } } }
    val res = vol?.let { runCatching { Calculator.concrete(it, dosage, loss.toDoubleOrNull() ?: 5.0) }.getOrNull() }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Calculateur b\u00e9ton (hors ligne)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(l, { l = it }, label = { Text("Longueur (m)") })
        OutlinedTextField(w, { w = it }, label = { Text("Largeur (m)") })
        OutlinedTextField(t, { t = it }, label = { Text("\u00c9paisseur (cm)") })
        OutlinedTextField(loss, { loss = it }, label = { Text("Pertes (%)") })
        Column(Modifier.selectableGroup()) {
            Dosages.concretes.forEachIndexed { i, d ->
                Row(Modifier.fillMaxWidth().selectable(selected = idx == i, onClick = { idx = i }, role = Role.RadioButton).padding(vertical = 4.dp)) {
                    RadioButton(selected = idx == i, onClick = null)
                    Text("${d.cementKgM3.toInt()} kg/m\u00b3 \u2014 ${d.labelKey}")
                }
            }
        }
        res?.let {
            HorizontalDivider()
            Text("Volume avec pertes: ${"%.2f".format(it.volumeM3)} m\u00b3")
            Text("Ciment: ${"%.0f".format(it.cementKg)} kg = ${it.bagsRoundedUp} sacs de 50 kg")
            Text("Sable: ${"%.0f".format(it.sandKg)} kg (${"%.2f".format(it.sandM3)} m\u00b3)")
            Text("Gravier: ${"%.0f".format(it.gravelKg)} kg (${"%.2f".format(it.gravelM3)} m\u00b3)")
            Text("Eau: ${"%.0f".format(it.waterL)} L (E/C 0.50, ne pas rajouter apr\u00e8s malaxage)")
        }
    }
}

@Composable
fun MortarScreen() {
    var vol by remember { mutableStateOf("1") }
    var idx by remember { mutableIntStateOf(0) }
    val d = Dosages.mortars[idx]
    val r = vol.toDoubleOrNull()?.let { runCatching { Calculator.mortar(it, d) }.getOrNull() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Calculateur mortier", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(vol, { vol = it }, label = { Text("Volume (m\u00b3)") })
        Column(Modifier.selectableGroup()) {
            Dosages.mortars.forEachIndexed { i, m ->
                Row(Modifier.fillMaxWidth().selectable(selected = idx == i, onClick = { idx = i }, role = Role.RadioButton).padding(vertical = 4.dp)) {
                    RadioButton(selected = idx == i, onClick = null)
                    Text("${m.cementKgM3.toInt()} kg/m\u00b3 \u2014 ${m.labelKey}")
                }
            }
        }
        r?.let {
            Text("Ciment: ${"%.0f".format(it.cementKg)} kg = ${it.bagsRoundedUp} sacs de 50 kg")
            Text("Sable: ${"%.0f".format(it.sandKg)} kg (${"%.2f".format(it.sandM3)} m\u00b3)")
            Text("Eau: ${"%.0f".format(it.waterL)} L")
        }
    }
}

@Composable
fun CostScreen() {
    var cement by remember { mutableStateOf("490") }
    var sand by remember { mutableStateOf("2500") }
    var gravel by remember { mutableStateOf("3000") }
    var vol by remember { mutableStateOf("2.4") }
    val d = Dosages.concretes[2]
    val r = vol.toDoubleOrNull()?.let { runCatching { Calculator.concrete(it, d) }.getOrNull() }
    val p = Calculator.Prices(cement.toDoubleOrNull() ?: 490.0, sand.toDoubleOrNull() ?: 2500.0, gravel.toDoubleOrNull() ?: 3000.0)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Co\u00fbt mat\u00e9riaux (DZD)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(cement, { cement = it }, label = { Text("Sac ciment 50kg (DZD)") })
        OutlinedTextField(sand, { sand = it }, label = { Text("Sable par m\u00b3 (DZD)") })
        OutlinedTextField(gravel, { gravel = it }, label = { Text("Gravier par m\u00b3 (DZD)") })
        OutlinedTextField(vol, { vol = it }, label = { Text("Volume b\u00e9ton (m\u00b3, dosage 300)") })
        r?.let { Text("Total estim\u00e9: ${"%.0f".format(Calculator.costConcrete(it, p))} DZD") }
        Text("Prix par d\u00e9faut modifiables, 100% hors ligne. Aucun compte.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun LogbookScreen(db: AppDb) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf(listOf<SiteEntry>()) }
    var title by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { items = db.entries().all() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Carnet de chantier (Room, offline)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(title, { title = it }, label = { Text("Mesure / note (ex: dalle 5x4)") })
        Button(onClick = {
            scope.launch {
                if (title.isNotBlank()) { db.entries().insert(SiteEntry(title = title, details = title, totalDzd = 0.0)); title = ""; items = db.entries().all() }
            }
        }) { Text("Enregistrer") }
        items.forEach { e -> Text("\u2022 ${e.title}") }
    }
}
