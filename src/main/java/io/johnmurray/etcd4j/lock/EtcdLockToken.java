package io.johnmurray.etcd4j.lock;

import org.immutables.value.Value;

/**
 * Simple struct to hold the lock token (useful for fencing)
 */
@Value.Immutable
public interface EtcdLockToken {
    abstract String getName();
    abstract Long getIndex();
}
