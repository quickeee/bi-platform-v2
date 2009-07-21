package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import org.pentaho.metadata.repository.InMemoryMetadataDomainRepository;
import org.pentaho.metadata.util.InlineEtlModelGenerator;
import org.pentaho.metadata.util.SQLModelGenerator;
import org.pentaho.metadata.util.SQLModelGeneratorException;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetConverter;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceInMemoryServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.messages.LocaleHelper;

/*
 * TODO mlowery This class professes to be a datasource service yet it takes as inputs both IDatasource instances and 
 * lower-level BusinessData instances. (BusinessData instances are stored in IDatasources.) They are not currently being
 * kept in sync. I propose that the service only deals with IDatasources from a caller perspective.
 */
public class InMemoryDatasourceServiceImpl implements IDatasourceService{

  public static final IMetadataDomainRepository METADATA_DOMAIN_REPO = new InMemoryMetadataDomainRepository();
  private static final Log logger = LogFactory.getLog(InMemoryDatasourceServiceImpl.class);
  public static final String DEFAULT_UPLOAD_FILEPATH_FILE_NAME = "debug_upload_filepath.properties"; //$NON-NLS-1$
  public static final String UPLOAD_FILE_PATH = "upload.file.path"; //$NON-NLS-1$
  private IMetadataDomainRepository metadataDomainRepository;
  
  
  public InMemoryDatasourceServiceImpl() {
    // this needs to share the same one as MQL editor...
    metadataDomainRepository = METADATA_DOMAIN_REPO;
  }
 
  public boolean deleteLogicalModel(String domainId, String modelName) throws DatasourceServiceException {
    try {
      metadataDomainRepository.removeModel(domainId, modelName);
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domainId),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainId), dse); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$      
    }
    return true;
  }
  
  protected List<String> getPermittedRoleList() {
    DebugDataAccessViewPermissionHandler  dataAccessViewPermHandler = new DebugDataAccessViewPermissionHandler();
    return dataAccessViewPermHandler.getPermittedRoleList(PentahoSessionHolder.getSession());
  }
  protected List<String> getPermittedUserList() {
    DebugDataAccessViewPermissionHandler  dataAccessViewPermHandler = new DebugDataAccessViewPermissionHandler();
    return dataAccessViewPermHandler.getPermittedUserList(PentahoSessionHolder.getSession());
  }
  protected int getDefaultAcls() {
    DebugDataAccessViewPermissionHandler  dataAccessViewPermHandler = new DebugDataAccessViewPermissionHandler();
    return dataAccessViewPermHandler.getDefaultAcls(PentahoSessionHolder.getSession()); 
  }
  
  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit) throws DatasourceServiceException{
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    SerializedResultSet serializedResultSet = null;
    int limit = (previewLimit != null && previewLimit.length() > 0) ? Integer.parseInt(previewLimit): -1;
    try {
      conn = DatasourceInMemoryServiceHelper.getDataSourceConnection(connectionName);

      if (!StringUtils.isEmpty(query)) {
        stmt = conn.createStatement();
        if(limit >=0) {
          stmt.setMaxRows(limit);
        }        
        ResultSetConverter rsc = new ResultSetConverter(stmt.executeQuery(query));
        serializedResultSet =  new SerializedResultSet(rsc.getColumnTypeNames(), rsc.getMetaData(), rsc.getResultSet());
  
      } else {
        throw new DatasourceServiceException("Query not valid"); //$NON-NLS-1$
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DatasourceServiceException("Query validation failed", e); //$NON-NLS-1$
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
        throw new DatasourceServiceException(e);
      }
    }
    return serializedResultSet;

  }

  public boolean testDataSourceConnection(String connectionName) throws DatasourceServiceException {
    Connection conn = null;
    try {
      conn = DatasourceInMemoryServiceHelper.getDataSourceConnection(connectionName);
    } catch (DatasourceServiceException dme) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION", connectionName),dme);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connectionName),dme); //$NON-NLS-1$
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION", connectionName),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connectionName),e); //$NON-NLS-1$
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
    try {
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
        || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(modelName, connectionName, DatasourceInMemoryServiceHelper.getDataSourceConnection(connectionName),
          query,securityEnabled, getPermittedRoleList(),getPermittedUserList()
            ,getDefaultAcls(),"joe"); 
      Domain domain = sqlModelGenerator.generate();
      List<List<String>> data = DatasourceInMemoryServiceHelper.getRelationalDataSample(connectionName, query, Integer.parseInt(previewLimit));
      return new BusinessData(domain, data);
    } catch(SQLModelGeneratorException smge) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",smge.getLocalizedMessage()),smge);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0015_UNABLE_TO_GENERATE_MODEL"), smge); //$NON-NLS-1$
    }
  }

  
  /**
   * This method generates the business mode from the query and save it
   * 
   * @param modelName, connection, query, previewLimit
   * @return BusinessData
   * @throws DatasourceServiceException
   */  
  public BusinessData generateAndSaveLogicalModel(String modelName, String connectionName, String query, boolean overwrite, String previewLimit)  throws DatasourceServiceException {
    Domain domain = null;
      try {
        Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
        || (getPermittedUserList() != null && getPermittedUserList().size() > 0); 

        SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(modelName, connectionName, DatasourceInMemoryServiceHelper.getDataSourceConnection(connectionName),
            query,securityEnabled, getPermittedRoleList(),getPermittedUserList()
              ,getDefaultAcls(),"joe"); 
        domain = sqlModelGenerator.generate();
        List<List<String>> data = DatasourceInMemoryServiceHelper.getRelationalDataSample(connectionName, query, Integer.parseInt(previewLimit));
        getMetadataDomainRepository().storeDomain(domain, overwrite);
        return new BusinessData(domain, data);
    } catch(SQLModelGeneratorException smge) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",smge.getLocalizedMessage()),smge);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0015_UNABLE_TO_GENERATE_MODEL",smge.getLocalizedMessage()), smge); //$NON-NLS-1$
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",modelName),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0016_UNABLE_TO_STORE_DOMAIN", modelName), dse); //$NON-NLS-1$      
    } catch(DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST",modelName),dae);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST", modelName), dae); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0019_DOMAIN_IS_NULL"),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$     
    }   
  }
  public IMetadataDomainRepository getMetadataDomainRepository() {
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository(IMetadataDomainRepository metadataDomainRepository) {
    this.metadataDomainRepository = metadataDomainRepository;
  }
  
  public BusinessData generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent, String delimiter, String enclosure) throws DatasourceServiceException {
    try  {
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
      || (getPermittedUserList() != null && getPermittedUserList().size() > 0); 
      InlineEtlModelGenerator inlineEtlModelGenerator = new InlineEtlModelGenerator(modelName,
          getUploadFilePath(), relativeFilePath, headersPresent, delimiter,enclosure,securityEnabled,
            getPermittedRoleList(),getPermittedUserList(),
              getDefaultAcls(), "joe");
      Domain domain  = inlineEtlModelGenerator.generate();
      List<List<String>> data = DatasourceInMemoryServiceHelper.getCsvDataSample(relativeFilePath, headersPresent,
          delimiter, enclosure, 5);
      return  new BusinessData(domain, data);

      } catch(Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",e.getLocalizedMessage()),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0015_UNABLE_TO_GENERATE_MODEL",e.getLocalizedMessage()), e); //$NON-NLS-1$
      }
  }
  
  public BusinessData loadBusinessData(String domainId, String modelId)  throws DatasourceServiceException {
      Domain domain = getMetadataDomainRepository().getDomain(domainId);
      List<List<String>> data = null;
      if (domain.getPhysicalModels().get(0) instanceof InlineEtlPhysicalModel) {
        InlineEtlPhysicalModel model = (InlineEtlPhysicalModel)domain.getPhysicalModels().get(0);
        data = DatasourceInMemoryServiceHelper.getCsvDataSample(
            model.getFileLocation(), model.getHeaderPresent(), model.getDelimiter(), model.getEnclosure(), 5);
      } else {
        SqlPhysicalModel model = (SqlPhysicalModel)domain.getPhysicalModels().get(0);
        String query = model.getPhysicalTables().get(0).getTargetTable();
        data = DatasourceInMemoryServiceHelper.getRelationalDataSample(model.getDatasource().getDatabaseName(), query, 5);
      }
      return new BusinessData(domain, data);
  }

  public boolean saveLogicalModel(Domain domain, boolean overwrite) throws DatasourceServiceException  {
    String domainName = domain.getId();
    try {
      getMetadataDomainRepository().storeDomain(domain, overwrite);
      return true;
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domainName),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainName), dse); //$NON-NLS-1$      
    } catch(DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST",domainName),dae);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST", domainName), dae); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0019_DOMAIN_IS_NULL"),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$      
    }
  }
  
  private String getUploadFilePath() throws DatasourceServiceException {
    try {
      URL url = ClassLoader.getSystemResource(DEFAULT_UPLOAD_FILEPATH_FILE_NAME);
      URI uri = url.toURI();
      File file = new File(uri);
      FileInputStream fis = new FileInputStream(file);
      Properties properties = new Properties();
      properties.load(fis);
      return (String) properties.get( UPLOAD_FILE_PATH );
    } catch (Exception e) {
      throw new DatasourceServiceException(e);
    }
  }
  
  public boolean hasPermission() {
    return true;
  }

  public List<LogicalModelSummary> getLogicalModels() throws DatasourceServiceException {
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

  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }
}