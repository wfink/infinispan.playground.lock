package org.infinispan.wfink.playground.lock;

import java.io.Console;

import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.lock.EmbeddedClusteredLockManagerFactory;
import org.infinispan.manager.DefaultCacheManager;

/**
 * A simple embedded Infinispan applicaction to show how clustered locks are used. It is possible to set several locks with a timeout of 2 seconds. Even if it is possible to lock without timeouts this is not recommended as a thread or client can hang forever waiting on this lock! This instance can
 * be started several times, it will use JGroups UDP multicast to form a cluster. The IP address in use is shown as messages. If not the correct IP, or multicast is not working for it it can be set by <br>
 * mvn exec:java -Djgroups.bind_addr=127.0.0.1 <br>
 * or a vaild IP address.
 *
 * The Cache- and LockManager are created with the defaults.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public class ClusteredLockEmbeddedFactory extends AbstractEmbeddedClusteredLock {

  public ClusteredLockEmbeddedFactory(Console console) {
    super(console);

    GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();

    // Initialize 1 cache managers
    cacheManager = new DefaultCacheManager(global.build());

    // Initialize the clustered lock manager from the cache manager
    lockManager = EmbeddedClusteredLockManagerFactory.from(cacheManager);
  }

  public static void main(String[] args) {
    final Console con = System.console();

    con.printf("Locks with default Factory settigs\n");

    ClusteredLockEmbeddedFactory client = new ClusteredLockEmbeddedFactory(con);

    client.inputLoop();

    client.stop();
    System.out.println("\nDone !");
  }
}
