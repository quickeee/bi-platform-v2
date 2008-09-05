package org.pentaho.test.platform.web;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.dom4j.Document;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.servlet.HttpWebService;
import org.pentaho.test.platform.engine.core.BaseTestCase;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.HttpWebService</code>.
 * 
 * @author mlowery
 */
public class HttpWebServiceTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/solution";


  public String getSolutionPath() {
      return SOLUTION_PATH;  
  }
	public void setUp() {
		StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(
				getSolutionPath(), ""); //$NON-NLS-1$
		PentahoSystem.init(applicationContext, getRequiredListeners());
	}

	protected Map getRequiredListeners() {
		HashMap listeners = new HashMap();
		listeners.put("globalObjects", "globalObjects"); //$NON-NLS-1$ //$NON-NLS-2$
		return listeners;
	}

	public void testDoGet() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setupAddParameter("action", "securitydetails"); //$NON-NLS-1$//$NON-NLS-2$
		request.setupAddParameter("details", "all"); //$NON-NLS-1$ //$NON-NLS-2$
		MockHttpServletResponse response = new MockHttpServletResponse();
		HttpWebService servlet = new HttpWebService();
		servlet.doGet(request, response);
		assertTrue("missing or invalid SOAP wrapper elements", //$NON-NLS-1$
				isSoapValid(response.getOutputStreamContent()));
		assertTrue("missing or invalid users, roles, or acls elements", //$NON-NLS-1$
				isBodyValid(response.getOutputStreamContent()));
		// System.out.println(response.getOutputStreamContent());
	}

	public void testDoPostWithBody() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		StringBuffer buf = new StringBuffer();
		buf
				.append(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">") //$NON-NLS-1$
				.append("<SOAP-ENV:Body>"); //$NON-NLS-1$
		buf.append("<parameters>"); //$NON-NLS-1$
		buf.append("<action>securitydetails</action>").append( //$NON-NLS-1$
				"<details>all</details>"); //$NON-NLS-1$
		buf.append("</parameters>"); //$NON-NLS-1$
		buf.append("</SOAP-ENV:Body>").append("</SOAP-ENV:Envelope>"); //$NON-NLS-1$ //$NON-NLS-2$
		request.setBodyContent(buf.toString());
		request.setContentLength(buf.toString().getBytes().length);
		request.setContentType("text/xml"); //$NON-NLS-1$
		MockHttpServletResponse response = new MockHttpServletResponse();
		HttpWebService servlet = new HttpWebService();
		servlet.doGet(request, response);
		assertTrue("missing or invalid SOAP wrapper elements", //$NON-NLS-1$
				isSoapValid(response.getOutputStreamContent()));
		assertTrue("missing or invalid users, roles, or acls elements", //$NON-NLS-1$
				isBodyValid(response.getOutputStreamContent()));
	}

	protected boolean isSoapValid(final String outputStreamContent) {
		Document doc = XmlDom4JHelper.getDocFromString(outputStreamContent, null);
		if (null != doc) {
			return null != doc.selectSingleNode("/SOAP-ENV:Envelope") //$NON-NLS-1$
					&& null != doc
							.selectSingleNode("/SOAP-ENV:Envelope/SOAP-ENV:Body"); //$NON-NLS-1$
		} else {
			return false;
		}
	}

	protected boolean isBodyValid(final String outputStreamContent) {
		Document doc = XmlDom4JHelper.getDocFromString(outputStreamContent, null);
		if (null != doc) {
			return null != doc.selectSingleNode("//content/users") //$NON-NLS-1$
					&& null != doc.selectSingleNode("//content/roles") //$NON-NLS-1$
					&& null != doc.selectSingleNode("//content/acls"); //$NON-NLS-1$
		} else {
			return false;
		}
	}

}
