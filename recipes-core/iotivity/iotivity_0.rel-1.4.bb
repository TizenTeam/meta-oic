SUMMARY = "IoTivity framework and SDK sponsored by the Open Connectivity Foundation."
DESCRIPTION = "IoTivity is an open source software framework enabling seamless device-to-device connectivity to address the emerging needs of the Internet of Things."
HOMEPAGE = "https://www.iotivity.org/"
DEPENDS = "boost virtual/gettext chrpath-replacement-native expat openssl util-linux curl glib-2.0 glib-2.0-native"
DEPENDS += "sqlite3"

EXTRANATIVEPATH += "chrpath-native"
SECTION = "libs"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=86d3f3a95c324c9479bd8986968f4327"

branch_iotivity ?= "1.4-rel"
baseurl_iotivity ?= "git://github.com/iotivity/iotivity.git"
SRCREV_iotivity = "${branch_iotivity}"
SRCREV = "${SRCREV_iotivity}"
url_iotivity ?= "${baseurl_iotivity};destsuffix=${S};branch=${branch_iotivity};protocol=http"
SRC_URI = "${url_iotivity}"

url_tinycbor = "git://github.com/01org/tinycbor.git"
SRCREV_tinycbor = "3cdb9c890b785fac25e68e5db19961cfbf49aba3"
SRC_URI += "${url_tinycbor};name=tinycbor;destsuffix=${S}/extlibs/tinycbor/tinycbor;protocol=http"

url_gtest = "https://github.com/google/googletest/archive/release-1.7.0.zip"
SRC_URI[gtest.md5sum] = "ef5e700c8a0f3ee123e2e0209b8b4961"
SRC_URI[gtest.sha256sum] = "b58cb7547a28b2c718d1e38aee18a3659c9e3ff52440297e965f5edffe34b6d0"
SRC_URI += "${url_gtest};name=gtest;subdir=${BP}/extlibs/gtest"

url_hippomocks = "git://github.com/dascandy/hippomocks.git"
SRCREV_hippomocks = "dca4725496abb0e41f8b582dec21d124f830a8e5"
SRC_URI += "${url_hippomocks};name=hippomocks;destsuffix=${S}/extlibs/hippomocks/hippomocks;protocol=http"
SRC_URI += "file://hippomocks_mips_patch"

SRCREV_mbedtls = "85c2a928ed352845793db000e78e2b42c8dcf055"
url_mbedtls="git://github.com/ARMmbed/mbedtls.git"
SRC_URI += "${url_mbedtls};name=mbedtls;destsuffix=${S}/extlibs/mbedtls/mbedtls;protocol=http"

url_rapidjson = "git://github.com/miloyip/rapidjson.git"
SRCREV_rapidjson = "3d5848a7cd3367c5cb451c6493165b7745948308"
SRC_URI += "${url_rapidjson};name=rapidjson;;nobranch=1;destsuffix=${S}/extlibs/rapidjson/rapidjson;protocol=http"

branch_libcoap = "IoTivity-1.2.1d"
SRCREV_libcoap = "${branch_libcoap}"
url_libcoap = "git://github.com/dthaler/libcoap.git"
SRC_URI += "${url_libcoap};name=libcoap;destsuffix=${S}/extlibs/libcoap/libcoap;protocol=http;nobranch=1"

inherit pkgconfig scons


python () {
    IOTIVITY_TARGET_ARCH = d.getVar("TARGET_ARCH", True)
    d.setVar("IOTIVITY_TARGET_ARCH", IOTIVITY_TARGET_ARCH)
    EXTRA_OESCONS = d.getVar("EXTRA_OESCONS", True)
    EXTRA_OESCONS += " --prefix=${prefix}"
    EXTRA_OESCONS += " TARGET_OS=yocto TARGET_ARCH=" + IOTIVITY_TARGET_ARCH + " RELEASE=1"
    EXTRA_OESCONS += " VERBOSE=1"
    # Aligned to default configuration, but features can be changed here (at your own risk):
#   EXTRA_OESCONS += " -j1"
#   EXTRA_OESCONS += " ERROR_ON_WARN=False"
    # EXTRA_OESCONS += " ROUTING=GW"
    # EXTRA_OESCONS += " SECURED=0"
    # EXTRA_OESCONS += " TCP=1"
#    EXTRA_OESCONS += " TARGET_TRANRSPORT=IP"
#    EXTRA_OESCONS += " WITH_CLOUD=True"
#    EXTRA_OESCONS += " WITH_TCP=True"
#    EXTRA_OESCONS += " WITH_MQ=PUB,SUB"
    d.setVar("EXTRA_OESCONS", EXTRA_OESCONS)
}


IOTIVITY_BIN_DIR = "${libdir}/${PN}"
IOTIVITY_BIN_DIR_D = "${D}${IOTIVITY_BIN_DIR}"


do_compile_prepend() {
    export PKG_CONFIG_PATH="${PKG_CONFIG_PATH}"
    export PKG_CONFIG="PKG_CONFIG_SYSROOT_DIR=\"${PKG_CONFIG_SYSROOT_DIR}\" pkg-config"
    export LD_FLAGS="${LD_FLAGS}"
}

make_dir() {
    install -d $1
}

copy_file() {
    install -c -m 444 $1 $2
}

copy_exec() {
    install -c -m 555 $1 $2
}

copy_file_recursive() {
    cd $1 && find . -type d -exec install -d $2/"{}" \;
    cd $1 && find . -type f -exec install -c -m 444 "{}" $2/"{}" \;
}

copy_exec_recursive() {
    cd $1 && find . -executable -exec install -c -m 555 "{}" $2/"{}" \;
}


scon_do_install() {
    echo "TODO: fix https://git.yoctoproject.org/cgit.cgi/poky/plain/meta/classes/scons.bbclass"
    ${STAGING_BINDIR_NATIVE}/scons --install-sandbox=${D} ${EXTRA_OESCONS} install
}

do_install() {
    scon_do_install
    # TODO: Add conveinance link to support tests 
    # ln -fs ${D}${libdir}/iotivity/tests/service/resource-container/service/resource-container/unittests/libTestBundle.so ${D}${libdir}

    # TODO: Support legacy path (transitional, use pkg-config)
    ln -s iotivity/resource ${D}${includedir}/resource
    ln -s iotivity/service ${D}${includedir}/service
    ln -s iotivity/c_common ${D}${includedir}/c_common

    find "${D}" -type f -perm /u+x -exec chrpath -d "{}" \;
    find "${D}" -type f -iname "lib*.so" -exec chrpath -d "{}" \;
#TODO
    rm -rf ${D}/usr/src/debug/iotivity
}

#IOTIVITY packages:
#Resource: iotivity-resource, iotivity-resource-dev, iotivity-resource-thin-staticdev, iotivity-resource-dbg
#Resource Samples: iotivity-resource-samples, iotivity-resource-samples-dbg
#Service: iotivity-service, iotivity-service-dev, iotivity-service-staticdev, iotivity-service-dbg
#Service Samples: iotivity-service-samples, iotivity-service-samples-dbg
#Tests: iotivity-tests, iotivity-tests-dbg
#Misc: iotivity-tools

FILES_${PN}-tests-dbg = "\
        ${IOTIVITY_BIN_DIR}/tests/**/.debug \
        ${libdir}/.debug/libgtest.so \
        ${libdir}/.debug/libgtest_main.so"

FILES_${PN}-tests = "\
        ${IOTIVITY_BIN_DIR}/tests/** \
        ${libdir}/liboctbstack_test.so"

FILES_${PN}-resource-dev = "\
        ${includedir}/iotivity/resource \
        ${includedir}/iotivity/c_common \
        ${includedir}/resource \
        ${includedir}/c_common \
        ${libdir}/pkgconfig/iotivity.pc"

FILES_${PN}-resource-thin-staticdev = "\
        ${libdir}/libocsrm.a \
        ${libdir}/libconnectivity_abstraction*.a \
        ${libdir}/liboctbstack*.a \
        ${libdir}/libcoap.a \
        ${libdir}/libc_common.a \
        ${libdir}/libipca*.a \
        ${libdir}/libroutingmanager.a \
        ${libdir}/libtimer.a \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=0', '', '${libdir}/libocpmapi.a', d)}"

FILES_${PN}-plugins-staticdev = "\
        ${includedir}/iotivity/plugins \
        ${libdir}/libplugin_interface.a \
        ${libdir}/libzigbee_wrapper.a \
        ${libdir}/libtelegesis_wrapper.a"

FILES_${PN}-plugins-dbg = "\
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/plugins \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/bridging"

FILES_${PN}-resource = "\
        ${libdir}/libconnectivity_abstraction.so \
        ${libdir}/liboc.so \
        ${libdir}/liboctbstack.so \
        ${libdir}/liboc_logger.so \
        ${libdir}/liboc_logger_core.so \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=0', '', '${libdir}/libocprovision.so', d)} \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=0', '', '${libdir}/libocpmapi.so', d)} \
        ${libdir}/libresource_directory.so"

FILES_${PN}-resource-dbg = "\
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/resource \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/extlibs \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/examples \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/out \
        ${libdir}/.debug/liboc.so \
        ${libdir}/.debug/liboctbstack.so \
        ${libdir}/.debug/liboc_logger.so \
        ${libdir}/.debug/liboc_logger_core.so \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=0', '', '${libdir}/.debug/libocprovision.so', d)} \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=0', '', '${libdir}/.debug/libocpmapi.so', d)}"

FILES_${PN}-resource-samples-dbg = "\
        ${IOTIVITY_BIN_DIR}/resource/**/.debug\
        ${IOTIVITY_BIN_DIR}/examples/**/.debug"

FILES_${PN}-resource-samples = "\
        ${IOTIVITY_BIN_DIR}/resource/**\
        ${IOTIVITY_BIN_DIR}/examples/**"

FILES_${PN}-plugins-samples = "\
        ${IOTIVITY_BIN_DIR}/plugins/**"


FILES_${PN}-service-dbg = "\
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/service \
        ${libdir}/.debug"

FILES_${PN}-service-dev = "\
        ${includedir}/iotivity/service \
        ${includedir}/service"

FILES_${PN}-service = "\
        ${libdir}/lib*plugin.so \
        ${libdir}/libBMISensorBundle.so \
        ${libdir}/libDISensorBundle.so \
        ${libdir}/libESEnrolleeSDK.so \
        ${libdir}/libESMediatorRich.so \
        ${libdir}/libHueBundle.so \
        ${libdir}/libipca.so \
        ${libdir}/libnotification_*.so \
        ${libdir}/librcs_client.so \
        ${libdir}/librcs_common.so \
        ${libdir}/librcs_container.so \
        ${libdir}/librcs_server.so \
        ${libdir}/lib*.so "

FILES_${PN}-service-staticdev = "\
        ${libdir}/librcs_client.a \
        ${libdir}/librcs_server.a \
        ${libdir}/librcs_common.a \
        ${libdir}/librcs_container.a \
        ${libdir}/libresource_directory.a \
        ${libdir}/libscene_manager.a\
        ${libdir}/lib*.a "

FILES_${PN}-service-samples-dbg = "\
        ${IOTIVITY_BIN_DIR}/service/**.debug"

FILES_${PN}-service-samples = "\
        ${IOTIVITY_BIN_DIR}/service/**"

FILES_${PN}-tools = "\
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=0', '', '${IOTIVITY_BIN_DIR}/json2cbor', d)}"

PACKAGES = "${PN}-tests-dbg ${PN}-tests ${PN}-plugins-dbg ${PN}-plugins-staticdev ${PN}-plugins-samples-dbg ${PN}-plugins-samples ${PN}-resource-dbg ${PN}-resource ${PN}-resource-dev ${PN}-resource-thin-staticdev ${PN}-resource-samples-dbg ${PN}-resource-samples ${PN}-service-dbg ${PN}-service ${PN}-service-dev ${PN}-service-staticdev ${PN}-service-samples-dbg ${PN}-service-samples ${PN}-tools ${PN}-dev ${PN}"
ALLOW_EMPTY_${PN} = "1"
RDEPENDS_${PN} += "boost"
RRECOMMENDS_${PN} += "${PN}-resource ${PN}-service"
RRECOMMENDS_${PN}-dev += "${PN}-resource-dev ${PN}-resource-thin-staticdev ${PN}-plugins-staticdev ${PN}-service-dev ${PN}-service-staticdev"
RDEPENDS_${PN}-resource += "glib-2.0"
RRECOMMENDS_${PN}-plugins-staticdev += "${PN}-resource-dev ${PN}-resource-thin-staticdev ${PN}-resource"
RRECOMMENDS_${PN}-resource-thin-staticdev += "${PN}-resource-dev"
RRECOMMENDS_${PN}-service-dev += "${PN}-service ${PN}-service-staticdev ${PN}-resource"
RDEPENDS_${PN}-plugins-samples += "${PN}-resource glib-2.0"
RDEPENDS_${PN}-resource-samples += "${PN}-resource glib-2.0"
RDEPENDS_${PN}-tests += "${PN}-resource ${PN}-service glib-2.0"
RDEPENDS_${PN}-service-samples += "${PN}-service ${PN}-resource glib-2.0"
RDEPENDS_${PN}-service += "${PN}-resource glib-2.0"
RDEPENDS_${PN}-tools += "${PN}-resource"
BBCLASSEXTEND = "native nativesdk"

