package io.johnmurray.etcd4j.lock.exceptions;

/**
 * Exception to be thrown when the integrity of the lock can no longer be trusted.
 */
public class EtcdDirtyLockException extends EtcdLockException {
    public EtcdDirtyLockException() {
        super();
    }

    public EtcdDirtyLockException(String message) {
        super(message);
    }

    public EtcdDirtyLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public EtcdDirtyLockException(Throwable cause) {
        super(cause);
    }

    public EtcdDirtyLockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
