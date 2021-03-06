package org.pentaho.test.platform.engine.core;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;

@SuppressWarnings( { "all" })
public class BootTest {

  @Test
  public void testBoot() throws Exception {
    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath("test-src/solution");

    IPentahoObjectFactory factory = boot.getFactory();
    assertNotNull("object factory is null", factory);

    assertTrue("object factory not definable", factory instanceof IPentahoDefinableObjectFactory);

    boot.define(ISolutionEngine.class.getSimpleName(), Object1.class.getName(),
        IPentahoDefinableObjectFactory.Scope.GLOBAL);
    boot.define("MyObject", Object1.class.getName(), IPentahoDefinableObjectFactory.Scope.GLOBAL);
    boot.define("MyObject", Object2.class.getName(), IPentahoDefinableObjectFactory.Scope.GLOBAL);

    assertFalse(boot.isInitialized());

    boolean ok = boot.start();

    assertNull(boot.getSettingsProvider());

    assertTrue(boot.isInitialized());
    assertTrue(ok);

    factory = boot.getFactory();
    Object2 object = factory.get(Object2.class, "MyObject", null);

    assertNotNull("object get failed", object);

    assertEquals("file path is wrong", "test-src/solution", boot.getFilePath());

    boot.stop();
    assertFalse(boot.isInitialized());

  }

  @Test
  public void testBootListeners() throws Exception {
    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath("test-src/solution");
    boot.define(ISolutionEngine.class.getSimpleName(), Object1.class.getName(),
        IPentahoDefinableObjectFactory.Scope.GLOBAL);
    boot.define("systemStartupSession", StandaloneSession.class.getName(), IPentahoDefinableObjectFactory.Scope.GLOBAL);

    TestLifecycleListener lifecycleListener1 = new TestLifecycleListener();
    TestLifecycleListener lifecycleListener2 = new TestLifecycleListener();
    boot.addLifecycleListener(lifecycleListener1);

    List<IPentahoSystemListener> lifecycleListeners1 = boot.getLifecycleListeners();
    assertEquals(1, lifecycleListeners1.size());
    assertEquals(lifecycleListener1, lifecycleListeners1.get(0));
    assertFalse(TestLifecycleListener.startupCalled);
    assertFalse(TestLifecycleListener.shutdownCalled);

    List<IPentahoSystemListener> lifecycleListeners2 = new ArrayList<IPentahoSystemListener>();
    lifecycleListeners2.add(lifecycleListener2);
    boot.setLifecycleListeners(lifecycleListeners2);
    List<IPentahoSystemListener> lifecycleListeners3 = boot.getLifecycleListeners();
    assertEquals(1, lifecycleListeners3.size());
    assertEquals(lifecycleListener2, lifecycleListeners3.get(0));
    assertEquals(lifecycleListeners2, lifecycleListeners3);

    IPentahoObjectFactory factory = boot.getFactory();
    assertNotNull("object factory is null", factory);

    assertTrue("object factory not definable", factory instanceof IPentahoDefinableObjectFactory);

    assertFalse(boot.isInitialized());

    boolean ok = boot.start();

    assertNull(boot.getSettingsProvider());

    assertTrue(boot.isInitialized());
    assertTrue(ok);

    assertTrue(TestLifecycleListener.startupCalled);
    assertFalse(TestLifecycleListener.shutdownCalled);

    boot.stop();
    assertFalse(boot.isInitialized());

    assertTrue(TestLifecycleListener.startupCalled);
    assertTrue(TestLifecycleListener.shutdownCalled);

  }

  @Test
  public void testBootActions() throws Exception {
    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath("test-src/solution");
    boot.define(ISolutionEngine.class.getSimpleName(), Object1.class.getName(),
        IPentahoDefinableObjectFactory.Scope.GLOBAL);

    TestStartupAction startupAction1 = new TestStartupAction();
    TestStartupAction startupAction2 = new TestStartupAction();
    boot.addStartupAction(startupAction1);

    List<ISessionStartupAction> startupActions1 = boot.getStartupActions();
    assertEquals(1, startupActions1.size());
    assertEquals(startupAction1, startupActions1.get(0));

    List<ISessionStartupAction> startupActions2 = new ArrayList<ISessionStartupAction>();
    startupActions2.add(startupAction2);
    boot.setStartupActions(startupActions2);
    List<ISessionStartupAction> startupActions3 = boot.getStartupActions();
    assertEquals(1, startupActions3.size());
    assertEquals(startupAction2, startupActions3.get(0));
    assertEquals(startupActions2, startupActions3);

    IPentahoObjectFactory factory = boot.getFactory();
    assertNotNull("object factory is null", factory);

    assertTrue("object factory not definable", factory instanceof IPentahoDefinableObjectFactory);

    assertFalse(boot.isInitialized());

    boolean ok = boot.start();

    assertNull(boot.getSettingsProvider());

    assertTrue(boot.isInitialized());
    assertTrue(ok);

    boot.stop();
    assertFalse(boot.isInitialized());

  }

  @Test
  public void testBootSettings() throws Exception {
    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath("test-src/solution");

    IPentahoObjectFactory factory = boot.getFactory();
    assertNotNull("object factory is null", factory);

    SystemSettings settings = new SystemSettings();
    boot.setSettingsProvider(settings);

    assertEquals(settings, boot.getSettingsProvider());

  }

  @Test(expected = NoSuchMethodError.class)
  public void testReadOnlyFactory() {

    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath("test-src/solution");
    TestObjectFactory objectFactory = new TestObjectFactory();
    boot.setFactory(objectFactory);

    boot.define(ISolutionEngine.class.getSimpleName(), Object1.class.getName(),
        IPentahoDefinableObjectFactory.Scope.GLOBAL);

  }

}
