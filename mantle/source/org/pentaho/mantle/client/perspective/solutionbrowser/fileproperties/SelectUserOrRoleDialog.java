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

import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class SelectUserOrRoleDialog extends PromptDialogBox {

  private static FlexTable contentTable = new FlexTable();
  private static ListBox usersListBox = new ListBox(false);
  private static ListBox rolesListBox = new ListBox(false);

  static {
    usersListBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        rolesListBox.setSelectedIndex(-1);
      }
    });
    rolesListBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        usersListBox.setSelectedIndex(-1);
      }
    });
  }

  public SelectUserOrRoleDialog(List<String> existing, final IUserRoleSelectedCallback callback) {
    super("Select User or Role", "OK", "Cancel", false, true, contentTable);
    setCallback(new IDialogCallback() {

      public void cancelPressed() {
      }

      public void okPressed() {
        if (getSelectedUser() != null) {
          callback.userSelected(getSelectedUser());
        } else {
          callback.roleSelected(getSelectedRole());
        }
      }
    });
    usersListBox.setVisibleItemCount(5);
    rolesListBox.setVisibleItemCount(5);
    rolesListBox.setWidth("100%");
    usersListBox.setWidth("100%");
    contentTable.clear();
    contentTable.setWidth("100%");
    contentTable.setWidget(0, 0, new Label("Users:"));
    contentTable.setWidget(1, 0, usersListBox);
    contentTable.setWidget(2, 0, new Label("Roles:"));
    contentTable.setWidget(3, 0, rolesListBox);
    fetchAllUsers(existing);
    fetchAllRoles(existing);
    setWidth("200px");
  }

  public void fetchAllRoles(final List<String> existing) {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Object result) {
        // filter out existing
        rolesListBox.clear();
        List<String> roles = (List<String>) result;
        for (String role : roles) {
          if (!existing.contains(role)) {
            rolesListBox.addItem(role);
          }
        }

      }
    };
    MantleServiceCache.getService().getAllRoles(callback);
  }

  public void fetchAllUsers(final List<String> existing) {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Object result) {
        // filter out existing
        usersListBox.clear();
        List<String> users = (List<String>) result;
        for (String user : users) {
          if (!existing.contains(user)) {
            usersListBox.addItem(user);
          }
        }
      }
    };
    MantleServiceCache.getService().getAllUsers(callback);
  }

  public static String getSelectedUser() {
    if (usersListBox.getSelectedIndex() >= 0) {
      return usersListBox.getItemText(usersListBox.getSelectedIndex());
    }
    return null;
  }

  public static String getSelectedRole() {
    if (rolesListBox.getSelectedIndex() >= 0) {
      return rolesListBox.getItemText(rolesListBox.getSelectedIndex());
    }
    return null;
  }

}
