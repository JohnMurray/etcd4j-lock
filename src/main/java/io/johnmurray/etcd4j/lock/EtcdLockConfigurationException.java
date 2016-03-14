package io.johnmurray.etcd4j.lock;

/**
 * DESCRIPTION
 */
public class EtcdLockConfigurationException extends Exception {

    public EtcdLockConfigurationException() {
        this(null, null);
    }

    public EtcdLockConfigurationException(String message) {
        super(message);
    }

    public EtcdLockConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EtcdLockConfigurationException(Throwable cause) {
        super(cause);
    }

    public EtcdLockConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
