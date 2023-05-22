#include "simulator.h"

void Simulator::reset() {
    c->reset = 1;
    step(1);
    c->reset = 0;
}

void Simulator::step(uint64_t cycles) {
    cycle = 0;
    for (uint64_t i = 0; i < cycles * 10; i ++) {
        if ((i % 10) == 1) {
            cycle += 1;
            printf("Step %llu --\n", cycle);
            c->clock = 1;
        }

        if ((i % 10) == 6) {
            c->clock = 0;
        }

        c->eval();

        if ((i % 10) == 9 && halt) {
            return;
        }
    }
    return;
}

uint64_t real_wdata(uint64_t wdata, char wmask) {
    uint64_t result = wdata;
    for (int i = 0; i < 8; i ++) {
        if (((wmask >> i) & 1) == 0) {
            uint64_t mask = ~(0xffULL << (i << 3));
            result &= mask;
        }
    }
    return result;
}

void Simulator::check_bus(
    long long* resp_bits_rdata,
    unsigned char* resp_valid,
    unsigned char   resp_ready,
    char   req_bits_wmask,
    long long   req_bits_wdata,
    char   req_bits_size,
    long long   req_bits_addr,
    unsigned char   req_bits_rw,
    unsigned char   req_valid,
    unsigned char * req_ready
) {
    *req_ready = 1;
    *resp_valid = req_valid;
    *resp_bits_rdata = 0;

    bool read = req_valid == 1 && req_bits_rw == 0;
    if (read) {
        auto rdata = memory.read(req_bits_addr, req_bits_size);
        printf("Reading %016llx <- %016llx\n", req_bits_addr, rdata);
        *resp_bits_rdata = rdata;
    }

    bool write = req_valid == 1 && req_bits_rw == 1;
    if (write) {
        printf("Write: %llx %d %016llx %02x\n", req_bits_addr, req_bits_size, req_bits_wdata, req_bits_wmask);
        if ((req_bits_addr & 0xFFFFFFFFLL) == 0xFFFF0000LL) {
            uint64_t wdata = real_wdata(req_bits_wdata, req_bits_wmask);
            fprintf(stderr, "%c", (char)wdata);
        } else if ((req_bits_addr & 0xFFFFFFFFLL) == 0xFFFF0010LL) {
            uint64_t wdata = real_wdata(req_bits_wdata, req_bits_wmask);
            if (wdata != 0) {
                fprintf(stderr, "Test failed with error %lld.\n", wdata);
            } else {
                fprintf(stderr, "Test passed.\n");
            }
            halt = true;
        } else {
            memory.write(req_bits_addr, req_bits_size, req_bits_wdata);
        }
    }
}

void Simulator::bus_init(const char *elf_path) {
    memory.init(elf_path);
}
