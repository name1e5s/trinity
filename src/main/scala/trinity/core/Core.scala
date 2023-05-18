package trinity.core

import chisel3._
import chisel3.util._
import trinity.bus.cachebus.{CacheBus, CacheBusBase, CacheBusToBase}
import trinity.core.decode.Decoder
import trinity.core.decode.isa.InstType
import trinity.core.execute.Executor
import trinity.core.frontend.{Fetcher, ICacheExtra}
import trinity.core.memory.MemoryOperator
import trinity.core.writeback.WriteBack
import trinity.util.{PipelineStage, TrinityModule}

class CoreIO extends Bundle {
  val icache = CacheBusBase()
  val dcache = CacheBusBase()
}

class Core extends TrinityModule {
  val io = IO(new CoreIO)

  val registerFile = Module(new RegisterFile(3))

  val fetcher = Module(new Fetcher)
  val decoder = Module(new Decoder)
  val regDeToEx = Module(PipelineStage(new ControlFlowBundle))
  val executor = Module(new Executor)
  val regExToMem = Module(PipelineStage(new ControlFlowBundle))
  val memoryOperator = Module(new MemoryOperator)
  val writeBack = Module(new WriteBack)

  val iCacheConverter = Module(new CacheBusToBase(new ICacheExtra))
  val dCacheConverter = Module(new CacheBusToBase(new ControlFlowBundle))

  iCacheConverter.io.base <> io.icache
  dCacheConverter.io.base <> io.dcache

  fetcher.io.cache <> iCacheConverter.io.full
  fetcher.io.instruction <> decoder.io.in
  fetcher.io.exceptionRedirection.valid := false.B
  fetcher.io.exceptionRedirection.bits := DontCare
  fetcher.io.mispredictRedirection := executor.io.redirect

  decoder.io.out <> regDeToEx.io.in
  decoder.io.read <> registerFile.io.read
  decoder.io.mfuAllReady := memoryOperator.extra.allReady
  decoder.io.feedBack.zip(Seq(regDeToEx, regExToMem)).foreach { p =>
    val feedBack = p._1
    val register = p._2.io.out
    feedBack.valid := register.valid
    feedBack.bits := register.bits
  }

  regDeToEx.io.flush := executor.io.redirect.valid
  regDeToEx.io.out <> executor.io.in

  executor.io.out <> regExToMem.io.in

  regExToMem.io.flush := false.B
  regExToMem.io.out <> memoryOperator.io.in

  memoryOperator.io.flush := false.B
  memoryOperator.extra.lsuIO.cache <> dCacheConverter.io.full
  memoryOperator.io.out <> writeBack.io.in

  writeBack.io.write <> registerFile.io.write

  val bypasses =
    Seq(executor.io.bypass) ++ Seq(regExToMem.io.out, memoryOperator.io.out)
      .map { p =>
        val bypass = Wire(new RegisterBypassPort)
        val flow = p.bits
        val microOp = flow.microOp

        bypass.valid := p.valid && InstType.writeGpr(microOp.fnType)
        bypass.addr := flow.rd.addr
        bypass.data := flow.rd.data
        bypass
      }
  registerFile.io.bypass.zip(bypasses).foreach(p => p._1 := p._2)
}
