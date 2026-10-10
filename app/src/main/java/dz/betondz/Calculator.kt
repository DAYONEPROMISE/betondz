package dz.betondz

/** Reference dosage table. Sources: NF EN 206/CN classes, calculis.net, calculs-btp.fr.
 *  Values are kg per m3 of fresh concrete (dry aggregates), water = E/C 0.50. */
data class ConcreteDosage(
    val labelKey: String, val cementKgM3: Double,
    val sandKgM3: Double, val gravelKgM3: Double, val waterLM3: Double
)

data class MortarDosage(
    val labelKey: String, val cementKgM3: Double,
    val sandKgM3: Double, val waterLM3: Double
)

object Dosages {
    val concretes = listOf(
        ConcreteDosage("cleanliness_150", 150.0, 980.0, 1150.0, 75.0),
        ConcreteDosage("light_250", 250.0, 850.0, 1150.0, 125.0),
        ConcreteDosage("slab_300", 300.0, 880.0, 1100.0, 150.0),
        ConcreteDosage("foundation_350", 350.0, 820.0, 1125.0, 175.0),
        ConcreteDosage("reinforced_400", 400.0, 750.0, 1150.0, 200.0),
    )
    val mortars = listOf(
        MortarDosage("mortar_laying_250", 250.0, 1300.0, 125.0),
        MortarDosage("mortar_render_300", 300.0, 1300.0, 150.0),
        MortarDosage("mortar_screed_350", 350.0, 1250.0, 175.0),
    )
    const val SAND_DENSITY = 1500.0   // kg/m3
    const val GRAVEL_DENSITY = 1600.0 // kg/m3
}

data class ConcreteResult(
    val volumeM3: Double, val cementKg: Double, val bags: Double, val bagsRoundedUp: Int,
    val sandKg: Double, val sandM3: Double, val gravelKg: Double, val gravelM3: Double,
    val waterL: Double
)

data class MortarResult(
    val volumeM3: Double, val cementKg: Double, val bags: Double, val bagsRoundedUp: Int,
    val sandKg: Double, val sandM3: Double, val waterL: Double
)

object Calculator {
    fun concrete(volumeM3: Double, dosage: ConcreteDosage, lossPct: Double = 5.0, bagKg: Double = 50.0): ConcreteResult {
        require(volumeM3 > 0 && lossPct >= 0 && bagKg > 0)
        val v = volumeM3 * (1 + lossPct / 100.0)
        val cement = dosage.cementKgM3 * v
        val sand = dosage.sandKgM3 * v
        val gravel = dosage.gravelKgM3 * v
        val water = dosage.waterLM3 * v
        return ConcreteResult(v, cement, cement / bagKg, kotlin.math.ceil(cement / bagKg).toInt(),
            sand, sand / Dosages.SAND_DENSITY, gravel, gravel / Dosages.GRAVEL_DENSITY, water)
    }

    fun mortar(volumeM3: Double, dosage: MortarDosage, lossPct: Double = 5.0, bagKg: Double = 50.0): MortarResult {
        require(volumeM3 > 0 && lossPct >= 0 && bagKg > 0)
        val v = volumeM3 * (1 + lossPct / 100.0)
        val cement = dosage.cementKgM3 * v
        val sand = dosage.sandKgM3 * v
        return MortarResult(v, cement, cement / bagKg, kotlin.math.ceil(cement / bagKg).toInt(),
            sand, sand / Dosages.SAND_DENSITY, dosage.waterLM3 * v)
    }

    fun volumeFromDims(lengthM: Double, widthM: Double, thicknessCm: Double): Double {
        require(lengthM > 0 && widthM > 0 && thicknessCm > 0)
        return lengthM * widthM * (thicknessCm / 100.0)
    }

    data class Prices(val cementBagDzd: Double, val sandM3Dzd: Double, val gravelM3Dzd: Double) {
        companion object { fun defaults() = Prices(490.0, 2500.0, 3000.0) }
    }

    fun costConcrete(r: ConcreteResult, p: Prices, bagKg: Double = 50.0): Double {
        val bags = kotlin.math.ceil(r.cementKg / bagKg)
        return bags * p.cementBagDzd + r.sandM3 * p.sandM3Dzd + r.gravelM3 * p.gravelM3Dzd
    }
}
