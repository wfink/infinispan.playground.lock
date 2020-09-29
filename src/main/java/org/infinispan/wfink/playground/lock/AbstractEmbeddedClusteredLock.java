package org.infinispan.wfink.playground.lock;

import java.io.Console;
import java.sql.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.infinispan.lock.api.ClusteredLock;
import org.infinispan.lock.api.ClusteredLockManager;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * Base for ClusteredLocks application to run all the different commands with the console.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public abstract class AbstractEmbeddedClusteredLock {
  final Console con;
  EmbeddedCacheManager cacheManager;
  ClusteredLockManager lockManager;
  private String currentLockName = null;

  public AbstractEmbeddedClusteredLock(Console console) {
    this.con = console;

    SimpleFormatter sf = new SimpleFormatter() {
      @Override
      public synchronized String format(LogRecord lr) {
        return String.format("%1$tH:%1$tM:%1$tS.%1$tL %2$-4s [%3$s] (%5$s) %4$s%n", new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getSourceClassName(), lr.getMessage(), lr.getThreadID());
      }
    };

    Logger root = Logger.getLogger("");

    root.getHandlers()[0].setFormatter(sf);
    root.getHandlers()[0].setLevel(Level.ALL);

    Logger.getLogger("org.infinispan.remoting").setLevel(Level.SEVERE);
    Logger.getLogger("org.infinispan.lock").setLevel(Level.ALL);
  }

  void defineLock() {
    currentLockName = readLockName(false);
    lockManager.defineLock(currentLockName);
  }

  void tryLock() {
    String name = readLockName(true);
    ClusteredLock lock = lockManager.get(name);

    try {
      if (lock.tryLock(2, TimeUnit.SECONDS).get()) {
        System.out.println("lock " + name + " is acquired");
      } else {
        System.out.println("lock of " + name + " failed");
      }
    } catch (InterruptedException | ExecutionException e) {
      System.out.println("lock of " + name + " failed with " + e.getMessage());
    }
  }

  void lock() throws Exception {
    String name = readLockName(true);
    ClusteredLock lock = lockManager.get(name);

    lock.lock().get();
    System.out.println("lock " + name + " is acquired");
  }

  void release() throws Exception {
    String name = readLockName(true);
    ClusteredLock lock = lockManager.get(name);

    if (lock.isLockedByMe().get()) {
      lock.unlock().whenComplete((nil, ex) -> {
        System.out.println("lock " + name + " is released");
      });
    } else {
      System.out.println("can not release lock " + name + " not the owner!");
    }
  }

  void check() {
    String name = readLockName(true);

    if (!lockManager.isDefined(name)) {
      System.out.println(" No lock with name " + name + " defined!");
      return;
    } else {
      ClusteredLock lock = lockManager.get(name);
      boolean locked = false;
      boolean owned = false;
      try {
        locked = lock.isLocked().get();
        owned = lock.isLockedByMe().get();
      } catch (InterruptedException | ExecutionException e) {
        // ignore
        e.printStackTrace();
      }
      System.out.println(" The lock '" + name + "' is " + (locked ? "" : " not") + " locked " + (owned ? " by this instance" : ""));
    }
  }

  void forceRelease() throws Exception {
    String name = readLockName(false);
    if (lockManager.forceRelease(name).get()) {
      System.out.println(" lock with name " + name + " hard released!");
    }
  }

  void remove() throws Exception {
    String name = readLockName(false);
    if (lockManager.remove(name).get()) {
      System.out.println(" lock with name " + name + " removed!");
    }
  }

  void stop() {
    cacheManager.stop();
  }

  void inputLoop() {
    while (true) {
      String action = null;
      try {
        action = con.readLine(">");
        if ("lock".equals(action) || "l".equals(action)) {
          tryLock();
        } else if ("LOCK".equals(action) || "L".equals(action)) {
          lock();
        } else if ("unlock".equals(action) || "u".equals(action)) {
          release();
        } else if ("define".equals(action) || "dl".equals(action)) {
          defineLock();
        } else if ("check".equals(action) || "c".equals(action)) {
          check();
        } else if ("remove".equals(action) || "rm".equals(action)) {
          remove();
        } else if ("force".equals(action) || "f".equals(action)) {
          forceRelease();
        } else if ("h".equals(action)) {
          printConsoleHelp();
        } else if ("q".equals(action)) {
          break;
        }
      } catch (Exception e) {
        System.out.println("  --> command '" + action + "' failed!");
        e.printStackTrace();
      }
    }
  }

  private String readLockName(boolean useCurrent) {
    String name = null;
    while (name == null) {
      String n = con.readLine("Enter lock name: ");
      if (n.isEmpty() && useCurrent) {
        name = currentLockName;
      } else if (!n.isEmpty()) {
        name = n;
      }
    }

    return name;
  }

  private void printConsoleHelp() {
    con.printf("Choose:\n" + "============= \n" + "dl  define  -  define a lock\n" + "l  lock    -  try to set the lock within 2 seconds\n" + "L  LOCK    -  block until the lock is set\n" + "u  unlock  -  release the lock\n" + "c  check   -  show state of a lock\n"
        + "rm remove  - delete the lock definition\n" + "f  force   - force a lock release\n" + "q     -  quit\n");
  }
}
