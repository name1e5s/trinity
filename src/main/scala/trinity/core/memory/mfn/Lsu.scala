package trinity.core.memory.mfn

import chisel3._
import chisel3.util._
import trinity.bus.cachebus.{CacheBus, CacheBusSize}
import trinity.core._
import trinity.core.execute.fn._
import trinity.core.Constants._
import trinity.util.{SignExtension, ZeroExtension}

class LsuIO extends Bundle {
  val cache = CacheBus(new ControlFlowBundle)
}

class Lsu extends MfnModule {
  override def id: UInt = FnType.LSU
  val extra = IO(new LsuIO)

  val in = io.in
  val out = io.out
  val cache = extra.cache

  cache.req.valid := in.valid
  cache.req.bits.extra := in.bits
  in.ready := cache.req.ready

  out.valid := cache.resp.valid
  out.bits := cache.resp.bits.extra
  cache.resp.ready := out.ready

  val op = in.bits.microOp.fnOp
  val info = in.bits.addressInfo
  val wdata = info.data

  val req = cache.req.bits
  val rw = LsuOp.rw(op)
  val size = LsuOp.size(op)

  req.base.rw := rw
  req.base.size := size
  req.base.addr := info.addr
  req.base.wmask := MuxLookup(size, 0.U)(
    List(
      CacheBusSize.B -> 1.U,
      CacheBusSize.H -> 3.U,
      CacheBusSize.W -> 15.U,
      CacheBusSize.D -> 255.U
    )
  ) << info.addr(2, 0)
  req.base.wdata := MuxLookup(size, 0.U)(
    List(
      CacheBusSize.B -> Fill(8, wdata(7, 0)),
      CacheBusSize.H -> Fill(4, wdata(15, 0)),
      CacheBusSize.W -> Fill(2, wdata(31, 0)),
      CacheBusSize.D -> wdata
    )
  )

  val rOp = cache.resp.bits.extra.microOp.fnOp
  val raddr = cache.resp.bits.extra.addressInfo.addr
  val rawData = cache.resp.bits.base.rdata
  val rdata = MuxLookup(raddr(2, 0), 0.U) {
    (0 until 8) map { p =>
      p.U -> rawData(63, p * 8)
    }
  }
  val regData = MuxLookup(rOp, 0.U) {
    (0 until 8) map { p =>
      val index = p % 4
      val base = rdata((8 << index) - 1, 0)
      val data = if (p < 4) {
        SignExtension(base, xLen)
      } else {
        ZeroExtension(base, xLen)
      }
      p.U -> data
    }
  }
  out.bits.rd.data := regData
}
