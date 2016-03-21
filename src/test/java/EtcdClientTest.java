import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Some base-tests that gives us some basic guarantees about our client.
 */
public class EtcdClientTest extends TestBase {

    /**
     * Test that the client does not have a thread-leak
     *
     * Open Bug: https://github.com/jurmous/etcd4j/issues/84
     */
    @Ignore
    @Test public void testEtcdClientClosing() throws IOException, EtcdAuthenticationException, TimeoutException, EtcdException {
        long beforeThreadCount = TestUtil.currentThreadCount();

        for (int i = 0; i < 100; i++) {
            try(EtcdClient client = TestUtil.createClient()) {
                client.put("test-" + i, "value-" + i).send().get();
                client.get("test-" + i).send().get();
            } // should close automatically
        }

        long afterThreadCount = TestUtil.currentThreadCount();
        assertThat((double)afterThreadCount).isBetween(beforeThreadCount * 0.75, beforeThreadCount * 1.25);
    }

    /**
     * Test that the client can be shared amongst multiple threads
     *
     * Open Bug: https://github.com/jurmous/etcd4j/issues/85
     */
    @Ignore
    @Test public void testSharedClient() throws Exception{
        EtcdClient client = new EtcdClient(
                URI.create("http://127.0.0.1:12379"),
                URI.create("http://127.0.0.1:22379"),
                URI.create("http://127.0.0.1:32379")
        );
        for (int i = 0; i < 10_000; i++) {
            Thread t = new Thread(() -> {
                try {
                    client.put("test", "test").ttl(3600).send().get();
                    client.get("test").send().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.start();
            client.put("test-2", "test-2").ttl(3600).send().get();
            t.join();
        }
    }
}
