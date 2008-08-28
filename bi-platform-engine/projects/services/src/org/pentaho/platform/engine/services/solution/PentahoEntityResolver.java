/*
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
 * @created Jul 27, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.engine.services.solution;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PentahoEntityResolver implements EntityResolver {

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
   *      java.lang.String)
   */
  public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {

    int idx = systemId.lastIndexOf('/');
    String dtdName = systemId.substring(idx + 1);
    String fullPath = PentahoSystem.getApplicationContext().getSolutionPath("system/dtd/" + dtdName); //$NON-NLS-1$

    try {
      FileInputStream xslIS = new FileInputStream(fullPath);
      InputSource source = new InputSource(xslIS);
      return source;
    } catch (FileNotFoundException e) {

    }
    return null;
  }

}
