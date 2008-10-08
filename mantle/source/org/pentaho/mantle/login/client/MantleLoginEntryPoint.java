package org.pentaho.mantle.login.client;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.login.client.messages.Messages;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MantleLoginEntryPoint implements EntryPoint {

  private MantleLoginDialog loginDialog;
  private String returnLocation;

  public void onModuleLoad() {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onFailure(Throwable err) {
        MessageDialogBox dialog = new MessageDialogBox(Messages.getInstance().error(), err.getMessage(), false, true, true);
        dialog.setCallback(new IDialogCallback() {
          public void cancelPressed() {
          }

          public void okPressed() {
            loginDialog.show();
          }
        });
        dialog.center();
      }

      public void onSuccess(Boolean newWindow) {
        if (newWindow) {
          String URL = (!returnLocation.equals("")) ? returnLocation : Window.Location.getPath().replace("Login", "Home"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

          Window.open(URL, "puc", "menubar=no,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
          // calling reload here will make the login button say logout
          Window.Location.reload();
        } else if (!returnLocation.equals("")) { //$NON-NLS-1$
          Window.Location.assign(returnLocation);
        } else {
          Window.Location.replace(Window.Location.getPath().replace("Login", "Home")); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }

    };

    loginDialog = new MantleLoginDialog(callback, true);

    setupNativeHooks(loginDialog, this);
    setReturnLocation(null);

  }

  public void setReturnLocation(String str) {
    returnLocation = str;
  }

  public native void setupNativeHooks(MantleLoginDialog dialog, MantleLoginEntryPoint entry) /*-{
      $wnd.openLoginDialog = function(location) {
        entry.@org.pentaho.mantle.login.client.MantleLoginEntryPoint::setReturnLocation(Ljava/lang/String;)(location);
        dialog.@org.pentaho.mantle.login.client.MantleLoginDialog::center()();
      }
    }-*/;

}
