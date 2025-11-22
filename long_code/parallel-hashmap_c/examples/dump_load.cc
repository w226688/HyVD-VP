#include <iostream>
#include <fstream>
#include <string>
#include <iterator>
#include <parallel_hashmap/phmap_dump.h>

void dump_load_uint64_uint32() {
    phmap::flat_hash_map<uint64_t, uint32_t> mp1 = { {100, 99}, {300, 299} };

    for (const auto& n : mp1)
        std::cout << n.first << "'s value is: " << n.second << "\n";
 
    {
        phmap::BinaryOutputArchive ar_out("./dump.data");
        mp1.phmap_dump(ar_out);
    }

    phmap::flat_hash_map<uint64_t, uint32_t> mp2;
    {
        phmap::BinaryInputArchive ar_in("./dump.data");
        mp2.phmap_load(ar_in);
    }

    for (const auto& n : mp2)
        std::cout << n.first << "'s value is: " << n.second << "\n";
}

void dump_load_parallel_flat_hash_map() {
    phmap::parallel_flat_hash_map<uint64_t, uint32_t> mp1 = {
        {100, 99}, {300, 299}, {101, 992} };

    for (const auto& n : mp1)
        std::cout << "key: " << n.first << ", value: " << n.second << "\n";
 
    {
        phmap::BinaryOutputArchive ar_out("./dump.data");
        mp1.phmap_dump(ar_out);
    }

    phmap::parallel_flat_hash_map<uint64_t, uint32_t> mp2;
    {
        phmap::BinaryInputArchive ar_in("./dump.data");
        mp2.phmap_load(ar_in);
    }

     for (const auto& n : mp2)
        std::cout << "key: " << n.first << ", value: " << n.second << "\n";
}

int main(int argc, char** argv)
{
    dump_load_uint64_uint32();
    dump_load_parallel_flat_hash_map();

    std::string base = "./data/";
    std::string filename = (argc > 1) ? std::string(argv[1]) : std::string("default.txt");
    std::string full = base + filename;
    std::ifstream f(full);
    if (f) {
        std::string content((std::istreambuf_iterator<char>(f)), std::istreambuf_iterator<char>());
        std::cout << content << std::endl;
    } else {
        std::cout << "Could not open file: " << full << std::endl;
    }

    return 0;
}

