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
package com.smartitengineering.common.dao.search.impl.consistent;

import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import com.google.inject.name.Named;
import com.smartitengineering.common.dao.search.CommonFreeTextPersistentDao;
import com.smartitengineering.common.dao.search.impl.CommonAsyncFreeTextPersistentDaoImpl;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.domain.PersistentDTO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ConsistentAsyncFreeTextPersistentDaoImpl<I extends Comparable<I> & Serializable, V extends Comparable<V> & Serializable, T extends PersistentDTO<T, I, V>>
    extends CommonAsyncFreeTextPersistentDaoImpl<T> {

  public static final String DELETE = "delete";
  public static final String SAVE = "save";
  public static final String UPDATE = "update";
  private final String appName;
  private final String agentName;
  private final CommonWriteDao<IndexableObject> writeDao;
  private final CommonReadDao<IndexableObject, IndexableObjectId> readDao;
  private final IdToStringProvider<I> toStringProvider;

  @Inject
  public ConsistentAsyncFreeTextPersistentDaoImpl(@Named("saveInterval") long saveInterval,
                                                  @Named("updateInterval") long updateInterval,
                                                  @Named("deleteInterval") long deleteInterval,
                                                  @Named("intervalTimeUnit") TimeUnit timeUnit,
                                                  @Named("agentName") String agentName,
                                                  @Named("appName") String appName,
                                                  CommonWriteDao<IndexableObject> writeDao,
                                                  CommonReadDao<IndexableObject, IndexableObjectId> readDao,
                                                  @Named("primaryFreeTextPersistentDao") CommonFreeTextPersistentDao<T> primaryDao,
                                                  @Nullable IdToStringProvider<I> toStringProvider) {
    super(saveInterval, updateInterval, deleteInterval, timeUnit, new WrapperDao<T>(primaryDao, appName, agentName,
                                                                                    writeDao, toStringProvider));
    this.writeDao = writeDao;
    this.readDao = readDao;
    this.appName = appName;
    this.agentName = agentName;
    if (toStringProvider == null) {
      this.toStringProvider = new DefaultIdToStringProvider<I>();
    }
    else {
      this.toStringProvider = toStringProvider;
    }
    //TODO Check persistent storage for un syncd data
  }

  protected void saveBeans(String opName, T... data) {
    if (data == null || data.length <= 0) {
      return;
    }
    List<IndexableObject> objects = new ArrayList<IndexableObject>(data.length);
    for (T datum : data) {
      IndexableObject object = new IndexableObject();
      object.setSerializable(datum);
      object.setOperationName(opName);
      IndexableObjectId objectId = new IndexableObjectId();
      objectId.setAgentName(agentName);
      objectId.setAppName(appName);
      objectId.setClassName(object.getClazzName());
      objectId.setObjectId(toStringProvider.getToString(datum.getId()));
      objectId.setOpName(opName);
      object.setId(objectId);
      objects.add(object);
    }
    try {
      writeDao.save(objects.toArray(new IndexableObject[objects.size()]));
    }
    catch (Exception ex) {
      logger.error("Saving data not successful!", ex);
    }
  }

  @Override
  protected void deleteOps(T[] data) {
    saveBeans(DELETE, data);
    super.deleteOps(data);
  }

  @Override
  protected void saveOps(T... data) {
    saveBeans(UPDATE, data);
    super.saveOps(data);
  }

  @Override
  protected void updateOps(T[] data) {
    saveBeans(SAVE, data);
    super.updateOps(data);
  }

  protected static class WrapperDao<T extends PersistentDTO> implements CommonFreeTextPersistentDao<T> {

    private final CommonFreeTextPersistentDao<T> persistentDao;
    private final String appName;
    private final String agentName;
    private final CommonWriteDao<IndexableObject> writeDao;
    private final IdToStringProvider toStringProvider;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public WrapperDao(CommonFreeTextPersistentDao<T> persistentDao, String appName, String agentName,
                      CommonWriteDao<IndexableObject> writeDao, IdToStringProvider toStringProvider) {
      this.persistentDao = persistentDao;
      this.appName = appName;
      this.agentName = agentName;
      this.writeDao = writeDao;
      if (toStringProvider == null) {
        this.toStringProvider = new DefaultIdToStringProvider();
      }
      else {
        this.toStringProvider = toStringProvider;
      }
    }

    @Override
    public void save(T... data) {
      persistentDao.save(data);
      deleteBeans(SAVE, data);
    }

    @Override
    public void update(T... data) {
      persistentDao.update(data);
      deleteBeans(UPDATE, data);
    }

    @Override
    public void delete(T... data) {
      persistentDao.delete(data);
      deleteBeans(DELETE, data);
    }

    protected void deleteBeans(String opName, T... data) {
      if (data == null || data.length <= 0) {
        return;
      }
      List<IndexableObject> objects = new ArrayList<IndexableObject>(data.length);
      for (T datum : data) {
        IndexableObject object = new IndexableObject();
        object.setSerializable(datum);
        object.setOperationName(opName);
        IndexableObjectId objectId = new IndexableObjectId();
        objectId.setAgentName(agentName);
        objectId.setAppName(appName);
        objectId.setClassName(object.getClazzName());
        objectId.setObjectId(toStringProvider.getToString(datum.getId()));
        object.setId(objectId);
        objects.add(object);
      }
      try {
        writeDao.delete(objects.toArray(new IndexableObject[objects.size()]));
      }
      catch (Exception ex) {
        logger.error("Deleting data not successful!", ex);
      }
    }
  }
}
