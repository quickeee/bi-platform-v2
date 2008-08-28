package org.pentaho.test.platform.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.uifoundation.component.xml.PMDUIComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

public class MetadataTest extends BaseTest {
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
    listeners.put("metadata", "metadata"); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }


  public void testViewList() {
    startTest();

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(""); //$NON-NLS-1$

    PMDUIComponent component = new PMDUIComponent(urlFactory, new ArrayList());
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    component.validate(session, null);
    component.setAction(PMDUIComponent.ACTION_LIST_MODELS);

    Document doc = component.getXmlContent();
    System.out.println(doc.asXML());
    try {
      OutputStream outputStream = getOutputStream("MetadataTest.testViewList", ".xml"); //$NON-NLS-1$//$NON-NLS-2$
      outputStream.write(doc.asXML().getBytes());
    } catch (IOException e) {
    }
    finishTest();
  }

  public void testLoadView() {
    startTest();

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(""); //$NON-NLS-1$

    PMDUIComponent component = new PMDUIComponent(urlFactory, new ArrayList());
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    component.validate(session, null);
    component.setAction(PMDUIComponent.ACTION_LOAD_MODEL);
    component.setDomainName("test"); //$NON-NLS-1$
    component.setModelId("Orders"); //$NON-NLS-1$

    Document doc = component.getXmlContent();
    System.out.println(doc.asXML());
    try {
      OutputStream outputStream = getOutputStream("MetadataTest.testLoadView", ".xml"); //$NON-NLS-1$//$NON-NLS-2$
      outputStream.write(doc.asXML().getBytes());
    } catch (IOException e) {
    }
    finishTest();
  }

  public void testLookup() {
    startTest();

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(""); //$NON-NLS-1$

    PMDUIComponent component = new PMDUIComponent(urlFactory, new ArrayList());
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    component.validate(session, null);
    component.setAction(PMDUIComponent.ACTION_LOOKUP);
    component.setDomainName("test"); //$NON-NLS-1$
    component.setModelId("Orders"); //$NON-NLS-1$
    component.setColumnId("BC_CUSTOMERS_CUSTOMERNAME"); //$NON-NLS-1$

    Document doc = component.getXmlContent();
    System.out.println(doc.asXML());
    try {
      OutputStream outputStream = getOutputStream("MetadataTest.testLoadView", ".xml"); //$NON-NLS-1$//$NON-NLS-2$
      outputStream.write(doc.asXML().getBytes());
    } catch (IOException e) {
    }
    finishTest();
  }

  public static void main(String[] args) {
    MetadataTest test = new MetadataTest();
    test.setUp();
    try {
      test.testViewList();
      test.testLoadView();
      test.testLookup();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
