package org.pentaho.test.platform.plugin;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlDataSource;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.repository.InMemoryMetadataDomainRepository;
import org.pentaho.metadata.util.SerializationService;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.pentahometadata.MetadataQueryComponent;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class MetadataQueryComponentTest {
    
  private MicroPlatform microPlatform;
    
  @Before
  public void init0() {
    microPlatform = new MicroPlatform("test-src/solution");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(IMetadataDomainRepository.class, InMemoryMetadataDomainRepository.class, Scope.GLOBAL);
    microPlatform.define("connection-SQL", SQLConnection.class);
    
    // TODO: need to define the IDatasourceService.class
    microPlatform.define(IDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL);
    try {
      IMetadataDomainRepository repo = PentahoSystem.get(IMetadataDomainRepository.class, null);
      Domain domain = getBasicDomain();
      // System.out.println(new SerializationService().serializeDomain(domain));
      repo.storeDomain(domain, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    // JNDI
    
    System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty("org.osjava.sj.root", "test-src/solution/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$


  }
    
  @Ignore @Test
  public void testParameters() {
    String mql = "<mql><domain_id>DOMAIN</domain_id><model_id>MODEL</model_id>" + 
                 "<parameters><parameter name=\"param1\" type=\"STRING\" defaultValue=\"A%\"/></parameters>" + 
                 "<selections><selection>" +
                 "<view>CATEGORY</view>" +
                 "<column>LC_CUSTOMERNAME</column>" +
                 "</selection>" +
                 "</selections>" +
                 "<constraints>" +
                 "<constraint><operator>AND</operator><condition>LIKE([CATEGORY.LC_CUSTOMERNAME];[param:param1])</condition></constraint>" +
                 "</constraints>" +
                 "</mql>";
    
    MetadataQueryComponent component = new MetadataQueryComponent();
    component.setQuery(mql);
    component.execute();
    
    IPentahoResultSet rs = component.getResultSet();
    try {
      Assert.assertNotNull(rs);
      Assert.assertEquals(16, rs.getRowCount());
      Object obj[];
      while ((obj = rs.next()) != null) {
        System.out.println(obj[0]);
      }
      
      
    } finally {
      rs.close();
      rs.closeConnection();
    }
    
    component = new MetadataQueryComponent();
    Map<String, Object> inputs = new HashMap<String, Object>();
    inputs.put("param1", "B%");
    component.setInputs(inputs);
    component.setQuery(mql);
    component.execute();
    
    rs = component.getResultSet();
    try {
      Assert.assertNotNull(rs);
      Assert.assertEquals(5, rs.getRowCount());
      Object obj[];
      while ((obj = rs.next()) != null) {
        System.out.println(obj[0]);
      }
      
    } finally {
      rs.close();
      rs.closeConnection();
    }
    
    
    
  }

  @Ignore @Test
  public void testComponent() {
    String mql = "<mql><domain_id>DOMAIN</domain_id><model_id>MODEL</model_id>" + 
                 "<selections><selection>" +
                 "<view>CATEGORY</view>" +
                 "<column>LC_CUSTOMERNAME</column>" +
                 "</selection>" +
                 "</selections></mql>";
    
    
    MetadataQueryComponent component = new MetadataQueryComponent();
    component.setQuery(mql);
    component.execute();
    
    IPentahoResultSet rs = component.getResultSet();
    try {
      Assert.assertNotNull(rs);
      Assert.assertEquals(1, rs.getColumnCount());
      Assert.assertEquals(122, rs.getRowCount());
      Object obj[];
      while ((obj = rs.next()) != null) {
        System.out.println(obj[0]);
      }
      
    } finally {
      rs.close();
      rs.closeConnection();
    }
  }

  
  public Domain getBasicDomain() {
    
    SqlPhysicalModel model = new SqlPhysicalModel();
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName("SampleData");
    model.setDatasource(dataSource);
    SqlPhysicalTable table = new SqlPhysicalTable(model);
    table.setId("PT1");
    model.getPhysicalTables().add(table);
    table.setTargetTableType(TargetTableType.INLINE_SQL);
    table.setTargetTable("select distinct customername from customers");
    
    SqlPhysicalColumn column = new SqlPhysicalColumn(table);
    column.setId("PC1");
    column.setTargetColumn("customername");
    column.setName(new LocalizedString("en_US", "Customer Name"));
    column.setDescription(new LocalizedString("en_US", "Customer Name Desc"));
    column.setDataType(DataType.STRING);
    table.getPhysicalColumns().add(column);
    
    LogicalModel logicalModel = new LogicalModel();
    logicalModel.setId("MODEL");
    logicalModel.setName(new LocalizedString("en_US", "My Model"));
    logicalModel.setDescription(new LocalizedString("en_US", "A Description of the Model"));
    
    LogicalTable logicalTable = new LogicalTable();
    logicalTable.setId("LT");
    logicalTable.setPhysicalTable(table);
    
    logicalModel.getLogicalTables().add(logicalTable);
    
    LogicalColumn logicalColumn = new LogicalColumn();
    logicalColumn.setId("LC_CUSTOMERNAME");
    logicalColumn.setPhysicalColumn(column);
    logicalColumn.setLogicalTable(logicalTable);
    logicalTable.addLogicalColumn(logicalColumn);
    
    Category mainCategory = new Category();
    mainCategory.setId("CATEGORY");
    mainCategory.setName(new LocalizedString("en_US", "Category"));
    mainCategory.addLogicalColumn(logicalColumn);
    
    logicalModel.getCategories().add(mainCategory);
    
    Domain domain = new Domain();
    domain.setId("DOMAIN");
    domain.addPhysicalModel(model);
    domain.addLogicalModel(logicalModel);
    
    return domain;
  }
  
  // TODO: Write test for inline ETL
}