/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
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
 * @created Jul 30, 2008 
 * @author wseyler
 */


package org.pentaho.mantle.client.perspective.solutionbrowser.scheduling;

import java.util.Date;

import org.pentaho.gwt.widgets.client.controls.schededitor.ScheduleEditor.ScheduleType;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.gwt.widgets.client.wizards.IWizardPanel;
import org.pentaho.gwt.widgets.client.wizards.panels.ScheduleEditorWizardPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author wseyler
 *
 */
public class NewScheduleDialog extends AbstractWizardDialog {
  FileItem fileItem = null;
  
  ScheduleEditorWizardPanel scheduleEditorWizardPanel = new ScheduleEditorWizardPanel();
  
  String solutionName;
  String path;
  String actionName;
  
  Boolean done = false;
  /**
   * @param solutionName
   * @param path
   * @param actionName
   */
  public NewScheduleDialog(String solutionName, String path, String actionName) {
    super(Messages.getInstance().newSchedule(), null, false, false);
    this.solutionName = solutionName;
    this.path = path;
    this.actionName = actionName;
    
    IWizardPanel[] wizardPanels = {scheduleEditorWizardPanel};
    this.setWizardPanels(wizardPanels);
    setPixelSize(410, 480);
  }

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#finish()
   */
  @Override
  protected boolean onFinish() {
    AsyncCallback scheduleCallback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), caught.toString(), false, false, true);
        dialogBox.center();
        setDone(false);
      }

      public void onSuccess(Object result) {
        MessageDialogBox dialogBox = new MessageDialogBox(
            Messages.getInstance().info(), Messages.getInstance().actionSequenceScheduledSuccess(), 
            true, false, true);
        dialogBox.center();
        setDone(true);
        NewScheduleDialog.this.hide();
      }
      
    };
    ScheduleType scheduleType = scheduleEditorWizardPanel.getScheduleType();
    if (scheduleType == ScheduleType.SECONDS ||   // Simple Trigger Types
        scheduleType == ScheduleType.MINUTES ||
        scheduleType == ScheduleType.HOURS) {
      String triggerName = scheduleEditorWizardPanel.getTriggerName();
      String triggerGroup = scheduleEditorWizardPanel.getTriggerGroup();
      String description = scheduleEditorWizardPanel.getDescription();
      Date startDate = scheduleEditorWizardPanel.getStartDate();
      Date endDate = scheduleEditorWizardPanel.getEndDate();
      String startTime = scheduleEditorWizardPanel.getStartTime();
      int startHour = getStartHour(startTime);
      int startMin = getStartMin(startTime);
      Date startDateTime = new Date(startDate.getYear(), startDate.getMonth(), startDate.getDay(), startHour, startMin );
      int repeatCount = scheduleEditorWizardPanel.getRepeatCount();
      int repeatInterval = Integer.parseInt(scheduleEditorWizardPanel.getRepeatInterval()) * 1000;
     
      MantleServiceCache.getService().createSimpleTriggerJob(triggerName, triggerGroup, description, startDateTime, endDate, repeatCount, repeatInterval, solutionName, path, actionName, scheduleCallback);
    } else if (scheduleType != ScheduleType.RUN_ONCE) { // CRON Trigger Types
      String cronExpression = scheduleEditorWizardPanel.getCronString();
      String triggerName = scheduleEditorWizardPanel.getTriggerName();
      String triggerGroup = scheduleEditorWizardPanel.getTriggerGroup();
      String description = scheduleEditorWizardPanel.getDescription();
      
      MantleServiceCache.getService().createCronJob(solutionName, path, actionName, triggerName, triggerGroup, description, cronExpression, scheduleCallback);
    } else {  // Run once types
      String triggerName = scheduleEditorWizardPanel.getTriggerName();
      String triggerGroup = scheduleEditorWizardPanel.getTriggerGroup();
      String description = scheduleEditorWizardPanel.getDescription();
      Date startDate = scheduleEditorWizardPanel.getStartDate();
      
      MantleServiceCache.getService().createSimpleTriggerJob(triggerName, triggerGroup, description, startDate, null, 0, 0, solutionName, path, actionName, scheduleCallback);
    }

    return getDone();
  }

  /**
   * @param startTime
   * @return
   */
  private int getStartMin(String startTime) {
    if (startTime == null || startTime.length() < 1) {
      return 0;
    }
    int firstSeparator = startTime.indexOf(':');
    int secondSeperator = startTime.indexOf(':', firstSeparator+1);
    int min = Integer.parseInt(startTime.substring(firstSeparator+1, secondSeperator));
    return min;
  }

  /**
   * @param startTime
   * @return
   */
  private int getStartHour(String startTime) {
    if (startTime == null || startTime.length() < 1) {
      return 0;
    }
    int afternoonOffset = startTime.endsWith("PM") ? 12 : 0;
    int hour = Integer.parseInt(startTime.substring(0, startTime.indexOf(':')));
    hour += afternoonOffset;
    return hour;
  }

  public Boolean getDone() {
    return done;
  }

  public void setDone(Boolean done) {
    this.done = done;
  }

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onNext(org.pentaho.gwt.widgets.client.wizards.IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onNext(IWizardPanel nextPanel, IWizardPanel previousPanel) {
    // TODO Auto-generated method stub
    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onPrevious(org.pentaho.gwt.widgets.client.wizards.IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onPrevious(IWizardPanel previousPanel, IWizardPanel currentPanel) {
    // TODO Auto-generated method stub
    return true;
  }

}
