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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jan 6, 2009
 * @author James Dixon
 */
package org.pentaho.test.platform.engine.core;

import java.io.InputStream;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.ILogger;

@SuppressWarnings({ "deprecation" })
public class TestFileInfoGenerator implements IFileInfoGenerator {

  public ContentType getContentType() {
    return null;
  }

  public IFileInfo getFileInfo(String solution, String path, String filename, InputStream in) {
    return new TestFileInfo( "inputstream", null, null, null, null ); //$NON-NLS-1$
  }

  public IFileInfo getFileInfo(String solution, String path, String filename, Document in) {
    return new TestFileInfo( "document", null, null, null, null ); //$NON-NLS-1$
  }

  public IFileInfo getFileInfo(String solution, String path, String filename, byte[] bytes) {
    return new TestFileInfo( "bytes", null, null, null, null ); //$NON-NLS-1$
  }

  public IFileInfo getFileInfo(String solution, String path, String filename, String str) {
    return new TestFileInfo( "string", null, null, null, null ); //$NON-NLS-1$
  }

  public void setLogger(ILogger logger) {
  }

}
