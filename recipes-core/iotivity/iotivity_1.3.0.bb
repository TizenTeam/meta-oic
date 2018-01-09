#TODO
PR = "r0"
SUMMARY = "IoTivity framework and SDK sponsored by the Open Connectivity Foundation."
DESCRIPTION = "IoTivity is an open source software framework enabling seamless device-to-device connectivity to address the emerging needs of the Internet of Things."
HOMEPAGE = "https://www.iotivity.org/"
DEPENDS = "boost virtual/gettext chrpath-replacement-native expat openssl util-linux curl glib-2.0 glib-2.0-native"
DEPENDS += "sqlite3"

EXTRANATIVEPATH += "chrpath-native"
SECTION = "libs"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=22bf216f3077c279aed7b36b1fa9e6d1"

branch_iotivity = "1.3-rel"
SRC_URI = "git://github.com/iotivity/iotivity.git;destsuffix=${S};branch=${branch_iotivity};protocol=http"
#{ TODO
#branch_iotivity = "master"
SRCREV = "${branch_iotivity}"
#} TODO

url_tinycbor = "git://github.com/01org/tinycbor.git"
SRCREV_tinycbor = "31c7f81d45d115d2007b1c881cbbd3a19618465c"
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

branch_libcoap = "IoTivity-1.2.1d"
SRCREV_libcoap = "${branch_libcoap}"
url_libcoap = "git://github.com/dthaler/libcoap.git"
SRC_URI += "${url_libcoap};name=libcoap;destsuffix=${S}/extlibs/libcoap/libcoap;protocol=http;nobranch=1"

url_rapidjson = "https://github.com/miloyip/rapidjson/archive/v1.0.2.zip"
SRC_URI += "${url_rapidjson};name=rapidjson;subdir=${BP}/extlibs/rapidjson"
SRC_URI[rapidjson.md5sum] = "446a0673d58766e507d641412988dcaa"
SRC_URI[rapidjson.sha256sum] = "69e876bd07670189214f44475add2e0afb8374e5798270208488c973a95f501d"

inherit pkgconfig scons


python () {
    IOTIVITY_TARGET_ARCH = d.getVar("TARGET_ARCH", True)
    d.setVar("IOTIVITY_TARGET_ARCH", IOTIVITY_TARGET_ARCH)
    EXTRA_OESCONS = d.getVar("EXTRA_OESCONS", True)
    EXTRA_OESCONS += " TARGET_OS=yocto TARGET_ARCH=" + IOTIVITY_TARGET_ARCH + " RELEASE=1"
    EXTRA_OESCONS += " VERBOSE=1"
    # Aligned to default configuration, but features can be changed here (at your own risk):
    # EXTRA_OESCONS += " -j1"
    # EXTRA_OESCONS += " ROUTING=GW"
    # EXTRA_OESCONS += " SECURED=1"
    # EXTRA_OESCONS += " WITH_TCP=1"
    EXTRA_OESCONS += " WITH_EXAMPLES=yes"
    EXTRA_OESCONS += " WITH_UNIT_TESTS=yes"
    d.setVar("EXTRA_OESCONS", EXTRA_OESCONS)
}


IOTIVITY_BIN_DIR = "/opt/${PN}"
IOTIVITY_BIN_DIR_D = "${D}${IOTIVITY_BIN_DIR}"

do_compile_prepend() {
    export PKG_CONFIG_PATH="${PKG_CONFIG_PATH}"
    export PKG_CONFIG="PKG_CONFIG_SYSROOT_DIR=\"${PKG_CONFIG_SYSROOT_DIR}\" pkg-config"
    export LD_FLAGS="${LD_FLAGS}"
}

do_install() {
    scons DESTDIR=${D} PREFIX=${prefix} ${EXTRA_OESCONS} install

    # XXX: Compatibility links; remove those eventually
    ln -s iotivity/resource ${D}${includedir}/resource
    ln -s iotivity/service ${D}${includedir}/service
    ln -s iotivity/c_common ${D}${includedir}/c_common

    # Change runpath of shared objects
    find ${D} -type f -iname "lib*.so" -exec chrpath -d "{}" \;
    find ${D}${bindir} -type f -executable -exec chrpath -d "{}" \;

    # Remove undesired libraries
    rm -f ${D}${libdir}/liboc_logger_internal.a
    rm -f ${D}${libdir}/liboctbstack_internal.a
    rm -f ${D}${libdir}/libconnectivity_abstraction_internal.a
    rm -f ${D}${libdir}/libmpmcommon.a
    rm -f ${D}${libdir}/libocpmapi_internal.a
    rm -f ${D}${libdir}/libocpmapi_internal.so
}

#IOTIVITY packages:
#Core: iotivity-dev
#Bridging: iotivity-bridging-dev, iotivity-bridging-staticdev,
#Bridging: iotivity-bridging-plugins, iotivity-bridging-dbg
#Bridging: iotivity-bridging-plugins-dbg
#Resource: iotivity-resource, iotivity-resource-dev, iotivity-resource-thin-staticdev, iotivity-resource-dbg
#Service: iotivity-service, iotivity-service-dev, iotivity-service-staticdev, iotivity-service-dbg
#Service Samples: iotivity-service-samples, iotivity-service-samples-dbg
#Tests: iotivity-tests, iotivity-tests-dbg

FILES_${PN}-dev = "\
        ${libdir}/pkgconfig/iotivity.pc"

FILES_${PN}-bridging-dbg = "\
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/bridging"

FILES_${PN}-bridging-dev = "\
        ${includedir}/iotivity/bridging"

FILES_${PN}-bridging-staticdev = "\
        ${libdir}/libminipluginmanager.a"

FILES_${PN}-bridging-plugins-dbg = "\
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/bridging/plugins/hue_plugin \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/bridging/plugins/lifx_plugin \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/bridging/plugins/lyric_plugin \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/bridging/plugins/nest_plugin \
        ${libdir}/.debug/libhueplugin.so \
        ${libdir}/.debug/liblifxplugin.so \
        ${libdir}/.debug/liblyricplugin.so \
        ${libdir}/.debug/libnestplugin.so"

FILES_${PN}-bridging-plugins = "\
        ${libdir}/libhueplugin.so \
        ${libdir}/liblifxplugin.so \
        ${libdir}/liblyricplugin.so \
        ${libdir}/libnestplugin.so"

FILES_${PN}-resource-dev = "\
        ${includedir}/iotivity/resource \
        ${includedir}/iotivity/c_common \
        ${includedir}/iotivity/extlibs \
        ${includedir}/resource \
        ${includedir}/c_common"

FILES_${PN}-resource-thin-staticdev = "\
        ${libdir}/libocsrm.a \
        ${libdir}/libconnectivity_abstraction.a \
        ${libdir}/liboctbstack.a \
        ${libdir}/libcoap.a \
        ${libdir}/libc_common.a \
        ${libdir}/libroutingmanager.a \
        ${libdir}/libtimer.a \
        ${libdir}/libipca_static.a \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=1', '${libdir}/libocpmapi.a', '', d)}"

FILES_${PN}-plugins-staticdev = "\
        ${includedir}/iotivity/plugins \
        ${libdir}/libplugin_interface.a \
        ${libdir}/libzigbee_wrapper.a \
        ${libdir}/libtelegesis_wrapper.a"

FILES_${PN}-resource-dbg = "\
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/resource \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/extlibs \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/examples \
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/out \
        ${libdir}/.debug/libconnectivity_abstraction.so \
        ${libdir}/.debug/liboc.so \
        ${libdir}/.debug/liboctbstack.so \
        ${libdir}/.debug/liboc_logger.so \
        ${libdir}/.debug/liboc_logger_core.so \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=1', '${libdir}/.debug/libocprovision.so', '', d)} \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=1', '${libdir}/.debug/libocpmapi.so', '', d)} \
        ${libdir}/.debug/libresource_directory.so \
        ${libdir}/.debug/libipca.so"

FILES_${PN}-resource = "\
        ${libdir}/libconnectivity_abstraction.so \
        ${libdir}/liboc.so \
        ${libdir}/liboctbstack.so \
        ${libdir}/liboc_logger.so \
        ${libdir}/liboc_logger_core.so \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=1', '${libdir}/libocprovision.so', '', d)} \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=1', '${libdir}/libocpmapi.so', '', d)} \
        ${libdir}/libresource_directory.so \
        ${libdir}/libipca.so"

FILES_${PN}-resource-samples-dbg = "\
        ${bindir}/.debug/directpairingclient \
        ${bindir}/.debug/presenceclient \
        ${bindir}/.debug/presenceserver \
        ${bindir}/.debug/groupclient \
        ${bindir}/.debug/groupserver \
        ${bindir}/.debug/rdclient \
        ${bindir}/.debug/roomclient \
        ${bindir}/.debug/roomserver \
        ${bindir}/.debug/simpleclient \
        ${bindir}/.debug/simpleclientserver \
        ${bindir}/.debug/simpleserver \
        ${bindir}/.debug/fridgeclient \
        ${bindir}/.debug/fridgeserver \
        ${bindir}/.debug/garageclient \
        ${bindir}/.debug/garageserver \
        ${bindir}/.debug/simpleclientHQ \
        ${bindir}/.debug/simpleserverHQ \
        ${bindir}/.debug/devicediscoveryserver \
        ${bindir}/.debug/devicediscoveryclient \
        ${bindir}/.debug/threadingsample \
        ${bindir}/.debug/lightserver \
        ${bindir}/.debug/OICMiddle"

FILES_${PN}-resource-samples = "\
        ${bindir}/directpairingclient \
        ${bindir}/presenceclient \
        ${bindir}/presenceserver \
        ${bindir}/groupclient \
        ${bindir}/groupserver \
        ${bindir}/rdclient \
        ${bindir}/roomclient \
        ${bindir}/roomserver \
        ${bindir}/simpleclient \
        ${bindir}/simpleclientserver \
        ${bindir}/simpleserver \
        ${bindir}/fridgeclient \
        ${bindir}/fridgeserver \
        ${bindir}/garageclient \
        ${bindir}/garageserver \
        ${bindir}/simpleclientHQ \
        ${bindir}/simpleserverHQ \
        ${bindir}/devicediscoveryserver \
        ${bindir}/devicediscoveryclient \
        ${bindir}/threadingsample \
        ${bindir}/lightserver \
        ${bindir}/OICMiddle"

FILES_${PN}-service-dbg = "\
        ${prefix}/src/debug/${PN}/${EXTENDPE}${PV}-${PR}/${PN}-${PV}/service \
        ${libdir}/.debug/libBMISensorBundle.so \
        ${libdir}/.debug/libDISensorBundle.so \
        ${libdir}/.debug/librcs_server.so \
        ${libdir}/.debug/librcs_common.so \
        ${libdir}/.debug/librcs_container.so \
        ${libdir}/.debug/libHueBundle.so \
        ${libdir}/.debug/libESEnrolleeSDK.so \
        ${libdir}/.debug/libESMediatorRich.so \
        ${libdir}/.debug/libnotification_consumer.so \
        ${libdir}/.debug/libnotification_provider.so \
        ${libdir}/.debug/librcs_client.so \
        ${libdir}/.debug/libTestBundle.so \
        ${libdir}/.debug/libnotification_consumer_wrapper.so \
        ${libdir}/.debug/libnotification_provider_wrapper.so"

FILES_${PN}-service = "\
        ${libdir}/libBMISensorBundle.so \
        ${libdir}/libDISensorBundle.so \
        ${libdir}/librcs_server.so \
        ${libdir}/librcs_common.so \
        ${libdir}/librcs_container.so \
        ${libdir}/libHueBundle.so \
        ${libdir}/libESEnrolleeSDK.so \
        ${libdir}/libESMediatorRich.so \
        ${libdir}/libnotification_consumer.so \
        ${libdir}/libnotification_provider.so \
        ${libdir}/librcs_client.so \
        ${libdir}/libTestBundle.so \
        ${libdir}/libnotification_consumer_wrapper.so \
        ${libdir}/libnotification_provider_wrapper.so"

FILES_${PN}-service-dev = "\
        ${includedir}/iotivity/service \
        ${includedir}/service"

FILES_${PN}-service-staticdev = "\
        ${libdir}/librcs_client.a \
        ${libdir}/librcs_server.a \
        ${libdir}/librcs_common.a \
        ${libdir}/librcs_container.a \
        ${libdir}/libresource_directory.a \
        ${libdir}/libscene_manager.a \
        ${libdir}/libnotification_consumer.a \
        ${libdir}/libnotification_consumer_wrapper.a \
        ${libdir}/libnotification_provider.a \
        ${libdir}/libnotification_provider_wrapper.a \
        ${libdir}/libESEnrolleeSDK.a \
        ${@bb.utils.contains('EXTRA_OESCONS', 'SECURED=1', '${libdir}/libcoap_http_proxy.a', '', d)}"


FILES_${PN}-service-samples-dbg = "\
        ${bindir}/.debug/ContainerSample \
        ${bindir}/.debug/ContainerSampleClient \
        ${bindir}/.debug/fanserver \
        ${bindir}/.debug/HeightSensorApp \
        ${bindir}/.debug/mediator \
        ${bindir}/.debug/sceneserver \
        ${bindir}/.debug/submediator \
        ${bindir}/.debug/THSensorApp \
        ${bindir}/.debug/THSensorApp1 \
        ${bindir}/.debug/WeightSensorApp"

FILES_${PN}-service-samples = "\
        ${bindir}/ContainerSample \
        ${bindir}/ContainerSampleClient \
        ${bindir}/fanserver \
        ${bindir}/HeightSensorApp \
        ${bindir}/mediator \
        ${bindir}/sceneserver \
        ${bindir}/submediator \
        ${bindir}/THSensorApp \
        ${bindir}/THSensorApp1 \
        ${bindir}/WeightSensorApp"

FILES_${PN}-tests-dbg = "\
        ${libdir}/.debug/libgtest.so \
        ${libdir}/.debug/libgtest_main.so \
        ${bindir}/.debug/broker_test \
        ${bindir}/.debug/cache_test \
        ${bindir}/.debug/catests \
        ${bindir}/.debug/cbortests \
        ${bindir}/.debug/container_test \
        ${bindir}/.debug/easysetup_mediator_test \
        ${bindir}/.debug/examples_c \
        ${bindir}/.debug/examples_cpp \
        ${bindir}/.debug/rcs_client_test \
        ${bindir}/.debug/rcs_common_test \
        ${bindir}/.debug/rcs_server_test \
        ${bindir}/.debug/randomtests \
        ${bindir}/.debug/remote_scene_action_test \
        ${bindir}/.debug/remote_scene_col_test \
        ${bindir}/.debug/remote_scene_list_test \
        ${bindir}/.debug/remote_scene_test \
        ${bindir}/.debug/scene_action_test \
        ${bindir}/.debug/scene_collection_test \
        ${bindir}/.debug/scene_list_test \
        ${bindir}/.debug/scene_test \
        ${bindir}/.debug/stacktests"

FILES_${PN}-tests = "\
        ${bindir}/broker_test \
        ${bindir}/cache_test \
        ${bindir}/catests \
        ${bindir}/cbortests \
        ${bindir}/container_test \
        ${bindir}/easysetup_mediator_test \
        ${bindir}/examples_c \
        ${bindir}/examples_cpp \
        ${bindir}/rcs_client_test \
        ${bindir}/rcs_common_test \
        ${bindir}/rcs_server_test \
        ${bindir}/randomtests \
        ${bindir}/remote_scene_action_test \
        ${bindir}/remote_scene_col_test \
        ${bindir}/remote_scene_list_test \
        ${bindir}/remote_scene_test \
        ${bindir}/scene_action_test \
        ${bindir}/scene_collection_test \
        ${bindir}/scene_list_test \
        ${bindir}/scene_test \
        ${bindir}/stacktests"

PACKAGES = "\
        ${PN} \
        ${PN}-dev \
        ${PN}-tests-dbg \
        ${PN}-tests \
        ${PN}-bridging-plugins-dbg \
        ${PN}-bridging-plugins \
        ${PN}-bridging-dbg \
        ${PN}-bridging-dev \
        ${PN}-bridging-staticdev \
        ${PN}-plugins-staticdev \
        ${PN}-resource-dbg \
        ${PN}-resource \
        ${PN}-resource-dev \
        ${PN}-resource-thin-staticdev \
        ${PN}-resource-samples-dbg \
        ${PN}-resource-samples \
        ${PN}-service-dbg \
        ${PN}-service \
        ${PN}-service-dev \
        ${PN}-service-staticdev \
        ${PN}-service-samples-dbg \
        ${PN}-service-samples"

NOAUTOPACKAGEDEBUG = "1"
ALLOW_EMPTY_${PN} = "1"
RDEPENDS_${PN} += "boost"
RRECOMMENDS_${PN} += "${PN}-bridging-plugins ${PN}-resource ${PN}-service"
RRECOMMENDS_${PN}-dev += "${PN}-bridging-dev ${PN}-bridging-staticdev ${PN}-resource-dev ${PN}-resource-thin-staticdev ${PN}-plugins-staticdev ${PN}-service-dev ${PN}-service-staticdev"
RDEPENDS_${PN}-resource += "glib-2.0"
RRECOMMENDS_${PN}-plugins-staticdev += "${PN}-resource-dev ${PN}-resource-thin-staticdev ${PN}-resource"
RRECOMMENDS_${PN}-resource-thin-staticdev += "${PN}-resource-dev"
RRECOMMENDS_${PN}-service-dev += "${PN}-service ${PN}-service-staticdev ${PN}-resource"
RDEPENDS_${PN}-bridging-staticdev += "${PN}-bridging-dev"
RDEPENDS_${PN}-bridging-plugins += "${PN}-resource"
RDEPENDS_${PN}-resource-samples += "${PN}-resource glib-2.0"
RDEPENDS_${PN}-tests += "${PN}-resource ${PN}-service glib-2.0"
RDEPENDS_${PN}-service-samples += "${PN}-service ${PN}-resource glib-2.0"
RDEPENDS_${PN}-service += "${PN}-resource glib-2.0"
BBCLASSEXTEND = "native nativesdk"
