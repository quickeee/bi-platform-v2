/*
 * 
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Aug 31, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.uifoundation.component.xml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.ui.UIException;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.uifoundation.messages.Messages;

/**
 * This class provides a user interface that lets users select items from lists
 * or radio buttons. The selections are used by other components to filter
 * queries used to populate data.
 * 
 * @author James Dixon
 */
public class FilterPanelComponent extends XmlComponent {
  private static final Log log = LogFactory.getLog(FilterPanelComponent.class);

  /**
   * 
   */
  private static final long serialVersionUID = 700681534058825526L;

  private static final Log logger = LogFactory.getLog(FilterPanelComponent.class);

  /**
   * repository relative path and filename to the filter panel definition file
   */
  private String definitionPath;

  private FilterPanel filterPanel;

  private String xslName;

  private Map defaultValues;

  public FilterPanelComponent(final String definitionPath, String xslName, final IPentahoUrlFactory urlFactory,
      final List messages) {
    super(urlFactory, messages, null);
    this.definitionPath = definitionPath;
    if (xslName == null) {
      // use a default XSL
      xslName = "FilterPanelDefault.xsl"; //$NON-NLS-1$
    }
    ActionInfo info = ActionInfo.parseActionString(definitionPath);
    if (info != null) {
      setSourcePath(info.getSolutionName() + File.separator + info.getPath());
    }
    this.xslName = xslName;
    defaultValues = new HashMap();
  }

  public void setDefaultValue(final String filterName, final String[] defaultValue) {
    defaultValues.put(filterName, defaultValue);
  }

  @Override
  public Log getLogger() {
    return FilterPanelComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }

  public List getFilters() {
    return filterPanel.getFilters();
  }

  public boolean init() {
    if (filterPanel == null) {
      // load the XML document that defines the dial
      IActionSequenceResource resource = new ActionSequenceResource(
          "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$ //$NON-NLS-2$
          definitionPath);
      Document filterDocument = null;
      try {
        filterDocument = PentahoSystem.get(ISolutionRepository.class, getSession()).getResourceAsDocument(resource);
      } catch (IOException e) {
        // TODO sbarkdull localize
        FilterPanelComponent.logger.error(Messages.getString("FilterPanelComponent.ERROR_0002_CREATE_XML"), e); //$NON-NLS-1$
        return false;
      }
      try {
        filterPanel = getFilterPanel(filterDocument);
      } catch (FilterPanelException e) {
        // TODO sbarkdull localize
        FilterPanelComponent.logger.error(Messages.getString("FilterPanelComponent.ERROR_0003_CREATE"), e); //$NON-NLS-1$
        return false;
      }
    }
    return true;
  }

  @Override
  public Document getXmlContent() {
    //      assert null != urlFactory : Messages.getString("FilterPanelComponent.ERROR_0000_FACTORY_CANNOT_BE_NULL"); //$NON-NLS-1$

    boolean ok = filterPanel.populate(getParameterProviders(), defaultValues);

    if (!ok) {
      String msg = Messages.getString("FilterPanelComponent.ERROR_0001_POPULATE"); //$NON-NLS-1$
      FilterPanelComponent.log.error(msg);
      throw new UIException(msg);
    }

    String actionUrl = urlFactory.getActionUrlBuilder().getUrl();
    Document xForm = filterPanel.getXForm(actionUrl);

    setXsl("text/html", xslName); //$NON-NLS-1$

    return xForm;
  }

  private FilterPanel getFilterPanel(final Document filterDocument) throws FilterPanelException {
    FilterPanel newFilterPanel = new FilterPanel(getSession(), filterDocument, this);
    return newFilterPanel;
  }

}
