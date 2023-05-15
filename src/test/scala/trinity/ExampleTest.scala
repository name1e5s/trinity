package trinity

import chisel3._
import chisel3.stage.ChiselGeneratorAnnotation
import chisel3.util._
import trinity.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import trinity.core.decode.Decoder
import trinity.core.execute.Executor
import trinity.core.{
  ControlFlowBundle,
  RegisterBypassPort,
  RegisterFile,
  RegisterFileIO
}
import trinity.core.execute.fn.{Agu, Alu, AluOp, Bru, BruOp}
import trinity.core.frontend.Fetcher
import trinity.core.memory.MemoryOperator
import trinity.core.memory.mfn.{Dummy, Lsu}

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
      assert(AluOp.OR.isLit)
      assert(BruOp.J.isLit)
      val c = BitPat(BruOp.NE)
      println(c)
    }
  }
}

class ExampleModule2 extends Module {
  val io = IO(new RegisterFileIO(2))

  val in1 = Wire(new RegisterBypassPort)
  in1.valid := true.B
  in1.addr := 4.U
  in1.data := 4.U

  val in2 = Wire(new RegisterBypassPort)
  in2 := in1
  val reg = Module(new RegisterFile(2))

  reg.io.bypass.zip(List(in1, in2)).foreach { p =>
    p._1 := p._2
  }
  reg.io <> io
}

object ViewVerilog {
  def main(args: Array[String]): Unit = {
    (new chisel3.stage.ChiselStage).execute(
      Array("-X", "verilog"),
      Seq(
        ChiselGeneratorAnnotation(() => new MemoryOperator)
      )
    )
  }
}
