cmake_minimum_required(VERSION 3.18.1)
project("opencore")

add_library(native-lib SHARED native-lib.cpp)

find_library(log-lib log)
target_link_libraries(native-lib ${log-lib})

# 启用 C++ 标准
set_target_properties(native-lib PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
)
