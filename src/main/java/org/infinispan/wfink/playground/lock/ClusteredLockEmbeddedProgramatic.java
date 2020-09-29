package org.infinispan.wfink.playground.lock;

import java.io.Console;

import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.lock.EmbeddedClusteredLockManagerFactory;
import org.infinispan.lock.configuration.ClusteredLockManagerConfiguration;
import org.infinispan.lock.configuration.ClusteredLockManagerConfigurationBuilder;
import org.infinispan.lock.configuration.Reliability;
import org.infinispan.manager.DefaultCacheManager;

/**
 * A simple embedded Infinispan applicaction to show how clustered locks are used. It is possible to set several locks with a timeout of 2 seconds. Even if it is possible to lock without timeouts this is not recommended as a thread or client can hang forever waiting on this lock! This instance can
 * be started several times, it will use JGroups UDP multicast to form a cluster. The IP address in use is shown as messages. If not the correct IP, or multicast is not working for it it can be set by <br>
 * mvn exec:java -Djgroups.bind_addr=127.0.0.1 <br>
 * or a vaild IP address.
 *
 * The Cache- and LockManager are created programatically.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public class ClusteredLockEmbeddedProgramatic extends AbstractEmbeddedClusteredLock {

  public ClusteredLockEmbeddedProgramatic(Console console) {
    super(console);

    GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();

    // Initialization for the LockManager is needed for the CacheManager to take effect
    final ClusteredLockManagerConfiguration config = global.addModule(ClusteredLockManagerConfigurationBuilder.class).numOwner(2).reliability(Reliability.AVAILABLE).create();

    // Initialize 1 cache managers
    cacheManager = new DefaultCacheManager(global.build());
    // All necessary links are injected to the LockManager
    lockManager = EmbeddedClusteredLockManagerFactory.from(cacheManager);
  }

  public static void main(String[] args) {
    final Console con = System.console();

    con.printf("Locks with programatic configuration\n");

    ClusteredLockEmbeddedProgramatic client = new ClusteredLockEmbeddedProgramatic(con);

    client.inputLoop();

    client.stop();
    System.out.println("\nDone !");
  }
}
