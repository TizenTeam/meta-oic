PKG_CONFIG?=pkg-config
override CPPFLAGS+=$(shell ${PKG_CONFIG} iotivity --cflags)
override LDLIBS+=$(shell ${PKG_CONFIG} iotivity --libs)
override CXXFLAGS+=-std=c++0x

all: simpleclient

clean:
	rm -rf simpleclient *.o
