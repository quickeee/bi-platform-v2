package org.pentaho.test.platform.plugin;


import java.io.File;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ResultSetExportComponentTest extends BaseTest {
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
	public void testRSExportComponent() {
	    startTest();
	    IRuntimeContext context = run("test", "rules", "ResultSetExportTest.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

        IActionParameter rtn = context.getOutputParameter("EXPORTRESULT");//$NON-NLS-1$
	    assertNotNull("Result is null",rtn);	 //$NON-NLS-1$
		    
	    //Check that the data, Eastern, is in the result set
	    String content = rtn.getStringValue();
	    assertNotNull( "Exported content is null", content ); //$NON-NLS-1$
	    
	    int containsEastern = content.indexOf("Eastern");          //$NON-NLS-1$ 
        assertEquals("ResultSet export does not contain 'Eastern'", Math.max(containsEastern, -1), containsEastern);		     //$NON-NLS-1$
	    finishTest();
	  }	  
	  
	/*
	 * The test removes the result-set element from the 
	 * action inputs.
	 */
	public void testRSExportComponent_error1() {
	    startTest();
	    IRuntimeContext context = run("test", "rules", "ResultSetExportTest_error1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
	    finishTest();
	}
		  
	/*
	 * The test removes outputs and action-outputs. This causes
	 * ResultSetExportComponent's validateAction() to return false.
	 */
	public void testRSExportComponent_error2() {
	    startTest();
	    IRuntimeContext context = run("test", "rules", "ResultSetExportTest_error2.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
	    finishTest();
	}
		
	public static void main(String[] args) {
		ResultSetExportComponentTest test = new ResultSetExportComponentTest();
		try {
			test.setUp();
		    test.testRSExportComponent();
		    test.testRSExportComponent_error1();
		    test.testRSExportComponent_error2();
		} finally {
		    test.tearDown();
		    BaseTest.shutdown();
		}
	}
}
