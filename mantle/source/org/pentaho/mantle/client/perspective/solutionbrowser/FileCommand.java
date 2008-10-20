/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser;

import java.util.Date;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class FileCommand implements Command {

  public static enum COMMAND {
    RUN, EDIT, PROPERTIES, BACKGROUND, NEWWINDOW, SCHEDULE_DAILY, SCHEDULE_WEEKDAYS, SCHEDULE_MWF, SCHEDULE_TUTH, SCHEDULE_WEEKLY_MON, SCHEDULE_WEEKLY_TUE, SCHEDULE_WEEKLY_WED, SCHEDULE_WEEKLY_THU, SCHEDULE_WEEKLY_FRI, SCHEDULE_WEEKLY_SAT, SCHEDULE_WEEKLY_SUN, SCHEDULE_MONTHLY_1ST, SCHEDULE_MONTHLY_15TH, SCHEDULE_MONTHLY_FIRST_SUN, SCHEDULE_MONTHLY_FIRST_MON, SCHEDULE_MONTHLY_LAST_FRI, SCHEDULE_MONTHLY_LAST_SUN, SCHEDULE_MONTHLY_LAST_DAY, SCHEDULE_ANNUALLY, SCHEDULE_CUSTOM, SCHEDULE_NEW, SUBSCRIBE, SHARE, EDIT_ACTION
  };

  COMMAND mode = COMMAND.RUN;
  PopupPanel popupMenu;
  IFileItemCallback fileItemCallback;

  public FileCommand(COMMAND inMode, PopupPanel popupMenu, IFileItemCallback fileItemCallback) {
    this.mode = inMode;
    this.popupMenu = popupMenu;
    this.fileItemCallback = fileItemCallback;
  }

  public void execute() {
    if (popupMenu != null) {
      popupMenu.hide();
    }
    if (mode == COMMAND.RUN || mode == COMMAND.BACKGROUND || mode == COMMAND.NEWWINDOW) {
      fileItemCallback.openFile(mode);
    } else if (mode == COMMAND.PROPERTIES) {
      fileItemCallback.loadPropertiesDialog();
    } else if (mode == COMMAND.EDIT) {
      fileItemCallback.editFile();
    } else if (mode == COMMAND.EDIT_ACTION) {
      fileItemCallback.editActionFile();
    } else if (mode == COMMAND.SCHEDULE_NEW) {
      fileItemCallback.createSchedule();
    } else if (mode == COMMAND.SCHEDULE_CUSTOM) {
      Date now = new Date();
      int second = now.getSeconds();
      int minute = now.getMinutes();
      int hour = now.getHours();
      final TextBox cronTextBox = new TextBox();
      cronTextBox.setText(second + " " + minute + " " + hour + " * * ?");
      IDialogCallback callback = new IDialogCallback() {

        public void cancelPressed() {
        }

        public void okPressed() {
          TextBox cronTextBox = new TextBox();
          fileItemCallback.createSchedule(cronTextBox.getText());
        }

      };
      PromptDialogBox inputDialog = new PromptDialogBox("Custom CRON Schedule", "Schedule", "Cancel", false, true);
      inputDialog.setContent(cronTextBox);
      inputDialog.setCallback(callback);
      inputDialog.center();
    } else {
      Date now = new Date();
      // need to build cron expressions for each possible type
      // Seconds 0-59 , - * /
      // Minutes 0-59 , - * /
      // Hours 0-23 , - * /
      // Day-of-month 1-31 , - * ? / L W C
      // Month 1-12 or JAN-DEC , - * /
      // Day-of-Week 1-7 or SUN-SAT , - * ? / L C #
      // Year (Optional) empty, 1970-2099 , - * /
      int second = now.getSeconds();
      int minute = now.getMinutes();
      int hour = now.getHours();
      int day = now.getDate();
      int month = now.getMonth() + 1;
      if (mode == COMMAND.SCHEDULE_DAILY) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " * * ?");
      } else if (mode == COMMAND.SCHEDULE_WEEKDAYS) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * MON-FRI");
      } else if (mode == COMMAND.SCHEDULE_MWF) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * MON,WED,FRI");
      } else if (mode == COMMAND.SCHEDULE_TUTH) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * TUE,THU");
      } else if (mode == COMMAND.SCHEDULE_WEEKLY_MON) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * MON");
      } else if (mode == COMMAND.SCHEDULE_WEEKLY_TUE) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * TUE");
      } else if (mode == COMMAND.SCHEDULE_WEEKLY_WED) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * WED");
      } else if (mode == COMMAND.SCHEDULE_WEEKLY_THU) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * THU");
      } else if (mode == COMMAND.SCHEDULE_WEEKLY_FRI) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * FRI");
      } else if (mode == COMMAND.SCHEDULE_WEEKLY_SAT) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * SAT");
      } else if (mode == COMMAND.SCHEDULE_WEEKLY_SUN) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * SUN");
      } else if (mode == COMMAND.SCHEDULE_MONTHLY_1ST) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " 1 * ?");
      } else if (mode == COMMAND.SCHEDULE_MONTHLY_15TH) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " 15 * ?");
      } else if (mode == COMMAND.SCHEDULE_MONTHLY_FIRST_SUN) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * 1#1");
      } else if (mode == COMMAND.SCHEDULE_MONTHLY_FIRST_MON) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * 2#1");
      } else if (mode == COMMAND.SCHEDULE_MONTHLY_LAST_FRI) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * 6L");
      } else if (mode == COMMAND.SCHEDULE_MONTHLY_LAST_SUN) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * 1L");
      } else if (mode == COMMAND.SCHEDULE_MONTHLY_LAST_DAY) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " L * ?");
      } else if (mode == COMMAND.SCHEDULE_ANNUALLY) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " " + day + " " + month + " ?");
      } else if (mode == COMMAND.SUBSCRIBE) {
        fileItemCallback.openFile(mode);
      } else if (mode == COMMAND.SHARE) {
        fileItemCallback.shareFile();
      }
    }
  }

  public void setFileItemCallback(IFileItemCallback fileItemCallback) {
    this.fileItemCallback = fileItemCallback;
  }

}