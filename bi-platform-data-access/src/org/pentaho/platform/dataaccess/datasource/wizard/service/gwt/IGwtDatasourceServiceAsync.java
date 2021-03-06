/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGwtDatasourceServiceAsync {
  void getLogicalModels(AsyncCallback<List<LogicalModelSummary>> callback);
  void deleteLogicalModel(String domainId, String modelName, AsyncCallback<Boolean> callback);
  void doPreview(String connectionName, String query, String previewLimit, AsyncCallback<SerializedResultSet> callback);
  void generateLogicalModel(String modelName, String connectionName, String query, String previewLimit, AsyncCallback<BusinessData> callback);
  void saveLogicalModel(Domain domain, boolean overwrite,AsyncCallback<Boolean> callback);
  void generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent, String delimeter, String enclosure, AsyncCallback<BusinessData> callback);
  void hasPermission(AsyncCallback<Boolean> callback);
  void gwtWorkaround (BogoPojo pojo, AsyncCallback<BogoPojo> callback);
  void loadBusinessData(String domainId, String modelId, AsyncCallback<BusinessData> callback);
}

  