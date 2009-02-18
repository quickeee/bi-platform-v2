package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.dom4j.Node;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.PluginComponentException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.PluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PluginMessageLogger;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.core.MockComponent;
import org.pentaho.ui.xul.XulOverlay;

@SuppressWarnings("nls")
public class PluginManagerTest {

  @Before
  public void init() {
    MicroPlatform microPlatform = new MicroPlatform("plugin-mgr/test-res/PluginManagerTest/");
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IPluginProvider.class, SystemPathXmlPluginProvider.class);

    microPlatform.init();
  }

  @Test
  public void testReload() {
    StandaloneSession session = new StandaloneSession();
    IPluginManager pluginManager = new PluginManager();
    pluginManager.reload(session);

    //one of the plugins serves a content generator with id=test1.  Make sure we can load it.
    assertNotNull("The plugin serving content generator with id=test1 was not loaded", pluginManager
        .getContentGeneratorInfo("test1", session));
  }

  
  private class TestComponentPluginManager extends PluginManager {
    TestComponentPluginManager() {
      super();
      this.reload(new StandaloneSession());
      pluginComponentMap.put("TestMockComponent", "org.pentaho.test.platform.engine.core.MockComponent");
      pluginComponentMap.put("TestPojo", "java.lang.String");
      pluginComponentMap.put("TestClassNotFoundComponent", "org.pentaho.test.NotThere");
    }
  }
  
  @Test
  public void testComponentMethods() {
    StandaloneSession session = new StandaloneSession();
    IPluginManager pluginManager = new TestComponentPluginManager();
    
    assertTrue(pluginManager.isObjectRegistered("TestMockComponent"));
    assertTrue(pluginManager.isObjectRegistered("TestPojo"));
    
    assertFalse(pluginManager.isObjectRegistered("IDoNotExist"));

    try {
      Object obj = pluginManager.getRegisteredObject("TestMockComponent");
      assertTrue(obj instanceof IComponent);
    } catch (PluginComponentException ex) {
      assertFalse("Exception was not expected", true);
    }

    try {
      Object pojo = pluginManager.getRegisteredObject("TestPojo");
      assertTrue(pojo instanceof String);
    } catch (PluginComponentException ex) {
      assertFalse("Exception was not expected", true);
    }
    
    try {
      Object bogus = pluginManager.getRegisteredObject("IDoNotExist");
      assertNull(bogus);
    } catch (PluginComponentException ex) {
      assertFalse("Exception was not expected", true);
    }
    
    try {
      Object bogus = pluginManager.getRegisteredObject("TestClassNotFoundComponent");
      assertFalse("Exception was not expected", true);
    } catch (PluginComponentException ex) {
      assertTrue("Exception was expected", true);
    }
    
  }
  
  @Test
  public void testGetOverlays() throws Exception {
    IPentahoSession session = new StandaloneSession("test user"); //$NON-NLS-1$
    IPluginManager pluginManager = new PluginManager();
    assertNotNull(pluginManager);

    PluginMessageLogger.clear();
    boolean result = pluginManager.reload(session);
    System.err.println(PluginMessageLogger.prettyPrint());

    List<XulOverlay> overlays = pluginManager.getOverlays();

    assertNotNull("Overlays is null", overlays); //$NON-NLS-1$
    
    System.err.println(overlays);
    
    assertEquals("Wrong number of overlays", 2, overlays.size()); //$NON-NLS-1$
    XulOverlay overlay = overlays.get(0);
    assertEquals("Wrong overlay id", "overlay1", overlay.getId()); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals("Wrong overlay resource uri", "uri1", overlay.getResourceBundleUri()); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node1") != -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node2") != -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node3") == -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node4") == -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertNull("Overlay URI should be null", overlay.getOverlayUri()); //$NON-NLS-1$

    overlay = overlays.get(1);

    assertEquals("Wrong overlay id", "overlay2", overlay.getId()); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals("Wrong overlay resource uri", "uri2", overlay.getResourceBundleUri()); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node1") == -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node2") == -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node3") != -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node4") != -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertNull("Overlay URI should be null", overlay.getOverlayUri()); //$NON-NLS-1$
  }
}
