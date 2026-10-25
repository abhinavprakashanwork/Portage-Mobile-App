# Automatically register all .c and .cpp files in this component
file(GLOB_RECURSE component_sources
     "${CMAKE_CURRENT_LIST_DIR}/*.c"
     "${CMAKE_CURRENT_LIST_DIR}/*.cpp"
)

if(CONFIG_TOOTHPASTE_SOFTWARE_CRYPTO)
    set(CRYPTO_REQUIRES "")
else()
    set(CRYPTO_REQUIRES "esp-cryptoauthlib")
endif()

file(TO_CMAKE_PATH "$ENV{IDF_PATH}" IDF_PATH_CMAKING)

idf_component_register(
    SRCS ${component_sources}
    INCLUDE_DIRS "${CMAKE_CURRENT_LIST_DIR}"
                 "${IDF_PATH_CMAKING}/components/mbedtls/mbedtls/tf-psa-crypto/include"
    REQUIRES arduino-esp32 nvs_flash toothPacket stateManager espHID rgbRMT mbedtls ${CRYPTO_REQUIRES}
)

if(CONFIG_TOOTHPASTE_SOFTWARE_CRYPTO)
    target_compile_definitions(${COMPONENT_LIB} PUBLIC USE_SOFTWARE_CRYPTO)
endif()