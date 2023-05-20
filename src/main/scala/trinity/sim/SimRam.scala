package trinity.sim

import chisel3._
import chisel3.util._
import chisel3.reflect.DataMirror
import chisel3.experimental.{ExtModule, UnlocatableSourceInfo}
import trinity.bus.cachebus.CacheBusBase

class SimRamIO extends Bundle {
  val icache = Flipped(CacheBusBase())
  val dcache = Flipped(CacheBusBase())
}

class SimRamInner extends ExtModule with HasExtModuleInline {
  val clock = IO(Input(Clock())).suggestName("clock")
  val reset = IO(Input(Bool())).suggestName("reset")
  val io = IO(new SimRamIO)

  def moduleName = this.getClass.getSimpleName
  def dpiFuncName = "c_sim_ram"

  def directionOf(data: Data) = {
    if (DataMirror.directionOf(data) == ActualDirection.Input) {
      "input "
    } else {
      "output"
    }
  }
  def portDefinitionOf(name: String, data: Data) = {
    val width = if (data.getWidth <= 1) {
      "      "
    } else {
      f"[${data.getWidth - 1}%2d:0]"
    }
    Seq(directionOf(data), width, name).mkString(" ")
  }
  def portsDefinition = {
    val ioInfo = mapBundle(io, Some("io"))
    val defs = Seq(
      "input         clock",
      "input         reset"
    ) ++ ioInfo.map { case (name, data) =>
      portDefinitionOf(name, data)
    }
    defs.mkString(",\n")
  }

  def portDPIDefinitionOf(name: String, data: Data) = {
    val typ = data.getWidth match {
      case 1                                  => "bit"
      case width if width > 1 && width <= 8   => "byte"
      case width if width > 8 && width <= 32  => "int"
      case width if width > 32 && width <= 64 => "longint"
      case _ => throw new Exception(s"unsupported width ${data.getWidth}!!")
    }
    Seq(directionOf(data), f"${typ}%7s", name).mkString(" ")
  }
  def portsDPIDefinition = {
    val defs = mapBundle(io.icache).map { case (name, data) =>
      portDPIDefinitionOf(name, data)
    }
    defs.mkString(",\n")
  }

  def portsDPICall(bundle: CacheBusBase, prefix: String) = {
    s"""
       |${dpiFuncName}(
       |${mapBundle(bundle, Some(prefix))
        .map(p => s"  ${p._1}")
        .mkString(",\n")}
       |);
       |""".stripMargin
  }

  def mapBundle(
      b: Bundle,
      prefix: Option[String] = None
  ): Seq[(String, Data)] = {
    b.elements.toSeq.flatMap { case (name, data) =>
      val fullName = prefix match {
        case Some(value) => s"${value}_${name}"
        case None        => name
      }
      data match {
        case bundle: Bundle => mapBundle(bundle, Some(fullName))
        case _              => Seq(fullName -> data)
      }
    }
  }

  def verilogSource = {
    s"""
       |/* verilator lint_off WIDTH */
       |import "DPI-C" function void $dpiFuncName (
       |$portsDPIDefinition
       |);
       |
       |module $moduleName (
       |$portsDefinition
       |);
       |
       |always @(posedge clock) begin: call_c_func
       |  ${portsDPICall(io.icache, "io_icache")}
       |  ${portsDPICall(io.dcache, "io_dcache")}
       |end
       |
       |endmodule
       |""".stripMargin
  }

  println(verilogSource)
  setInline(s"$moduleName.v", verilogSource)
}

class SimRam extends Module {
  val io = IO(new SimRamIO)

  val inner = Module(new SimRamInner)
  io <> inner.io
  inner.clock := clock
  inner.reset := reset
}
