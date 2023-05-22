package trinity.sim

import chisel3._
import chisel3.util._
import chisel3.reflect.DataMirror
import chisel3.experimental.{ExtModule, UnlocatableSourceInfo}
import trinity.bus.cachebus.{CacheBusBase, CacheBusRespBase}

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

  def portCDefinitionOf(name: String, data: Data) = {
    val typ = data.getWidth match {
      case 1                                  => "unsigned char"
      case width if width > 1 && width <= 8   => "char"
      case width if width > 8 && width <= 32  => "int"
      case width if width > 32 && width <= 64 => "long long"
      case _ => throw new Exception(s"unsupported width ${data.getWidth}!!")
    }
    val suffix = if (DataMirror.directionOf(data) == ActualDirection.Input) {
      " "
    } else {
      "*"
    }
    Seq(f"${typ}%13s", suffix, name).mkString(" ")
  }

  def portsCDefinition = {
    val defs = mapBundle(io.icache).map { case (name, data) =>
      portCDefinitionOf(name, data)
    }
    defs.mkString(",\n")
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

  def cSource = {
    s"""
       |#ifdef __cplusplus
       |extern "C" {
       |#endif
       |void $dpiFuncName (
       |$portsCDefinition
       |);
       |#ifdef __cplusplus
       |}
       |#endif
       |""".stripMargin
  }

  println(verilogSource)
  println(cSource)
  setInline(s"$moduleName.v", verilogSource)
  scala.reflect.io.File(s"$moduleName.h").writeAll(cSource)
}

class SimRam extends Module {
  val io = IO(new SimRamIO)

  val inner = Module(new SimRamInner)
  inner.clock := clock
  inner.reset := reset

  io <> inner.io

  val cachedICacheRespValid = RegInit(false.B)
  val cachedICacheResp = Reg(CacheBusRespBase())
  val cachedDCacheRespValid = RegInit(false.B)
  val cachedDCacheResp = Reg(CacheBusRespBase())

  io.icache.req.ready := !(cachedICacheRespValid || inner.io.icache.resp.valid) || io.icache.resp.ready
  io.dcache.req.ready := !(cachedDCacheRespValid || inner.io.dcache.resp.valid) || io.dcache.resp.ready

  inner.io.icache.req.valid := io.icache.req.fire
  inner.io.dcache.req.valid := io.dcache.req.fire

  io.icache.resp.valid := cachedICacheRespValid
  io.icache.resp.bits := cachedICacheResp
  when(inner.io.icache.resp.valid) {
    io.icache.resp.valid := true.B
    io.icache.resp.bits := inner.io.icache.resp.bits
    cachedICacheResp := inner.io.icache.resp.bits
    cachedICacheRespValid := !io.icache.resp.fire
  } otherwise {
    when(io.icache.resp.fire) {
      cachedICacheRespValid := false.B
    }
  }

  io.dcache.resp.valid := cachedDCacheRespValid
  io.dcache.resp.bits := cachedDCacheResp
  when(inner.io.dcache.resp.valid) {
    io.dcache.resp.valid := true.B
    io.dcache.resp.bits := inner.io.dcache.resp.bits
    cachedDCacheResp := inner.io.dcache.resp.bits
    cachedDCacheRespValid := true.B
  } otherwise {
    when(io.dcache.resp.fire) {
      cachedDCacheRespValid := false.B
    }
  }
}
