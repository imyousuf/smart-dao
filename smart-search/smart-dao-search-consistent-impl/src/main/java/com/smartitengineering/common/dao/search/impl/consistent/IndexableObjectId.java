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

import com.smartitengineering.dao.impl.hbase.spi.Externalizable;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.commons.codec.binary.StringUtils;

/**
 *
 * @author imyousuf
 */
public class IndexableObjectId implements Externalizable, Comparable<IndexableObjectId> {

  private String className;
  private String agentName;
  private String appName;
  private String objectId;

  public String getAgentName() {
    return agentName;
  }

  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  @Override
  public String toString() {
    return new StringBuilder(appName).append('|').append(agentName).append('|').append(className).append('|').append(
        objectId).toString();
  }

  public static IndexableObjectId fromString(String idString) throws Exception {
    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(StringUtils.getBytesUtf8(idString)));
    IndexableObjectId id = new IndexableObjectId();
    id.readExternal(inputStream);
    return id;
  }

  @Override
  public void writeExternal(DataOutput output) throws IOException {
    output.write(StringUtils.getBytesUtf8(toString()));
  }

  @Override
  public void readExternal(DataInput input) throws IOException, ClassNotFoundException {
    String idString = readStringInUTF8(input);
    if (org.apache.commons.lang.StringUtils.isBlank(idString)) {
      throw new IOException("No content!");
    }
    String[] params = idString.split("|");
    if (params == null || params.length != 4) {
      throw new IOException(
          "Object should have been in the format globalNamespace:workspace-name:type-name!");
    }
    appName = params[0];
    agentName = params[1];
    className = params[2];
    objectId = params[3];
  }

  @Override
  public int compareTo(IndexableObjectId o) {
    if (o == null) {
      return 1;
    }
    if (equals(o)) {
      return 0;
    }
    return toString().compareTo(o.toString());
  }

  private static String readStringInUTF8(DataInput in) throws IOException, UnsupportedEncodingException {
    int allocationBlockSize = 2000;
    int capacity = allocationBlockSize;
    int length = 0;
    ByteBuffer buffer = ByteBuffer.allocate(allocationBlockSize);
    boolean notEof = true;
    while (notEof) {
      try {
        buffer.put(in.readByte());
        if (++length >= capacity) {
          capacity += allocationBlockSize;
          buffer.limit(capacity);
        }
      }
      catch (EOFException ex) {
        notEof = false;
      }
    }
    String string = StringUtils.newStringUtf8(Arrays.copyOf(buffer.array(), length));
    return string;
  }
}
