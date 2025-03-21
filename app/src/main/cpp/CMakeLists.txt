
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)
# 指定使用共享 STL

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_ANDROID_STL_TYPE c++_shared)
# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("project2")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.

#set(oboe_DIR "${CMAKE_SOURCE_DIR}/external/oboe/cmake")
#find_package(FluidSynth CONFIG REQUIRED)

add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        native-lib.cpp)

#find_package (oboe REQUIRED CONFIG)		# 查找 Oboe 函数库
#find_package(FluidSynth REQUIRED CONFIG)

#include_directories(${CMAKE_SOURCE_DIR}/fluidsynth/include)

#set(FLUIDSYNTH_LIB_PATH ${CMAKE_SOURCE_DIR}/fluidsynth/lib/${ANDROID_ABI}/libfluidsynth.so)

#add_library(fluidsynth SHARED IMPORTED)
#set_target_properties(fluidsynth PROPERTIES IMPORTED_LOCATION ${FLUIDSYNTH_LIB_PATH})

message(STATUS "Target architecture: ${CMAKE_ANDROID_ARCH_ABI}")
# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
link_directories(${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI})
add_library(libfluidsynth SHARED IMPORTED)
set(SHARED_LIBRARY_SO ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libfluidsynth.so)
set_target_properties(libfluidsynth PROPERTIES IMPORTED_LOCATION ${SHARED_LIBRARY_SO})

add_library(libfluidsynth-assetloader SHARED IMPORTED)
set_target_properties(libfluidsynth-assetloader PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libfluidsynth-assetloader.so)
add_library(libFLAC SHARED IMPORTED)
set_target_properties(libFLAC PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libFLAC.so)
add_library(libgio-2.0 SHARED IMPORTED)
set_target_properties(libgio-2.0 PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libgio-2.0.so)
add_library(libglib-2.0 SHARED IMPORTED)
set_target_properties(libglib-2.0 PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libglib-2.0.so)
add_library(libgmodule-2.0 SHARED IMPORTED)
set_target_properties(libgmodule-2.0 PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libgmodule-2.0.so)
add_library(libgobject-2.0 SHARED IMPORTED)
set_target_properties(libgobject-2.0 PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libgobject-2.0.so)
add_library(libgthread-2.0 SHARED IMPORTED)
set_target_properties(libgthread-2.0 PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libgthread-2.0.so)
add_library(libinstpatch-1.0 SHARED IMPORTED)
set_target_properties(libinstpatch-1.0 PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libinstpatch-1.0.so)
add_library(liboboe SHARED IMPORTED)
set_target_properties(liboboe PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/liboboe.so)
add_library(libogg SHARED IMPORTED)
set_target_properties(libogg PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libogg.so)
add_library(libopus SHARED IMPORTED)
set_target_properties(libopus PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libopus.so)
add_library(libpcre SHARED IMPORTED)
set_target_properties(libpcre PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libpcre.so)
add_library(libpcreposix SHARED IMPORTED)
set_target_properties(libpcreposix PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libpcreposix.so)
add_library(libsndfile SHARED IMPORTED)
set_target_properties(libsndfile PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libsndfile.so)
add_library(libvorbis SHARED IMPORTED)
set_target_properties(libvorbis PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libvorbis.so)
add_library(libvorbisenc SHARED IMPORTED)
set_target_properties(libvorbisenc PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libvorbisenc.so)
add_library(libvorbisfile SHARED IMPORTED)
set_target_properties(libvorbisfile PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth/lib/${CMAKE_ANDROID_ARCH_ABI}/libvorbisfile.so)

find_package(OpenMP REQUIRED)

add_library(fluidsynth INTERFACE)
target_link_libraries(fluidsynth INTERFACE
        libFLAC
        libfluidsynth-assetloader
        libgio-2.0
        libglib-2.0
        libgmodule-2.0
        libgobject-2.0
        libgthread-2.0
        libinstpatch-1.0
        liboboe
        libogg
        libopus
        libpcre
        libpcreposix
        libsndfile
        libvorbis
        libvorbisenc
        libvorbisfile
        libfluidsynth
        OpenMP::OpenMP_CXX
)

target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        fluidsynth
        android
        log)