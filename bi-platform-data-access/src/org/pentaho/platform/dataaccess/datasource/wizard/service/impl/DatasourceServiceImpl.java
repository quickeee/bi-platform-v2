package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.InlineEtlPhysicalModel;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.InlineEtlModelGenerator;
import org.pentaho.metadata.util.SQLModelGenerator;
import org.pentaho.metadata.util.SQLModelGeneratorException;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetConverter;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.util.messages.LocaleHelper;

public class DatasourceServiceImpl implements IDatasourceService{

  private static final Log logger = LogFactory.getLog(DatasourceServiceImpl.class);
  
  // This is also defined in UploadFileServlet and MetadataQueryComponent, so don't change it in just one place
  private static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles" + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  
  private IDataAccessPermissionHandler dataAccessPermHandler;
  private IDataAccessViewPermissionHandler dataAccessViewPermHandler;
  private IMetadataDomainRepository metadataDomainRepository;

  public DatasourceServiceImpl() {
    metadataDomainRepository = PentahoSystem.get(IMetadataDomainRepository.class, null);
  }


  protected boolean hasDataAccessPermission() {
    if (dataAccessPermHandler == null) {
      String dataAccessClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessPermHandler = (IDataAccessPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"),e);        
          // TODO: Unhardcode once this is an actual plugin
          dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
      }
      
    }
    return dataAccessPermHandler != null && dataAccessPermHandler.hasDataAccessPermission(PentahoSessionHolder.getSession());
  }
  
  protected boolean hasDataAccessViewPermission() {
    if (dataAccessViewPermHandler == null) {
      String dataAccessClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-view-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessViewPermHandler = (IDataAccessViewPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0030_DATAACCESS_VIEW_PERMISSIONS_INIT_ERROR"),e);         //$NON-NLS-1$
          // TODO: Unhardcode once this is an actual plugin
        dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
      }
      
    }
    return dataAccessViewPermHandler != null && dataAccessViewPermHandler.hasDataAccessViewPermission(PentahoSessionHolder.getSession());
  }
  
  protected List<String> getPermittedRoleList() {
    if (dataAccessViewPermHandler == null) {
      String dataAccessViewClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessViewClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessViewClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessViewPermHandler = (IDataAccessViewPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"),e);        
          // TODO: Unhardcode once this is an actual plugin
          dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
      }
      
    }
    if(dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedRoleList(PentahoSessionHolder.getSession());
  }
  protected List<String> getPermittedUserList() {
    if (dataAccessViewPermHandler == null) {
      String dataAccessViewClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessViewClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessViewClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessViewPermHandler = (IDataAccessViewPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"),e);        
          // TODO: Unhardcode once this is an actual plugin
          dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
      }
      
    }
    if(dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedUserList(PentahoSessionHolder.getSession());
  }

  protected int getDefaultAcls() {
    if (dataAccessViewPermHandler == null) {
      String dataAccessViewClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessViewClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessViewClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessViewPermHandler = (IDataAccessViewPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"),e);        
          // TODO: Unhardcode once this is an actual plugin
          dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
      }
      
    }
    if(dataAccessViewPermHandler == null) {
      return -1;
    }
    return dataAccessViewPermHandler.getDefaultAcls(PentahoSessionHolder.getSession());
  }
  public boolean deleteLogicalModel(String domainId, String modelName) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      return false;
    }
    try {
      metadataDomainRepository.removeModel(domainId, modelName);
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domainId, dse.getLocalizedMessage()),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainId, dse.getLocalizedMessage()), dse); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL", dne.getLocalizedMessage()),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL",dne.getLocalizedMessage()), dne); //$NON-NLS-1$      
    }
    return true;
  }

  
  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    SerializedResultSet serializedResultSet = null;
    int limit = (previewLimit != null && previewLimit.length() > 0) ? Integer.parseInt(previewLimit): -1;
    try {
      conn = DatasourceServiceHelper.getDataSourceConnection(connectionName, PentahoSessionHolder.getSession());
      // Only SELECT or read only operations are allowed through the SQL executer 
      conn.setReadOnly(true);
      if (!StringUtils.isEmpty(query)) {
        stmt = conn.createStatement();
        if(limit >=0) {
          stmt.setMaxRows(limit);
        }        
        ResultSetConverter rsc = new ResultSetConverter(stmt.executeQuery(query));
        serializedResultSet =  new SerializedResultSet(rsc.getColumnTypeNames(), rsc.getMetaData(), rsc.getResultSet());
  
      } else {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0021_QUERY_IS_EMPTY"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0021_QUERY_IS_EMPTY")); //$NON-NLS-1$
      }
    } catch (SQLException e) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()),e);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0009_QUERY_VALIDATION_FAILED",e.getLocalizedMessage()), e); //$NON-NLS-1$
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0010_PREVIEW_FAILED", e.getLocalizedMessage()), e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0010_PREVIEW_FAILED",e.getLocalizedMessage()),e);
      }
    }
    return serializedResultSet;

  }
  
  public boolean testDataSourceConnection(String connectionName) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    Connection conn = null;
    try {
      conn = DatasourceServiceHelper.getDataSourceConnection(connectionName, PentahoSessionHolder.getSession());
      if(conn == null) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION", connectionName));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connectionName)); //$NON-NLS-1$
      }
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION", connectionName, e.getLocalizedMessage()),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connectionName,e.getLocalizedMessage()),e); //$NON-NLS-1$
      }
    }
    return true;
  }


  /**
   * This method gets the business data which are the business columns, columns types and sample preview data
   * 
   * @param modelName, connection, query, previewLimit
   * @return BusinessData
   * @throws DatasourceServiceException
   */
  
  public BusinessData generateLogicalModel(String modelName, String connectionName, String query, String previewLimit) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    try {
      Connection conn = DatasourceServiceHelper.getDataSourceConnection(connectionName, PentahoSessionHolder.getSession());
      conn.setReadOnly(true);
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
        || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(modelName, connectionName, conn,
          query,securityEnabled, getPermittedRoleList(),getPermittedUserList()
            ,getDefaultAcls(),(PentahoSessionHolder.getSession() != null) ? PentahoSessionHolder.getSession().getName(): null); 
      Domain domain = sqlModelGenerator.generate();
      List<List<String>> data = DatasourceServiceHelper.getRelationalDataSample(connectionName, query, Integer.parseInt(previewLimit), PentahoSessionHolder.getSession());
      return new BusinessData(domain, data);
    } catch(SQLException sqle) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GET_READONLY_CONNECTION",sqle.getLocalizedMessage()),sqle);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GET_READONLY_CONNECTION",sqle.getLocalizedMessage()), sqle); //$NON-NLS-1$
    } catch(SQLModelGeneratorException smge) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",smge.getLocalizedMessage()),smge);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",smge.getLocalizedMessage()), smge); //$NON-NLS-1$
    }
  }

  /**
   * This method generates the business mode from the query and save it
   * 
   * @param modelName, connection, query
   * @return BusinessData
   * @throws DatasourceServiceException
   */  
  public BusinessData generateAndSaveLogicalModel(String modelName, String connectionName, String query, boolean overwrite, String previewLimit)  throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    Domain domain = null;
    try {
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
      || (getPermittedUserList() != null && getPermittedUserList().size() > 0); 
      Connection conn = DatasourceServiceHelper.getDataSourceConnection(connectionName, PentahoSessionHolder.getSession());
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(modelName, connectionName, conn,
          query,securityEnabled, getPermittedRoleList(),getPermittedUserList()
            ,getDefaultAcls(),(PentahoSessionHolder.getSession() != null) ? PentahoSessionHolder.getSession().getName(): null); 
      domain = sqlModelGenerator.generate();
      List<List<String>> data = DatasourceServiceHelper.getRelationalDataSample(connectionName, query, Integer.parseInt(previewLimit), PentahoSessionHolder.getSession());
      getMetadataDomainRepository().storeDomain(domain, overwrite);
      return new BusinessData(domain, data);
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domain.getId(), dse.getLocalizedMessage()),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN", domain.getId(), dse.getLocalizedMessage()), dse); //$NON-NLS-1$      
    } catch(DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST",domain.getId(), dae.getLocalizedMessage()),dae);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST", domain.getId(), dae.getLocalizedMessage()), dae); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL",dne.getLocalizedMessage()),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL", dne.getLocalizedMessage()), dne); //$NON-NLS-1$      
    } catch(SQLModelGeneratorException smge) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",smge.getLocalizedMessage()),smge);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",smge.getLocalizedMessage()), smge); //$NON-NLS-1$
    }
  }

  public IMetadataDomainRepository getMetadataDomainRepository() {
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository(IMetadataDomainRepository metadataDomainRepository) {
    this.metadataDomainRepository = metadataDomainRepository;
  }

  public BusinessData generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent, String delimiter, String enclosure) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }

    try  {
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
      || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
      
      String relativePath = PentahoSystem.getSystemSetting("file-upload-defaults/relative-path", String.valueOf(DEFAULT_RELATIVE_UPLOAD_FILE_PATH));  //$NON-NLS-1$
      String csvFileLoc = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);
      
      InlineEtlModelGenerator inlineEtlModelGenerator = new InlineEtlModelGenerator(modelName,
          csvFileLoc, relativeFilePath, headersPresent, delimiter,enclosure,securityEnabled,
            getPermittedRoleList(),getPermittedUserList(),
              getDefaultAcls(), (PentahoSessionHolder.getSession() != null) ? PentahoSessionHolder.getSession().getName(): null);
      
      Domain domain  = inlineEtlModelGenerator.generate();
      List<List<String>> data = DatasourceServiceHelper.getCsvDataSample(csvFileLoc + relativeFilePath, headersPresent,
          delimiter, enclosure, 5);
      return  new BusinessData(domain, data);
    } catch(Exception e) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",e.getLocalizedMessage()),e);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL", e.getLocalizedMessage()), e); //$NON-NLS-1$
    }
  }
  public boolean saveLogicalModel(Domain domain, boolean overwrite) throws DatasourceServiceException  {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }

    String domainName = domain.getId();    
    try {
      getMetadataDomainRepository().storeDomain(domain, overwrite);
      return true;
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domainName, dse.getLocalizedMessage()),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN", domainName, dse.getLocalizedMessage()), dse); //$NON-NLS-1$      
    } catch(DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST",domainName, dae.getLocalizedMessage()),dae);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST", domainName, dae.getLocalizedMessage()), dae); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL",dne.getLocalizedMessage()),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL", dne.getLocalizedMessage()), dne); //$NON-NLS-1$      
    }
  }

  public boolean hasPermission() {
    if(PentahoSessionHolder.getSession() != null) {
      return (SecurityHelper.isPentahoAdministrator(PentahoSessionHolder.getSession()) || hasDataAccessPermission());
    } else {
      return false;
    }
  }

  public List<LogicalModelSummary> getLogicalModels() throws DatasourceServiceException {
    if (!hasDataAccessViewPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }
    List<LogicalModelSummary> logicalModelSummaries = new ArrayList<LogicalModelSummary>();
    for (String domainId : getMetadataDomainRepository().getDomainIds()) {
      Domain domain = getMetadataDomainRepository().getDomain(domainId);
      
      String locale = LocaleHelper.getLocale().toString();
      String locales[] = new String[domain.getLocales().size()];
      for (int i = 0; i < domain.getLocales().size(); i++) {
        locales[i] = domain.getLocales().get(i).getCode();
      }
      locale = LocaleHelper.getClosestLocale( locale, locales );
      
      for (LogicalModel model : domain.getLogicalModels()) {
        logicalModelSummaries.add(new LogicalModelSummary(domainId, model.getId(), model.getName().getString(locale)));
      }
    }
    return logicalModelSummaries;
  }
  
  public BusinessData loadBusinessData(String domainId, String modelId)  throws DatasourceServiceException {
      Domain domain = getMetadataDomainRepository().getDomain(domainId);
      List<List<String>> data = null;
      if (domain.getPhysicalModels().get(0) instanceof InlineEtlPhysicalModel) {
        InlineEtlPhysicalModel model = (InlineEtlPhysicalModel)domain.getPhysicalModels().get(0);
        
        String relativePath = PentahoSystem.getSystemSetting("file-upload-defaults/relative-path", String.valueOf(DEFAULT_RELATIVE_UPLOAD_FILE_PATH));  //$NON-NLS-1$
        String csvFileLoc = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);
        
        data = DatasourceServiceHelper.getCsvDataSample(
            csvFileLoc + model.getFileLocation(), model.getHeaderPresent(), model.getDelimiter(), model.getEnclosure(), 5);
      } else {
        SqlPhysicalModel model = (SqlPhysicalModel)domain.getPhysicalModels().get(0);
        String query = model.getPhysicalTables().get(0).getTargetTable();
        data = DatasourceServiceHelper.getRelationalDataSample(model.getDatasource().getDatabaseName(), query, 5, PentahoSessionHolder.getSession());
      }
      return new BusinessData(domain, data);
  }


  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }

}