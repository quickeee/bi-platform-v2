package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceInMemoryDelegate;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConnectionDebugGwtServlet extends RemoteServiceServlet implements ConnectionGwtService {

  ConnectionServiceInMemoryDelegate SERVICE;

 
  public ConnectionDebugGwtServlet() {

  }

  private ConnectionServiceInMemoryDelegate getService(){
    if(SERVICE == null){
      try {
        SERVICE = new ConnectionServiceInMemoryDelegate();
        
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    return SERVICE;
  }

  public List<IConnection> getConnections() {
    return getService().getConnections();
  }
  public IConnection getConnectionByName(String name) {
    return getService().getConnectionByName(name);
  }
  public Boolean addConnection(IConnection connection) {
    return getService().addConnection(connection);
  }

  public Boolean updateConnection(IConnection connection) {
    return getService().updateConnection(connection);
  }

  public Boolean deleteConnection(IConnection connection) {
    return getService().deleteConnection(connection);
  }
    
  public Boolean deleteConnection(String name) {
    return getService().deleteConnection(name);    
  }

  public Boolean testConnection(IConnection connection)  throws ConnectionServiceException{
    return getService().testConnection(connection);
  }
}