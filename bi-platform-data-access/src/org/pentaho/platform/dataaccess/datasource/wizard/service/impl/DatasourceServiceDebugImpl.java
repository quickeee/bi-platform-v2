package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.ui.xul.XulServiceCallback;

public class DatasourceServiceDebugImpl implements IXulAsyncDatasourceService{

  InMemoryDatasourceServiceImpl SERVICE;
  public DatasourceServiceDebugImpl(){
    SERVICE = new InMemoryDatasourceServiceImpl();
  }
 
  public void doPreview(String connectionName, String query, String previewLimit, XulServiceCallback<SerializedResultSet> callback){
    try {    
      callback.success(SERVICE.doPreview(connectionName, query, previewLimit));
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }    
  }
  public void generateLogicalModel(String modelName, String connectionName, String query, String previewLimit,
      XulServiceCallback<BusinessData> callback) {
      try {
        callback.success(SERVICE.generateLogicalModel(modelName, connectionName, query, previewLimit));
      } catch (DatasourceServiceException e) {
        callback.error(e.getLocalizedMessage(), e);
      }
  }
  public void generateAndSaveLogicalModel(String modelName, String connectionName, String query, boolean overwrite, String previewLimit,
      XulServiceCallback<BusinessData> callback){
    try {    
      callback.success(SERVICE.generateAndSaveLogicalModel(modelName, connectionName, query, overwrite, previewLimit));
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void saveLogicalModel(Domain domain, boolean overwrite, XulServiceCallback<Boolean> callback) {
    try {
      callback.success(SERVICE.saveLogicalModel(domain, overwrite));
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent,
      String delimeter, String enclosure, XulServiceCallback<BusinessData> callback) {
    try {
      callback.success(SERVICE.generateInlineEtlLogicalModel(modelName, relativeFilePath, headersPresent, delimeter, enclosure));
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void hasPermission(XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.hasPermission());
  }

  public void getUploadFilePath(XulServiceCallback<String> callback) {
    try {
    callback.success(SERVICE.getUploadFilePath());
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void deleteLogicalModel(String domainId, String modelName, XulServiceCallback<Boolean> callback) {
    try {
      Boolean res = SERVICE.deleteLogicalModel(domainId, modelName);
      callback.success(res);
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void getLogicalModels(XulServiceCallback<List<LogicalModelSummary>> callback) {
    try {
      List<LogicalModelSummary> res = SERVICE.getLogicalModels();
      callback.success(res);
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void loadBusinessData(String domainId, String modelId, XulServiceCallback<BusinessData> callback) {
    try {
      BusinessData res = SERVICE.loadBusinessData(domainId, modelId);
      callback.success(res);
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }
}

  