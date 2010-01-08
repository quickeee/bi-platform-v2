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
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.core.output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.commons.connection.SimpleStreamSource;
import org.pentaho.platform.api.engine.IContentListener;

public class BufferedContentItem extends SimpleContentItem {

  private ByteArrayOutputStream outputStream;

  private InputStream inputStream;

  private IContentListener listener;

  public BufferedContentItem(final IContentListener listener) {
    super();
    this.listener = listener;
    outputStream = new ByteArrayOutputStream();
    inputStream = null;
    setOutputStream(outputStream);
  }

  @Override
  public void closeOutputStream() {
    inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    if (listener != null) {
      listener.close();
    }
  }

  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

  @Override
  public void setMimeType(final String mimeType) {
    super.setMimeType(mimeType);
    if (listener != null) {
      listener.setMimeType(mimeType);
    }
  }

  @Override
  public IPentahoStreamSource getDataSource() {
    if( inputStream == null ) {
      inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    }
    return new SimpleStreamSource( getName(), getMimeType(), inputStream, outputStream );
  }
  
}
