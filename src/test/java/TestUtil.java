import mousio.etcd4j.EtcdClient;

import java.lang.management.ManagementFactory;
import java.net.URI;

/**
 * Simple test-helper utility with various helper functions
 */
public class TestUtil {

    public static EtcdClient createClient() {
        return new EtcdClient(
                URI.create("http://127.0.0.1:12379"),
                URI.create("http://127.0.0.1:22379"),
                URI.create("http://127.0.0.1:32379")
        );
    }

    public static long currentThreadCount() {
        return ManagementFactory.getThreadMXBean().getThreadCount();
    }
}
