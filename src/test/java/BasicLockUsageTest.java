import io.johnmurray.etcd4j.lock.EtcdLock;
import org.junit.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Basic lock usage (acquire, release, renew, etc)
 */
public class BasicLockUsageTest extends TestBase {

    @Test public void acquireNamelessLock() {
        try(EtcdLock lock = new EtcdLock(TestUtil.createClient())) {
            assertThat(lock.acquire()).isTrue();
            assertThat(lock.getLockToken().getIndex()).isGreaterThan(0);
            assertThat(lock.release()).isTrue();
        }
    }

    @Test public void acquiredNamedLock() {
        try(EtcdLock lock = new EtcdLock(TestUtil.createClient())) {
            assertThat(lock.withName("test-acquired-name-lock").acquire()).isTrue();
            assertThat(lock.getLockToken().getIndex()).isGreaterThan(0);
            assertThat(lock.release()).isTrue();
        }
    }

    @Test public void acquireAndReleaseLockAndAcquireItAgain() {
        String name = "acquire-and-release-lock-and-acquire-it-again";
        try(EtcdLock lock = new EtcdLock(TestUtil.createClient())) {
            assertThat(lock.withName(name).acquire()).isTrue();
            assertThat(lock.getLockToken().getIndex()).isGreaterThan(0);
            assertThat(lock.release()).isTrue();
        }
        try(EtcdLock lock = new EtcdLock(TestUtil.createClient())) {
            assertThat(lock.withName(name).acquire()).isTrue();
        }
    }

    @Test public void acquireAndRenewLock() {
        try(EtcdLock lock = new EtcdLock(TestUtil.createClient())) {
            assertThat(lock.acquire()).isTrue();
            assertThat(lock.renew(Duration.ofMinutes(5))).isTrue();
            assertThat(lock.renew(Duration.ofMinutes(5))).isTrue();
            assertThat(lock.renew(Duration.ofMinutes(5))).isTrue();
            assertThat(lock.renew(Duration.ofMinutes(5))).isTrue();
            assertThat(lock.release()).isTrue();
        }
    }
}
