package io.johnmurray.etcd4j.lock;

import io.johnmurray.etcd4j.lock.exceptions.EtcdDirtyLockException;
import io.johnmurray.etcd4j.lock.exceptions.EtcdLockException;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.requests.EtcdKeyPutRequest;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * The main class for creating and using a lock via etcd.
 *
 * TODO: use fencing tokens based on the node's index ID as the monotonically increasing number
 *       see: http://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html
 *       basically I think we just need to expose this number to the user and it's up to them to
 *       use it.
 */
public class EtcdLock implements AutoCloseable {

    private static final long YEAR = 365;
    private static final Random RAND = new Random(System.currentTimeMillis());

    protected EtcdClient client;
    protected Duration lockTtl;
    protected String name;

    // todo: track the modified index to make sure only _we_ are modifying the value
    // todo: throw dirty-lock exceptions if the index changes
    // same as the create-index for the etcd node
    private Long lockCreateIndex;
    private long lockModifiedIndex;
    private boolean lockHeld;
    private Logger logger;


    public EtcdLock(EtcdClient client) {
        this.client = client;
        this.lockTtl = Duration.ofDays(YEAR);
        this.name = "EtcdLock_UnNamed_" + RAND.nextLong() + ":" + RAND.nextLong();
        this.logger = LoggerFactory.getLogger("etcd-lock");
    }

    public EtcdLock withName(String name) {
        this.name = name;
        return this;
    }

    public EtcdLock withLockTtl(Duration lockTtl) {
        this.lockTtl = lockTtl;
        return this;
    }



    //
    // LOCK FUNCTIONS
    //

    // TODO 'acquire' should return 'this' or throw. Then I can use it in an ARM block
    /**
     * Attempt to acquire a lock.
     *
     * @return boolean indicating success
     */
    synchronized public boolean acquire() {
        String lockContent = "";
        EtcdKeysResponse response;
        try {
            response = client.put(name, lockContent)
                    .prevExist(false)
                    .ttl((int) lockTtl.getSeconds())
                    .send()
                    .get();
            lockCreateIndex = response.node.modifiedIndex;
            lockModifiedIndex = response.node.createdIndex;
            lockHeld = true;
        } catch (IOException | EtcdException | TimeoutException | EtcdAuthenticationException ex) {
            logger.error("Error encountered when attempting to acquire lock", ex);
            lockHeld = false;
        }
        return lockHeld;
    }

    /**
     * Monotonically increasing number that is granted for each lock. This is useful for
     * lock fencing. May be used with {{getLockName()}} for fencing.
     *
     * @see this.getLockName()
     */
    synchronized public Long getLockToken() {
        if (! lockHeld || lockCreateIndex == null) {
            throw new RuntimeException("Lock token cannot be retrieved if no lock has been acquired");
        }
        return lockCreateIndex;
    }

    /**
     * Get the current lock name. This may have been auto-generated if not user-supplied. May be used
     * with {{getLockToken()}} for fencing.
     *
     * @see this.getLockToken()
     */
    synchronized public String getLockName() {
        return name;
    }

    /**
     * Refresh the current lock-lease.
     *
     * @return boolean indicating success
     * @throws EtcdLockException If no lock is held
     */
    synchronized public boolean renew(Duration amount) throws EtcdLockException {
        // validate that we have a lock
        if (! lockHeld) {
            throw new EtcdLockException("Lock cannot be released unless first acquired");
        }

        EtcdKeyPutRequest request = client.put(this.name, null)
                .prevIndex(lockModifiedIndex)
                .prevExist(true)
                .ttl((int)amount.getSeconds());
        Map<String, String> requestParams = request.getRequestParams();
        if (requestParams.containsKey("value")) {
            requestParams.remove("value");
        }
        requestParams.put("refresh", "true");
        try {
            EtcdKeysResponse resp = request.send().get();
            lockModifiedIndex = resp.node.modifiedIndex;
        } catch (EtcdAuthenticationException | IOException | TimeoutException ex) {
            logger.error("Lock could not be renewed", ex);
            return false;
        } catch (EtcdException ex) {
            handleDirtyLock(ex);
            logger.error("Lock could not be renewed", ex);
        }
        return true;
    }

    /**
     * Release the currently held lock.
     *
     * @return boolean indicating success
     * @throws EtcdLockException If no lock is held
     */
    synchronized public boolean release() throws EtcdLockException {
        // validate that we have a lock
        if (! lockHeld) {
            throw new EtcdLockException("Lock cannot be released unless first acquired");
        }

        // attempt to release the lock
        try {
            client.delete(name).prevIndex(lockModifiedIndex).send().get();
            lockHeld = false;
            return true;
        } catch (IOException | TimeoutException | EtcdAuthenticationException  e) {
            logger.error("Lock could not be released", e);
        } catch (EtcdException e) {
            handleDirtyLock(e);
            logger.error("Lock could not be released", e);
        }
        return false;
    }


    @Override
    public void close() {
        // attempt to close the connection
        try {
            if (lockHeld) {
                release();
            }
        } catch (EtcdLockException ex) { }

        // close the Etcd connection
        try {
            client.close();
        } catch (IOException ex) {
            logger.error("Could not close client", ex);
        }
    }


    /**
     * Check an {{EtcdLockException}} to make sure the error thrown is not integrity related. If it is,
     * raise an {{EtcdDirtyLockException}}.
     *
     * @throws io.johnmurray.etcd4j.lock.exceptions.EtcdDirtyLockException
     */
    private void handleDirtyLock(EtcdException ex) throws EtcdDirtyLockException {
        // compare failed
        if (ex.errorCode == 101) {
            throw new EtcdDirtyLockException("Lock has been tampered with", ex);
        }
    }
}
