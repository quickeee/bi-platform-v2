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
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties;

import java.io.File;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.MantleApplicationConstants;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class GeneralPanel extends FlexTable implements IFileModifier {

  Label nameLabel = new Label();
  Label locationLabel = new Label();
  Label sourceLabel = new Label();
  Label typeLabel = new Label();
  Label sizeLabel = new Label();
  Label lastModifiedDateLabel = new Label();
  FileItem fileItem;
  private static MantleApplicationConstants MSG = Messages.getInstance();

  public GeneralPanel() {
    setWidget(0, 0, new Label(MSG.name()+":"));
    setWidget(0, 1, nameLabel);
    setWidget(1, 0, new Label(MSG.location()+":"));
    setWidget(1, 1, locationLabel);
    setWidget(2, 0, new Label(MSG.source()+":"));
    setWidget(2, 1, sourceLabel);
    setWidget(3, 0, new Label(MSG.type()+":"));
    setWidget(3, 1, typeLabel);
    setWidget(4, 0, new Label(MSG.size()+":"));
    setWidget(4, 1, sizeLabel);
    Label lbl = new Label(MSG.lastModified()+":");
    lbl.addStyleName("nowrap");
    setWidget(5, 0, lbl);
    setWidget(5, 1, lastModifiedDateLabel);
  }

  public void apply() {
    // hit server with new settings
  }

  public void init(FileItem file, SolutionFileInfo fileInfo) {
    fileItem = file;
    // possibly hit server to pull all this data
    if (fileInfo == null) {
      populateUIFromServer();
    } else {
      nameLabel.setText(fileInfo.localizedName);
      locationLabel.setText(fileInfo.solution+fileInfo.path);
      sourceLabel.setText(fileInfo.solution+fileInfo.path+"/"+fileInfo.name);
      this.typeLabel.setText(getFileTypeDescription(fileInfo.type, fileInfo.pluginTypeName));
      NumberFormat numberFormat = NumberFormat.getDecimalFormat();
      sizeLabel.setText(numberFormat.format(fileInfo.size/1000.00)+" KB");
      lastModifiedDateLabel.setText(fileInfo.lastModifiedDate.toString());
    }
  }
  
  private String getFileTypeDescription(SolutionFileInfo.Type type, String pluginTypeName){
    switch(type){
      case FOLDER:
        return MSG.folder();
      case ANALYSIS_VIEW:
        return MSG.analysisView();
      case XACTION:
        return MSG.xaction();
      case URL:
        return "URL";   //$NON-NLS-1$
      case REPORT:
        return MSG.report();
      case PLUGIN:
        return pluginTypeName;
      default:
        return "";
    }
  }

  public void populateUIFromServer() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Object result) {
        SolutionFileInfo fileInfo = (SolutionFileInfo) result;
        init(fileItem, fileInfo);
      }
    };
    MantleServiceCache.getService().getSolutionFileInfo(fileItem.getSolution(), fileItem.getPath(), fileItem.getName(), callback);
  }

}
