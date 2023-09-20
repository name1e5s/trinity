#ifndef ELFLOADER_SIMULATOR_H
#define ELFLOADER_SIMULATOR_H

#include <cstdint>
#include <VSimCore.h>
#include "sim_memory.h"

class Simulator {
public:
    Simulator() : cycle(0), halt(false), c(new VSimCore) {
    }

    void reset();

    void step(uint64_t cycles = 1);

    void bus_init(const char *elf_path);

    void check_bus (
        long long * resp_bits_rdata,
        unsigned char * resp_valid,
        unsigned char   resp_ready,
        char   req_bits_wmask,
        long long   req_bits_wdata,
        char   req_bits_size,
        long long   req_bits_addr,
        unsigned char   req_bits_rw,
        unsigned char   req_valid,
        unsigned char * req_ready
    );

    std::shared_ptr<VSimCore> c;
    bool halt;
private:
    uint64_t cycle;
    SimMemory memory;
};


#endif //ELFLOADER_SIMULATOR_H
