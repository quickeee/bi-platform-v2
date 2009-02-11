package org.pentaho.webservice.core.content;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBindingMessage;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MessageContextBuilder;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;
import org.pentaho.webservice.core.AxisConfig;
import org.pentaho.webservice.core.messages.Messages;

/**
 * The base content generator for all of the webservices content generators.
 * This class ensures that the webservices system is properly configured before
 * handing over to a subclass for processing of the request.
 * @author jamesdixon
 *
 */
public abstract class WebServiceContentGenerator extends SimpleContentGenerator {
  
  @Override
  public void createContent( OutputStream out ) throws Exception {

    // make sure this classes class loader is the context classloader
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
      // try to get the AxisConfig object
      AxisConfig config = AxisConfig.getInstance( );
      if( config == null || config.getConfigurationContext() == null || config.getConfigurationContext().getAxisConfiguration() == null ) {
        // return an error
        String message = Messages.getErrorString("WebServiceContentGenerator.ERROR_0001_AXIS_CONFIG_IS_NULL"); //$NON-NLS-1$
        getLogger().error( message );
        out.write( message.getBytes() );
        return;
      }
      
      // get the ConfigurationContext for this config
      ConfigurationContext context = config.getConfigurationContext();
      
      // get the AxisConfiguration for this context
      AxisConfiguration axisConfiguration = context.getAxisConfiguration();

      // hand over to a subclass to process this request
      createContent( axisConfiguration, context, out );
    } finally {
      Thread.currentThread().setContextClassLoader( originalLoader );
    }

  }

  /**
   * Creates content for this request. Subclasses of this class implement this method to
   * handle the processing of a web services request.
   * @param axisConfiguration AxisConfiguration
   * @param context ConfigurationContext
   * @param out The output stream to write to
   * @throws Exception
   */
  public abstract void createContent( AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out ) throws Exception;
  
  /**
   * Handles processing of Axis exceptions.
   * @param msgContext The message context that experienced an error
   * @param out The output stream to write to
   * @param e The error that occurred
   */
  protected void processAxisFault(MessageContext msgContext, OutputStream out, Throwable e) {
    
    IParameterProvider pathParams = parameterProviders.get( "path" ); //$NON-NLS-1$
    // is this HTTP?
    boolean http = msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE) != null ; 
    if( http ) {
      HttpServletResponse res = (HttpServletResponse) pathParams.getParameter( "httpresponse" ); //$NON-NLS-1$
      // If the fault is not going along the back channel we should be 202ing
      if (AddressingHelper.isFaultRedirected(msgContext)) {
        res.setStatus(HttpServletResponse.SC_ACCEPTED);
      } else {
        // set the status of the HTTP response
        String status = (String) msgContext.getProperty(Constants.HTTP_RESPONSE_STATE);
        if (status == null) {
          res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else {
          res.setStatus(Integer.parseInt(status));
        }

        AxisBindingOperation axisBindingOperation = (AxisBindingOperation) msgContext.getProperty(Constants.AXIS_BINDING_OPERATION);
        if (axisBindingOperation != null) {
          AxisBindingMessage fault = axisBindingOperation.getFault((String) msgContext.getProperty(Constants.FAULT_NAME));
          if (fault != null) {
            Integer code = (Integer) fault.getProperty(WSDL2Constants.ATTR_WHTTP_CODE);
            if (code != null) {
              res.setStatus(code.intValue());
            }
          }
        }
      }
    }

    try {
      // now process the fault
      handleFault(msgContext, out, http, e);
    } catch (AxisFault axisFault) {
      String message = Messages.getErrorString("WebServiceContentGenerator.ERROR_0003_PROCESSING_FAULT"); //$NON-NLS-1$
      getLogger().error( message, axisFault );
    }
  }
  
  protected void handleFault(MessageContext msgContext, OutputStream out, boolean http, Throwable e) throws AxisFault {
    
    msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

    MessageContext faultContext =
          MessageContextBuilder.createFaultMessageContext(msgContext, e);
    // SOAP 1.2 specification mentions that we should send HTTP code 400 in a fault if the
    // fault code Sender
    HttpServletResponse response = http ?
          (HttpServletResponse) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE) : null;
    if (response != null) {

      //TODO : Check for SOAP 1.2!
      SOAPFaultCode code = faultContext.getEnvelope().getBody().getFault().getCode();

      OMElement valueElement = null;
      if (code != null) {
          valueElement = code.getFirstChildWithName(new QName(
                  SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                  SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
      }

      if (valueElement != null) {
          if (SOAP12Constants.FAULT_CODE_SENDER.equals(valueElement.getTextAsQName().getLocalPart())
                  && !msgContext.isDoingREST()) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          }
      }
    }

    AxisEngine.sendFault(faultContext);
  }

}