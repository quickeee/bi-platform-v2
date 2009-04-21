package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.ui.xul.XulServiceCallback;

public interface ConnectionService {
  void getConnections(XulServiceCallback<List<IConnection>> callback);
  void getConnectionByName(String name, XulServiceCallback<IConnection> callback);
  void addConnection(IConnection connection, XulServiceCallback<Boolean> callback);
  void updateConnection(IConnection connection, XulServiceCallback<Boolean> callback);
  void deleteConnection(IConnection connection, XulServiceCallback<Boolean> callback);
  void deleteConnection(String name, XulServiceCallback<Boolean> callback);
  void testConnection(IConnection connection, XulServiceCallback<Boolean> callback) throws ConnectionServiceException;
}

  