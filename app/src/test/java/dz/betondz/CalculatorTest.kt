package dz.betondz

import org.junit.Assert.*
import org.junit.Test

class CalculatorTest {
    @Test fun `1m3 at 350 gives 10 bags of 35kg`() {
        val d = Dosages.concretes.first { it.cementKgM3 == 350.0 }
        val r = Calculator.concrete(1.0, d, lossPct = 0.0, bagKg = 35.0)
        assertEquals(350.0, r.cementKg, 0.001)
        assertEquals(10, r.bagsRoundedUp)
        assertEquals(175.0, r.waterL, 0.001)
    }
    @Test fun `300 dosage slab reference values`() {
        val d = Dosages.concretes.first { it.cementKgM3 == 300.0 }
        val r = Calculator.concrete(1.0, d, lossPct = 0.0, bagKg = 50.0)
        assertEquals(880.0, r.sandKg, 0.001)
        assertEquals(1100.0, r.gravelKg, 0.001)
        assertEquals(150.0, r.waterL, 0.001)
    }
    @Test fun `loss margin scales volume`() {
        val d = Dosages.concretes.first { it.cementKgM3 == 350.0 }
        val r = Calculator.concrete(2.0, d, lossPct = 10.0, bagKg = 50.0)
        assertEquals(2.2, r.volumeM3, 1e-9)
        assertEquals(770.0, r.cementKg, 0.001)
    }
    @Test fun `dims volume`() {
        assertEquals(3.0, Calculator.volumeFromDims(5.0, 4.0, 15.0), 1e-9)
    }
    @Test fun `cost uses rounded bags`() {
        val d = Dosages.concretes.first { it.cementKgM3 == 300.0 }
        val r = Calculator.concrete(1.0, d, lossPct = 0.0, bagKg = 50.0)
        val cost = Calculator.costConcrete(r, Calculator.Prices(490.0, 2500.0, 3000.0))
        val expected = 6 * 490.0 + (880.0 / 1500.0) * 2500.0 + (1100.0 / 1600.0) * 3000.0
        assertEquals(expected, cost, 0.01)
    }
    @Test fun `mortar 1m3 laying`() {
        val m = Dosages.mortars.first()
        val r = Calculator.mortar(1.0, m, lossPct = 0.0, bagKg = 50.0)
        assertEquals(5, r.bagsRoundedUp)
        assertEquals(1300.0, r.sandKg, 0.001)
    }
    @Test(expected = IllegalArgumentException::class)
    fun `rejects zero volume`() {
        Calculator.concrete(0.0, Dosages.concretes.first())
    }
}
