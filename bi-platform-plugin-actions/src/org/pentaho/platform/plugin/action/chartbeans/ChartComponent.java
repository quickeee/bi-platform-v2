package org.pentaho.platform.plugin.action.chartbeans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import org.pentaho.chart.ChartBoot;
import org.pentaho.chart.ChartFactory;
import org.pentaho.chart.ChartThemeFactory;
import org.pentaho.chart.InvalidChartDefinition;
import org.pentaho.chart.core.ChartDocument;
import org.pentaho.chart.model.ChartModel;
import org.pentaho.chart.model.ChartModel.ChartTheme;
import org.pentaho.chart.model.util.ChartSerializer;
import org.pentaho.chart.plugin.ChartPluginFactory;
import org.pentaho.chart.plugin.ChartProcessingException;
import org.pentaho.chart.plugin.IChartPlugin;
import org.pentaho.chart.plugin.api.PersistenceException;
import org.pentaho.chart.plugin.api.IOutput.OutputTypes;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;

public class ChartComponent {
  protected static final String DEFAULT_CHART_PLUGIN = "org.pentaho.chart.plugin.jfreechart.JFreeChartPlugin"; //$NON-NLS-1$
  
  protected static final int DEFAULT_CHART_WIDTH = 400;
  protected static final int DEFAULT_CHART_HEIGHT = 300;
  
  protected String seriesColumnName = null;
  protected int seriesColumn = -1;
  
  protected String categoryColumnName = null;
  protected int categoryColumn = -1;
  
  protected String valueColumnName = null;
  protected int valueColumn = -1;
  
  protected IPentahoResultSet resultSet = null;
  
  protected String chartPlugin = null;
  
  protected Exception bootException = null;
  
  protected String outputType = "image-png"; //$NON-NLS-1$
  
  protected int chartWidth = -1;

  protected int chartHeight = -1;
  
  protected OutputStream outputStream = null;
  
  protected String serializedChartModel = null;
  
  protected ChartModel chartModel = null;
  
  //Initialize ChartBeans engine
  {
    synchronized (ChartBoot.getInstance()) {
      while (!ChartBoot.getInstance().isBootDone()) {
        if (ChartBoot.getInstance().isBootInProgress()) {
          // Wait 1 second
          try {
            java.lang.Thread.sleep(1000);
          } catch (InterruptedException e) {
            // Do nothing
          }
        } else {
          if (!ChartBoot.getInstance().isBootFailed()) {
            ChartBoot.getInstance().start();
          }
        }
      }// End while: boot is not done

      //Check for an error
      if (ChartBoot.getInstance().isBootFailed()) {
        bootException = ChartBoot.getInstance().getBootFailureReason();
      }
    }// End thread synchronization
  }
  
  public boolean execute() throws ChartBootException, ChartProcessingException, ResourceException,
    InvalidChartDefinition, IOException, PersistenceException {
    if (bootException != null) {
      throw new ChartBootException(bootException);
    }
    
    // Transform IPentahoResultSet to an object array
    
    Object[][] data = processChartData(resultSet);
    
    try{
      
      ChartThemeFactory chartThemeFactory = new ChartThemeFactory() {
        public ChartDocument getThemeDocument(ChartTheme theme) {
          ChartDocument themeDocument = null;
          File themeFile = null;
          if (theme != null) {
            switch (theme) {
              case THEME1:
                themeFile = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/dashboards/resources/gwt/Theme1.xml"));
                break;
              default:
                themeFile = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/dashboards/resources/gwt/Theme2.xml"));
                break;
            }
            try {
              themeDocument = org.pentaho.chart.ChartFactory.getChartDocument(themeFile.toURL(), true);
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
          return themeDocument;
        }
      };
      
      InputStream is = ChartFactory.createChart(data, valueColumn, seriesColumn, categoryColumn, chartModel, chartWidth, chartHeight, getOutputType(), chartThemeFactory);

      int val = 0;
      
      //TODO: Buffer for more efficiency
      while((val = is.read()) != -1){
        outputStream.write(val);
      }
    } catch(SQLException e){
      //No SQLException possible from this usage
    }
    
    return true;
  }
  
  /**
   * Transform the IPentahoResultSet into the data format suitable
   * for chart creation.
   * 
   * @return Row / Column data table or null
   */
  protected Object[][] processChartData(IPentahoResultSet resultSet){
    if(resultSet == null){
      return null;
    }
    
    Object[][] result = null;
    
    result = new Object[resultSet.getRowCount()][resultSet.getMetaData().getColumnCount()];
    
    for(int r = 0; r < resultSet.getRowCount(); r++){
      for(int c = 0; c < resultSet.getMetaData().getColumnCount(); c++){
        result[r][c] = resultSet.getValueAt(r, c);
      }
    }
    
    return(result);
  }
  
  public void setOutputStream(OutputStream outStream) {
    outputStream = outStream;
  }
  
  public void setChartData(IPentahoResultSet chartDataSet){
    resultSet = chartDataSet.memoryCopy();
  }
  
  public boolean validate() throws Exception{
  //Must have a valid result set
    if(resultSet == null){
      return false;
    }
    
    //Default to the first three columns if no others are explicitly specified
    //Resolve column name to column ordinal if present
    if((seriesColumnName != null) && (!seriesColumnName.equals(""))){ //$NON-NLS-1$
      seriesColumn = resultSet.getMetaData().getColumnIndex(seriesColumnName);
    } else { 
      if(seriesColumn < 0){
        seriesColumn = 0;
      }
    }
    
    if((categoryColumnName != null) && (!categoryColumnName.equals(""))){ //$NON-NLS-1$
      categoryColumn = resultSet.getMetaData().getColumnIndex(categoryColumnName);
    } else {
      if(categoryColumn < 0){
        categoryColumn = 1;
      }
    }
    if((valueColumnName != null) && (!valueColumnName.equals(""))){ //$NON-NLS-1$
      valueColumn = resultSet.getMetaData().getColumnIndex(valueColumnName); 
    } else {
      if(valueColumn < 0){
        valueColumn = 2;
      }
    }
    
    //Verify that ALL columns are valid
    if((seriesColumn < 0) || (categoryColumn < 0) || (valueColumn < 0)){
      return false;
    }
    
    if(chartModel == null){
      if(serializedChartModel != null){
        chartModel = ChartSerializer.deSerialize(serializedChartModel);
      } else {
        // No chart model is available
        return false;
      }
    }
    
    if(chartWidth <= 0){
      chartWidth = DEFAULT_CHART_WIDTH;
    }
    
    if(chartHeight <= 0){
      chartHeight = DEFAULT_CHART_HEIGHT;
    }

    return true;
  }
  
  public void setSeriesColumn(String seriesCol){
    seriesColumnName = seriesCol;
  }

  public void setCategoryColumn(String categoryCol) {
    categoryColumnName = categoryCol;
  }

  public void setValueColumn(String valueCol) {
    valueColumnName = valueCol;
  }
  
  protected IChartPlugin getChartPlugin() throws ChartProcessingException {
    if (chartPlugin == null) {
      chartPlugin = DEFAULT_CHART_PLUGIN;
    }

    return ChartPluginFactory.getInstance(chartPlugin);
  }
  
  protected OutputTypes getOutputType(){
    if(outputType.equals("image-jpg")){ //$NON-NLS-1$
      return OutputTypes.FILE_TYPE_JPEG;
    } else if (outputType.equals("image-png")){ //$NON-NLS-1$
      return OutputTypes.FILE_TYPE_PNG;
    }
    
    return null;    
  }
  
  public String getMimeType(){
      if(outputType.equals("image-jpg")){ //$NON-NLS-1$
        return "image/jpeg"; //$NON-NLS-1$
      } else if (outputType.equals("image-png")){ //$NON-NLS-1$
        return "image/png";  //$NON-NLS-1$
      } else if (outputType.equals("text-html")){ //$NON-NLS-1$
        return "text/html"; //$NON-NLS-1$
      }
      
      return null;
  }
  
  public void setChartModel(String serializedChartModel) {
    this.serializedChartModel = serializedChartModel;
  }
  
  public void setChartModel(ChartModel chartModel){
    this.chartModel = chartModel;
  }
  
  public void setChartWidth(int chartWidth){
    this.chartWidth = chartWidth;
  }
  
  public void setChartHeight(int chartHeight){
    this.chartHeight = chartHeight;
  }
  
  public void setChartWidth(String chartWidth){
    this.chartWidth = Integer.valueOf(chartWidth);
  }
  
  public void setChartHeight(String chartHeight){
    this.chartHeight = Integer.valueOf(chartHeight);
  }
}