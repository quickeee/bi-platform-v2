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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;

/**
 * Executes the Open Document command.
 * 
 * @author nbaker / dkincade
 */
public class OpenDocCommand extends AbstractCommand {
  private String documentationURL;

  public OpenDocCommand(String documentationURL) {
    this.documentationURL = documentationURL;
  }

  /**
   * Executes the command to open the help documentation. Based on the subscription setting, the document being opened will be the CE version of the document or
   * the EE version of the document.
   */
  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    SolutionBrowserPerspective.getInstance().getContentTabPanel().showNewURLTab(Messages.getString("documentation"), Messages.getString("documentation"), documentationURL, false);
  }
  

}
