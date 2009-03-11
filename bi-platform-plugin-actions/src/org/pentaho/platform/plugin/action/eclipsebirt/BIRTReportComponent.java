/* * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 * *  * @created Jul 25, 2005  * @author James Dixon */package org.pentaho.platform.plugin.action.eclipsebirt;import java.io.FileNotFoundException;import java.io.IOException;import java.io.InputStream;import java.io.OutputStream;import java.util.ArrayList;import java.util.Collection;import java.util.HashMap;import java.util.Iterator;import java.util.Locale;import org.apache.commons.logging.Log;import org.apache.commons.logging.LogFactory;import org.eclipse.birt.report.engine.api.EngineConstants;import org.eclipse.birt.report.engine.api.EngineException;import org.eclipse.birt.report.engine.api.HTMLRenderContext;import org.eclipse.birt.report.engine.api.HTMLRenderOption;import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;import org.eclipse.birt.report.engine.api.IParameterDefn;import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;import org.eclipse.birt.report.engine.api.IRenderOption;import org.eclipse.birt.report.engine.api.IReportEngine;import org.eclipse.birt.report.engine.api.IReportRunnable;import org.eclipse.birt.report.engine.api.IRunAndRenderTask;import org.eclipse.birt.report.engine.api.IScalarParameterDefn;import org.eclipse.birt.report.engine.api.PDFRenderContext;import org.eclipse.birt.report.engine.api.ReportParameterConverter;import org.eclipse.birt.report.engine.api.impl.ScalarParameterDefn;import org.pentaho.actionsequence.dom.ActionInputConstant;import org.pentaho.actionsequence.dom.IActionOutput;import org.pentaho.actionsequence.dom.actions.BirtReportAction;import org.pentaho.platform.api.engine.IActionParameter;import org.pentaho.platform.api.engine.IActionSequenceResource;import org.pentaho.platform.api.engine.IOutputHandler;import org.pentaho.platform.api.engine.IPentahoSession;import org.pentaho.platform.api.engine.IRuntimeContext;import org.pentaho.platform.api.repository.IContentItem;import org.pentaho.platform.api.repository.ISolutionRepository;import org.pentaho.platform.engine.core.system.PentahoSystem;import org.pentaho.platform.engine.services.solution.ComponentBase;import org.pentaho.platform.plugin.action.messages.Messages;/** * @author James Dixon *  * This component uses the BIRT report engine to generate PDF and HTML report * output. It uses inputs provided by the runtime context and writes the output * to the *  */public class BIRTReportComponent extends ComponentBase {  protected static final Log logger = LogFactory.getLog(BIRTReportComponent.class);  protected static IReportEngine reportEngine = null;  private static final long serialVersionUID = -6051215996386090147L;  private static final int OUTPUT_TYPE_HTML = 0;  private static final int OUTPUT_TYPE_PDF = 1;  private static final ArrayList outputTypeStrings = new ArrayList();  private String type;  private int typeIdx = -1;  private String mimeType;  private String extension = ""; //$NON-NLS-1$  private IRenderOption renderOptions;  private IGetParameterDefinitionTask parameterDefinitionTask;  static {    BIRTReportComponent.outputTypeStrings.add("html"); //$NON-NLS-1$    BIRTReportComponent.outputTypeStrings.add("pdf"); //$NON-NLS-1$  }  @Override  public Log getLogger() {    return LogFactory.getLog(BIRTReportComponent.class);  }  @Override  protected boolean validateSystemSettings() {    // This component does not have any system settings to validate    return true;  }  /*   * This validates that the inputs, outputs, and resoruces that are available   * to us are correct. The actual values are not available at this point.   *    * @see org.pentaho.component.ComponentBase#validate()   */  @Override  protected boolean validateAction() {    boolean actionValidated = true;    BirtReportAction reportAction = null;    if (getActionDefinition() instanceof BirtReportAction) {      reportAction = (BirtReportAction) getActionDefinition();      if (BIRTReportComponent.reportEngine == null) {        error(Messages.getErrorString("BIRTReportComponent.ERROR_0016_ENGINE_NOT_INITIALIZED")); //$NON-NLS-1$        actionValidated = false;      }      // Check that we have an input called 'output-typ' that will specified      // 'html' or 'pdf' output      if (actionValidated) {        if (reportAction.getOutputType() != ActionInputConstant.NULL_INPUT) {          String outputType = reportAction.getOutputType().getStringValue();          if (!outputType.equals("html") && !outputType.equals("pdf")) {            actionValidated = false;            error(Messages.getErrorString("BIRT.ERROR_0003_REPORT_TYPE_NOT_VALID", type)); //$NON-NLS-1$                    }        } else {          // We don't know what kind of output to produce, so return an error          actionValidated = false;          error(Messages.getErrorString("BIRT.ERROR_0001_REPORT_TYPE_NOT_SPECIFIED")); //$NON-NLS-1$                }      }    } else {      actionValidated = false;      error(Messages.getErrorString(          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML())); //$NON-NLS-1$    }    // Check that we have a resource called 'report-definition' tht should    // be the BIRT report definition file    if (actionValidated && (reportAction.getReportDefinition() == null)) {      // We don't have a report definition resource, so return an error      error(Messages.getErrorString("BIRT.ERROR_0002_REPORT_DEFINITION_NOT_SPECIFIED")); //$NON-NLS-1$      actionValidated = false;    }    //  Looks like everything is ok so far    return actionValidated;  }  /*   * perform any initialization required   *    * @see org.pentaho.component.ComponentBase#init()   */  @Override  public boolean init() {    // BIRT report engine is initialized by the BIRT System Listener so just return    return true;  }  /*   * Execute the BIRT report using the input, resources and outputs defined.   * If we require parameters to be selected by the user, generate a form   * definition for the user   *    * @see org.pentaho.component.ComponentBase#execute()   */  @Override  protected boolean executeAction() {    // IRuntimeContext context = getRuntimeContext();    // Get the real location of the report definition file    BirtReportAction reportAction = (BirtReportAction) getActionDefinition();    IActionSequenceResource reportDefResource = getResource(reportAction.getReportDefinition().getName());    String reportDefinitionPath = reportDefResource.getAddress();    InputStream reportDefinition;    try {      reportDefinition = PentahoSystem.get(ISolutionRepository.class, getSession()).getResourceInputStream(reportDefResource,          true);    } catch (FileNotFoundException e1) {      error(e1.getLocalizedMessage());      return false;    }    // Get the type of the output    type = reportAction.getOutputType().getStringValue();    typeIdx = BIRTReportComponent.outputTypeStrings.indexOf(type);    if (typeIdx < 0) {      error(Messages.getErrorString("BIRT.ERROR_0003_REPORT_TYPE_NOT_VALID", type)); //$NON-NLS-1$      return false;    }    switch (typeIdx) {      case OUTPUT_TYPE_HTML: {        mimeType = "text/html"; //$NON-NLS-1$        extension = ".html"; //$NON-NLS-1$        renderOptions = new HTMLRenderOption();        break;      }      case OUTPUT_TYPE_PDF: {        mimeType = "application/pdf"; //$NON-NLS-1$        extension = ".pdf"; //$NON-NLS-1$        renderOptions = new HTMLRenderOption();        break;      }      default: {        error(Messages.getErrorString("BIRT.ERROR_0003_REPORT_TYPE_NOT_VALID", type)); //$NON-NLS-1$        return false;      }    }    if (ComponentBase.debug) {      debug(Messages.getString("BIRT.DEBUG_EXECUTING_REPORT", reportDefinitionPath, type)); //$NON-NLS-1$    }    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();    boolean result = false;    try {      // TODO support caching or the ReportRunnable object      // Create an isntance of a runnable report using the report      // definition file provided      IReportRunnable design = BIRTReportComponent.reportEngine.openReportDesign(reportDefinition);      if (design == null) {        return false;      }      //getParameterDefns      //IGetParameterDefinitionTask parameterDefinitionTask = reportEngine.createGetParameterDefinitionTask(design);      parameterDefinitionTask = BIRTReportComponent.reportEngine.createGetParameterDefinitionTask(design);      // Set up the parameters for the report      HashMap parameterMap = new HashMap();      int parameterStatus = setupParameters(parameterDefinitionTask.getParameterDefns(false), parameterMap);      if (parameterStatus == IRuntimeContext.PARAMETERS_FAIL) {        error(Messages.getErrorString("BIRT.ERROR_0005_INVALID_REPORT_PARAMETERS")); //$NON-NLS-1$        return false;      } else if (parameterStatus == IRuntimeContext.PARAMETERS_UI_NEEDED) {        return true;      }      parameterDefinitionTask.close();      // Run task      IRunAndRenderTask runTask = BIRTReportComponent.reportEngine.createRunAndRenderTask(design);      IPentahoSession session = this.getSession();      runTask.setLocale(session.getLocale());      // Get the output stream that the content is going into      OutputStream outputStream = null;      IContentItem contentItem = null;      IActionOutput actionOutput = reportAction.getOutputReport();      if (actionOutput != null) {        if (actionOutput.getName().equals(BirtReportAction.REPORT_OUTPUT_ELEMENT)) {          contentItem = getOutputItem(reportAction.getOutputReport().getName(), mimeType, extension);        } else {          contentItem = getOutputContentItem(actionOutput.getName());          contentItem.setMimeType(mimeType);        }        try {          outputStream = contentItem.getOutputStream(getActionName());        } catch (Exception e) {        }      } else {        // There was no output in the action-sequence document, so make        // a default        // outputStream.        warn(Messages.getString("Base.WARN_NO_OUTPUT_STREAM")); //$NON-NLS-1$        outputStream = getDefaultOutputStream(mimeType);        if (outputStream != null) {          setOutputMimeType(mimeType);        }      }      if (outputStream == null) {        // We could not get an output stream for the content        error(Messages.getErrorString("BIRT.ERROR_0006_INVALID_OUTPUT_STREAM")); //$NON-NLS-1$        return false;      }      // Execute the report      try {        result = generateReport(parameterMap, outputStream, runTask, baseUrl);      } finally {        if (contentItem != null) {          contentItem.closeOutputStream();        }      }    } catch (Throwable e) {      error(Messages.getErrorString("BIRT.ERROR_0007_REPORT_ERRORS_ENCOUNTERED", reportDefinitionPath), e); //$NON-NLS-1$    }    return result;  }  /*   * Create an instance of the EngineConfig class for the BIRT report engine.   */  /*   * Set up the report parameters   */  private int setupParameters(final Collection params, final HashMap reportParameterMap) {    try {      boolean parameterUINeeded = false;      if (getOutputPreference() == IOutputHandler.OUTPUT_TYPE_PARAMETERS) {        parameterUINeeded = true;      }      // Process each parameter in turn      Iterator it = params.iterator();      while (it.hasNext()) {        IParameterDefn param = (IParameterDefn) it.next();        ScalarParameterDefn scalarParm = null;        if (param instanceof ScalarParameterDefn) {          scalarParm = (ScalarParameterDefn) param;        }        String paramName = param.getName();        String defaultValue = ""; //$NON-NLS-1$        if (scalarParm != null) {          defaultValue = scalarParm.getDefaultValue();        }        if (!isDefinedInput(paramName)) {          // what do we do here?          continue;        }        String contextValue = getInputStringValue(paramName);        // Get the value for the parameter from the current context        IActionParameter paramParameter = getInputParameter(paramName);        // if(debug)        // debug(Messages.getString("BIRT.DEBUG_SETTING_PARAMETER",        // paramName, paramValue )); //$NON-NLS-1$        // There is no parameter in the runtime or the parameter found        // has no value and no selections        if ((paramParameter == null)            || ((!paramParameter.hasValue() || parameterUINeeded) && !paramParameter.hasSelections())) {          if (paramParameter.getPromptStatus() == IActionParameter.PROMPT_PENDING) {            parameterUINeeded = true;            continue;          } else {            paramParameter = null;          }        }        if (paramParameter == null) { // Still no parameter, use BIRTs          // default way          if ((scalarParm != null)) {            if (parameterUIOk()) {              // The parameter value was not provided, and we are              // allowed to create user interface forms              int controlType = scalarParm.getControlType();              if ((controlType == IScalarParameterDefn.LIST_BOX)) {                ArrayList values = new ArrayList();                HashMap displayNames = new HashMap();                Collection coll = parameterDefinitionTask.getSelectionList(scalarParm.getName());                if (coll != null) {                  Iterator valueIterator = coll.iterator();                  while (valueIterator.hasNext()) {                    IParameterSelectionChoice selectionItem = (IParameterSelectionChoice) valueIterator.next();                    Object value = selectionItem.getValue();                    if (value != null) {                      String label = selectionItem.getLabel();                      displayNames.put(value.toString(), (label != null) ? label : value);                      values.add(value);                    }                  }                }                createFeedbackParameter(param.getName(), /*param.getDisplayName()*/scalarParm.getPromptText(),                    scalarParm.getHelpText(), defaultValue, values, displayNames, null);              } else {                createFeedbackParameter(param.getName(), /*param.getDisplayName()*/scalarParm.getPromptText(),                    scalarParm.getHelpText(), defaultValue, true);              }              parameterUINeeded = true;              continue;            } else {              return IRuntimeContext.PARAMETERS_FAIL;            }          } else if (defaultValue.length() == 0) {            // If the length of the defaultValue is not zero, then:            // 1- It's a scalar parameter            // 2- There's a default value expression associated with            // the parameter.            // Therefore, let BIRT use that expression            // automatically.            // The parameter was not provided and we are not allowed            // to create forms            error(Messages.getErrorString("BIRT.ERROR_0009_PARAMETER_NOT_PROVIDED", paramName)); //$NON-NLS-1$            return IRuntimeContext.PARAMETERS_FAIL;          }        }        String paramValue = contextValue;        if ((paramValue != null) && (!"".equals(paramValue))) { //$NON-NLS-1$          if (scalarParm != null) {            String format = scalarParm.getDisplayFormat();            ReportParameterConverter cfgConverter = new ReportParameterConverter(format, Locale.getDefault());            Object configValueObj = cfgConverter.parse(paramValue, scalarParm.getDataType());            reportParameterMap.put(paramName, configValueObj);          } else {            reportParameterMap.put(paramName, paramValue);          }        }      }      if (parameterUINeeded) {        return IRuntimeContext.PARAMETERS_UI_NEEDED;      } else if (getRuntimeContext().isPromptPending()) {        return IRuntimeContext.PARAMETERS_UI_NEEDED;      }      return IRuntimeContext.PARAMETERS_OK;    } catch (Throwable error) {      error(Messages.getErrorString("BIRT.ERROR_0005_INVALID_REPORT_PARAMETERS"), error); //$NON-NLS-1$    }    return IRuntimeContext.PARAMETERS_FAIL;  }  private boolean parameterUIOk() {    /*     * See if we are allowed to generate a parameter selection user     * interface. If we are being called as part of a process, this will not     * be allowed.     */    if (!feedbackAllowed()) {      return false;    }    // We need input from the user, we have delivered an input form into the    // feeback stream    setFeedbackMimeType("text/html"); //$NON-NLS-1$    return true;  }  /*   * Generate the report output   */  private boolean generateReport(final HashMap parameterMap, final OutputStream outputStream,      final IRunAndRenderTask task, final String baseUrl) {    try {      HashMap appContextMap = new HashMap();      // Set the output options for the report      if (typeIdx == BIRTReportComponent.OUTPUT_TYPE_HTML) {        HTMLRenderContext htmlContext = new HTMLRenderContext();        htmlContext.setBaseImageURL(baseUrl + "getImage?image="); //$NON-NLS-1$        htmlContext.setImageDirectory(PentahoSystem.getApplicationContext().getSolutionPath("system/tmp")); //$NON-NLS-1$        htmlContext.setSupportedImageFormats("PNG;GIF;JPG;BMP"); //$NON-NLS-1$        appContextMap.put(EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT, htmlContext);      } else if (typeIdx == BIRTReportComponent.OUTPUT_TYPE_PDF) {        PDFRenderContext pdfContext = new PDFRenderContext();        pdfContext.setBaseURL(baseUrl + "getImage?image="); //$NON-NLS-1$        pdfContext.setSupportedImageFormats("PNG;GIF;JPG;BMP"); //$NON-NLS-1$        appContextMap.put(EngineConstants.APPCONTEXT_PDF_RENDER_CONTEXT, pdfContext);      }      task.setAppContext(appContextMap);      task.setRenderOption(renderOptions);      renderOptions.setOutputFormat(type);      // Set the output stream for the content      renderOptions.setOutputStream(outputStream);      task.setParameterValues(parameterMap);      if (ComponentBase.debug) {        debug(task.toString());      }      task.run();      outputStream.flush();      outputStream.close();      return true;    } catch (IOException ioe) {      error(null, ioe);      error(Messages.getErrorString("BIRT.ERROR_0010_REPORT_COULD_NOT_BE_RUN", ioe.getMessage())); //$NON-NLS-1$    } catch (EngineException ee) {      error(null, ee);      error(Messages.getErrorString("BIRT.ERROR_0010_REPORT_COULD_NOT_BE_RUN", ee.getMessage())); //$NON-NLS-1$    }    return false;  }  /*   * Perform any cleanup necessary   *    * @see org.pentaho.component.ComponentBase#done()   */  @Override  public void done() {    // we don't have anything to clean up to do  }  /* DM This should be removed when the JPivot/BIRT conflict is resolved   * Create an instance of the EngineConfig class for the BIRT report engine.      private IReportEngine createBIRTEngine() {   try {   // Get the global settings for the BIRT engine from our system settings   String birtHome = PentahoSystem.getApplicationContext().getSolutionPath("system/BIRT"); //$NON-NLS-1$   birtHome = birtHome.replaceAll("\\\\.\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$   if (PentahoSystem.debug)   Logger.debug(BirtSystemListener.class.getName(), Messages.getString("BIRT.DEBUG_BIRT_HOME", birtHome)); //$NON-NLS-1$   // Create an appropriate Config object   EngineConfig config = new EngineConfig();   config.setEngineHome(birtHome); // Configuring where BIRT engine is installed      // Set the directory where the BIRT log files will go   String logDest = PentahoSystem.getApplicationContext().getFileOutputPath("system/logs/BIRT"); //$NON-NLS-1$   // Set the logging level   int loggingLevel = Logger.getLogLevel();   if (loggingLevel == ILogger.TRACE) {   config.setLogConfig(logDest, Level.ALL);   } else if (loggingLevel == ILogger.DEBUG) {   config.setLogConfig(logDest, Level.FINE);   } else if (loggingLevel == ILogger.INFO) {   config.setLogConfig(logDest, Level.INFO);   } else if (loggingLevel == ILogger.WARN) {   config.setLogConfig(logDest, Level.WARNING);   } else if (loggingLevel == ILogger.ERROR) {   config.setLogConfig(logDest, Level.SEVERE);   } else if (loggingLevel == ILogger.FATAL) {   config.setLogConfig(logDest, Level.SEVERE);   }   // Register new image handler   HTMLEmitterConfig emitterConfig = new HTMLEmitterConfig( );   emitterConfig.setActionHandler( new HTMLActionHandler( ) );   emitterConfig.setImageHandler( new HTMLServerImageHandler( ) );   config.getEmitterConfigs( ).put( RenderOptionBase.OUTPUT_FORMAT_HTML, emitterConfig );       Platform.startup( config );   IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY );   IReportEngine engine = factory.createReportEngine( config );   return engine;   }    catch (Throwable error) {   error.printStackTrace();   Logger.error(BirtSystemListener.class.getName(), Messages.getErrorString("BIRT.ERROR_0008_INVALID_CONFIGURATION")); //$NON-NLS-1$   }   return null;   }   */}