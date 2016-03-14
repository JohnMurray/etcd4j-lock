package io.johnmurray.etcd4j.lock;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.requests.EtcdKeyPutRequest;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdVersionResponse;
import org.immutables.value.Value;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * The main class for creating and using a lock via etcd.
 *
 * TODO: use fencing tokens based on the node's index ID as the monotonically increasing number
 *       see: http://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html
 *       basically I think we just need to expose this number to the user and it's up to them to
 *       use it.
 */
public class EtcdLock implements AutoCloseable {

    private final long YEAR = 365;

    protected EtcdClient client;
    protected Duration lockTtl;
    protected String name;
    private Long lockIndex;

    private static Random rand = new Random(System.currentTimeMillis());

    public EtcdLock(EtcdClient client) throws EtcdLockConfigurationException {
        this.client = client;
        this.lockTtl = Duration.ofDays(YEAR);
        this.name = "EtcdLock_UnNamed_" + rand.nextLong() + ":" + rand.nextLong();

        // Sanity check to make sure we can connect to etcd
        EtcdVersionResponse version = this.client.version();
        if (version == null) {
            throw new EtcdLockConfigurationException("Could not communicate with etcd server to acquire version");
        }
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

    synchronized public boolean acquire() {
        String lockContent = "";
        EtcdKeysResponse response;
        try {
            response = client.put(this.name, lockContent)
                    .prevExist(false)
                    .ttl((int) this.lockTtl.getSeconds())
                    .send()
                    .get();
        } catch (IOException | EtcdException | TimeoutException | EtcdAuthenticationException ex) {
            // TODO: do some sort of logging to a default slf4j logger or some magic like other libs do
            ex.printStackTrace();
            return false;
        }
        this.lockIndex = response.node.modifiedIndex;
        return true;
    }

    /**
     * Monotonically increasing number that is granted for each lock. This is useful for
     * lock fencing.
     * @return
     */
    synchronized public Long lockToken() {
        if (lockIndex == null) {
            throw new RuntimeException("Lock token cannot be retrieved if no lock has been acquired");
        }
        return this.lockIndex;
    }

    /**
     * Refresh the current lock-lease
     * @return
     */
    synchronized public boolean renew(Duration amount) {
        EtcdKeyPutRequest request = client.put(this.name, null).ttl((int)amount.getSeconds());
        Map<String, String> requestParams = request.getRequestParams();
        if (requestParams.containsKey("value")) {
            requestParams.remove("value");
        }
        requestParams.put("prevExist", "true");
        requestParams.put("refresh", "true");
        try {
            request.send().get();
        } catch (EtcdException | EtcdAuthenticationException | IOException | TimeoutException e) {
            // TODO: do some sort of logging
            e.printStackTrace();
            return false;
        }
        return true;
    }

    synchronized public void release() {
        try {
            client.delete(this.name).prevIndex(this.lockIndex).send().get();
        } catch (IOException | EtcdException | EtcdAuthenticationException | TimeoutException e) {
            // TODO: do some sort of logging
        }
    }


    @Override
    public void close() throws Exception {
        client.close();
    }
}
