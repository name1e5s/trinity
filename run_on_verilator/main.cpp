#include <iostream>
#include "loadelf.h"
#include "simulator.h"

Simulator s;

extern "C" void c_sim_ram(
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
    s.check_bus(
        resp_bits_rdata, 
        resp_valid, 
        resp_ready, 
        req_bits_wmask,
        req_bits_wdata,
        req_bits_size,
        req_bits_addr,
        req_bits_rw,
        req_valid,
        req_ready
    );
}

int main(int argc, char **argv) {
    if (argc != 2) {
        printf("Argument mismatch.\n");
        exit(1);
    } else {
        printf("Loading elf %s to memory ...\n", argv[1]);
    }
    s.bus_init(argv[1]);
    s.reset();
    s.step(1000);
    return 0;
}
