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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.scheduler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.api.scheduler.IJobDetail;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.repository.content.ContentRepository;
import org.pentaho.platform.repository.content.CoreContentRepositoryOutputHandler;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.scheduler.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.logging.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

public class QuartzBackgroundExecutionHelper implements IBackgroundExecution {

  public static final String DEFAULT_JOB_NAME = "bgExecution"; //$NON-NLS-1$

  public static final String DEFAULT_TRIGGER_NAME = "bgTrigger"; //$NON-NLS-1$

  public static final String DEFAULT_BACKGROUND_LOCATION = "background"; //$NON-NLS-1$

  public static final String BACKGROUND_USER_NAME_STR = "background_user_name"; //$NON-NLS-1$

  public static final String BACKGROUND_CONTENT_GUID_STR = "background_output_content_guid"; //$NON-NLS-1$

  public static final String BACKGROUND_CONTENT_LOCATION_STR = "background_output_location"; //$NON-NLS-1$

  public static final String BACKGROUND_CONTENT_COOKIE_PREFIX = "pentaho_background_content"; //$NON-NLS-1$

  public static final String BACKGROUND_EXECUTION_FLAG = "backgroundExecution"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog(QuartzBackgroundExecutionHelper.class);

  /*
   * **************************** Methods from the Interface****************************
   */

  /**
   * NOTE: client code is responsible for making sure a job with the name identified by the parameter StandardSettings.SCHEDULE_NAME in the parameter provider
   * does not already exist in the quartz scheduler. If such a job does already exist,
   * 
   * @param parameterProvider
   *          IParameterProvider expected to have the following parameters: required: solution path action optional (cron-string is required to create a
   *          CronTrigger): cron-string repeat-count repeat-time-milliseconds start-date end-date
   * 
   */
  public String backgroundExecuteAction(IPentahoSession userSession, IParameterProvider parameterProvider) throws BackgroundExecutionException {
    try {
      String cronString = parameterProvider.getStringParameter(StandardSettings.CRON_STRING, null);
      String repeatInterval = parameterProvider.getStringParameter(StandardSettings.REPEAT_TIME_MILLISECS, null);
      assert (repeatInterval == null && cronString != null) || (repeatInterval != null && cronString == null) || (repeatInterval == null && cronString == null) : Messages.getInstance()
          .getErrorString("QuartzBackgroundExecutionHelper.ERROR_0423_INVALID_INTERVAL"); //$NON-NLS-1$

      String solutionName = parameterProvider.getStringParameter(StandardSettings.SOLUTION, null); //$NON-NLS-1$
      String actionPath = parameterProvider.getStringParameter(StandardSettings.PATH, null); //$NON-NLS-1$
      String actionName = parameterProvider.getStringParameter(StandardSettings.ACTION, null); //$NON-NLS-1$

      String actionSeqPath = parameterProvider.getStringParameter(StandardSettings.ACTIONS_REF, null);
      if (actionSeqPath == null || actionSeqPath.length() <= 0) {
        actionSeqPath = solutionName + ISolutionRepository.SEPARATOR + actionPath + ISolutionRepository.SEPARATOR + actionName;
      }
      actionSeqPath = cleanActionPath(actionSeqPath);
      String description = parameterProvider.getStringParameter(StandardSettings.DESCRIPTION, null);
      String scheduleName = null;
      String scheduleGroupName = null;

      String outputContentGUID = UUIDUtil.getUUIDAsString();

      if ((null == cronString) && (null == repeatInterval)) {
        // must be a quick one-shot background schedule
        scheduleName = outputContentGUID;
        scheduleGroupName = getUserName(userSession);
      } else {
        // must be some kind of repeating or cron schedule
        scheduleName = parameterProvider.getStringParameter(StandardSettings.SCHEDULE_NAME, null);
        scheduleGroupName = parameterProvider.getStringParameter(StandardSettings.SCHEDULE_GROUP_NAME, null);
      }

      JobDetail jobDetail = createDetailFromParameterProvider(parameterProvider, userSession, outputContentGUID, scheduleName, scheduleGroupName, description,
          actionSeqPath);

      // stores the user name and outputContentGUID in the Content Repository's persistent store (e.g. a database via hibernate)
      trackBackgroundExecution(userSession, outputContentGUID);

      Scheduler sched = QuartzSystemListener.getSchedulerInstance();

      Trigger bgTrigger = null;
      if (null != cronString) {
        String startDate = parameterProvider.getStringParameter(StandardSettings.START_DATE_TIME, null);
        String endDate = parameterProvider.getStringParameter(StandardSettings.END_DATE_TIME, null);
        bgTrigger = SchedulerHelper.createCronTrigger(scheduleName, scheduleGroupName, startDate, endDate, cronString);
      } else if (null != repeatInterval) {
        String startDate = parameterProvider.getStringParameter(StandardSettings.START_DATE_TIME, null);
        String endDate = parameterProvider.getStringParameter(StandardSettings.END_DATE_TIME, null);
        String repeatCount = parameterProvider.getStringParameter(StandardSettings.REPEAT_COUNT, null);
        bgTrigger = SchedulerHelper.createRepeatTrigger(scheduleName, scheduleGroupName, startDate, endDate, repeatCount, repeatInterval);
      } else {
        // listener's name (as returned by listener.getName() will be the value of outputContentGUID
        // the listener arranges for a flag to be set in the user's session, and can be used by web UI
        // to inform user that job has completed
        BackgroundExecuteListener listener = new BackgroundExecuteListener(userSession, scheduleName, sched, jobDetail.getName());

        sched.addJobListener(listener);
        jobDetail.addJobListener(listener.getName());

        bgTrigger = new SimpleTrigger(scheduleName, scheduleGroupName); // trigger fires now (or as soon as possible)
      }

      sched.scheduleJob(jobDetail, bgTrigger);
      // TODO: Fix with properly formatted HTML template for this status message. (<--this comment is incorrect)
      // Keep in mind that this class should be UI agnostic, and it should be the UI layer
      // that creates the UI-technology-appropriate message. (The HTML content of this message
      // should NOT be in this class.)
      return Messages.getInstance()
          .getString(
              "BackgroundExecuteHelper.USER_JOB_SUBMITTED", "UserContent", "if(window.opener) {window.opener.location.href='UserContent'; window.close() } else { return true; }"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } catch (SchedulerException ex) {
      throw new BackgroundExecutionException(Messages.getInstance().getErrorString("QuartzBackgroundExecutionHelper.ERROR_0421_UNABLE_TO_SUBMIT_USER_JOB", ex.getLocalizedMessage()), ex);
    } catch (ParseException ex) {
      throw new BackgroundExecutionException(Messages.getInstance().getErrorString("QuartzBackgroundExecutionHelper.ERROR_0422_INVALID_DATE_FORMAT", ex.getLocalizedMessage()), ex);
    }
  }

  public void trackBackgroundExecution(IPentahoSession userSession, String GUID) {
    IContentRepository repo = ContentRepository.getInstance(userSession);
    repo.newBackgroundExecutedContentId(userSession, GUID);
  }

  public IContentItem getBackgroundContent(String contentGUID, IPentahoSession userSession) {
    IContentRepository repo = ContentRepository.getInstance(userSession);
    try {
      IContentItem item = repo.getContentItemById(contentGUID);
      return item;
    } catch (Exception ex) {
      Logger.error(this.getClass().getName(), ex.getLocalizedMessage(), ex);
    }
    return null;
  }

  public List<IJobDetail> getScheduledAndExecutingBackgroundJobs(IPentahoSession userSession) throws BackgroundExecutionException {
    try {
      Scheduler sched = QuartzSystemListener.getSchedulerInstance();
      String userName = getUserName(userSession);
      String[] jobNames = sched.getJobNames(userName); // can throw SchedulerException
      List<IJobDetail> rtn = new ArrayList<IJobDetail>();
      if (jobNames != null) {
        for (int i = 0; i < jobNames.length; i++) {
          JobDetail jobDetail = sched.getJobDetail(jobNames[i], userName); // can throw SchedulerException
          rtn.add(new QuartzJobDetail(jobDetail));
        }
      }
      return rtn;
    } catch (SchedulerException ex) {
      throw new BackgroundExecutionException(Messages.getInstance().getErrorString("QuartzBackgroundExecutionHelper.ERROR_0420_FAILED_TO_GET_JOBS_FROM_SCHEDULER", ex.getLocalizedMessage()), ex);
    }
  }

  public void removeBackgroundExecutedContentForID(String contentGUID, IPentahoSession userSession) {

    // First, remove content item from the repo with that GUID.
    IContentRepository repo = ContentRepository.getInstance(userSession);
    try {
      IContentItem item = repo.getContentItemById(contentGUID);
      if (item != null) {
        item.makeTransient();
      } else {
        return;
      }
    } finally {
      HibernateUtil.commitTransaction();
    }
    repo.removeBackgroundExecutedContentId(userSession, contentGUID);
  }

  public List getBackgroundExecutedContentList(IPentahoSession userSession) {
    IContentRepository repo = ContentRepository.getInstance(userSession);
    ArrayList idList = new ArrayList();
    List idObjectList = repo.getBackgroundExecutedContentItemsForUser(userSession);
    if (idObjectList != null) {
      IContentItem contentItem = null;
      for (int i = 0; i < idObjectList.size(); i++) {
        contentItem = (IContentItem) idObjectList.get(i);
        idList.add(contentItem);
      }
    }
    return idList;
  }

  // Helper Utility Methods
  /**
   * @param parameterProvider
   * @param userSession
   * @param outputContentGUID
   *          String will be used as the job name in Quartz
   * @param jobGroup
   *          String will be used as the job group name in Quartz
   * @param solutionName
   * @param actionPath
   * @param actionName
   */
  protected JobDetail createDetailFromParameterProvider(IParameterProvider parameterProvider, IPentahoSession userSession, String outputContentId,
      String jobName, String jobGroup, String description, String actionSeqPath) {

    JobDetail jobDetail = new JobDetail(jobName, jobGroup, QuartzExecute.class);
    if (null != description) {
      jobDetail.setDescription(description);
    }
    JobDataMap data = jobDetail.getJobDataMap();
    Iterator<String> inputNamesIterator = parameterProvider.getParameterNames();
    while (inputNamesIterator.hasNext()) {
      String inputName = (String) inputNamesIterator.next();
      Object inputValue = parameterProvider.getParameter(inputName);
      data.put(inputName, inputValue);
    }
    ActionInfo actionInfo = ActionInfo.parseActionString(actionSeqPath);
    String actionName = actionInfo.getActionName();

    int lastDot = actionName.lastIndexOf('.');
    String type = actionName.substring(lastDot + 1);
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, userSession);
    IContentGenerator generator = null;
    try {
      generator = pluginManager.getContentGeneratorForType(type, userSession);
    } catch (Exception ignored) {
      // don't let a bad plugin situation take us down
      logger.warn(ignored.getMessage(), ignored);
    }
    
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, userSession);
    String title = jobName;
    if (generator != null) {
      // get the title for the plugin file
      InputStream inputStream = null;
      try {
        inputStream = repo.getResourceInputStream(actionSeqPath, true, ISolutionRepository.ACTION_EXECUTE);
      } catch (FileNotFoundException e) {
        logger.warn(e.getMessage(), e);
        // proceed to get the file info from the plugin manager. getFileInfo will return a failsafe fileInfo when something goes wrong.
      }
      IFileInfo fileInfo = pluginManager.getFileInfo(type, userSession, repo.getSolutionFile(actionSeqPath, ISolutionRepository.ACTION_EXECUTE), inputStream);
      title = fileInfo.getTitle();
    } else {
      IActionSequence action = repo.getActionSequence(actionInfo.getSolutionName(), actionInfo.getPath(), actionInfo.getActionName(), repo.getLoggingLevel(),
          ISolutionRepository.ACTION_EXECUTE);
      title = action.getTitle();
    }

    data.put(BACKGROUND_ACTION_NAME_STR, title);
    data.put("processId", this.getClass().getName()); //$NON-NLS-1$
    data.put(BACKGROUND_USER_NAME_STR, getUserName(userSession));
    data.put(BACKGROUND_CONTENT_GUID_STR, outputContentId);
    data.put(BACKGROUND_CONTENT_LOCATION_STR, DEFAULT_BACKGROUND_LOCATION + "/" + UUIDUtil.getUUIDAsString()); //$NON-NLS-1$ 
    SimpleDateFormat fmt = new SimpleDateFormat();
    data.put(BACKGROUND_SUBMITTED, fmt.format(new Date()));

    data.put(StandardSettings.SOLUTION, actionInfo.getSolutionName());
    data.put(StandardSettings.PATH, actionInfo.getPath());
    data.put(StandardSettings.ACTION, actionInfo.getActionName());

    // This tells our execution component (QuartzExecute) that we're running a background job instead of
    // a standard quartz execution.
    data.put(BACKGROUND_EXECUTION_FLAG, "true"); //$NON-NLS-1$

    return jobDetail;

  }

  public IPentahoSession getEffectiveUserSession(final String user) {
    UserSession us = new UserSession(user, null, true, null);
    return us;
  }

  public static class BackgroundExecuteListener implements JobListener {

    private IPentahoSession userSession;

    private String contentGUID;

    private Scheduler sched;

    private String jobName;

    public BackgroundExecuteListener(IPentahoSession session, String contentGUID, Scheduler scheduler, String jobName) {
      userSession = session;
      this.contentGUID = contentGUID;
      this.jobName = jobName;
      this.sched = scheduler;
    }

    public String getName() {
      return contentGUID;
    }

    public void jobExecutionVetoed(JobExecutionContext context) {
      // TODO Auto-generated method stub

    }

    public void jobToBeExecuted(JobExecutionContext context) {
      // TODO Auto-generated method stub

    }

    public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
      // Update the userSession with the updated content item.
      JobDetail ctxDetail = context.getJobDetail();
      if ((ctxDetail != null) && (ctxDetail.getName().equals(this.jobName))) { // Only do if it's for our job...
        Object contentItemGUID = ctxDetail.getJobDataMap().get(BACKGROUND_CONTENT_GUID_STR);
        if (contentItemGUID != null && userSession != null) {
          userSession.setBackgroundExecutionAlert(); // Toggle the alert status
        } else if (contentItemGUID == null) {
          Logger.warn(this.getClass().getName(), Messages.getInstance().getString("BackgroundExecuteHelper.WARN_CONTENT_ITEM_NOT_CREATED")); //$NON-NLS-1$
        }
        this.userSession = null; // Make sure nothing keeps a handle to the user session.
        try {
          if (sched != null) {
            sched.removeJobListener(this.getName());
          }
        } catch (RuntimeException ex) {
          throw ex; // programmer error, let RuntimeExceptions leak
        } catch (Exception ex) {
          logger.error(Messages.getInstance().getErrorString("BackgroundExecuteHelper.ERROR_0002_REMOVE_LISTENER_FAILED"), ex); //$NON-NLS-1$
        }
      }
    }

  }

  public IOutputHandler getContentOutputHandler(final String location, final String fileName, final String solutionName, final IPentahoSession userSession,
      final IParameterProvider parameterProvider) {
    return new CoreContentRepositoryOutputHandler(location, fileName, solutionName, userSession);
  }

  private static String getUserName(IPentahoSession userSession) {
    return userSession.isAuthenticated() ? userSession.getName() : IBackgroundExecution.DEFAULT_USER_NAME;
  }
  /*
   * cleanActionPath cleans action path like steelswheels////myreport.xaction to steelwheels/myreport.xaction
   * Note - the ISolutionRepository.SEPARATOR could be used here, but it's hardcoded to
   * forward slash and has been for two years. So - Changing to be clean with no
   * additional temporary object allocations. MB 2009-08-13
   */
  private String cleanActionPath(String s) {
    while (s.indexOf("//")>=0) { //$NON-NLS-1$
      return cleanActionPath(s.replaceAll("//", "/")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return s;
  }


}
