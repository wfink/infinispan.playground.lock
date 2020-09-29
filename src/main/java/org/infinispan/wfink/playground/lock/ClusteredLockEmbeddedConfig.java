package org.infinispan.wfink.playground.lock;

import java.io.Console;
import java.io.IOException;

import org.infinispan.lock.EmbeddedClusteredLockManagerFactory;
import org.infinispan.manager.DefaultCacheManager;

/**
 * A simple embedded Infinispan applicaction to show how clustered locks are used. It is possible to set several locks with a timeout of 2 seconds. Even if it is possible to lock without timeouts this is not recommended as a thread or client can hang forever waiting on this lock! This instance can
 * be started several times, it will use JGroups UDP multicast to form a cluster. The IP address in use is shown as messages. If not the correct IP, or multicast is not working for it it can be set by <br>
 * mvn exec:java -Djgroups.bind_addr=127.0.0.1 <br>
 * or a vaild IP address.
 *
 * The Cache- and LockManager are created by XML configuration with infinispan.xml
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public class ClusteredLockEmbeddedConfig extends AbstractEmbeddedClusteredLock {

  public ClusteredLockEmbeddedConfig(Console console) {
    super(console);

    // Initialize 1 cache managers
    try {
      cacheManager = new DefaultCacheManager("infinispan.xml");
    } catch (IOException e) {
      throw new RuntimeException("Problem to read infinispan.xml");
    }

    // Initialize the clustered lock manager from the cache manager
    lockManager = EmbeddedClusteredLockManagerFactory.from(cacheManager);
  }

  public static void main(String[] args) {
    final Console con = System.console();

    con.printf("Locks with configuration file\n");

    ClusteredLockEmbeddedConfig client = new ClusteredLockEmbeddedConfig(con);

    client.inputLoop();

    client.stop();
    System.out.println("\nDone !");
  }
}
