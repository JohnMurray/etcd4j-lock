# etcd4j-lock

[![Build Status](https://travis-ci.org/JohnMurray/etcd4j-lock.svg?branch=master)](https://travis-ci.org/JohnMurray/etcd4j-lock)
[![Maven Central](https://img.shields.io/maven-central/v/io.johnmurray/etcd4j-lock.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.johnmurray%22%20AND%20a%3A%22etcd4j-lock%22)

A simple locking implementation built on top of [etcd4j](https://github.com/jurmous/etcd4j).

## Installing

Currently this library is targetted to Java 8. I do not have plans at the moment to target anyting lower
than this.

```xml
<dependency>
  <groupId>io.johnmurray</groupId>
  <artifactId>etcd4j-lock</artifactId>
  <version>0.1</version>
</dependency>
```

## Simple Usage

Acquire simple lock (no timeout)

```java
try( EtcdLock lock = new EtcdLock(new EtcdClient()) ) {
  lock.acquire();

  // perform critical operations here

}
// lock released in ARM block (automatically after closing brace)
```

However this isn't very useful if your lock does not have a name (a unique name will be generated for you). Instead
let's assume that we're updating a customer, we can name our lock appropriately.

```java
UUID customerId = ...;

try( EtcdLock lock = new EtcdLock(new EtcdClient()) ) {
  lock.withName("update_customer_" + customerId.toString()).acquire();

  // perform critical operations here

}
```

Another useful feature to take advantage of during distributed locks is a lock-lease. If your process dies while you
have the lock, you want to make sure that it can be made free after some time limit. Using the same example, let's
see how we can define a lease on our lock.

```java
UUID customerId = ...;

try( EtcdLock lock = new EtcdLock(new EtcdClient()) ) {
  lock
      .withName("update_customer_" + customerId.toString())
      .withLockTtl(Duration.ofMinutes(3))
      .acquire();

  // perform critical operations here

}
```

Now if our execution time exceeds 3 minutes, we can assume something bad has happened and release the lock. Of
course you will also need to make use of proper [request fencing][fencing] when using this feature, but I assume you
know distributed locks come with their drawbacks and are ready for this. On that note, you can generate a fencing
token that is unique for your lock with:

```java
try( EtcdLock lock = new EtcdLock(new EtcdClient()) ) {
  lock
      .withName("update_customer_" + customerId.toString())
      .withLockTtl(Duration.ofMinutes(3))
      .acquire();

  EtcdLockToken fencingToken = lock.getLockToken();

  // perform critical operations here, pass token to downstream systems

}
```

The token is a simple structure containing the lock name and the lock index (atomically increasing number). These two
values can be used to properly implement downstream fencing. Note that `EtcdLockToken` is returned as an
`ImmutableEtcdLockToken` and you are free to share the token value in your program (thread safe) as it cannot be
mutated.

If you are running a long-lived process and need to periodically renew your lease, you can do that as well.

```java
try( EtcdLock lock = new EtcdLock(new EtcdClient()) ) {
  lock
      .withName("update_customer_" + customerId.toString())
      .withLockTtl(Duration.ofMinutes(3))
      .acquire();

  // perform critical operations here, pass token to downstream systems
  // if getting close to timeout, or on some interval, you can renew your lease
  lock.renew(Duration.ofMinutes(3))

}
// lock still automatically released irrespective of lease renewal
```



## Open Features

- [x] Dead simple lock
- [ ] Retry lock acquire attempts (`withRetries(N)`)
- [ ] Acquire attempt timeout (`withAcquireTimeout(Duration)`)
- [ ] Reader/Writer lock
- [ ] Other lock types??



  [fencing]: https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html#making-the-lock-safe-with-fencing
