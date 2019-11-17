# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.
cmake_minimum_required(VERSION 3.6)
#set(ANDROID_ALLOW_UNDEFINED_SYMBOLS 0)
#set(CMAKE_BUILD_TYPE Release) # Yes for release!

#set(CMAKE_SHARED_LINKER_FLAGS "-fPIE -pie")

add_library(dl SHARED IMPORTED)
set_target_properties(dl PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libdl.so)


# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.
add_library( # Specifies the name of the library.
             nfcgate-native

             # Sets the library as a shared library.
             SHARED
             # Provides a relative path to your source file(s).
        src/main/jni/main.cpp
        src/main/jni/chip.cpp
        src/main/jni/javainterface.cpp
        src/main/jni/vendor/adbi/hook.cpp
             )

target_link_libraries(nfcgate-native android log dl)