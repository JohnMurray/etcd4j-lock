#!/bin/sh

# Copied from: https://github.com/jplana/python-etcd

if [  $# -gt 0 ]
    then
    ETCD_VERSION="$1";
    else
    ETCD_VERSION="master";
fi

echo "Using ETCD version $ETCD_VERSION"

git clone https://github.com/coreos/etcd.git
cd etcd
git checkout $ETCD_VERSION
./build

# setup go
export GOPATH='/tmp/gopath'
mkdir -p /tmp/gopath

# install goreman and run etcd
go get -u github.com/mattn/goreman
nohup $GOPATH/bin/goreman start >etcd.log 2>&1 &
sleep 5

${TRAVIS:?"This is not a Travis build. All Done"}
#Temporal solution to travis issue #155
sudo rm -rf /dev/shm && sudo ln -s /run/shm /dev/shm
echo "All Done"
