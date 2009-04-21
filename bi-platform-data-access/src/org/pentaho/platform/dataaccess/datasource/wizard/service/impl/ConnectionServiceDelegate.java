package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ConnectionServiceDelegate {

  private String locale = Locale.getDefault().toString();

  private List<IConnection> connectionList = new ArrayList<IConnection>();
  private IDatasourceMgmtService datasourceMgmtSvc;
  public ConnectionServiceDelegate() {
    // get the datasource management service
    datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class,null);

  }
  
  public List<IConnection> getConnections() {
    List<IDatasource> datasources = null;
    try  {
    datasources = datasourceMgmtSvc.getDatasources();
    } catch(DatasourceMgmtServiceException dme) {
      
    }
    return connectionList;
  }
  public IConnection getConnectionByName(String name) {
    for(IConnection connection:connectionList) {
      if(connection.getName().equals(name)) {
        return connection;
      }
    }
    return null;
  }
  public Boolean addConnection(IConnection connection) {
    connectionList.add(connection);
    return true;
  }
  public Boolean updateConnection(IConnection connection) {
    IConnection conn = getConnectionByName(connection.getName());
    conn.setDriverClass(connection.getDriverClass());
    conn.setPassword(connection.getPassword());
    conn.setUrl(connection.getUrl());
    conn.setUsername(connection.getUsername());
    return true;
  }
  public Boolean deleteConnection(IConnection connection) {
    connectionList.remove(connectionList.indexOf(connection));
    return true;
  }
  public Boolean deleteConnection(String name) {
    for(IConnection connection:connectionList) {
      if(connection.getName().equals(name)) {
        return deleteConnection(connection);
      }
    }
    return false;
  }
  
  public boolean testConnection(IConnection connection) throws ConnectionServiceException {
    Connection conn = null;
    try {
      conn = getConnection(connection);
    } catch (ConnectionServiceException dme) {
      throw new ConnectionServiceException(dme.getMessage(), dme);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        throw new ConnectionServiceException(e);
      }
    }
    return true;
  }
  
  /**
   * NOTE: caller is responsible for closing connection
   * 
   * @param ds
   * @return
   * @throws DataSourceManagementException
   */
  private static Connection getConnection(IConnection connection) throws ConnectionServiceException {
    Connection conn = null;

    String driverClass = connection.getDriverClass();
    if (StringUtils.isEmpty(driverClass)) {
      throw new ConnectionServiceException("Connection attempt failed"); //$NON-NLS-1$  
    }
    Class<?> driverC = null;

    try {
      driverC = Class.forName(driverClass);
    } catch (ClassNotFoundException e) {
      throw new ConnectionServiceException("Driver not found in the class path. Driver was " + driverClass, e); //$NON-NLS-1$
    }
    if (!Driver.class.isAssignableFrom(driverC)) {
      throw new ConnectionServiceException("Driver not found in the class path. Driver was " + driverClass); //$NON-NLS-1$    }
    }
    Driver driver = null;
    
    try {
      driver = driverC.asSubclass(Driver.class).newInstance();
    } catch (InstantiationException e) {
      throw new ConnectionServiceException("Unable to instance the driver", e); //$NON-NLS-1$
    } catch (IllegalAccessException e) {
      throw new ConnectionServiceException("Unable to instance the driver", e); //$NON-NLS-1$    }
    }
    try {
      DriverManager.registerDriver(driver);
      conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword());
      return conn;
    } catch (SQLException e) {
      throw new ConnectionServiceException("Unable to connect", e); //$NON-NLS-1$
    }
  }

}
