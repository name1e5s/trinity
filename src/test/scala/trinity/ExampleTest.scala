package trinity

import chisel3._
import chisel3.stage.ChiselGeneratorAnnotation
import chisel3.util._
import trinity.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

class ExampleModule extends TrinityModule {
  val io = IO(new Bundle {
    val timer = Output(UInt(64.W))
  })

  log("timer: %x", io.timer)

  io.timer := timer
}

class ExampleTest extends AnyFreeSpec with ChiselScalatestTester {
  "test should work" in {
    test(new ExampleModule) { dut =>
      for (i <- 0 to 100) {
        dut.io.timer.expect(i)
        dut.clock.step(1)
      }
    }
  }
}

object ViewVerilog {
  def main(args: Array[String]): Unit = {
    (new chisel3.stage.ChiselStage).execute(
      Array("-X", "verilog"),
      Seq(ChiselGeneratorAnnotation(() => new ExampleModule))
    )
  }
}
