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
package org.pentaho.mantle.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.mantle.client.MantleXulOverlay;
import org.pentaho.mantle.client.objects.JobDetail;
import org.pentaho.mantle.client.objects.JobSchedule;
import org.pentaho.mantle.client.objects.RolePermission;
import org.pentaho.mantle.client.objects.SimpleMessageException;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.SubscriptionBean;
import org.pentaho.mantle.client.objects.SubscriptionSchedule;
import org.pentaho.mantle.client.objects.SubscriptionState;
import org.pentaho.mantle.client.objects.UserPermission;
import org.pentaho.mantle.client.objects.WorkspaceContent;
import org.pentaho.mantle.client.service.MantleService;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;
import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.api.scheduler.IJobDetail;
import org.pentaho.platform.api.scheduler.IJobSchedule;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.services.versionchecker.PentahoVersionCheckReflectHelper;
import org.pentaho.platform.repository.content.ContentItemFile;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.subscription.Schedule;
import org.pentaho.platform.repository.subscription.Subscription;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.pentaho.platform.scheduler.SchedulerHelper;
import org.pentaho.platform.util.VersionHelper;
import org.pentaho.platform.util.VersionInfo;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.http.session.PentahoHttpSession;
import org.pentaho.platform.web.refactor.UserFilesComponent;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.IMenuCustomization.CustomizationType;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class MantleServlet extends RemoteServiceServlet implements MantleService {

  protected static final Log logger = LogFactory.getLog(MantleServlet.class);
  private static final String DESC_SEPERATOR = " : "; //$NON-NLS-1$

  protected void onBeforeRequestDeserialized(String serializedRequest) {
    PentahoSystem.systemEntryPoint();
  }

  protected void onAfterResponseSerialized(String serializedResponse) {
    PentahoSystem.systemExitPoint();
  }

  @Override
  protected void doUnexpectedFailure(Throwable e) {
    try {
      getThreadLocalResponse().sendRedirect("../Home"); //$NON-NLS-1$
      PentahoSystem.systemExitPoint();
    } catch (IOException e1) {
      logger.error("doUnexpectedFailure", e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    logger.warn("GET request not supported");
    try {
      resp.sendRedirect("../Home"); //$NON-NLS-1$
    } catch (IOException e1) {
    }
  }

  private IPentahoSession getPentahoSession() {
    HttpSession session = getThreadLocalRequest().getSession();
    IPentahoSession userSession = (IPentahoSession) session.getAttribute(IPentahoSession.PENTAHO_SESSION_KEY);

    LocaleHelper.setLocale(getThreadLocalRequest().getLocale());
    if (userSession != null) {
      return userSession;
    }
    userSession = new PentahoHttpSession(getThreadLocalRequest().getRemoteUser(), getThreadLocalRequest().getSession(), getThreadLocalRequest().getLocale(),
        null);
    LocaleHelper.setLocale(getThreadLocalRequest().getLocale());
    session.setAttribute(IPentahoSession.PENTAHO_SESSION_KEY, userSession);
    return userSession;
  }

  public boolean isAdministrator() {
    return SecurityHelper.isPentahoAdministrator(getPentahoSession());
  }

  @SuppressWarnings("unchecked")//$NON-NLS-1$
  private UserFilesComponent getUserFilesComponent() {
    UserFilesComponent userFiles = PentahoSystem.get(UserFilesComponent.class, "IUserFilesComponent", getPentahoSession()); //$NON-NLS-1$
    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    String thisUrl = baseUrl + "UserContent?"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory(thisUrl);
    userFiles.setUrlFactory(urlFactory);
    userFiles.setRequest(getThreadLocalRequest());
    userFiles.setResponse(getThreadLocalResponse());
    userFiles.setMessages(new ArrayList());
    userFiles.validate(getPentahoSession(), null);
    return userFiles;
  }

  @SuppressWarnings("unchecked")//$NON-NLS-1$
  public String getSoftwareUpdatesDocument() {
    if (PentahoVersionCheckReflectHelper.isVersionCheckerAvailable()) {
      List results = PentahoVersionCheckReflectHelper.performVersionCheck(false, -1);
      return PentahoVersionCheckReflectHelper.logVersionCheck(results, logger);
    }
    return "<vercheck><error><[!CDATA[Version Checker is disabled]]></error></vercheck>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public void executeGlobalActions() {
    if (isAdministrator()) {
      PentahoSystem.publish(getPentahoSession(), org.pentaho.platform.engine.core.system.GlobalListsPublisher.class.getName());
    }
  }

  public String refreshMetadata() {
    String result = null;
    if (isAdministrator()) {
      result = PentahoSystem.publish(getPentahoSession(), org.pentaho.platform.engine.services.metadata.MetadataPublisher.class.getName());
    }
    return result;
  }

  public void refreshSystemSettings() {
    if (isAdministrator()) {
      PentahoSystem.publish(getPentahoSession(), org.pentaho.platform.engine.core.system.SettingsPublisher.class.getName());
    }
  }

  public boolean isAuthenticated() {
    return getPentahoSession() != null && getPentahoSession().isAuthenticated();
  }

  public WorkspaceContent getWorkspaceContent() {
    WorkspaceContent content = new WorkspaceContent();
    content.setAllSchedules(getAllSchedules());
    content.setCompletedJobs(getCompletedBackgroundContent());
    content.setMySchedules(getMySchedules());
    content.setScheduledJobs(getScheduledBackgroundContent());
    content.setSubscriptions(getSubscriptionsForMyWorkspace());
    return content;
  }

  @SuppressWarnings("unchecked")//$NON-NLS-1$
  public ArrayList<JobDetail> getScheduledBackgroundContent() {
    getPentahoSession().resetBackgroundExecutionAlert();
    IBackgroundExecution backgroundExecution = PentahoSystem.get(IBackgroundExecution.class, getPentahoSession());
    if (backgroundExecution != null) {
      try {
        List<IJobDetail> jobsList = (List<IJobDetail>) backgroundExecution.getScheduledAndExecutingBackgroundJobs(getPentahoSession());
        ArrayList<JobDetail> myJobs = new ArrayList<JobDetail>(jobsList.size());
        for (IJobDetail jobDetail : jobsList) {
          JobDetail myJobDetail = new JobDetail();
          myJobDetail.id = jobDetail.getName();
          myJobDetail.name = jobDetail.getActionName();
          myJobDetail.fullname = jobDetail.getFullName();
          myJobDetail.description = jobDetail.getDescription();
          myJobDetail.timestamp = jobDetail.getSubmissionDate();
          myJobDetail.group = jobDetail.getGroupName();
          myJobs.add(myJobDetail);
        }
        return myJobs;
      } catch (BackgroundExecutionException bee) {
        // since this is GWT-RPC we cannot serialize this particular exception
        // so we will return an empty list, like the else condition below
        return new ArrayList<JobDetail>();
      }
    } else {
      return new ArrayList<JobDetail>();
    }
  }

  @SuppressWarnings("unchecked")//$NON-NLS-1$
  public ArrayList<JobDetail> getCompletedBackgroundContent() {
    getPentahoSession().resetBackgroundExecutionAlert();
    IBackgroundExecution backgroundExecution = PentahoSystem.get(IBackgroundExecution.class, getPentahoSession());
    if (backgroundExecution != null) {
      List<IContentItem> jobsList = (List<IContentItem>) backgroundExecution.getBackgroundExecutedContentList(getPentahoSession());
      ArrayList<JobDetail> myJobs = new ArrayList<JobDetail>(jobsList.size());
      SimpleDateFormat fmt = new SimpleDateFormat();
      for (IContentItem contentItem : jobsList) {
        JobDetail myJobDetail = new JobDetail();
        myJobDetail.id = contentItem.getId();
        String dateStr = ""; //$NON-NLS-1$
        Date time = contentItem.getFileDateTime();
        if (time != null) {
          dateStr = fmt.format(time);
        }
        myJobDetail.name = contentItem.getTitle();
        myJobDetail.fullname = contentItem.getActionName();
        myJobDetail.description = contentItem.getActionName();
        myJobDetail.timestamp = dateStr;
        myJobDetail.size = Long.toString(contentItem.getFileSize());
        myJobDetail.type = contentItem.getMimeType();
        myJobs.add(myJobDetail);
      }
      return myJobs;
    } else {
      return new ArrayList<JobDetail>();
    }
  }

  public boolean cancelBackgroundJob(String jobName, String jobGroup) {
    UserFilesComponent userFiles = getUserFilesComponent();
    boolean status = userFiles.cancelJob(jobName, jobGroup);
    return status;
  }

  public boolean deleteContentItem(String contentId) {
    UserFilesComponent userFiles = getUserFilesComponent();
    boolean status = userFiles.deleteContent(contentId);
    return status;
  }

  public void refreshRepository() {
    if (isAdministrator()) {
      PentahoSystem.get(ISolutionRepository.class, getPentahoSession()).reloadSolutionRepository(getPentahoSession(), getPentahoSession().getLoggingLevel());
    }
  }

  public int cleanContentRepository(int daysBack) {
    int deleteCount = 0;
    if (isAdministrator()) {
      // get daysback off the input
      daysBack = Math.abs(daysBack) * -1;

      // get todays calendar
      Calendar calendar = Calendar.getInstance();
      // subtract (by adding a negative number) the daysback amount
      calendar.add(Calendar.DATE, daysBack);
      // create the new date for the content repository to use
      Date agedDate = new Date(calendar.getTimeInMillis());
      // get the content repository and tell it to remove the items older than
      // agedDate
      IContentRepository contentRepository = PentahoSystem.get(IContentRepository.class, getPentahoSession());
      deleteCount = contentRepository.deleteContentOlderThanDate(agedDate);
    }
    return deleteCount;
  }

  public void flushMondrianSchemaCache() {
    if (isAdministrator()) {
      MondrianCatalogHelper.getInstance().reInit(getPentahoSession());
    }
  }

  public ArrayList<JobSchedule> getMySchedules() {
    ArrayList<JobSchedule> jobSchedules = null;
    try {
      List<IJobSchedule> schedules = SchedulerHelper.getMySchedules(getPentahoSession());
      jobSchedules = iJobSchedule2JobSchedule(schedules);
      // these are functionally the same exact objects (mantle
      // JobSchedule/platform JobSchedule)
    } catch (Exception e) {
      logger.error(e.getMessage());
      jobSchedules = new ArrayList<JobSchedule>();
    }
    return jobSchedules;
  }

  public ArrayList<JobSchedule> getAllSchedules() {
    ArrayList<JobSchedule> jobSchedules = null;
    try {
      List<IJobSchedule> schedules = SchedulerHelper.getAllSchedules(getPentahoSession());
      jobSchedules = iJobSchedule2JobSchedule(schedules);
      // these are functionally the same exact objects (mantle
      // JobSchedule/platform JobSchedule)
    } catch (Exception e) {
      logger.error(e.getMessage());
      jobSchedules = new ArrayList<JobSchedule>();
    }
    return jobSchedules;
  }

  private ArrayList<JobSchedule> iJobSchedule2JobSchedule(List<IJobSchedule> iJobSchedules) {
    ArrayList<JobSchedule> jobSchedules = new ArrayList<JobSchedule>();
    for (IJobSchedule iJobSchedule : iJobSchedules) {
      JobSchedule jobSchedule = new JobSchedule();
      jobSchedule.fullname = iJobSchedule.getFullname();
      jobSchedule.jobDescription = iJobSchedule.getJobDescription();
      jobSchedule.jobGroup = iJobSchedule.getJobGroup();
      jobSchedule.jobName = iJobSchedule.getJobName();
      jobSchedule.name = iJobSchedule.getName();
      jobSchedule.nextFireTime = iJobSchedule.getNextFireTime();
      jobSchedule.previousFireTime = iJobSchedule.getPreviousFireTime();
      jobSchedule.triggerGroup = iJobSchedule.getTriggerGroup();
      jobSchedule.triggerName = iJobSchedule.getTriggerName();
      jobSchedule.triggerState = iJobSchedule.getTriggerState();

      jobSchedules.add(jobSchedule);
    }
    return jobSchedules;
  }

  public void deleteJob(String jobName, String jobGroup) {
    SchedulerHelper.deleteJob(getPentahoSession(), jobName, jobGroup);
  }

  public void runJob(String jobName, String jobGroup) {
    SchedulerHelper.runJob(getPentahoSession(), jobName, jobGroup);
  }

  public void resumeJob(String jobName, String jobGroup) {
    SchedulerHelper.resumeJob(getPentahoSession(), jobName, jobGroup);
  }

  public void suspendJob(String jobName, String jobGroup) {
    SchedulerHelper.suspendJob(getPentahoSession(), jobName, jobGroup);
  }

  // public void createCronJob(String solutionName, String path, String
  // actionName, String cronExpression) throws SimpleMessageException {
  //    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  //      throw new SimpleMessageException(ServerMessages.getString("featureDisabled")); //$NON-NLS-1$
  // }
  // try {
  // SchedulerHelper.createCronJob(getPentahoSession(), solutionName, path,
  // actionName, cronExpression);
  // } catch (Exception e) {
  // throw new SimpleMessageException(e.getMessage());
  // }
  // }

  public void createCronJob(String solutionName, String path, String actionName, String triggerName, String triggerGroup, String description,
      String cronExpression) throws SimpleMessageException {
    if (!hasAccess(solutionName, path, actionName, ISolutionRepository.ACTION_SUBSCRIBE)) {
      throw new SimpleMessageException(ServerMessages.getInstance().getString("noSchedulePermission")); //$NON-NLS-1$
    }

    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      throw new SimpleMessageException(ServerMessages.getInstance().getString("featureDisabled")); //$NON-NLS-1$
    }

    try {
      IBackgroundExecution backgroundExecutionHandler = PentahoSystem.get(IBackgroundExecution.class, getPentahoSession());
      SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
      parameterProvider.setParameter(StandardSettings.SOLUTION, solutionName);
      parameterProvider.setParameter(StandardSettings.PATH, path);
      parameterProvider.setParameter(StandardSettings.ACTION, actionName);
      parameterProvider.setParameter(StandardSettings.CRON_STRING, cronExpression);
      parameterProvider.setParameter(StandardSettings.SCHEDULE_NAME, triggerName);
      parameterProvider.setParameter(StandardSettings.SCHEDULE_GROUP_NAME, getPentahoSession().getName());
      parameterProvider.setParameter(StandardSettings.DESCRIPTION, triggerGroup + DESC_SEPERATOR + description);
      backgroundExecutionHandler.backgroundExecuteAction(getPentahoSession(), parameterProvider);
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    } finally {
      PentahoSystem.systemExitPoint(); // Since we're creating something an
      // hibernate might throw an exception on
      // the onAfterResponseSerialized() method
      // of this
      // class
      // we need to do it before hand to see if we're going to error out.
    }
  }

  public void createSimpleTriggerJob(String triggerName, String triggerGroup, String description, Date startDate, Date endDate, int repeatCount,
      int repeatInterval, String solutionName, String path, String actionName) throws SimpleMessageException {
    if (!hasAccess(solutionName, path, actionName, ISolutionRepository.ACTION_SUBSCRIBE)) {
      throw new SimpleMessageException(ServerMessages.getInstance().getString("noSchedulePermission")); //$NON-NLS-1$
    }

    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      throw new SimpleMessageException(ServerMessages.getInstance().getString("featureDisabled")); //$NON-NLS-1$
    }

    try {
      IBackgroundExecution backgroundExecutionHandler = PentahoSystem.get(IBackgroundExecution.class, getPentahoSession());
      SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
      parameterProvider.setParameter(StandardSettings.SOLUTION, solutionName);
      parameterProvider.setParameter(StandardSettings.PATH, path);
      parameterProvider.setParameter(StandardSettings.ACTION, actionName);
      parameterProvider.setParameter(StandardSettings.REPEAT_COUNT, Integer.toString(repeatCount));
      parameterProvider.setParameter(StandardSettings.REPEAT_TIME_MILLISECS, Integer.toString(repeatInterval));
      parameterProvider.setParameter(StandardSettings.START_DATE_TIME, startDate);
      parameterProvider.setParameter(StandardSettings.END_DATE_TIME, endDate);
      parameterProvider.setParameter(StandardSettings.SCHEDULE_NAME, triggerName);
      parameterProvider.setParameter(StandardSettings.SCHEDULE_GROUP_NAME, getPentahoSession().getName());
      parameterProvider.setParameter(StandardSettings.DESCRIPTION, triggerGroup + DESC_SEPERATOR + description);
      backgroundExecutionHandler.backgroundExecuteAction(getPentahoSession(), parameterProvider);
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    } finally {
      PentahoSystem.systemExitPoint(); // Since we're creating something an
      // hibernate might throw an exception on
      // the onAfterResponseSerialized() method
      // of this
      // class
      // we need to do it before hand to see if we're going to error out.
    }
  }

  @SuppressWarnings("unchecked")//$NON-NLS-1$
  public ArrayList<String> getAllRoles() {
    IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.get(IUserDetailsRoleListService.class);
    return (ArrayList<String>) userDetailsRoleListService.getAllRoles();
  }

  @SuppressWarnings("unchecked")//$NON-NLS-1$
  public ArrayList<String> getAllUsers() {
    IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.get(IUserDetailsRoleListService.class);
    return (ArrayList<String>) userDetailsRoleListService.getAllUsers();
  }

  public SolutionFileInfo getSolutionFileInfo(String solutionName, String path, String fileName) {
    if (fileName == null || path == null || solutionName == null) {
      throw new IllegalArgumentException("getSolutionFileInfo called with null parameters"); //$NON-NLS-1$
    }

    SolutionFileInfo solutionFileInfo = new SolutionFileInfo();
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());

    String fullPath = ActionInfo.buildSolutionPath(solutionName, path, fileName);
    ISolutionFile solutionFile = repository.getSolutionFile(fullPath, ISolutionRepository.ACTION_EXECUTE);
    solutionFileInfo.solution = solutionName;
    solutionFileInfo.path = path;
    solutionFileInfo.name = fileName;

    // Get Localized name
    if (!solutionFile.isDirectory()) {
      solutionFileInfo.localizedName = repository.getLocalizedFileProperty(solutionFile, "title", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    }
    if (StringUtils.isEmpty(solutionFileInfo.localizedName)) {
      solutionFileInfo.localizedName = repository.getLocalizedFileProperty(solutionFile, "name", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    }

    // Find file Type, if plugin, also get the title
    int lastDot = -1;
    if (solutionFile.isDirectory()) {
      solutionFileInfo.type = SolutionFileInfo.Type.FOLDER;
    } else if ((lastDot = fileName.lastIndexOf('.')) > -1 && !fileName.startsWith(".")) { //$NON-NLS-1$
      String extension = fileName.substring(lastDot + 1);

      // Check to see if its a plug-in
      boolean isPlugin = false;
      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, getPentahoSession()); //$NON-NLS-1$
      if (pluginManager != null) {
        Set<String> types = pluginManager.getContentTypes();
        isPlugin = types != null && types.contains(extension);
      }

      if (isPlugin) {
        // Get the reported type from the plug-in manager
        IContentGeneratorInfo info = pluginManager.getDefaultContentGeneratorInfoForType(extension, getPentahoSession());
        solutionFileInfo.type = SolutionFileInfo.Type.PLUGIN;
        solutionFileInfo.pluginTypeName = info.getDescription();

        // get the title for the plugin file
        InputStream inputStream = null;
        try {
          inputStream = repository.getResourceInputStream(solutionFile.getFullPath(), true, ISolutionRepository.ACTION_EXECUTE);
        } catch (FileNotFoundException e) {
          logger.warn(e.getMessage(), e);
          // proceed to get the file info from the plugin manager. getFileInfo
          // will return a failsafe fileInfo when something goes wrong.
        }
        IFileInfo fileInfo = pluginManager.getFileInfo(extension, getPentahoSession(), solutionFile, inputStream);
        solutionFileInfo.localizedName = fileInfo.getTitle();
      } else if (fileName.endsWith("waqr.xaction")) { //$NON-NLS-1$
        solutionFileInfo.type = SolutionFileInfo.Type.REPORT;
      } else if (fileName.endsWith("analysisview.xaction")) { //$NON-NLS-1$
        solutionFileInfo.type = SolutionFileInfo.Type.ANALYSIS_VIEW;
      } else if (fileName.endsWith(".url")) { //$NON-NLS-1$
        solutionFileInfo.type = SolutionFileInfo.Type.URL;
      } else {
        solutionFileInfo.type = SolutionFileInfo.Type.XACTION;
      }
    }

    if (solutionFile.getData() == null) {
      solutionFileInfo.size = 0;
    } else {
      solutionFileInfo.size = solutionFile.getData().length;
    }
    solutionFileInfo.lastModifiedDate = new Date(solutionFile.getLastModified());

    solutionFileInfo.isDirectory = solutionFile.isDirectory();
    if (!solutionFile.isDirectory()) {
      ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
      ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(solutionName + path + "/" + fileName); //$NON-NLS-1$
      solutionFileInfo.isSubscribable = (subscribeContent != null) && (subscribeContent.getSchedules() != null && subscribeContent.getSchedules().size() > 0);
    } else {
      solutionFileInfo.isSubscribable = false;
    }

    solutionFileInfo.canEffectiveUserManage = isAdministrator() || repository.hasAccess(solutionFile, PentahoAclEntry.PERM_UPDATE_PERMS);
    solutionFileInfo.supportsAccessControls = repository.supportsAccessControls();
    if (solutionFileInfo.canEffectiveUserManage && solutionFileInfo.supportsAccessControls) {
      ArrayList<RolePermission> rolePermissions = new ArrayList<RolePermission>();
      ArrayList<UserPermission> userPermissions = new ArrayList<UserPermission>();
      if (solutionFile instanceof IAclSolutionFile) {
        Map<IPermissionRecipient, IPermissionMask> filePermissions = repository.getEffectivePermissions((solutionFile));
        for (Map.Entry<IPermissionRecipient, IPermissionMask> filePerm : filePermissions.entrySet()) {
          IPermissionRecipient permRecipient = filePerm.getKey();
          if (permRecipient instanceof SimpleRole) {
            // entry belongs to a role
            rolePermissions.add(new RolePermission(permRecipient.getName(), filePerm.getValue().getMask()));
          } else {
            // entry belongs to a user
            userPermissions.add(new UserPermission(permRecipient.getName(), filePerm.getValue().getMask()));
          }
        }
      }
      solutionFileInfo.userPermissions = userPermissions;
      solutionFileInfo.rolePermissions = rolePermissions;
    }
    return solutionFileInfo;
  }

  public boolean hasAccess(String solutionName, String path, String fileName, int actionOperation) {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    String fullPath = ActionInfo.buildSolutionPath(solutionName, path, fileName);
    ISolutionFile solutionFile = repository.getSolutionFile(fullPath, actionOperation);
    return solutionFile != null;
  }

  public void setSolutionFileInfo(SolutionFileInfo fileInfo) throws SimpleMessageException {
    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      throw new SimpleMessageException(ServerMessages.getInstance().getString("featureDisabled")); //$NON-NLS-1$
    }
    try {
      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
      if (repository.supportsAccessControls()) {
        String fullPath = ActionInfo.buildSolutionPath(fileInfo.solution, fileInfo.path, fileInfo.name);
        ISolutionFile solutionFile = repository.getSolutionFile(fullPath, ISolutionRepository.ACTION_SHARE);

        Map<IPermissionRecipient, IPermissionMask> origAcl = repository.getEffectivePermissions((solutionFile));

        Map<IPermissionRecipient, IPermissionMask> acl = new HashMap<IPermissionRecipient, IPermissionMask>();
        for (UserPermission userPermission : fileInfo.userPermissions) {
          acl.put(new SimpleUser(userPermission.name), new SimplePermissionMask(userPermission.mask));
        }
        for (RolePermission rolePermission : fileInfo.rolePermissions) {
          acl.put(new SimpleRole(rolePermission.name), new SimplePermissionMask(rolePermission.mask));
        }

        // only set the permissions if the user made a change to the effective
        // acls (otherwise, keep inheriting);
        // this will avoid creating access control entries in the database when
        // they are the same as the ACEs
        // that would be inherited!
        if (!origAcl.equals(acl)) {
          repository.setPermissions(solutionFile, acl);
          repository.resetRepository();
        }

        if (!solutionFile.isDirectory()) {
          ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
          String actionRef = fileInfo.solution + fileInfo.path + "/" + fileInfo.name; //$NON-NLS-1$
          ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(actionRef);
          if (fileInfo.isSubscribable && subscribeContent == null) {
            // make this actionRef subscribable
            subscriptionRepository.addContent(actionRef, ""); //$NON-NLS-1$
          } else if (!fileInfo.isSubscribable && subscribeContent != null) {
            // remove this actionRef from the subscribable list
            subscriptionRepository.deleteSubscribeContent(subscribeContent);
          }
        }
      }
    } catch (Exception e) {
      // e.printStackTrace();
      throw new SimpleMessageException(e.getMessage());
    } finally {
    }
  }

  public boolean doesSolutionRepositorySupportPermissions() {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    return repository.supportsAccessControls();
  }

  public HashMap<String, String> getMantleSettings() {
    HashMap<String, String> settings = new HashMap<String, String>();
    // read properties file
    Properties props = new Properties();
    try {
      props.load(getClass().getResourceAsStream("/org/pentaho/mantle/server/MantleSettings.properties")); //$NON-NLS-1$
      Enumeration keys = props.keys();
      while (keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        String value = (String) props.getProperty(key);
        settings.put(key, value);
      }

      settings.put("login-show-users-list", PentahoSystem.getSystemSetting("login-show-users-list", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      settings.put("documentation-url", PentahoSystem.getSystemSetting("documentation-url", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      // Check for override of New Analysis View via pentaho.xml
      // Poked in via pentaho.xml entries
      // <new-analysis-view>
      // <command-url>http://www.google.com</command-url>
      // <command-title>Marc Analysis View</command-title>
      // </new-analysis-view>
      // <new-report>
      // <command-url>http://www.yahoo.com</command-url>
      // <command-title>Marc New Report</command-title>
      // </new-report>
      // 
      String overrideNewAnalysisViewCommmand = PentahoSystem.getSystemSetting("new-analysis-view/command-url", null); //$NON-NLS-1$
      String overrideNewAnalysisViewTitle = PentahoSystem.getSystemSetting("new-analysis-view/command-title", null); //$NON-NLS-1$
      if ((overrideNewAnalysisViewCommmand != null) && (overrideNewAnalysisViewTitle != null)) {
        settings.put("new-analysis-view-command-url", overrideNewAnalysisViewCommmand); //$NON-NLS-1$
        settings.put("new-analysis-view-command-title", overrideNewAnalysisViewTitle); //$NON-NLS-1$
      }
      String overrideNewReportCommmand = PentahoSystem.getSystemSetting("new-report/command-url", null); //$NON-NLS-1$
      String overrideNewReportTitle = PentahoSystem.getSystemSetting("new-report/command-title", null); //$NON-NLS-1$
      if ((overrideNewReportCommmand != null) && (overrideNewReportTitle != null)) {
        settings.put("new-report-command-url", overrideNewReportCommmand); //$NON-NLS-1$
        settings.put("new-report-command-title", overrideNewReportTitle); //$NON-NLS-1$
      }

      // see if we have any plugin settings
      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, getPentahoSession()); //$NON-NLS-1$
      if (pluginManager != null) {
        // get the menu customizations for the plugins, if any
        List<IMenuCustomization> customs = (List<IMenuCustomization>)pluginManager.getMenuCustomizations();
        int fileIdx = 0;
        int fileNewIdx = 0;
        int fileManageIdx = 0;
        int viewIdx = 0;
        int toolsIdx = 0;
        int toolsRefreshIdx = 0;
        int aboutIdx = 0;
        int overrideIdx = 0;
        // process each customization
        for (IMenuCustomization custom : customs) {
          // we only support appending children to the first level sub-menus
          if (custom.getCustomizationType() == CustomizationType.LAST_CHILD) {
            String anchor = custom.getAnchorId();
            // do we have any additions to the file menu?
            // TODO: support file->new
            if ("file-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("fileMenuTitle" + fileIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("fileMenuCommand" + fileIdx, custom.getCommand()); //$NON-NLS-1$
              fileIdx++;
            } else if ("file-new-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("file-newMenuTitle" + fileNewIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("file-newMenuCommand" + fileNewIdx, custom.getCommand()); //$NON-NLS-1$
              fileNewIdx++;
            } else if ("file-manage-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("file-manageMenuTitle" + fileManageIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("file-manageMenuCommand" + fileManageIdx, custom.getCommand()); //$NON-NLS-1$
              fileManageIdx++;
            }
            // do we have any additions to the view menu?
            else if ("view-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("viewMenuTitle" + viewIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("viewMenuCommand" + viewIdx, custom.getCommand()); //$NON-NLS-1$
              viewIdx++;
            }
            // do we have any additions to the tools menu?
            else if ("tools-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("toolsMenuTitle" + toolsIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("toolsMenuCommand" + toolsIdx, custom.getCommand()); //$NON-NLS-1$
              toolsIdx++;
            }
            // do we have any additions to the refresh menu?
            else if ("tools-refresh-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("tools-refreshMenuTitle" + toolsRefreshIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("tools-refreshMenuCommand" + toolsRefreshIdx, custom.getCommand()); //$NON-NLS-1$
              toolsRefreshIdx++;
            }
            // do we have any additions to the about menu?
            else if ("about-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("helpMenuTitle" + aboutIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("helpMenuCommand" + aboutIdx, custom.getCommand()); //$NON-NLS-1$
              aboutIdx++;
            }
          } else if (custom.getCustomizationType() == CustomizationType.REPLACE) {
            // Support replace of new analysis view and new report only
            //
            // Example of overriding via plugin.xml
            // <menu-item id="waqr_menu_item"
            // anchor="file-new-submenu-waqr_menu_item"
            // label="New WAQR"
            // command="http://www.amazon.com"
            // type="MENU_ITEM"
            // how="REPLACE"/>
            // <menu-item id="new_analysis_view_menu_item"
            // anchor="file-new-submenu-new_analysis_view_menu_item"
            // label="New Analysis"
            // command="http://www.dogpile.com"
            // type="MENU_ITEM"
            // how="REPLACE"/>
            //
            String anchor = custom.getAnchorId();
            String anchorStart = "file-new-submenu-"; //$NON-NLS-1$
            if (anchor.startsWith(anchorStart)) {
              // Anchor needs to be in two parts
              // file-new-submenu and the submenu being replaced
              // e.g. file-new-submenu-waqr_menu_item
              String overrideMenuItem = anchor.substring(anchorStart.length());
              settings.put("file-newMenuOverrideTitle" + overrideIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("file-newMenuOverrideCommand" + overrideIdx, custom.getCommand()); //$NON-NLS-1$
              settings.put("file-newMenuOverrideMenuItem" + overrideIdx, overrideMenuItem); //$NON-NLS-1$
              overrideIdx++;
            }
          }
        }

        // load content types from IPluginSettings
        int i = 0;
        for (String contentType : pluginManager.getContentTypes()) {
          IContentInfo info = pluginManager.getContentInfoFromExtension(contentType, getPentahoSession());
          if (info != null) {
            settings.put("plugin-content-type-" + i, "." + contentType); //$NON-NLS-1$ //$NON-NLS-2$
            settings.put("plugin-content-type-icon-" + i, info.getIconUrl()); //$NON-NLS-1$
            int j = 0;
            for (IPluginOperation operation : info.getOperations()) {
              settings.put("plugin-content-type-" + i + "-command-" + j, operation.getId()); //$NON-NLS-1$
              settings.put("plugin-content-type-" + i + "-command-url-" + j, operation.getCommand()); //$NON-NLS-1$
              j++;
            }
            i++;
          }

        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.service.MantleService#isSubscriptionContent(java .lang.String)
   */
  public Boolean isSubscriptionContent(String actionRef) {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    return new Boolean(subscriptionRepository.getContentByActionReference(actionRef) != null
        && subscriptionRepository.getContentByActionReference(actionRef).getSchedules().size() > 0);
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.pentaho.mantle.client.service.MantleService# getAvailableSubscriptionSchedules()
   */
  public ArrayList<SubscriptionSchedule> getAvailableSubscriptionSchedules(String actionRef) {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(actionRef);
    List<ISchedule> appliedList = subscribeContent == null ? new ArrayList<ISchedule>() : subscribeContent.getSchedules();
    List<ISchedule> availableList = subscriptionRepository.getSchedules();
    ArrayList<SubscriptionSchedule> unusedScheduleList = new ArrayList<SubscriptionSchedule>();
    for (ISchedule schedule : availableList) {
      if (!appliedList.contains(schedule)) {
        SubscriptionSchedule subSchedule = new SubscriptionSchedule();
        subSchedule.id = schedule.getId();
        subSchedule.title = schedule.getTitle();
        subSchedule.scheduleReference = schedule.getScheduleReference();
        subSchedule.description = schedule.getDescription();
        subSchedule.cronString = schedule.getCronString();
        subSchedule.group = schedule.getGroup();
        subSchedule.lastTrigger = schedule.getLastTrigger();

        unusedScheduleList.add(subSchedule);
      }
    }
    return unusedScheduleList;
  }

  /**
   * Runs and archives the given public schedule.
   * 
   * @param publicScheduleName
   *          The public scedule to be run.
   * @return message The message that was returned from the API after running and archiving the given public schedule.
   */
  public String runAndArchivePublicSchedule(String publicScheduleName) throws SimpleMessageException {
    final IPentahoSession userSession = getPentahoSession();
    HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);

    String response = null;
    try {
      response = SubscriptionHelper.createSubscriptionArchive(publicScheduleName, userSession, null, sessionParameters);
    } catch (BackgroundExecutionException bex) {
      response = bex.getLocalizedMessage();
      throw new SimpleMessageException(Messages.getInstance().getErrorString("ViewAction.ViewAction.ERROR_UNABLE_TO_CREATE_SUBSCRIPTION_ARCHIVE")); //$NON-NLS-1$      
    }
    return response;
  }

  /**
   * Delete the contents under the public schedule and then delete the public schedule
   * 
   * @param publicScheduleName
   *          The public schedule name for the given content id
   * @param contentItemList
   *          The list of content items belonging to the given public schedule to be deleted
   * @return Error message if error occurred else success message
   */
  public String deletePublicScheduleAndContents(String publicScheduleName, ArrayList<String> contentItemList) {
    /*
     * Iterate through all the content items and delete them
     */
    if (contentItemList != null) {
      Iterator<String> iter = contentItemList.iterator();
      if (iter != null) {
        while (iter.hasNext()) {
          deleteSubscriptionArchive(publicScheduleName, iter.next());
        }
      }
    }
    /*
     * Once all the content items are deleted, go ahead and delete the actual public schedule
     */
    final String result = SubscriptionHelper.deleteSubscription(publicScheduleName, getPentahoSession());
    return result;
  }

  /**
   * Delete the given content item for the given public schedule.
   * 
   * @param publicScheduleName
   *          The public schedule name for the given content id
   * @param contentId
   *          The content item id to be deleted
   * @return Error message if error occurred else success message
   */
  public String deleteSubscriptionArchive(String publicScheduleName, String contentId) {
    final IPentahoSession session = getPentahoSession();
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    ISubscription subscription = subscriptionRepository.getSubscription(publicScheduleName, session);
    if (subscription == null) {
      // TODO surface an error
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_DOES_NOT_EXIST"); //$NON-NLS-1$
    }
    IContentItem contentItem = subscriptionRepository.getContentItem(publicScheduleName, session);
    if (contentItem == null) {
      // TODO surface an error
      return Messages.getInstance().getString("SubscriptionHelper.USER_CONTENT_ITEM_DOES_NOT_EXIST"); //$NON-NLS-1$
    }

    contentItem.removeVersion(contentId);

    return Messages.getInstance().getString("SubscriptionHelper.USER_ARCHIVE_DELETED"); //$NON-NLS-1$
  }

  /**
   * This method provides the content for the My Subscription section in the Workspace.
   * 
   * @return List<SubscriptionBean> List of subscriptions and their related information contained within the object.
   */
  public ArrayList<SubscriptionBean> getSubscriptionsForMyWorkspace() {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    final String currentUser = getPentahoSession().getName();
    final List<ISubscription> userSubscriptionList = subscriptionRepository.getUserSubscriptions(currentUser);
    final ArrayList<SubscriptionBean> opSubscrList = new ArrayList<SubscriptionBean>();

    Iterator<ISubscription> subscrIter = userSubscriptionList.iterator();
    while (subscrIter.hasNext()) {
      final ISubscription currentSubscr = subscrIter.next();
      final ActionInfo actionInfo = ActionInfo.parseActionString(currentSubscr.getContent().getActionReference());
      final String actionSeqTitle = getSolutionFileInfo(actionInfo.getSolutionName(), actionInfo.getPath(), actionInfo.getActionName()).getLocalizedName();

      Schedule schedule = null;
      final Iterator schedIterator = currentSubscr.getSchedules().iterator();
      // Get the first schedule and get out of the loop
      // The code is below to avoid null pointer exceptions and get a schedule
      // only if it exists.
      if (schedIterator != null) {
        while (schedIterator.hasNext()) {
          schedule = (Schedule) schedIterator.next();
          break;
        }
      }

      final SubscriptionBean subscriptionBean = new SubscriptionBean();
      subscriptionBean.setId(currentSubscr.getId());
      subscriptionBean.setName(currentSubscr.getTitle());
      subscriptionBean.setXactionName(actionSeqTitle);

      if (!actionInfo.getActionName().endsWith(".")) {
        int lastDot = actionInfo.getActionName().lastIndexOf('.');
        String type = actionInfo.getActionName().substring(lastDot + 1);
        // not very generic :(
        // we need plugin.xml 'properties' for this sort of need
        if (type.equals("prpt")) {
          IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, getPentahoSession()); //$NON-NLS-1$
          // with this information, a content generator can perform a lookup of
          // all the information it will
          // need to build a new parameter form for the content requested to be
          // edited
          String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
          if (baseUrl.endsWith("/") == false) {
            baseUrl += "/";
          }
          String generatorId = pluginManager.getContentGeneratorIdForType(type, getPentahoSession());
          String pluginUrl = baseUrl + "content/" + generatorId + "/reportviewer/report.html?subscribe=true&subscription-id=" + currentSubscr.getId();
          subscriptionBean.setPluginUrl(pluginUrl);
        }
      }

      if (schedule != null) {
        subscriptionBean.setScheduleDate(schedule.getTitle());
      }
      // We have static dashes here because thats the way data is being
      // displayed currently in 1.7
      subscriptionBean.setSize("---"); //$NON-NLS-1$
      subscriptionBean.setType("---"); //$NON-NLS-1$
      subscriptionBean.setContent(getContentItems(subscriptionRepository, (Subscription) currentSubscr));
      opSubscrList.add(subscriptionBean);
    }
    return opSubscrList;
  }

  /**
   * Helper method that returns action sequence title for the given subscription.
   * 
   * @param currentSubscr
   *          Current subscription
   * @return Title of the action sequence attached to the subscription.
   */
  private String getActionSequenceTitle(final Subscription currentSubscr) {
    final String actionSeqPath = currentSubscr.getContent().getActionReference();
    final ActionInfo actionInfo = ActionInfo.parseActionString(actionSeqPath);
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    final IActionSequence action = repository.getActionSequence(actionInfo.getSolutionName(), actionInfo.getPath(), actionInfo.getActionName(), repository
        .getLoggingLevel(), ISolutionRepository.ACTION_EXECUTE);
    return action.getTitle();
  }

  /**
   * This is a helper method that gets the content item information
   * 
   * @param subscriptionRepository
   * @param currentSubscr
   * @return List of String arrays where the array consists of formatted date of the content, file type and size, file id, name and OS path.
   */
  private ArrayList<String[]> getContentItems(final ISubscriptionRepository subscriptionRepository, final Subscription currentSubscr) {
    final List<ContentItemFile> contentItemFileList = (List<ContentItemFile>) subscriptionRepository.getSubscriptionArchives(currentSubscr.getId(),
        getPentahoSession());
    ArrayList<String[]> archiveList = null;

    if (contentItemFileList != null) {
      archiveList = new ArrayList<String[]>();
      for (ContentItemFile contentItemFile : contentItemFileList) {
        final Date fileItemDate = contentItemFile.getFileDateTime();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy h:mm a"); //$NON-NLS-1$
        final String formattedDateStr = dateFormat.format(fileItemDate);
        final String fileType = contentItemFile.getParent().getMimeType();
        final String fileSize = String.valueOf(contentItemFile.getFileSize());
        final String[] tempArchiveArr = new String[6];
        tempArchiveArr[0] = formattedDateStr;
        tempArchiveArr[1] = fileType;
        tempArchiveArr[2] = fileSize;
        tempArchiveArr[3] = contentItemFile.getId();
        tempArchiveArr[4] = contentItemFile.getOsFileName();
        tempArchiveArr[5] = contentItemFile.getOsPath();

        archiveList.add(tempArchiveArr);
      }
    }
    return archiveList;
  }

  public String deleteArchive(String subscrName, String fileId) {
    final String result = SubscriptionHelper.deleteSubscriptionArchive(subscrName, fileId, getPentahoSession());
    return result;
  }

  // public String viewArchive(String subscrName, String fileId) {
  // final String result = SubscriptionHelper.getArchived(subscrName, fileId,
  // getPentahoSession());
  // return result;
  // }

  public ArrayList<SubscriptionSchedule> getAppliedSubscriptionSchedules(String actionRef) {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(actionRef);
    List<ISchedule> appliedList = subscribeContent == null ? new ArrayList<ISchedule>() : subscribeContent.getSchedules();
    ArrayList<SubscriptionSchedule> appliedScheduleList = new ArrayList<SubscriptionSchedule>();
    for (ISchedule schedule : appliedList) {
      SubscriptionSchedule subSchedule = new SubscriptionSchedule();
      subSchedule.id = schedule.getId();
      subSchedule.title = schedule.getTitle();
      subSchedule.scheduleReference = schedule.getScheduleReference();
      subSchedule.description = schedule.getDescription();
      subSchedule.cronString = schedule.getCronString();
      subSchedule.group = schedule.getGroup();
      subSchedule.lastTrigger = schedule.getLastTrigger();

      appliedScheduleList.add(subSchedule);
    }
    return appliedScheduleList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.service.MantleService#setSubscriptions(java.lang .String, boolean, java.util.List)
   */
  public void setSubscriptions(String actionRef, boolean enabled, ArrayList<SubscriptionSchedule> currentSchedules) {
    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      throw new RuntimeException(ServerMessages.getInstance().getString("featureDisabled")); //$NON-NLS-1$
    }
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(actionRef);
    if (enabled) {
      if (subscribeContent == null) {
        subscribeContent = subscriptionRepository.addContent(actionRef, ""); //$NON-NLS-1$
      }

      subscribeContent.clearsSchedules();
      ArrayList<ISchedule> updatedSchedules = new ArrayList<ISchedule>();
      List<ISchedule> availableSchedules = subscriptionRepository.getSchedules();
      for (SubscriptionSchedule currentSchedule : currentSchedules) {
        for (ISchedule availableSchedule : availableSchedules) {
          if (currentSchedule.id.equals(availableSchedule.getId())) {
            updatedSchedules.add(availableSchedule);
          }
        }
      }
      subscribeContent.setSchedules(updatedSchedules);
    } else {
      if (subscribeContent != null) {
        subscribeContent.clearsSchedules();
      }
    }
  }

  /**
   * Gets the mondrian catalogs and populates a hash map with schema name as the key and list of cube names as strings.
   * 
   * @return HashMap The hashmap has schema name as keys and a list of cube names as values
   */
  public HashMap<String, ArrayList<String>> getMondrianCatalogs() {
    HashMap<String, ArrayList<String>> catalogCubeHashMap = new HashMap<String, ArrayList<String>>();

    List<MondrianCatalog> catalogs = MondrianCatalogHelper.getInstance().listCatalogs(getPentahoSession(), true);

    for (MondrianCatalog cat : catalogs) {
      ArrayList<String> cubes = new ArrayList<String>();
      catalogCubeHashMap.put(cat.getName(), cubes);
      for (MondrianCube cube : cat.getSchema().getCubes()) {
        cubes.add(cube.getName());
      }
    }
    return catalogCubeHashMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.service.MantleService#getSubscriptionState(java .lang.String)
   */
  public SubscriptionState getSubscriptionState(String actionRef) {
    SubscriptionState state = new SubscriptionState();
    state.subscriptionsEnabled = isSubscriptionContent(actionRef);
    state.availableSchedules = getAvailableSubscriptionSchedules(actionRef);
    state.appliedSchedules = getAppliedSubscriptionSchedules(actionRef);
    return state;
  }

  public ArrayList<IUserSetting> getUserSettings() {
    try {
      IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
      ArrayList<IUserSetting> settings = (ArrayList<IUserSetting>)settingsService.getUserSettings();
      return settings;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public IUserSetting getUserSetting(String settingName) throws SimpleMessageException {
    try {
      IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
      IUserSetting setting = settingsService.getUserSetting(settingName, null);
      return setting;
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    }
  }

  public void setLocaleOverride(String locale) {
    getThreadLocalRequest().getSession().setAttribute("locale_override", locale);
    if (!StringUtils.isEmpty(locale)) {
      LocaleHelper.setLocaleOverride(new Locale(locale));
    }
  }

  public void setUserSetting(String settingName, String settingValue) throws SimpleMessageException {
    try {
      IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
      settingsService.setUserSetting(settingName, settingValue);
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    }
  }

  public void setShowNavigator(boolean showNavigator) {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_NAVIGATOR, "" + showNavigator); //$NON-NLS-1$
  }

  public void setShowLocalizedFileNames(boolean showLocalizedFileNames) {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_LOCALIZED_FILENAMES, "" + showLocalizedFileNames); //$NON-NLS-1$
  }

  public void setShowHiddenFiles(boolean showHiddenFiles) {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES, "" + showHiddenFiles); //$NON-NLS-1$
  }

  public boolean repositorySupportsACLS() {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    return repository.supportsAccessControls();
  }

  public String getVersion() {
    VersionInfo versionInfo = VersionHelper.getVersionInfo(PentahoSystem.class);
    return versionInfo.getVersionNumber();
  }

  public ArrayList<MantleXulOverlay> getOverlays() {
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, getPentahoSession()); //$NON-NLS-1$

    List<XulOverlay> overlays = pluginManager.getOverlays();
    ArrayList<MantleXulOverlay> result = new ArrayList<MantleXulOverlay>();
    for (XulOverlay overlay : overlays) {

      MantleXulOverlay tempOverlay = new MantleXulOverlay(overlay.getId(), overlay.getOverlayUri(), overlay.getSource(), overlay.getResourceBundleUri());
      result.add(tempOverlay);
    }
    if (pluginManager != null) {
      return result;
    } else {
      return null;
    }
  }

}