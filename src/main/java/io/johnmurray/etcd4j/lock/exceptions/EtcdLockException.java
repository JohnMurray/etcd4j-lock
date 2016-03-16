package io.johnmurray.etcd4j.lock.exceptions;

/**
 * Exceptions that get thrown for random lock related stuff.
 */
public class EtcdLockException extends RuntimeException {
    public EtcdLockException() {
        this(null, null);
    }

    public EtcdLockException(String message) {
        super(message);
    }

    public EtcdLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public EtcdLockException(Throwable cause) {
        super(cause);
    }

    public EtcdLockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
