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
package org.pentaho.mantle.login.client;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.PropertiesUtil;
import org.pentaho.gwt.widgets.client.utils.StringTokenizer;
import org.pentaho.mantle.login.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MantleLoginDialog extends PromptDialogBox {

  private AsyncCallback<Boolean> outerCallback; // from outside context
  private final TextBox userTextBox = new TextBox();
  private final ListBox usersListBox = new ListBox();
  private final PasswordTextBox passwordTextBox = new PasswordTextBox();
  private CheckBox newWindowChk = new CheckBox();
  private String returnLocation = null;
  
  private static boolean showUsersList = false;
  private static boolean showNewWindowOption = true;
  private static boolean openInNewWindowDefault = false;
  private static MantleLoginServiceAsync SERVICE;

  private static LinkedHashMap<String, String[]> defaultUsers = new LinkedHashMap<String, String[]>();

  static {
    SERVICE = (MantleLoginServiceAsync) GWT.create(MantleLoginService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    String moduleRelativeURL = GWT.getModuleBaseURL() + "MantleLoginService"; //$NON-NLS-1$
    endpoint.setServiceEntryPoint(moduleRelativeURL);
  }

  private final IDialogCallback myCallback = new IDialogCallback() {

    public void cancelPressed() {
    }

    public void okPressed() {
      String path = Window.Location.getPath();
      if (!path.endsWith("/")) { //$NON-NLS-1$
        path = path.substring(0, path.lastIndexOf("/") + 1); //$NON-NLS-1$
      }
      RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, path + "j_spring_security_check"); //$NON-NLS-1$
      builder.setHeader("Content-Type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
      RequestCallback callback = new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          outerCallback.onFailure(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onSuccess(Boolean result) {

              if (result) {
                long year = 1000 * 60 * 60 * 24 * 365;
                // one year into the future
                Date expirationDate = new Date(System.currentTimeMillis() + year);
                Cookies.setCookie("loginNewWindowChecked", "" + newWindowChk.isChecked(), expirationDate); //$NON-NLS-1$ //$NON-NLS-2$
                outerCallback.onSuccess(newWindowChk != null && newWindowChk.isChecked());
              } else {
                outerCallback.onFailure(new Throwable(Messages.getString("authFailed"))); //$NON-NLS-1$
              }
            }

            public void onFailure(final Throwable caught) {
              MessageDialogBox errBox = new MessageDialogBox(Messages.getString("loginError"), Messages.getString("authFailed"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
              errBox.setCallback(new IDialogCallback() {
                public void cancelPressed() {
                }

                public void okPressed() {
                  outerCallback.onFailure(caught);
                }
              });
              errBox.show();
            }
          };
          SERVICE.isAuthenticated(callback);
        }
      };
      try {
        String username = userTextBox.getText();
        String password = passwordTextBox.getText();
        builder.sendRequest("j_username=" + URL.encodeComponent(username) + "&j_password=" + URL.encodeComponent(password), callback); //$NON-NLS-1$ //$NON-NLS-2$
      } catch (RequestException e) {
        e.printStackTrace();
      }
    }

  };

  public MantleLoginDialog() {
    super(Messages.getString("login"), Messages.getString("login"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    setCallback(myCallback);
    SERVICE.isShowUsersList(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        getLoginSettingsAndShow(false);
      }

      public void onSuccess(Boolean showUsersList) {
        getLoginSettingsAndShow(showUsersList);
      }
    });
  }

  public void getLoginSettingsAndShow(final boolean showUsersListDefault) {
    // we can override showUsersList with a setting in loginsettings.properties (not present by default)
    showUsersList = showUsersListDefault;
    String path = Window.Location.getPath();
    if (!path.endsWith("/")) { //$NON-NLS-1$
      path = path.substring(0, path.lastIndexOf("/") + 1); //$NON-NLS-1$
    }
    path = path.replaceAll("/mantle/", "/mantleLogin/"); //$NON-NLS-1$ //$NON-NLS-2$
    if (path.indexOf("mantleLogin") == -1) { //$NON-NLS-1$
      path += "mantleLogin/"; //$NON-NLS-1$
    }
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, path + "loginsettings.properties"); //$NON-NLS-1$
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {
        public void onError(Request request, Throwable exception) {
          setContent(buildLoginPanel(false));
          if (isAttached() && isVisible()) {
            center();
          }
        }

        public void onResponseReceived(Request request, Response response) {
          String propertiesFileText = response.getText();
          // build a simple map of key/value pairs from the properties file
          HashMap<String,String> settings = PropertiesUtil.buildProperties(propertiesFileText);
          StringTokenizer useridTokenizer = new StringTokenizer(settings.get("userIds"), ','); //$NON-NLS-1$
          StringTokenizer passwordTokenizer = new StringTokenizer(settings.get("userPasswords"), ','); //$NON-NLS-1$
          StringTokenizer userdisplayTokenizer = new StringTokenizer(settings.get("userDisplayNames"), ','); //$NON-NLS-1$
          // build default users list
          defaultUsers.clear();
          defaultUsers.put(Messages.getString("selectUser"), new String[] { "", "" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          for (int i = 0; i < useridTokenizer.countTokens(); i++) {
            defaultUsers.put(userdisplayTokenizer.tokenAt(i), new String[] { useridTokenizer.tokenAt(i).trim(), passwordTokenizer.tokenAt(i).trim() }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          }
          // provide the opportunity to override showUsersList with a setting
          if (settings.get("showUsersList") != null) { //$NON-NLS-1$
            showUsersList = "true".equalsIgnoreCase(settings.get("showUsersList")); //$NON-NLS-1$ //$NON-NLS-2$
          }
          // get the default 'open in new window' flag, this is the default, overridden by a cookie
          openInNewWindowDefault = "true".equalsIgnoreCase(settings.get("openInNewWindow")); //$NON-NLS-1$ //$NON-NLS-2$
          setContent(buildLoginPanel(openInNewWindowDefault));
          if (isAttached() && isVisible()) {
            center();
          }
        }
      });
    } catch (RequestException e) {
      setContent(buildLoginPanel(openInNewWindowDefault));
      if (isAttached() && isVisible()) {
        center();
      }
    }
  }

  public MantleLoginDialog(AsyncCallback callback, boolean showNewWindowOption) {
    this();
    setCallback(callback);
    setShowNewWindowOption(showNewWindowOption);
  }

  public void setShowNewWindowOption(boolean show) {
    showNewWindowOption = show;
  }

  public static void performLogin(final AsyncCallback callback) {
    // let's only login if we are not actually logged in
    SERVICE.isAuthenticated(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        MantleLoginDialog dialog = new MantleLoginDialog(callback, false);
        dialog.show();
      }

      public void onSuccess(Boolean result) {
        if (!result) {
          MantleLoginDialog dialog = new MantleLoginDialog(callback, false);
          dialog.show();
        }
      }
    });
  }

  private Widget buildLoginPanel(boolean openInNewWindowDefault) {
    userTextBox.setWidth("100%"); //$NON-NLS-1$
    passwordTextBox.setWidth("100%"); //$NON-NLS-1$
    usersListBox.setWidth("100%"); //$NON-NLS-1$

    VerticalPanel loginPanel = new VerticalPanel();

    loginPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    loginPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    SimplePanel spacer;
    if (showUsersList) {
      // populate default users list box
      addDefaultUsers();
      loginPanel.add(new Label(Messages.getString("sampleUser") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
      loginPanel.add(usersListBox);
      spacer = new SimplePanel();
      spacer.setHeight("8px"); //$NON-NLS-1$
      loginPanel.add(spacer);
    }
    loginPanel.add(new Label(Messages.getString("username") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    loginPanel.add(userTextBox);

    spacer = new SimplePanel();
    spacer.setHeight("8px"); //$NON-NLS-1$
    loginPanel.add(spacer);

    loginPanel.setCellHeight(spacer, "8px"); //$NON-NLS-1$
    loginPanel.add(new HTML(Messages.getString("password") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    loginPanel.add(passwordTextBox);

    boolean reallyShowNewWindowOption = showNewWindowOption;
    
    String showNewWindowOverride = Window.Location.getParameter("showNewWindowOption"); //$NON-NLS-1$
    if (showNewWindowOverride != null && !"".equals(showNewWindowOverride)) { //$NON-NLS-1$
      // if the override is set, we MUST obey it above all else
      reallyShowNewWindowOption = "true".equals(showNewWindowOverride); //$NON-NLS-1$
    } else if (getReturnLocation() != null && !"".equals(getReturnLocation())) { //$NON-NLS-1$
      StringTokenizer st = new StringTokenizer(getReturnLocation(), "?&"); //$NON-NLS-1$
      // first token will be ignored, it is 'up to the ?'
      for (int i=1;i<st.countTokens();i++) {
        StringTokenizer paramTokenizer = new StringTokenizer(st.tokenAt(i), "="); //$NON-NLS-1$
        if (paramTokenizer.countTokens() == 2) {
          // we've got a name=value token
          if (paramTokenizer.tokenAt(0).equalsIgnoreCase("showNewWindowOption")) { //$NON-NLS-1$
            reallyShowNewWindowOption = "true".equals(paramTokenizer.tokenAt(1)); //$NON-NLS-1$
            break;
          }
        }
      }
    }
    
    // New Window checkbox
    if (reallyShowNewWindowOption) {
      spacer = new SimplePanel();
      spacer.setHeight("8px"); //$NON-NLS-1$
      loginPanel.add(spacer);
      loginPanel.setCellHeight(spacer, "8px"); //$NON-NLS-1$

      newWindowChk.setText(Messages.getString("launchInNewWindow")); //$NON-NLS-1$

      String cookieCheckedVal = Cookies.getCookie("loginNewWindowChecked"); //$NON-NLS-1$
      if (cookieCheckedVal != null) {
        newWindowChk.setChecked(Boolean.parseBoolean(cookieCheckedVal));
      } else {
        // default is false, per BISERVER-2384
        newWindowChk.setChecked(openInNewWindowDefault);
      }

      loginPanel.add(newWindowChk);
    }
    
    userTextBox.setTabIndex(1);
    passwordTextBox.setTabIndex(2);
    if (reallyShowNewWindowOption) {
      newWindowChk.setTabIndex(3);
    }
    passwordTextBox.setText(""); //$NON-NLS-1$
    setFocusWidget(userTextBox);

    return loginPanel;
  }

  public void setCallback(AsyncCallback<Boolean> callback) {
    outerCallback = callback;
  }

  public void addDefaultUsers() {
    usersListBox.clear();
    for (Map.Entry<String, String[]> entry : defaultUsers.entrySet()) {
      usersListBox.addItem(entry.getKey());
    }
    usersListBox.addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        String key = usersListBox.getValue(usersListBox.getSelectedIndex());
        userTextBox.setText(defaultUsers.get(key)[0]);
        passwordTextBox.setText(defaultUsers.get(key)[1]);
      }
    });
  }

  public String getReturnLocation() {
    return returnLocation;
  }

  public void setReturnLocation(String returnLocation) {
    this.returnLocation = returnLocation;
    // the return location might have a parameter in the url to configure options,
    // so we must rebuild the UI if the return location is changed
    setContent(buildLoginPanel(openInNewWindowDefault));    
  }

}
