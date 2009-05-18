package org.pentaho.platform.dataaccess.datasource.wizard.jsni;

import java.util.Iterator;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * This class makes available the created business model to the calling javascript.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 *
 */
public class WAQRTransport extends JavaScriptObject {
  
  public static WAQRTransport createFromMetadata(Domain domain) {
    
    // this assumes a single logical model with a single logical category
    
    LogicalModel model = domain.getLogicalModels().get(0);
    Iterator<String> iter = model.getName().getLocales().iterator();
    String locale = iter.next();
    Category category = model.getCategories().get(0);
    
    return createDomain(domain.getId(), model.getId(), model.getName().getString(locale), category.getId(), category.getName().getString(locale));
  }
  
  private native static WAQRTransport createDomain(String domainId, String modelId, String modelName, String categoryId, String categoryName) /*-{
    var waqrTransport = {};
    waqrTransport.domainId = domainId;
    waqrTransport.modelId = modelId;
    waqrTransport.modelName = modelName;
    waqrTransport.categoryId = categoryId;
    waqrTransport.categoryName = categoryName;
    return waqrTransport;
  }-*/;
  
  protected WAQRTransport() {
    
  }
}