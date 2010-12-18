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

import com.smartitengineering.domain.AbstractGenericPersistentDTO;
import java.io.Serializable;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class IndexableObject extends AbstractGenericPersistentDTO<IndexableObject, IndexableObjectId, Long> {

  private String clazzName;
  private byte[] serializedData;

  public String getClazzName() {
    return clazzName;
  }

  public void setClazzName(String clazzName) {
    this.clazzName = clazzName;
  }

  public byte[] getSerializedData() {
    return serializedData;
  }

  public void setSerializedData(byte[] serializedData) {
    this.serializedData = serializedData;
  }

  public void setSerializable(Serializable serializable) {
    clazzName = serializable.getClass().getName();
    serializedData = SerializationUtils.serialize(serializable);
  }

  public <S extends Serializable> S getSeriazable() {
    if (!isValid()) {
      return null;
    }
    return (S) SerializationUtils.deserialize(serializedData);
  }

  @Override
  public boolean isValid() {
    return StringUtils.isNotBlank(clazzName) && serializedData != null;
  }
}
