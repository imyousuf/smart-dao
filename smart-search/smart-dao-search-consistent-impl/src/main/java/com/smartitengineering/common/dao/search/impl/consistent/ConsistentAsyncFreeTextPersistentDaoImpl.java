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
import com.google.inject.name.Named;
import com.smartitengineering.common.dao.search.CommonFreeTextPersistentDao;
import com.smartitengineering.common.dao.search.impl.CommonAsyncFreeTextPersistentDaoImpl;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.domain.PersistentDTO;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author imyousuf
 */
public class ConsistentAsyncFreeTextPersistentDaoImpl<T extends PersistentDTO> extends CommonAsyncFreeTextPersistentDaoImpl<T> {

  @Inject
  @Named("appName")
  private String appName;
  @Inject
  @Named("agentName")
  private String agentName;
  @Inject
  private CommonWriteDao<IndexableObject> writeDao;
  @Inject
  private CommonReadDao<IndexableObject, IndexableObjectId> readDao;

  @Inject
  public ConsistentAsyncFreeTextPersistentDaoImpl(@Named("saveInterval") long saveInterval,
                                                  @Named("updateInterval") long updateInterval,
                                                  @Named("deleteInterval") long deleteInterval,
                                                  @Named("intervalTimeUnit") TimeUnit timeUnit,
                                                  @Named("primaryFreeTextPersistentDao") CommonFreeTextPersistentDao<T> primaryDao) {
    super(saveInterval, updateInterval, deleteInterval, timeUnit, primaryDao);
    //TODO Check persistent storage for un syncd data
  }

  @Override
  protected void deleteOps(T[] data) {
    //TODO Save first
    super.deleteOps(data);
  }

  @Override
  protected void saveOps(T... data) {
    //TODO Save first
    super.saveOps(data);
  }

  @Override
  protected void updateOps(T[] data) {
    //TODO Save first
    super.updateOps(data);
  }
}
