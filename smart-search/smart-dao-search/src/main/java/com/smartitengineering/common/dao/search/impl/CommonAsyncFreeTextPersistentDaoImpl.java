/*
 * This is a common dao with basic CRUD operations and is not limited to any
 * persistent layer implementation
 *
 * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.smartitengineering.common.dao.search.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.common.dao.search.CommonFreeTextPersistentDao;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class CommonAsyncFreeTextPersistentDaoImpl<T> implements CommonFreeTextPersistentDao<T> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  private final Queue<T> saveQueue;
  private final Queue<T> updateQueue;
  private final Queue<T> deleteQueue;
  private final long saveScheduleInterval;
  private final long updateScheduleInterval;
  private final long deleteScheduleInterval;
  private final ScheduledExecutorService scheduledExecutorService;
  private final CommonFreeTextPersistentDao<T> primaryDao;
  private final ExecutorService executorService;
  @Inject(optional = true)
  private Class<T> clazz;

  @Inject
  public CommonAsyncFreeTextPersistentDaoImpl(@Named("saveInterval") long saveInterval,
                                              @Named("updateInterval") long updateInterval,
                                              @Named("deleteInterval") long deleteInterval,
                                              @Named("intervalTimeUnit") TimeUnit timeUnit,
                                              @Named("primaryFreeTextPersistentDao") CommonFreeTextPersistentDao<T> primaryDao) {
    saveQueue = new ConcurrentLinkedQueue<T>();
    updateQueue = new ConcurrentLinkedQueue<T>();
    deleteQueue = new ConcurrentLinkedQueue<T>();
    scheduledExecutorService = Executors.newScheduledThreadPool(3);
    executorService = Executors.newCachedThreadPool();
    this.saveScheduleInterval = saveInterval;
    this.updateScheduleInterval = updateInterval;
    this.deleteScheduleInterval = deleteInterval;
    this.primaryDao = primaryDao;
    initScheduledThreads(timeUnit);

  }

  private void initScheduledThreads(TimeUnit timeUnit) {
    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        T[] next = new ArrayBuilder<T>(clazz, saveQueue).toArray();
        if (next == null) {
          return;
        }
        try {
          primaryDao.save(next);
        }
        catch (Exception ex) {
          logger.warn("Saving to search persistent dao did not succeed! Adding back to queue", ex);
          saveQueue.addAll(Arrays.asList(next));
        }
      }
    }, saveScheduleInterval, saveScheduleInterval, timeUnit);
    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        T[] next = new ArrayBuilder<T>(clazz, updateQueue).toArray();
        if (next == null) {
          return;
        }
        try {
          primaryDao.update(next);
        }
        catch (Exception ex) {
          logger.warn("Updating to search persistent dao did not succeed! Adding back to queue", ex);
          updateQueue.addAll(Arrays.asList(next));
        }
      }
    }, updateScheduleInterval, updateScheduleInterval, timeUnit);
    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        T[] next = new ArrayBuilder<T>(clazz, deleteQueue).toArray();
        if (next == null) {
          return;
        }
        try {
          primaryDao.delete(next);
        }
        catch (Exception ex) {
          logger.warn("Deleting to search persistent dao did not succeed! Adding back to queue", ex);
          deleteQueue.addAll(Arrays.asList(next));
        }
      }
    }, deleteScheduleInterval, deleteScheduleInterval, timeUnit);
  }

  @Override
  public final void save(final T... data) {
    executorService.submit(new Runnable() {

      @Override
      public void run() {
        saveOps(data);
      }
    });
  }

  @Override
  public final void update(final T... data) {
    executorService.submit(new Runnable() {

      @Override
      public void run() {
        updateOps(data);
      }
    });
  }

  @Override
  public final void delete(final T... data) {
    executorService.submit(new Runnable() {

      @Override
      public void run() {
        deleteOps(data);
      }
    });
  }

  protected void saveOps(final T... data) {
    saveQueue.addAll(Arrays.asList(data));
  }

  protected void deleteOps(final T[] data) {
    deleteQueue.addAll(Arrays.asList(data));
  }

  protected void updateOps(final T[] data) {
    updateQueue.addAll(Arrays.asList(data));
  }

  public static class ArrayBuilder<P> {

    private final P[] ps;

    public ArrayBuilder(Class<P> clazz, P... objects) {
      this(clazz, Arrays.asList(objects));
    }

    public ArrayBuilder(Class<P> clazz, Collection<P> objects) {
      if (objects == null || objects.size() <= 0) {
        ps = null;
      }
      else {
        final Iterator<P> iterator = objects.iterator();
        final P firstItem = iterator.next();
        ps = (P[]) Array.newInstance(clazz == null ? firstItem.getClass() : clazz, objects.size());
        try {
          iterator.remove();
        }
        catch (UnsupportedOperationException ex) {
          //Some implementations might not support this
        }
        ps[0] = firstItem;
        for (int i = 1; iterator.hasNext(); ++i) {
          ps[i] = iterator.next();
          try {
            iterator.remove();
          }
          catch (UnsupportedOperationException ex) {
            //Some implementations might not support this
          }
        }
      }
    }

    public P[] toArray() {
      return ps;
    }
  }
}