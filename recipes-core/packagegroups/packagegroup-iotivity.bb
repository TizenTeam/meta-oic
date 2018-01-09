DESCRIPTION = "IoTivity full package set including examples"
LICENSE = "Apache-2.0"

inherit packagegroup

PACKAGES = "\
    packagegroup-iotivity \
    "

ALLOW_EMPTY_${PN} = "1"

RDEPENDS_${PN} += "\
    iotivity \
    iotivity-bridging-plugins \
    iotivity-resource \
    iotivity-resource-samples \
    iotivity-service \
    iotivity-service-samples \
    iotivity-tests \
    "
