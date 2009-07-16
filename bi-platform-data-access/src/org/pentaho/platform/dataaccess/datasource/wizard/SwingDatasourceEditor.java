package org.pentaho.platform.dataaccess.datasource.wizard;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.ConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.CsvDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.DatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.RelationalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDebugImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceDebugImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.swing.SwingXulLoader;
import org.pentaho.ui.xul.swing.SwingXulRunner;

public class SwingDatasourceEditor implements IDatasourceEditor {

  private static Log log = LogFactory.getLog(SwingDatasourceEditor.class);
  
  private XulRunner runner;
  
  private DatasourceController datasourceController;
  
  public SwingDatasourceEditor(final IXulAsyncDatasourceService datasourceService, final IXulAsyncConnectionService connectionService) {
    try{
      XulDomContainer container = new SwingXulLoader().loadXul("org/pentaho/platform/dataaccess/datasource/wizard/public/connectionFrame.xul");
    
      runner = new SwingXulRunner();
      runner.addContainer(container);
      
      
      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument(container.getDocumentRoot());
      
    
      datasourceController = new DatasourceController();
      datasourceController.setBindingFactory(bf);
      container.addEventHandler(datasourceController);
      
      final CsvDatasourceController csvDatasourceController = new CsvDatasourceController();
      csvDatasourceController.setBindingFactory(bf);
      container.addEventHandler(csvDatasourceController);

      final RelationalDatasourceController relationalDatasourceController = new RelationalDatasourceController();
      relationalDatasourceController.setBindingFactory(bf);
      container.addEventHandler(relationalDatasourceController);

      final ConnectionController connectionController = new ConnectionController();
      container.addEventHandler(connectionController);
      
      connectionController.setService(connectionService);

      datasourceController.setService(datasourceService);

      connectionService.getConnections(new XulServiceCallback<List<IConnection>>(){

        public void error(String message, Throwable error) {
          System.out.println(error.getLocalizedMessage());
        }

        public void success(List<IConnection> connections) {
          DatasourceModel datasourceModel = new DatasourceModel();
          datasourceModel.getRelationalModel().setConnections(connections);
          datasourceController.setDatasourceModel(datasourceModel);
          connectionController.setDatasourceModel(datasourceModel);
          csvDatasourceController.setDatasourceModel(datasourceModel);
          relationalDatasourceController.setDatasourceModel(datasourceModel);
          
          try{
            runner.initialize();
          } catch(XulException e){
            log.error("error starting Xul application", e);
          }
        }
        
      });
    } catch(XulException e){
      log.error("error loading Xul application", e);
    }
  }
  
  public static void main(String[] args) throws XulException {
    IXulAsyncConnectionService connectionService = new ConnectionServiceDebugImpl();
    IXulAsyncDatasourceService datasourceService = new DatasourceServiceDebugImpl();

    SwingDatasourceEditor editor = new SwingDatasourceEditor(datasourceService, connectionService);
    editor.runner.start(); // shows the root window
    editor.showDialog();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<Domain> listener) {
    datasourceController.addDialogListener(listener);  
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void hideDialog() {
    datasourceController.hideDialog();  
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void removeDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<Domain> listener) {
    datasourceController.removeDialogListener(listener);
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {
    datasourceController.showDialog();  
  }

  public void showEditDialog(String domainId, String modelId) {
    datasourceController.showEditDialog(domainId, modelId);
    datasourceController.showDialog();
  }
  
}
