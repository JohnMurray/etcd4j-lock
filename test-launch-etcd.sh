#!/bin/sh

# Copied from: https://github.com/jplana/python-etcd

if [  $# -gt 0 ]
    then
    ETCD_VERSION="$1";
    else
    ETCD_VERSION="master";
fi

echo "Using ETCD version $ETCD_VERSION"

if [[ ! -e etcd ]] ; then
  rm -rf etcd
  git clone --depth=1 https://github.com/coreos/etcd.git etcd
  cd etcd
  git checkout $ETCD_VERSION
  echo 'building etcd (may take a while)...'
  ./build
else
  cd etcd
fi

# setup go
if [[ ! $GOPATH ]] ; then
  echo 'Setting up GOPATH'
  export GOPATH='/tmp/gopath'
  mkdir -p /tmp/gopath
fi

# install goreman and run etcd
if [[ $TRAVIS ]] ; then
  echo 'installing goreman'
  go get -u github.com/mattn/goreman
  echo 'starting etcd'
  nohup $GOPATH/bin/goreman start >etcd.log 2>&1 &
  sleep 5
else
  echo 'starting etcd'
  $GOPATH/bin/goreman start
fi

${TRAVIS:?"This is not a Travis build. All Done"}
#Temporal solution to travis issue #155
sudo rm -rf /dev/shm && sudo ln -s /run/shm /dev/shm
echo "All Done"
