package systolic.core

import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

class SystolicArrayTest extends AnyFreeSpec with ChiselScalatestTester {
  "array should work" in test(new SystolicArray) { dut =>
    dut.io.cmd.poke(ElementCmd.CLEAR)
    dut.clock.step(1)

    dut.io.cmd.poke(ElementCmd.COMPUTE)
    var seq = (0 until ELEMENT_COUNT).map(-_)
    while (seq.min < 2 * ELEMENT_COUNT) {
      val data = seq.map { data =>
        if (data >= 0 && data < ELEMENT_COUNT) {
          1
        } else {
          0
        }
      }

      println(seq)
      println(data)

      data.zipWithIndex.foreach { case (data, index) =>
        val value = data
        dut.io.src1(index).poke(value)
        dut.io.src2(index).poke(value)
      }

      seq = seq.map(_ + 1)
      dut.clock.step(1)
    }

    dut.io.cmd.poke(ElementCmd.IO)
    for (_ <- 0 until ELEMENT_COUNT) {
      for (i <- 0 until ELEMENT_COUNT) {
        dut.io.result(i).expect(8)
      }
      dut.clock.step(1)
    }
  }
}
