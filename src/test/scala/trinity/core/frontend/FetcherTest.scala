package trinity.core.frontend

import chisel3._
import chisel3.stage.ChiselGeneratorAnnotation
import chisel3.util._
import trinity.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

class FetcherTest extends AnyFreeSpec with ChiselScalatestTester {
  "fetcher should work" in test(new Fetcher) { dut =>
    dut.io.cache.req.ready.poke(true)
    dut.io.cache.resp.valid.poke(true)
    dut.io.instruction.ready.poke(true)
    dut.io.mispredictRedirection.valid.poke(false)
    dut.io.exceptionRedirection.valid.poke(false)

    val extra = dut.io.cache.req.bits.extra.peek()
    dut.io.cache.resp.bits.extra.poke(extra)

    for (i <- 0 to 10) {
      dut.io.cache.req.bits.addr.expect(0x8000_0000L + 4L * i)
      dut.clock.step(1)
    }

    dut.io.mispredictRedirection.valid.poke(true)
    dut.io.mispredictRedirection.bits.poke(0x8000_1000L)
    dut.clock.step(1)

    dut.io.mispredictRedirection.valid.poke(false)
    dut.io.cache.req.bits.addr.expect(0x8000_1000L)
  }
}
