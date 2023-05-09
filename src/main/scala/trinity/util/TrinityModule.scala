package trinity.util

import chisel3._
import chisel3.experimental.SourceInfo

class TrinityModule extends Module {
  def moduleName = getClass.getName

  val timer = SimpleGlobalTimer()
  val hasLog = true.B

  object log {
    def prefix = f":[$moduleName]: "

    def apply(fmt: String, data: Bits*)(implicit
        sourceInfo: SourceInfo,
        compileOptions: CompileOptions
    ): Unit = {
      when(hasLog) {
        printf("%x ", timer)
        printf(prefix + fmt + "\n", data: _*)(sourceInfo, compileOptions)
      }(sourceInfo, compileOptions)
    }

    def apply(
        pable: Printable
    )(implicit sourceInfo: SourceInfo, compileOptions: CompileOptions): Unit = {
      when(hasLog) {
        printf("%x ", timer)
        printf(Printables(List(PString(prefix), pable, PString("\n"))))(
          sourceInfo,
          compileOptions
        )
      }(sourceInfo, compileOptions)
    }

    def apply(cond: Bool, fmt: String, data: Bits*)(implicit
        sourceInfo: SourceInfo,
        compileOptions: CompileOptions
    ): Unit = {
      when(cond) {
        apply(fmt, data: _*)(sourceInfo, compileOptions)
      }(sourceInfo, compileOptions)
    }

    def apply(cond: Bool, pable: Printable)(implicit
        sourceInfo: SourceInfo,
        compileOptions: CompileOptions
    ): Unit = {
      when(cond) {
        apply(pable)(sourceInfo, compileOptions)
      }(sourceInfo, compileOptions)
    }
  }
}
