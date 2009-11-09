package org.pentaho.test.platform.engine.services.actions;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.ActionDelegate;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.core.PluginManagerAdapter;
import org.pentaho.test.platform.engine.services.ServiceTestHelper;

/**
 * This JUnit test verifies the proper functioning of IActions as surrogate components.
 * Let's not fool ourselves, these are not really unit tests, rather they are integration
 * tests created at as low altitude as possible to verify the correct functioning of
 * the ActionDelegate and proper execution of IAction.  It would be too complex and 
 * probably uselessly fragile to write real unit tests for the ActionDelegate.
 * <p>
 * ActionDelegate is the class that bridges the heavy IComponent layer with the lightweight
 * IAction.  Essentially it brokers and manages IActions, so it is the actual unit under
 * test with respect to this set of JUnit tests.  A test IAction, TestAction, is used to 
 * verify that ActionDelegate is working properly.
 * <p>
 * NOTE: the only way to get around having to define a ISolutionRepository here (which
 * is out of scope of this project) and still be able to test action sequence resources
 * is to use only embedded resources in your test xactions.
 * It is also very important that ActionDelegate use the getInputStream
 * API in actionsequence-dom to fetch resources, rather than getDataSource
 * (which does not support embedded resources).
 * 
 * @see ActionDelegate
 * @see IAction
 * @see IStreamingAction
 */
@SuppressWarnings("nls")
public class ActionDelegateTest {
  private MicroPlatform booter;

  private ByteArrayOutputStream out;

  IOutputHandler outputHandler;

  //this list is here merely to provide a static accessor to the test action object
  public static List<IAction> actionList = new ArrayList<IAction>();

  private static Map<String, String> veggieDataExpected = new HashMap<String, String>();

  private static List<Map<String, String>> fruitDataExpected = new ArrayList<Map<String, String>>();
  {
    veggieDataExpected.clear();
    veggieDataExpected.put("name", "carrot");
    veggieDataExpected.put("color", "orange");
    veggieDataExpected.put("shape", "cone");
    veggieDataExpected.put("texture", "bumpy");

    Map<String, String> orange = new HashMap<String, String>();
    orange.put("name", "orange");
    orange.put("color", "orange");
    orange.put("shape", "sphere");
    orange.put("texture", "dimply");
    Map<String, String> grapefruit = new HashMap<String, String>();
    grapefruit.put("name", "grapefruit");
    grapefruit.put("color", "Yellow");
    grapefruit.put("shape", "sphere");
    grapefruit.put("texture", "dimply");
    Map<String, String> cucumber = new HashMap<String, String>();
    cucumber.put("name", "cucumber");
    cucumber.put("color", "green");
    cucumber.put("shape", "ellipsoid");
    cucumber.put("texture", "smooth");
    fruitDataExpected.clear();
    fruitDataExpected.add(orange);
    fruitDataExpected.add(grapefruit);
    fruitDataExpected.add(cucumber);
  }

  @Before
  public void init() throws PlatformInitializationException {
    System.setProperty("log4j.logger.org.pentaho", "INFO");
    LogFactory.getLog("test").info("i'm here");

    booter = new MicroPlatform();
    booter.define(ISolutionEngine.class, SolutionEngine.class, Scope.GLOBAL);
    booter.define(IPluginManager.class, TestPluginManager.class, Scope.GLOBAL);
    booter.define("systemStartupSession", StandaloneSession.class, Scope.GLOBAL);

    actionList.clear();
    actionList.add(new TestAllIOAction());

    booter.start();
    PentahoSystem.get(ISolutionEngine.class).setLoggingLevel(ILogger.DEBUG);
  }
  
  @Test
  public void testIndexedInputs() {
    TestIndexedInputsAction action1 = new TestIndexedInputsAction();
    
    execute("testIndexedInputs.xaction", action1);
    
    
    
    assertTrue("messages list should have elements", action1.getAllMessages().size() > 0);
    assertTrue("otherMessages list should have elements", action1.getOtherMessages().size() > 0);
    
    for(int i=0; i < 3; i++) {
      assertEquals("action string type input \"messages_"+i+"\" is incorrect/not set", "indexed messages_"+i+" text", action1.getMessages(i));
      assertEquals("action string type input \"otherMessages_"+i+"\" is incorrect/not set", "other indexed messages_"+i+" text", action1.getOtherMessages().get(i));
    }
    
    assertEquals("action string type input \"scalarMessage\" is incorrect/not set", "scalar message text", action1.getTextOfScalarMessage());
  }

  @Test
  public void testMappedInput() {
    TestAction action1 = new TestAction();
    action1.setMessageBoard("Action 1 was here!");
    TestAction action2 = new TestAction();

    execute("testMappedInput.xaction", action1, action2);

    assertEquals("action1 string type input \"message\" is incorrect/not set", "message text", action1.getMessage());
    assertEquals("action2 string type input \"message\" is incorrect/not set", "internalMessage text", action2
        .getMessage());
    assertEquals("should see the message from action1 here", "Action 1 was here!", action2.getMessageBoard());
  }

  @Test
  public void testVarArgs() {
    TestVarArgsAction action = new TestVarArgsAction();

    execute("testVarArgs.xaction", action);

    //first check that normal bean inputs are working
    assertEquals("action1 string type input \"message\" is incorrect/not set", "message text", action.getMessage());

    //then see if all the rest are passed via VarArgs
    assertNotNull("varArgs not set", action.getVarArgs());
    assertEquals("incorrect number of varArgs set", 2, action.getVarArgs().size());
    assertTrue("varArg1 was not set", action.getVarArgs().containsKey("varArg1"));
    assertEquals("varArg1 has incorrect value", "varArg1 text", action.getVarArgs().get("varArg1"));
    assertTrue("varArg2 was not set", action.getVarArgs().containsKey("varArg2"));
    assertNull("varArg2 has incorrect value", action.getVarArgs().get("varArg2"));
  }

  @Test
  public void testComponentDefinitionInputs() {
    TestAction action = new TestAction();

    execute("testComponentDefinitionInputs.xaction", action);

    assertEquals("string type input \"embeddedMessage\" is incorrect/not set", "embedded message text", action
        .getEmbeddedMessage());
    assertEquals("numeric type input \"embeddedNumber\" is incorrect/not set", new Integer(2001), action
        .getEmbeddedNumber());
    assertNull(
        "bad numeric \"badEmbeddedNumber\" should not have been set, is [" + action.getBadEmbeddedNumber() + "]",
        action.getBadEmbeddedNumber());

    /*
     * Elements with only a text node and no sub-elements are treated by ASD as normal inputs.  However, if an element
     * in the component-definition has sub-elements, ASD will not return it as an input, so this test will verify that
     * current behavior -- that a bean property for a complex element will not be set.
     */
    assertNull(
        "complex input (input with sub-elements) \"complexInputWithSubEelements\" is not currently supported as an input to an Action",
        action.getComplexInputWithSubEelements());
  }

  @Test
  public void testCustomTypeIO() {
    TestAction.CustomType testCustomType = new TestAction.CustomType();
    TestAction action1 = new TestAction();
    action1.setCustom(testCustomType);
    TestAction action2 = new TestAction();

    execute("testCustomTypeIO.xaction", action1, action2);

    assertSame("custom type object should have been passed from action1 to action2", testCustomType, action2
        .getCustom());
  }

  /**
   * Here we are testing that actions can handle the old convention of using dashes instead of camelCase,
   * for action sequence inputs and outputs.
   */
  @Test
  public void testCompatibilityMode() {
    TestAllIOAction action = new TestAllIOAction();

    execute("testCompatibilityMode.xaction", action);

    assertEquals("string type input \"message\" is incorrect/not set", "Test 1..2..3", action.getMessage());

    assertNotNull("property-map type input \"veggie-data\" is not set", action.getVeggieData());

    assertNotNull("resource \"embedded-xml-resource\" is incorrect/not set", action.getEmbeddedXmlResource());

    assertEquals("output \"echo-message\" is not set or incorrect", "Test String Output", action.getEchoMessage());
  }

  @Test
  public void testLogging() {
    TestLoggingSessionAwareAction action = new TestLoggingSessionAwareAction();

    execute("testLoggingSessionAware.xaction", action);

    assertNotNull("logger was not set on action", action.getLogger());
  }

  @Test
  public void testSessionAwareness() {
    TestLoggingSessionAwareAction action = new TestLoggingSessionAwareAction();

    execute("testLoggingSessionAware.xaction", action);

    assertNotNull("session was not set on action", action.getSession());
  }

  @Test
  public void testDefinitionAwareness() {
    TestDefinitionPreProcessingAction action = new TestDefinitionPreProcessingAction();

    execute("testActionIOAllTypes.xaction", action);

    assertTrue("message should be in input list", action.getInputNames().contains("message"));
    assertTrue("addresses should be in input list", action.getInputNames().contains("addressees"));
    assertTrue("echoMessage should be in output list", action.getOutputNames().contains("echoMessage"));
    assertTrue("myContentOutput should be in output list", action.getOutputNames().contains("myContentOutput"));
  }

  @Test
  public void testPreProcessing() {
    TestDefinitionPreProcessingAction action = new TestDefinitionPreProcessingAction();

    assertFalse("pre-execution method was called too early", action.isDoPreExecutionWasCalled());
    assertFalse("execute method was called too early", action.isExecuteWasCalled());

    execute("testActionIOAllTypes.xaction", action);

    assertTrue("pre-execution method was not called", action.isDoPreExecutionWasCalled());
    assertTrue("execute method was not called", action.isExecuteWasCalled());
  }

  @Test
  public void testStreaming() throws PlatformInitializationException, FileNotFoundException {
    TestStreamingAction action1 = new TestStreamingAction();

    assertNull(action1.getMyContentOutputStream());

    execute("testStreaming.xaction", action1);

    assertNotNull("output stream was not set on action1", action1.getMyContentOutputStream());

    assertTrue(
        "the fact that we are executing an IStreamingAction should have caused the responseExpected flag to be set",
        outputHandler.isResponseExpected());

    assertEquals("string type input \"message\" is incorrect/not set", "message input text", action1.getMessage());

    System.out.println(out.toString());
  }

  @Test
  public void testNonStreamingContentOutput() throws PlatformInitializationException, FileNotFoundException {
    TestStreamingAction action1 = new TestStreamingAction();

    assertNull(action1.getMyContentOutputStream());

    execute("testContentNonStreamingOutput.xaction", action1);

    assertNull("output stream should not have been set on action1", action1.getMyContentOutputStream());
    assertNotNull("we expect some content here", action1.getMyContentOutput());
    assertEquals("this content is not written out as a normal non-streamed output", action1.getMyContentOutput()
        .toString());
  }

  /*
   * Here we specify an content type output with coming from the "request" destination, which is unsupported by
   * current IOutputHandler implementations.
   */
  @Test
  public void testUnsupportedContentOutput() throws PlatformInitializationException, FileNotFoundException {
    TestStreamingAction action1 = new TestStreamingAction();

    assertNull(action1.getMyContentOutputStream());

    execute("testUnsupportedContentOutput.xaction", action1);

    //by this point, we should see a nice stack trace in the log indicating the (expected) error
    assertNull("output stream was set on action1", action1.getMyContentOutputStream());
  }

  @Test
  public void testActionIOAllTypes() throws PlatformInitializationException, FileNotFoundException {
    TestAllIOAction action = new TestAllIOAction();
    execute("testActionIOAllTypes.xaction", action);

    //
    //Check inputs
    //
    assertEquals("string type input \"message\" is incorrect/not set", "Test 1..2..3", action.getMessage());

    assertArrayEquals("addresseess input is incorrect/not set", new String[] { "joe", "suzy", "fred", "sam" }, action
        .getAddressees().toArray());
    assertEquals("long type input \"count\" is incorrect/not set", new Long(99), action.getCount());

    assertNotNull("property-map type input \"veggieData\" is not set", action.getVeggieData());
    assertMapsEquivalent("property-map type input \"veggieData\" is incorrect", veggieDataExpected, action
        .getVeggieData());

    assertNotNull("property-map-list type input \"fruitData\" is not set", action.getFruitData());
    assertEquals("property-map-list type input \"fruitData\" wrong size", fruitDataExpected.size(), action
        .getFruitData().size());
    for (int i = 0; i < fruitDataExpected.size(); i++) {
      assertMapsEquivalent("property-map-list type input \"fruitData\" list element [" + i + "] is incorrect",
          fruitDataExpected.get(i), action.getFruitData().get(i));
    }

    //
    //Check resources
    //
    assertNotNull("resource \"embeddedXmlResource\" is incorrect/not set", action.getEmbeddedXmlResource());

    //
    //Check outputs
    //
    assertEquals("output \"echoMessage\" is not set or incorrect", "Test String Output", action.getEchoMessage());

    //
    //Check that that the various methods were invoked
    //
    assertTrue("execute method was not invoked", action.isExecuteWasCalled());

  }

  private static boolean assertMapsEquivalent(String comment, Map<String, String> expected, Map<String, String> actual) {
    for (Map.Entry<String, String> entry : expected.entrySet()) {
      String expectedKey = entry.getKey();
      String expectedVal = entry.getValue();
      assertNotNull(comment + ": entry for key [" + expectedKey + "] was expect and not found", actual.get(expectedKey));
      assertEquals(comment, expectedVal, actual.get(expectedKey));
    }
    return true;

  }

  public static class TestPluginManager extends PluginManagerAdapter {
    private ArrayList<IAction> actions = new ArrayList<IAction>();

    private int actionIdx = 0;

    @Override
    public boolean isBeanRegistered(String beanId) {
      return true;
    }

    public void addAction(IAction action) {
      this.actions.add(action);
    }

    @Override
    public Object getBean(String beanId) throws PluginBeanException {
      return actions.get(actionIdx++);
    }
  }

  @SuppressWarnings("unchecked")
  private void execute(String actionSequenceFile, IAction... actions) {
    TestPluginManager pm = (TestPluginManager) PentahoSystem.get(IPluginManager.class);
    for (IAction action : actions) {
      pm.addAction(action);
    }

    //content outputs will write to this stream
    out = new ByteArrayOutputStream();

    /* 
     * create SimpleOutputHandler (to handle outputs of type "response.content")
     */
    outputHandler = new SimpleOutputHandler(out, false);
    outputHandler.setOutputPreference(IOutputHandler.OUTPUT_TYPE_DEFAULT);

    IPentahoSession session = new StandaloneSession("system");
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    outputHandler.setSession(session);

    String xactionStr = ServiceTestHelper.getXAction("test-src/solution/test/ActionDelegateTest", actionSequenceFile);

    /*
     * execute the action sequence, providing the outputHandler created above
     */
    solutionEngine.execute(xactionStr, actionSequenceFile, "action sequence to test the TestAction", false, true, null,
        false, new HashMap(), outputHandler, null, new SimpleUrlFactory(""), new ArrayList());
  }

}
