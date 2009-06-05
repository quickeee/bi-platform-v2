package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ConnectionModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelDataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.RelationalModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCell;
import org.pentaho.ui.xul.components.XulTreeCol;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulGrid;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulRow;
import org.pentaho.ui.xul.containers.XulRows;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeChildren;
import org.pentaho.ui.xul.containers.XulTreeCols;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.TreeCellEditor;
import org.pentaho.ui.xul.util.TreeCellEditorCallback;
import org.pentaho.ui.xul.util.TreeCellRenderer;

public class RelationalDatasourceController extends AbstractXulEventHandler {
  public static final int MAX_SAMPLE_DATA_ROWS = 5;

  private DatasourceMessages datasourceMessages;
  
  private XulDialog connectionDialog;

  private XulDialog removeConfirmationDialog;

  private XulDialog waitingDialog = null;
  
  private XulDialog applyQueryConfirmationDialog = null;
  
  private XulLabel waitingDialogLabel = null;

  private XulDialog previewResultsDialog = null;

  private DatasourceService service;

  private DatasourceModel datasourceModel;

  private ConnectionModel connectionModel;

  BindingFactory bf;

  XulTree previewResultsTable = null;

  XulTextbox datasourceName = null;

  XulListbox connections = null;

  XulTextbox query = null;

  XulTreeCols previewResultsTreeCols = null;

  XulTextbox previewLimit = null;

  XulButton editConnectionButton = null;

  XulButton removeConnectionButton = null;

  XulButton editQueryButton = null;

  XulButton previewButton = null;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  private XulTree modelDataTable = null;

  private XulButton applyButton = null;

  private XulTreeCol columnNameTreeCol = null;
  private XulTreeCol columnTypeTreeCol = null;
  //private XulTreeCol columnFormatTreeCol = null;\
  XulTree sampleDataTree = null;
  XulDialog aggregationEditorDialog = null;
  XulDialog sampleDataDialog = null;
  CustomAggregateCellEditor aggregationCellEditor = null;
  CustomSampleDataCellEditor sampleDataCellEditor = null;
  //private XulRows rows = null;
  //private XulGrid grid = null;  
  CustomAggregationCellRenderer aggregationCellRenderer = null;
  public RelationalDatasourceController() {

  }

  public void init() {
    //rows = (XulRows) document.getElementById("relationalSampleDataRows");//$NON-NLS-1$
    //grid = (XulGrid) document.getElementById("relationalSampleDataGrid");//$NON-NLS-1$
    applyButton = (XulButton) document.getElementById("apply"); //$NON-NLS-1$
    modelDataTable = (XulTree) document.getElementById("modelDataTable");
    sampleDataTree = (XulTree) document.getElementById("relationalSampleDataTable");
    aggregationEditorDialog = (XulDialog) document.getElementById("relationalAggregationEditorDialog");
    aggregationCellEditor = new CustomAggregateCellEditor(aggregationEditorDialog);
    modelDataTable.registerCellEditor("aggregation-cell-editor", aggregationCellEditor);
    aggregationCellRenderer = new CustomAggregationCellRenderer();
    modelDataTable.registerCellRenderer("aggregation-cell-editor", aggregationCellRenderer);
    sampleDataDialog = (XulDialog) document.getElementById("relationalSampleDataDialog");
    sampleDataCellEditor = new CustomSampleDataCellEditor(sampleDataDialog);
    modelDataTable.registerCellEditor("sample-data-cell-editor", sampleDataCellEditor);

    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$    
    applyQueryConfirmationDialog = (XulDialog) document.getElementById("applyQueryConfirmationDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$    
    waitingDialog = (XulDialog) document.getElementById("waitingDialog"); //$NON-NLS-1$
    waitingDialogLabel = (XulLabel) document.getElementById("waitingDialogLabel");//$NON-NLS-1$    
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$
    datasourceName = (XulTextbox) document.getElementById("datasourcename"); //$NON-NLS-1$
    connections = (XulListbox) document.getElementById("connectionList"); //$NON-NLS-1$
    query = (XulTextbox) document.getElementById("query"); //$NON-NLS-1$
    connectionDialog = (XulDialog) document.getElementById("connectionDialog");//$NON-NLS-1$
    previewResultsDialog = (XulDialog) document.getElementById("previewResultsDialog");//$NON-NLS-1$
    removeConfirmationDialog = (XulDialog) document.getElementById("removeConfirmationDialog");//$NON-NLS-1$
    previewResultsTable = (XulTree) document.getElementById("previewResultsTable"); //$NON-NLS-1$
    previewResultsTreeCols = (XulTreeCols) document.getElementById("previewResultsTreeCols"); //$NON-NLS-1$
    previewLimit = (XulTextbox) document.getElementById("previewLimit"); //$NON-NLS-1$
    editConnectionButton = (XulButton) document.getElementById("editConnection"); //$NON-NLS-1$
    removeConnectionButton = (XulButton) document.getElementById("removeConnection"); //$NON-NLS-1$
    editQueryButton = (XulButton) document.getElementById("editQuery"); //$NON-NLS-1$
    previewButton = (XulButton) document.getElementById("preview"); //$NON-NLS-1$
    columnNameTreeCol = (XulTreeCol) document.getElementById("relationalColumnNameTreeCol"); //$NON-NLS-1$
    columnTypeTreeCol = (XulTreeCol) document.getElementById("relationalColumnTypeTreeCol"); //$NON-NLS-1$
    //columnFormatTreeCol = (XulTreeCol) document.getElementById("relationalColumnFormatTreeCol"); //$NON-NLS-1$

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(datasourceModel.getRelationalModel(), "validated", previewButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel.getRelationalModel(), "validated", applyButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$
    
    BindingConvertor<IConnection, Boolean> buttonConvertor = new BindingConvertor<IConnection, Boolean>() {

      @Override
      public Boolean sourceToTarget(IConnection value) {
        return !(value == null);
      }

      @Override
      public IConnection targetToSource(Boolean value) {
        return null;
      }

    };

    bf.setBindingType(Binding.Type.ONE_WAY);
    final Binding domainBinding = bf.createBinding(datasourceModel.getRelationalModel(), "connections", connections, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel.getRelationalModel(), "selectedConnection", editConnectionButton, "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding(datasourceModel.getRelationalModel(), "selectedConnection", removeConnectionButton, "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel.getRelationalModel(),
        "selectedConnection", connections, "selectedIndex", new BindingConvertor<IConnection, Integer>() { //$NON-NLS-1$ //$NON-NLS-2$

          @Override
          public Integer sourceToTarget(IConnection connection) {
            if (connection != null) {
              return datasourceModel.getRelationalModel().getConnectionIndex(connection);
            } else {
              return -1;
            }

          }

          @Override
          public IConnection targetToSource(Integer value) {
            if (value >= 0) {
              return datasourceModel.getRelationalModel().getConnections().get(value);
            } else {
              return null;
            }

          }

        });
    bf.createBinding(datasourceModel.getRelationalModel(), "dataRows", modelDataTable, "elements");
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);     
    bf.createBinding(datasourceModel.getRelationalModel(), "previewLimit", previewLimit, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    // Not sure if editQuery button is doing much
    //bf.createBinding(editQueryButton, "!disabled", "removeConnectionButton", "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel.getRelationalModel(), "query", query, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel, "datasourceName", datasourceName, "value"); //$NON-NLS-1$ //$NON-NLS-2$

    try {
      // Fires the population of the model listbox. This cascades down to the categories and columns. In essence, this
      // call initializes the entire UI.
      domainBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }

  public void setDatasourceModel(DatasourceModel model) {
    this.datasourceModel = model;
  }

  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  public void setConnectionModel(ConnectionModel model) {
    this.connectionModel = model;
  }

  public ConnectionModel getConnectionModel() {

    return this.connectionModel;
  }

  public String getName() {
    return "relationalDatasourceController";
  }

  public void applyQuery() {
    if(datasourceModel.getRelationalModel().getBusinessData() != null) {
      applyQueryConfirmationDialog.show();
    } else {
      generateModel();
    }
  }
  public void generateModel() {
      if (validateInputs()) {
        query.setDisabled(true);
        if(applyQueryConfirmationDialog.isVisible()) {
          applyQueryConfirmationDialog.hide();
        }
        try {
          showWaitingDialog(datasourceMessages.getString("DatasourceController.GENERATE_MODEL"), datasourceMessages.getString("DatasourceController.WAIT"));
          service.generateModel(datasourceModel.getDatasourceName(), datasourceModel.getRelationalModel().getSelectedConnection(),
              datasourceModel.getRelationalModel().getQuery(), datasourceModel.getRelationalModel().getPreviewLimit(), new XulServiceCallback<BusinessData>() {

                public void error(String message, Throwable error) {
                  hideWaitingDialog();
                  query.setDisabled(false);
                  displayErrorMessage(error);
                }

                public void success(BusinessData businessData) {
                  try {
                    hideWaitingDialog();
                    datasourceModel.getRelationalModel().setBusinessData(null);                    
                    query.setDisabled(false);
                    // Setting the editable property to true so that the table can be populated with correct cell types                    
                    columnNameTreeCol.setEditable(true);
                    columnTypeTreeCol.setEditable(true);
                    //columnFormatTreeCol.setEditable(true);
                    datasourceModel.getRelationalModel().setBusinessData(businessData);
                  } catch (Exception xe) {
                    xe.printStackTrace();
                  }
                }
              });
        } catch (DatasourceServiceException e) {
          hideWaitingDialog();
          query.setDisabled(false);
          displayErrorMessage(e);
        }
      } else {
        openErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages.getString("DatasourceController.ERROR_0001_MISSING_INPUTS"));
      }
  }
  private boolean validateInputs() {
    return (datasourceModel.getRelationalModel().getSelectedConnection() != null
        && (datasourceModel.getRelationalModel().getQuery() != null && datasourceModel.getRelationalModel().getQuery().length() > 0) && (datasourceModel
        .getDatasourceName() != null && datasourceModel.getDatasourceName().length() > 0));
  }
  public void editQuery() {

  }

  public void addConnection() {
    
    datasourceModel.getRelationalModel().setEditType(RelationalModel.EditType.ADD);
    connectionModel.clearModel();
    connectionModel.setDisableConnectionName(false);
    showConnectionDialog();
  }

  public void editConnection() {
    datasourceModel.getRelationalModel().setEditType(RelationalModel.EditType.ADD);
    connectionModel.setDisableConnectionName(true);
    connectionModel.setConnection(datasourceModel.getRelationalModel().getSelectedConnection());
    showConnectionDialog();
  }

  public void removeConnection() {
    // Display the warning message. If ok then remove the connection from the list
    int index = connections.getSelectedIndex();
    removeConfirmationDialog.show();
  }

  public void showConnectionDialog() {
    connectionDialog.show();
  }

  public void closeConnectionDialog() {
    connectionDialog.hide();
  }

  public void closeRemoveConfirmationDialog() {
    removeConfirmationDialog.hide();
  }

  public void closeApplyQueryConfirmationDialog() {
    applyQueryConfirmationDialog.hide();
  }
  
  public void displayPreview() {

    if (!validateInputs()) {
      openErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages.getString("DatasourceController.ERROR_0001_MISSING_INPUTS"));
    } else {
      try {
        showWaitingDialog(datasourceMessages.getString("DatasourceController.GENERATE_PREVIEW_DATA"), datasourceMessages.getString("DatasourceController.WAIT"));
        service.doPreview(datasourceModel.getRelationalModel().getSelectedConnection(), datasourceModel.getRelationalModel().getQuery(), datasourceModel
            .getRelationalModel().getPreviewLimit(), new XulServiceCallback<SerializedResultSet>() {

          public void error(String message, Throwable error) {
            hideWaitingDialog();
            displayErrorMessage(error);
          }

          public void success(SerializedResultSet rs) {
            try {
              String[][] data = rs.getData();
              String[] columns = rs.getColumns();
              int columnCount = columns.length;
              // Remove any existing children
              List<XulComponent> previewResultsList = previewResultsTable.getChildNodes();

              for (int i = 0; i < previewResultsList.size(); i++) {
                previewResultsTable.removeChild(previewResultsList.get(i));
              }
              XulTreeChildren treeChildren = previewResultsTable.getRootChildren();
              if(treeChildren != null) {
                treeChildren.removeAll();
/*                List<XulComponent> treeChildrenList = treeChildren.getChildNodes();
                for (int i = 0; i < treeChildrenList.size(); i++) {
                  treeChildren.removeItem(i);
                }*/
              }
              // Remove all the existing columns
              int curTreeColCount = previewResultsTable.getColumns().getColumnCount();
              List<XulComponent> cols = previewResultsTable.getColumns().getChildNodes();
              for (int i = 0; i < curTreeColCount; i++) {
                previewResultsTable.getColumns().removeChild(cols.get(i));
              }
              previewResultsTable.update();
              // Recreate the colums
              XulTreeCols treeCols = previewResultsTable.getColumns();
              if (treeCols == null) {
                try {
                  treeCols = (XulTreeCols) document.createElement("treecols");
                } catch (XulException e) {

                }
              }
              // Setting column data
              for (int i = 0; i < columnCount; i++) {
                try {
                  XulTreeCol treeCol = (XulTreeCol) document.createElement("treecol");
                  treeCol.setLabel(columns[i]);
                  treeCol.setFlex(1);
                  treeCols.addColumn(treeCol);
                } catch (XulException e) {

                }
              }

              XulTreeCols treeCols1 = previewResultsTable.getColumns();
              int count = previewResultsTable.getColumns().getColumnCount();
              // Create the tree children and setting the data
              try {
                for (int i = 0; i < data.length; i++) {
                  XulTreeRow row = (XulTreeRow) document.createElement("treerow");

                  for (int j = 0; j < columnCount; j++) {
                    XulTreeCell cell = (XulTreeCell) document.createElement("treecell");
                    cell.setLabel(data[i][j]);
                    row.addCell(cell);
                  }

                  previewResultsTable.addTreeRow(row);
                }
                previewResultsTable.update();
                hideWaitingDialog();
                previewResultsDialog.show();
              } catch (XulException e) {
                // TODO: add logging
                hideWaitingDialog();
                System.out.println(e.getMessage());
                e.printStackTrace();
              }
            } catch (Exception e) {
              hideWaitingDialog();
              displayErrorMessage(e);
            }
          }
        });
      } catch (DatasourceServiceException e) {
        hideWaitingDialog();
        openErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages.getString("RelationalDatasourceController.ERROR_0001_UNABLE_TO_PREVIEW_DATA",e.getLocalizedMessage()));
      }
    }
  }

  public void closePreviewResultsDialog() {
    previewResultsDialog.hide();
  }

  public DatasourceService getService() {
    return service;
  }

  public void setService(DatasourceService service) {
    this.service = service;
  }
  public void openErrorDialog(String title, String message) {
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
    errorDialog.show();
  }

  public void closeErrorDialog() {
    if (!errorDialog.isHidden()) {
      errorDialog.hide();
    }
  }

  public void openSuccesDialog(String title, String message) {
    successDialog.setTitle(title);
    successLabel.setValue(message);
    successDialog.show();
  }

  public void closeSuccessDialog() {
    if (!successDialog.isHidden()) {
      successDialog.hide();
    }
  }

  public void showWaitingDialog(String title, String message) {
    waitingDialog.setTitle(title);
    waitingDialogLabel.setValue(message);
    waitingDialog.show();

  }

  public void hideWaitingDialog() {
    waitingDialog.hide();
  }
  
  public void displayErrorMessage(Throwable th) {
    errorDialog.setTitle(ExceptionParser.getErrorHeader(th));
    errorLabel.setValue(ExceptionParser.getErrorMessage(th));
    errorDialog.show();
  }
  /**
   * @param datasourceMessages the datasourceMessages to set
   */
  public void setDatasourceMessages(DatasourceMessages datasourceMessages) {
    this.datasourceMessages = datasourceMessages;
  }

  /**
   * @return the datasourceMessages
   */
  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }
  
  public void closeAggregationEditorDialog() {
   aggregationCellEditor.hide(); 
  }
  
  public void saveAggregationValues() {
    aggregationCellEditor.notifyListeners();
  }

  public void closeSampleDataDialog() {
    sampleDataCellEditor.hide(); 
  }

  private class CustomAggregateCellEditor implements TreeCellEditor {
    XulDialog dialog = null;
    TreeCellEditorCallback callback = null;

    public CustomAggregateCellEditor(XulDialog dialog) {
      super();
      this.dialog = dialog;
      dialog.setBgcolor("#FFFFFF");
    }

    public Object getValue() {
      // TODO Auto-generated method stub
      return null;
    }

    public void hide() {
      dialog.hide();
    }

    public void setValue(Object val) {
      // Clear the dialog box with all the existing checkboxes if any
      for(XulComponent component: dialog.getChildNodes()) {
        if(component instanceof XulCheckbox) {
          dialog.removeChild(component);
        }
      }
      // Create the list of check box in XulDialog
      ArrayList<AggregationType> aggregationList = (ArrayList<AggregationType>) val;
      AggregationType[] aggregationTypeArray = AggregationType.values();
      for(int i=0;i<aggregationTypeArray.length;i++) {
        XulCheckbox aggregationCheckBox;
        try {
          aggregationCheckBox = (XulCheckbox) document.createElement("checkbox");
          aggregationCheckBox.setLabel(aggregationTypeArray[i].name());
          if(aggregationList.contains(aggregationTypeArray[i])) {
            aggregationCheckBox.setChecked(true);
          } else {
            aggregationCheckBox.setChecked(false);
          }
          dialog.addChild(aggregationCheckBox);
        } catch (XulException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    public void show(int row, int col, Object boundObj, String columnBinding, TreeCellEditorCallback callback) {
      this.callback = callback;
      dialog.show();
    }
    
    public void notifyListeners() {
      hide();  
      // Construct a new array list of aggregation based on what user selected
      // pass it to listener
      ArrayList<AggregationType> aggregationTypeList = new ArrayList<AggregationType>(); 
      for(XulComponent component: dialog.getChildNodes()) {
        if(component instanceof XulCheckbox) {
          XulCheckbox checkbox = (XulCheckbox) component;
          if(checkbox.isChecked()) {
            aggregationTypeList.add(AggregationType.valueOf(checkbox.getLabel()));
          }
        }
      }
      this.callback.onCellEditorClosed(aggregationTypeList);
    }
  }
  
  
  private class CustomSampleDataCellEditor implements TreeCellEditor {
    XulDialog dialog = null;
    TreeCellEditorCallback callback = null;

    public CustomSampleDataCellEditor(XulDialog dialog) {
      super();
      this.dialog = dialog;
      dialog.setBgcolor("#FFFFFF");
    }

    public Object getValue() {
      // TODO Auto-generated method stub
      return null;
    }

    public void hide() {
      dialog.hide();
    }

    public void setValue(Object val) {

    }
    public void show(int row, int col, Object boundObj, String columnBinding, TreeCellEditorCallback callback) {
      this.callback = callback;
      ModelDataRow modelDataRow = (ModelDataRow)boundObj;
      XulTreeCol  column = sampleDataTree.getColumns().getColumn(0);
      column.setLabel(modelDataRow.getSampleData());
      List<String> values = modelDataRow.getSampleDataList();
      List<String> sampleDataList = new ArrayList<String>();
      for(int i=1;i<MAX_SAMPLE_DATA_ROWS && i<modelDataRow.getSampleDataList().size();i++) {
        sampleDataList.add(values.get(i));
      }
      sampleDataTree.setElements(sampleDataList);
      sampleDataTree.update();
      dialog.show();
    }
  }
  private class CustomAggregationCellRenderer implements TreeCellRenderer {

    public Object getNativeComponent() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getText(Object value) {
      List<AggregationType> aggregationList = new ArrayList<AggregationType>();
      StringBuffer buffer = new StringBuffer();
      if(value instanceof List) {
        aggregationList.addAll((List) value);
        for(int i=0;i<aggregationList.size();i++) {
        buffer.append(aggregationList.get(i));
          if(i<aggregationList.size()-1) {
          buffer.append(',');  
          }
        }
      }
      return buffer.toString();
    }

    public boolean supportsNativeComponent() {
      // TODO Auto-generated method stub
      return false;
    }
    
  }
}
