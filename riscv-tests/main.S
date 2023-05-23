#include "riscv_test.h"

.section .text.init
.align 6
.globl _start
_start:
    INIT_XREG
    li  x30, 0
    li  x31, 0
    jal x31, test_add
    lui a5, 0xffff
    sll a5, a5, 4
    sd x0, 16(a5)
    nop
    nop
    nop
    nop
    nop