#!/bin/bash

[[ -z "${ANDROID_NDK_HOME}" ]] && ANDROID_NDK_HOME="${ANDROID_HOME}/ndk-bundle"
TOOLCHAIN="$(find ${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/* -maxdepth 1 -type d -print -quit)/bin"
ABIS=(arm64-v8a)
GO_ARCHS=(arm64)
CLANG_ARCHS=(aarch64-linux-android)

MIN_API="$1"
ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
OUT_DIR="$ROOT/build/go"

cd "$ROOT/src/main/go/v2ray-plugin"
BIN="libv2ray.so"
for i in "${!ABIS[@]}"; do
    ABI="${ABIS[$i]}"
    [[ -f "${OUT_DIR}/${ABI}/${BIN}" ]] && continue
    echo "Build ${BIN} ${ABI}"
    mkdir -p ${OUT_DIR}/${ABI} \
    && env \
        CGO_ENABLED=1 CC="${TOOLCHAIN}/${CLANG_ARCHS[$i]}${MIN_API}-clang" \
        GOOS=android GOARCH=${GO_ARCHS[$i]} \
        go build -v -ldflags='-s -w' -o "${OUT_DIR}/${ABI}/${BIN}"
done

cd "$ROOT"
