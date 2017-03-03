/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package org.rascalmpl.interpreter.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public final class Timing {

  private long start;

  public Timing() {
    super();
  }

  public void start() {
    start = getCpuTime();
  }

  public long duration() {
    long now = getCpuTime();
    long diff = now - start;
    start = now;
    return diff;
  }

  public static long getCpuTime() {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    return bean.isCurrentThreadCpuTimeSupported() ?
        bean.getCurrentThreadCpuTime() : 0L;
  }

  /**
   * Get user time in nanoseconds.
   */
  public static long getUserTime() {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    return bean.isCurrentThreadCpuTimeSupported() ?
        bean.getCurrentThreadUserTime() : 0L;
  }

  /**
   * Get system time in nanoseconds.
   */
  public static long getSystemTime() {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    return bean.isCurrentThreadCpuTimeSupported() ?
        (bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime()) : 0L;
  }
}