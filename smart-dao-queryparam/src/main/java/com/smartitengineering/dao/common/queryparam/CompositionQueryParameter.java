/*
 * This is a common dao with basic CRUD operations and is not limited to any 
 * persistent layer implementation
 * 
 * Copyright (C) 2008  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.dao.common.queryparam;

/**
 *
 * @author imyousuf
 */
public interface CompositionQueryParameter
    extends CompoundQueryParameter<Void>,
            QueryParameterWithPropertyName<Void> {

    public void init(ParameterType type,
                     String propertyName,
                     QueryParameter... parameters);

    public void init(ParameterType type,
                     String propertyName,
                     FetchMode fetchMode,
                     QueryParameter... parameters);
    
    public FetchMode getFetchMode();
}
