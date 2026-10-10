package dz.betondz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetonDzApp(db: AppDb) {
    var tab by remember { mutableIntStateOf(0) }
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("BetonDZ — مقدّر الخرسانة") }) },
            bottomBar = {
                NavigationBar {
                    listOf("Béton", "Mortier", "Coût", "Carnet").forEachIndexed { i, t ->
                        NavigationBarItem(selected = tab == i, onClick = { tab = i }, label = { Text(t) }, icon = {})
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
        Text("Calculateur béton (hors ligne)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(l, { l = it }, label = { Text("Longueur (m)") })
        OutlinedTextField(w, { w = it }, label = { Text("Largeur (m)") })
        OutlinedTextField(t, { t = it }, label = { Text("Épaisseur (cm)") })
        OutlinedTextField(loss, { loss = it }, label = { Text("Pertes (%)") })
        Dosages.concretes.forEachIndexed { i, d ->
            Row { RadioButton(idx == i, { idx = i }); Text("${d.cementKgM3.toInt()} kg/m³ — ${d.labelKey}") }
        }
        res?.let {
            HorizontalDivider()
            Text("Volume avec pertes: ${"%.2f".format(it.volumeM3)} m³")
            Text("Ciment: ${"%.0f".format(it.cementKg)} kg = ${it.bagsRoundedUp} sacs de 50 kg")
            Text("Sable: ${"%.0f".format(it.sandKg)} kg (${"%.2f".format(it.sandM3)} m³)")
            Text("Gravier: ${"%.0f".format(it.gravelKg)} kg (${"%.2f".format(it.gravelM3)} m³)")
            Text("Eau: ${"%.0f".format(it.waterL)} L (E/C 0.50, ne pas rajouter après malaxage)")
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
        OutlinedTextField(vol, { vol = it }, label = { Text("Volume (m³)") })
        Dosages.mortars.forEachIndexed { i, m ->
            Row { RadioButton(idx == i, { idx = i }); Text("${m.cementKgM3.toInt()} kg/m³ — ${m.labelKey}") }
        }
        r?.let {
            Text("Ciment: ${"%.0f".format(it.cementKg)} kg = ${it.bagsRoundedUp} sacs de 50 kg")
            Text("Sable: ${"%.0f".format(it.sandKg)} kg (${"%.2f".format(it.sandM3)} m³)")
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
        Text("Coût matériaux (DZD)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(cement, { cement = it }, label = { Text("Sac ciment 50kg (DZD)") })
        OutlinedTextField(sand, { sand = it }, label = { Text("Sable par m³ (DZD)") })
        OutlinedTextField(gravel, { gravel = it }, label = { Text("Gravier par m³ (DZD)") })
        OutlinedTextField(vol, { vol = it }, label = { Text("Volume béton (m³, dosage 300)") })
        r?.let { Text("Total estimé: ${"%.0f".format(Calculator.costConcrete(it, p))} DZD") }
        Text("Prix par défaut modifiables, 100% hors ligne. Aucun compte.", style = MaterialTheme.typography.bodySmall)
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
        items.forEach { e -> Text("• ${e.title}") }
    }
}
