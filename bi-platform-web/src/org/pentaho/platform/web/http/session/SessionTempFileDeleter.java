/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Jul 26, 2009 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.web.http.session;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.pentaho.platform.api.util.ITempFileDeleter;

public class SessionTempFileDeleter implements HttpSessionBindingListener, Serializable, ITempFileDeleter  {

  private static final long serialVersionUID = 1379936698516655051L;
  private List<File> tmpFileList = Collections.synchronizedList(new ArrayList<File>());
  
  public void trackTempFile(File aFile) {
    tmpFileList.add(aFile);
  }
  
  public void valueBound(HttpSessionBindingEvent event) {
  }

  public void valueUnbound(HttpSessionBindingEvent event) {
    doTempFileCleanup();
  }
  
  public void doTempFileCleanup() {
    synchronized(tmpFileList) {
      for (File file : tmpFileList) {
        if (file.exists()) {
          file.delete();
        }
      }
      tmpFileList.clear();
    }
  }

  public boolean hasTempFile(String aFileName) {
    if ( (aFileName != null) && (aFileName.length() > 0) ) {
      synchronized(tmpFileList) {
        for (File f : tmpFileList ) {
          if ((f.getName().equals(aFileName))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  
}
