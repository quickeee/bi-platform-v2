package org.pentaho.test.platform.plugin;


import java.io.File;
import java.util.Map;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class PivotViewComponentTest extends BaseTest {

  private static final String SOLUTION_PATH = "projects/actions/test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH);
      return SOLUTION_PATH;
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);
      return ALT_SOLUTION_PATH;
    }
  }
  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put( "mondrian", "mondrian" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }
  
  public void testQuery1() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "analysis", "query1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
  
  public void testQueryWithoutTitle() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "analysis", "query_without_title.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
  public void testQueryWithoutMode() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "analysis", "query_without_mode.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
  public void testQueryWithoutMdx() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "analysis", "query_without_mdx.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
  public void testQueryWithoutModel() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "analysis", "query_without_model.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
  public void testQueryWithoutOption() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "analysis", "query_without_options.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
 
  public void testQueryWithoutUrl() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "analysis", "query_without_url.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
  
  public void testQueryWithoutConnection() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "analysis", "query_without_connection.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
  
  public void testQueryWithModeExecute() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    parameterProvider.setParameter("mode", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    IRuntimeContext context = run("test", "analysis", "query1.xaction", null, false, parameterProvider,null, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
  
  public void testQuery2() {
    startTest();
    info("Expected: Successful test of query1"); //$NON-NLS-1$
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    parameterProvider.setParameter("charttype", "Bar"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("showgrid", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartlocation", "/chart.html"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartwidth", "333"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartheight", "222"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("charttitle", "My Chart"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartbackgroundr", "10"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartbackgroundg", "20"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartbackgroundb", "30"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("charttitlefontfamily", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("charttitlefontstyle", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("charttitlefontsize", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("charthorizaxislabel", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartvertaxislabel", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartaxislabelfontfamily", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartaxislabelfontstyle", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartaxislabelfontsize", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartaxistickfontfamily", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    
    parameterProvider.setParameter("chartaxistickfontstyle", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartaxistickfontsize", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartaxisticklabelrotation", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartshowlegend", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartlegendlocation", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartlegendfontfamily", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartlegendfontsize", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartshowslicer", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartslicerlocation", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartsliceralignment", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartslicerfontfamily", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartslicerfontstyle", "execute"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("chartslicerfontsize", "execute"); //$NON-NLS-1$//$NON-NLS-2$
    
    IRuntimeContext context = run("test", "analysis", "query1.xaction", null, false, parameterProvider,null, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
   
    assertTrue(true);

    finishTest();    
  }
  
  public static void main(String[] args) {
    PivotViewComponentTest test = new PivotViewComponentTest();
    try {
      test.setUp();
      test.testQuery1();
      test.testQueryWithoutConnection();
      test.testQueryWithoutMdx();
      test.testQueryWithoutMode();
      test.testQueryWithoutModel();
      test.testQueryWithoutOption();
      test.testQueryWithoutTitle();
      test.testQueryWithoutUrl();
      test.testQueryWithModeExecute();
      test.testQuery2();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
