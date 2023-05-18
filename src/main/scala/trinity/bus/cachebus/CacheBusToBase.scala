package trinity.bus.cachebus

import chisel3._
import chisel3.util._

class CacheBusToBaseIO[T <: Bundle](gen: => T) extends Bundle {
  val full = Flipped(CacheBus(gen))
  val base = CacheBusBase()
}

class CacheBusToBase[T <: Bundle](gen: => T) extends Module {
  val io = IO(new CacheBusToBaseIO(gen))

  import io.{full, base}

  val buf = Reg(gen)
  val bufValid = RegInit(false.B)

  val okToFireReq = !bufValid || (bufValid && full.resp.fire)
  full.req.ready := base.req.ready && okToFireReq
  base.req.valid := full.req.valid && okToFireReq
  base.req.bits := full.req.bits.base

  base.resp.ready := full.resp.ready
  full.resp.valid := base.resp.valid
  full.resp.bits.base := base.resp.bits
  full.resp.bits.extra := buf

  when(base.req.fire) {
    buf := full.req.bits.extra
  }

  when(!bufValid || base.resp.fire) {
    bufValid := base.req.fire
  }
}
