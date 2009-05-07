package org.pentaho.test.platform.plugin.services.webservices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.axis2.engine.AxisConfiguration;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SimpleSystemSettings;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemStartupSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.AxisWebServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.plugin.services.webservices.content.HtmlAxisServiceLister;
import org.pentaho.platform.plugin.services.webservices.content.StyledHtmlAxisServiceLister;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;
import org.pentaho.platform.util.web.SimpleUrlFactory;

@SuppressWarnings( { "all" })
public class HtmlServiceListerTest extends TestCase {

  public HtmlServiceListerTest() {
    super();
    String path = "./test-src/solution/system";
    File resourceDir = new File(path);
    if (!resourceDir.exists()) {
      System.err.println("The file system resources cannot be found");
    }
    System.out.println("File system resources located: " + resourceDir.getAbsolutePath());

    // create an object factory
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    // specify the objects we will use
    factory.defineObject(ISolutionEngine.class.getSimpleName(), SolutionEngine.class.getName(), Scope.LOCAL);
    factory.defineObject("systemStartupSession", SystemStartupSession.class.getName(), Scope.LOCAL);
    factory.defineObject(IPluginResourceLoader.class.getSimpleName(), PluginResourceLoader.class.getName(),
        Scope.GLOBAL);
    PentahoSystem.setObjectFactory(factory);

    // create a settings object.
    SimpleSystemSettings settings = new SimpleSystemSettings();
    settings.addSetting("pentaho-system", "");
    PentahoSystem.setSystemSettingsService(settings);

    // specify the startup listeners
    List<IPentahoSystemListener> listeners = new ArrayList<IPentahoSystemListener>();
    PentahoSystem.setSystemListeners(listeners);

    StandaloneApplicationContext app = new StandaloneApplicationContext(path, "");
    app.setBaseUrl("http://localhost:8080/pentaho/");

    // initialize the system
    boolean initOk = PentahoSystem.init(app);

    ((PluginResourceLoader) PentahoSystem.get(IPluginResourceLoader.class, null)).setRootDir(new File(
        "./test-src/solution/system/webservices"));
  }

  public void testRender() throws Exception {

    StandaloneSession session = new StandaloneSession("test"); //$NON-NLS-1$

//    WebServicesInitializer initializer = new WebServicesInitializer();
//    initializer.loaded();

    StyledHtmlAxisServiceLister contentGenerator = new StyledHtmlAxisServiceLister();
    assertNotNull("contentGenerator is null", contentGenerator); //$NON-NLS-1$

    assertNotNull("Logger is null", contentGenerator.getLogger()); //$NON-NLS-1$

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler(out, false);

    String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParams);
    SimpleUrlFactory urlFactory = new SimpleUrlFactory(baseUrl + "?"); //$NON-NLS-1$
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler(outputHandler);
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener(mimeTypeListener);
    contentGenerator.setMessagesList(messages);
    contentGenerator.setParameterProviders(parameterProviders);
    contentGenerator.setSession(session);
    contentGenerator.setUrlFactory(urlFactory);
    try {
      contentGenerator.createContent();
      String content = new String(out.toByteArray());
      System.out.println(content);

      assertTrue("style is missing", content.indexOf(".h1") != -1); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue("style is missing", content.indexOf("text/css") != -1); //$NON-NLS-1$ //$NON-NLS-2$

      assertTrue("WSDL URL is missing", content.indexOf("/content/ws-wsdl") != -1); //$NON-NLS-1$
      assertTrue("WSDL URL is missing", content.indexOf("/content/ws-wsdl/DatasourceService") != -1); //$NON-NLS-1$ //$NON-NLS-2$

      assertTrue("Run URL is missing", content.indexOf("/content/ws-run/DatasourceService") != -1); //$NON-NLS-1$ //$NON-NLS-2$

      assertTrue(
          "Title is missing", content.indexOf(Messages.getString("DatasourceServiceWrapper.USER_DATASOURCE_SERVICE_TITLE")) != -1); //$NON-NLS-1$ //$NON-NLS-2$

    } catch (Exception e) {
      assertTrue("Exception occurred", false); //$NON-NLS-1$
    }
  }
  
  public void testRender2() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    StubServiceSetup2 setup = new StubServiceSetup2();
    setup.setSession(session);
    
    fail("FIXME");
//    AxisConfigurationContextProvider.getInstance( setup );
//    
//    HtmlAxisWebServiceLister contentGenerator = new HtmlAxisWebServiceLister();
//    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
//    
//      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
//      
//      ByteArrayOutputStream out = new ByteArrayOutputStream();
//      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
//      
//      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
//      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
//      SimpleParameterProvider requestParams = new SimpleParameterProvider();
//      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
//        SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl+"?" ); //$NON-NLS-1$
//      List<String> messages = new ArrayList<String>();
//      contentGenerator.setOutputHandler(outputHandler);
//      MimeTypeListener mimeTypeListener = new MimeTypeListener();
//      outputHandler.setMimeTypeListener(mimeTypeListener);
//      contentGenerator.setMessagesList(messages);
//      contentGenerator.setParameterProviders(parameterProviders);
//      contentGenerator.setSession(session);
//      contentGenerator.setUrlFactory(urlFactory);
//      try {
//          contentGenerator.createContent();
//          String content = new String( out.toByteArray() );
//        System.out.println( content );
//        
//        assertTrue( "Messages is missing", content.indexOf( Messages.getString("ListServices.USER_NO_SERVICES") ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
//      } catch (Exception e) {
//        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
//      }
  }
  
  public void testRender3() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

     StubServiceSetup setup = new StubServiceSetup();
      setup.setSession(session);

      AxisConfiguration axisConfig = AxisWebServiceManager.currentAxisConfiguration;
      axisConfig.getService( "StubService3" ).setActive( false ); //$NON-NLS-1$


//    AxisConfig config = AxisConfig.getInstance();

    HtmlAxisServiceLister contentGenerator = new HtmlAxisServiceLister();
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
    
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
        SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl+"?" ); //$NON-NLS-1$
      List<String> messages = new ArrayList<String>();
      contentGenerator.setOutputHandler(outputHandler);
      MimeTypeListener mimeTypeListener = new MimeTypeListener();
      outputHandler.setMimeTypeListener(mimeTypeListener);
      contentGenerator.setMessagesList(messages);
      contentGenerator.setParameterProviders(parameterProviders);
      contentGenerator.setSession(session);
      contentGenerator.setUrlFactory(urlFactory);
      try {
          contentGenerator.createContent();
          String content = new String( out.toByteArray() );
        System.out.println( content );
        assertTrue( "WSDL URL is missing", content.indexOf( "/content/ws-wsdl" ) != -1 ); //$NON-NLS-1$
        assertTrue( "Test Service URL is missing", content.indexOf("/content/ws-run/StubService" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue( "setString is missing", content.indexOf( "setString" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue( "getString is missing", content.indexOf( "getString" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

      } catch (Exception e) {
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

}