import io.johnmurray.etcd4j.lock.EtcdLock;
import io.johnmurray.etcd4j.lock.exceptions.EtcdDirtyLockException;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Test to ensure the integrity of the lock if something goes wrong with etcd or the data
 * is tampered with outside of etcd-lock.
 */
public class LockSafetyTest extends TestBase {

    @Test(expected = EtcdDirtyLockException.class)
    public void testLockEditedDuringUse() throws IOException, EtcdAuthenticationException, TimeoutException, EtcdException {
        try(EtcdLock lock = new EtcdLock(TestUtil.createClient())) {
            assertThat(lock.acquire()).isTrue();

            EtcdClient client = TestUtil.createClient();
            client.put(lock.getLockName(), "custom-value").send().get();

            assertThat(lock.renew(Duration.ofMinutes(5))).isFalse();
        }
    }

    @Test public void testDirtyLockIsCatchable() throws IOException, EtcdAuthenticationException, TimeoutException, EtcdException {
        try(EtcdLock lock = new EtcdLock(TestUtil.createClient())) {
            assertThat(lock.acquire()).isTrue();

            EtcdClient client = TestUtil.createClient();
            client.put(lock.getLockName(), "custom-value").send().get();

            assertThat(lock.renew(Duration.ofMinutes(5))).isFalse();
        } catch (EtcdDirtyLockException e) {
            // rollback transaction or whatever
            return;
        }
        fail("should have thrown and caught exception and exited");
    }

    @Test public void multiThreadedRenewIsNotDirty() throws InterruptedException {
        // not sure a great way to test this other than to test it repeatedly
        for (int i = 0; i < 100; i++) {
            try (EtcdLock lock = new EtcdLock(TestUtil.createClient())) {
                assertThat(lock.acquire()).isTrue();
                Thread t = new Thread(() -> lock.renew(Duration.ofMinutes(5)));
                t.start();
                assertThat(lock.renew(Duration.ofMinutes(5))).isTrue();
                t.join(3_000);
            } catch (EtcdDirtyLockException e) {
                fail("should not be dirty during multi-threaded renew");
            }
        }
    }

}
