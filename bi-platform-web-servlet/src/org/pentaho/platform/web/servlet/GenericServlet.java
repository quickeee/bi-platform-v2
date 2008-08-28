package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.PluginSettings;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.servlet.ServletBase;

public class GenericServlet extends ServletBase {

	private static final long serialVersionUID = 6713118348911206464L;
	
	private static final Log logger = LogFactory.getLog(GenericServlet.class);
	
	  @Override
	  public Log getLogger() {
	    return GenericServlet.logger;
	  }

	  @Override
	  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		  doGet( request, response );
	  }

	  @Override
	  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
	    
	    PentahoSystem.systemEntryPoint();

	    try {
	    	String servletPath = request.getServletPath();
	    	String key = request.getPathInfo();
	    	String contentGeneratorId = "";
	    	String urlPath = "";
	    	if( key == null ) {
	    		contentGeneratorId = servletPath.substring(1);
	    		urlPath = contentGeneratorId;
	    	} else {
		    	contentGeneratorId = key.substring( 1 );
		    	urlPath = "content"+key;
	    	}
	    	IPentahoSession session = getPentahoSession( request );
	    	PluginSettings pluginSettings = (PluginSettings) PentahoSystem.getObject( session, "IPluginSettings" );
		    if( pluginSettings == null ) {
		    	OutputStream out = response.getOutputStream();
		    	String message = "Could not get system object: PluginSettings";
		    	out.write( message.getBytes() );
		    	return;
		    }

		    IContentGenerator contentGenerator = pluginSettings.getContentGenerator(contentGeneratorId, session);
	    	if( contentGenerator == null ) {
		    	OutputStream out = response.getOutputStream();
	    		String message = "Could not get content generator for type: "+contentGeneratorId;
	    		out.write( message.getBytes() );
	    		return;
	    	}

//		      String proxyClass = PentahoSystem.getSystemSetting( module+"/plugin.xml" , "plugin/content-generators/"+contentGeneratorId, "content generator not found");
	    	IParameterProvider requestParameters = new HttpRequestParameterProvider( request );
	    	// see if this is an upload
	    	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	    	if( isMultipart ) {
	    		requestParameters = new SimpleParameterProvider();
	    		// Create a factory for disk-based file items
	    		FileItemFactory factory = new DiskFileItemFactory();

	    		// Create a new file upload handler
	    		ServletFileUpload upload = new ServletFileUpload(factory);

	    		// Parse the request
	    		List<?> /* FileItem */ items = upload.parseRequest(request);
	    		Iterator<?> iter = items.iterator();
	    		while (iter.hasNext()) {
	    			FileItem item = (FileItem) iter.next();

	    			if (item.isFormField()) {
	    				((SimpleParameterProvider)requestParameters).setParameter( item.getFieldName() , item.getString() );
	    			} else {
	    				String name = item.getName();
	    				((SimpleParameterProvider)requestParameters).setParameter( name , item.getInputStream() );
	    			}
	    		}	
	    	}

//	    	IPentahoSession userSession = PentahoHttpSessionHelper.getPentahoSession( request );
//	    	IContentGenerator contentGenerator = getContentGenerator( proxyClass, userSession );
	    	
	    	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());

	        IMimeTypeListener listener = new HttpMimeTypeListener(request, response);
	    	
	    	OutputStream out = response.getOutputStream();
	    	HttpOutputHandler outputHandler = new HttpOutputHandler( response, out, true );
	        outputHandler.setMimeTypeListener(listener);
	    	
	    	IParameterProvider sessionParameters = new HttpSessionParameterProvider( session );
	    	
	    	Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
	    	parameterProviders.put( IParameterProvider.SCOPE_REQUEST , requestParameters );
	    	parameterProviders.put( IParameterProvider.SCOPE_SESSION , sessionParameters );
	        SimpleUrlFactory urlFactory = new SimpleUrlFactory(PentahoSystem.getApplicationContext().getBaseUrl()
	                + urlPath+"?"); //$NON-NLS-1$
	    	List<String> messages = new ArrayList<String>();
	    	contentGenerator.setOutputHandler(outputHandler);
	    	contentGenerator.setMessagesList(messages);
	    	contentGenerator.setParameterProviders(parameterProviders);
	    	contentGenerator.setSession(session);
	    	contentGenerator.setUrlFactory(urlFactory);
	    	contentGenerator.createContent();

	    	
	    } catch ( Exception e ) {
	    // TODO 
	    } finally {
	      PentahoSystem.systemExitPoint();
	    }
	  }
/*
	  private IContentGenerator getContentGenerator( String className, IPentahoSession userSession ) {
		  if( className.startsWith( "solution:" ) ) {
			  className = className.substring( "solution:".length() );
			  int pos = className.lastIndexOf( '/' );
			  String path = className.substring(0, pos);
			  className = className.substring( pos + 1 );
			  pos = path.lastIndexOf( '/' );
			  String jarName = path.substring( pos+1 );
			  path = path.substring(0, pos );
				IPentahoSession session = new StandaloneSession( "test" );
				ISolutionRepository repo = PentahoSystem.getSolutionRepository( session );
			  SolutionClassLoader loader = new SolutionClassLoader( path, jarName, repo, this ); 
			  
				try {
					Class<?> generatorClass = loader.loadClass( className );
					Object obj = generatorClass.newInstance();
					if( obj instanceof IContentGenerator ) {
						return (IContentGenerator) obj;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		  } else {
			  Object obj = PentahoSystem.createObject(className);
			  if( obj instanceof IContentGenerator ) {
				  return (IContentGenerator) obj;
			  }
		  }
		  return null;
	  }
*/	  
}
