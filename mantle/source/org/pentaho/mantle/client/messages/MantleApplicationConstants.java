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
package org.pentaho.mantle.client.messages;


/**
 * Interface to represent the messages contained in resource bundle:
 * 	C:/development-2.0/mantle/source/org/pentaho/mantle/client/messages/MantleApplicationConstants.properties'.
 */
public interface MantleApplicationConstants extends com.google.gwt.i18n.client.Messages {
  
  /**
   * Translated "Could not delete {0}, check permissions.".
   * 
   * @return translated "Could not delete {0}, check permissions."
  
   */
  @DefaultMessage("Could not delete {0}, check permissions.")
  String couldNotDelete(String arg0);

  /**
   * Translated "Reporting metadata refreshed successfully".
   * 
   * @return translated "Reporting metadata refreshed successfully"
  
   */
  @DefaultMessage("Reporting metadata refreshed successfully")
  String refreshReportingMetadataSuccess();

  /**
   * Translated "Reload All Tabs".
   * 
   * @return translated "Reload All Tabs"
  
   */
  @DefaultMessage("Reload All Tabs")
  String reloadAllTabs();

  /**
   * Translated "No".
   * 
   * @return translated "No"
  
   */
  @DefaultMessage("No")
  String no();

  /**
   * Translated "File exists, overwrite?".
   * 
   * @return translated "File exists, overwrite?"
  
   */
  @DefaultMessage("File exists, overwrite?")
  String fileExistsOverwrite();

  /**
   * Translated "Untitled".
   * 
   * @return translated "Untitled"
  
   */
  @DefaultMessage("Untitled")
  String untitled();

  /**
   * Translated "Manage".
   * 
   * @return translated "Manage"
  
   */
  @DefaultMessage("Manage")
  String manage();

  /**
   * Translated "User Preferences".
   * 
   * @return translated "User Preferences"
  
   */
  @DefaultMessage("User Preferences")
  String userPreferences();

  /**
   * Translated "Enter URL:".
   * 
   * @return translated "Enter URL:"
  
   */
  @DefaultMessage("Enter URL:")
  String enterURL();

  /**
   * Translated "Could not create folder {0}, check permissions.".
   * 
   * @return translated "Could not create folder {0}, check permissions."
  
   */
  @DefaultMessage("Could not create folder {0}, check permissions.")
  String couldNotCreateFolder(String arg0);

  /**
   * Translated "Open URL...".
   * 
   * @return translated "Open URL..."
  
   */
  @DefaultMessage("Open URL...")
  String openURLEllipsis();

  /**
   * Translated "Info".
   * 
   * @return translated "Info"
  
   */
  @DefaultMessage("Info")
  String info();

  /**
   * Translated "Schema".
   * 
   * @return translated "Schema"
  
   */
  @DefaultMessage("Schema")
  String schema();

  /**
   * Translated "Could not get user settings".
   * 
   * @return translated "Could not get user settings"
  
   */
  @DefaultMessage("Could not get user settings")
  String couldNotGetUserSettings();

  /**
   * Translated "Open".
   * 
   * @return translated "Open"
  
   */
  @DefaultMessage("Open")
  String open();

  /**
   * Translated "Solution Browser refreshed".
   * 
   * @return translated "Solution Browser refreshed"
  
   */
  @DefaultMessage("Solution Browser refreshed")
  String solutionBrowserRefreshed();

  /**
   * Translated "Help".
   * 
   * @return translated "Help"
  
   */
  @DefaultMessage("Help")
  String help();

  /**
   * Translated "Reset Repository".
   * 
   * @return translated "Reset Repository"
  
   */
  @DefaultMessage("Reset Repository")
  String resetRepository();

  /**
   * Translated "Manage Content".
   * 
   * @return translated "Manage Content"
  
   */
  @DefaultMessage("Manage Content")
  String manageContent();

  /**
   * Translated "Software Update Available".
   * 
   * @return translated "Software Update Available"
  
   */
  @DefaultMessage("Software Update Available")
  String softwareUpdateAvailable();

  /**
   * Translated "File".
   * 
   * @return translated "File"
  
   */
  @DefaultMessage("File")
  String file();

  /**
   * Translated "Report".
   * 
   * @return translated "Report"
  
   */
  @DefaultMessage("Report")
  String report();

  /**
   * Translated "Advanced".
   * 
   * @return translated "Advanced"
  
   */
  @DefaultMessage("Advanced")
  String advanced();

  /**
   * Translated "{0} (Role)back=Back".
   * 
   * @return translated "{0} (Role)back=Back"
  
   */
  @DefaultMessage("{0} (Role)back=Back")
  String role(String arg0);

  /**
   * Translated "Repository cache refreshed successfully".
   * 
   * @return translated "Repository cache refreshed successfully"
  
   */
  @DefaultMessage("Repository cache refreshed successfully")
  String refreshRepositorySuccess();

  /**
   * Translated "Favorite Groups".
   * 
   * @return translated "Favorite Groups"
  
   */
  @DefaultMessage("Favorite Groups")
  String favoriteGroups();

  /**
   * Translated "OK".
   * 
   * @return translated "OK"
  
   */
  @DefaultMessage("OK")
  String ok();

  /**
   * Translated "You do not have permission to schedule to this action sequence.".
   * 
   * @return translated "You do not have permission to schedule to this action sequence."
  
   */
  @DefaultMessage("You do not have permission to schedule to this action sequence.")
  String noSchedulePermission();

  /**
   * Translated "Documentation...".
   * 
   * @return translated "Documentation..."
  
   */
  @DefaultMessage("Documentation...")
  String documentationEllipsis();

  /**
   * Translated "Global Value".
   * 
   * @return translated "Global Value"
  
   */
  @DefaultMessage("Global Value")
  String globalValue();

  /**
   * Translated "Refresh".
   * 
   * @return translated "Refresh"
  
   */
  @DefaultMessage("Refresh")
  String refresh();

  /**
   * Translated "Close All Tabs".
   * 
   * @return translated "Close All Tabs"
  
   */
  @DefaultMessage("Close All Tabs")
  String closeAllTabs();

  /**
   * Translated "System Settings".
   * 
   * @return translated "System Settings"
  
   */
  @DefaultMessage("System Settings")
  String refreshSystemSettings();

  /**
   * Translated "Do you want to delete this content item?".
   * 
   * @return translated "Do you want to delete this content item?"
  
   */
  @DefaultMessage("Do you want to delete this content item?")
  String deleteContentItem();

  /**
   * Translated "Os".
   * 
   * @return translated "Os"
  
   */
  @DefaultMessage("Os")
  String os();

  /**
   * Translated "Ad hoc Reporting".
   * 
   * @return translated "Ad hoc Reporting"
  
   */
  @DefaultMessage("Ad hoc Reporting")
  String waqr();

  /**
   * Translated "Mondrian Schema Cache Flushed Successfully".
   * 
   * @return translated "Mondrian Schema Cache Flushed Successfully"
  
   */
  @DefaultMessage("Mondrian Schema Cache Flushed Successfully")
  String mondrianSchemaCacheFlushedSuccessfully();

  /**
   * Translated "URL not specified".
   * 
   * @return translated "URL not specified"
  
   */
  @DefaultMessage("URL not specified")
  String urlNotSpecified();

  /**
   * Translated "Open Tab in New Window".
   * 
   * @return translated "Open Tab in New Window"
  
   */
  @DefaultMessage("Open Tab in New Window")
  String openTabInNewWindow();

  /**
   * Translated "You must select a schema".
   * 
   * @return translated "You must select a schema"
  
   */
  @DefaultMessage("You must select a schema")
  String selectSchema();

  /**
   * Translated "Choose color...".
   * 
   * @return translated "Choose color..."
  
   */
  @DefaultMessage("Choose color...")
  String chooseColor();

  /**
   * Translated "Reporting metadata refresh failed".
   * 
   * @return translated "Reporting metadata refresh failed"
  
   */
  @DefaultMessage("Reporting metadata refresh failed")
  String refreshReportingMetadataFailed();

  /**
   * Translated "Waiting".
   * 
   * @return translated "Waiting"
  
   */
  @DefaultMessage("Waiting")
  String waiting();

  /**
   * Translated "Create Deep Link".
   * 
   * @return translated "Create Deep Link"
  
   */
  @DefaultMessage("Create Deep Link")
  String createDeepLink();

  /**
   * Translated "Switch to Classic View".
   * 
   * @return translated "Switch to Classic View"
  
   */
  @DefaultMessage("Switch to Classic View")
  String showClassicView();

  /**
   * Translated "XAction".
   * 
   * @return translated "XAction"
  
   */
  @DefaultMessage("XAction")
  String xaction();

  /**
   * Translated "Styles".
   * 
   * @return translated "Styles"
  
   */
  @DefaultMessage("Styles")
  String styles();

  /**
   * Translated "Navigating away from this page may terminate your session.".
   * 
   * @return translated "Navigating away from this page may terminate your session."
  
   */
  @DefaultMessage("Navigating away from this page may terminate your session.")
  String windowCloseWarning();

  /**
   * Translated "Available:".
   * 
   * @return translated "Available:"
  
   */
  @DefaultMessage("Available:")
  String available();

  /**
   * Translated "Show Localized File Names".
   * 
   * @return translated "Show Localized File Names"
  
   */
  @DefaultMessage("Show Localized File Names")
  String showLocalizedFileNames();

  /**
   * Translated "Open In New Window".
   * 
   * @return translated "Open In New Window"
  
   */
  @DefaultMessage("Open In New Window")
  String openInNewWindow();

  /**
   * Translated "Create".
   * 
   * @return translated "Create"
  
   */
  @DefaultMessage("Create")
  String create();

  /**
   * Translated "Clean Content Repository".
   * 
   * @return translated "Clean Content Repository"
  
   */
  @DefaultMessage("Clean Content Repository")
  String cleanContentRepository();

  /**
   * Translated "Preview".
   * 
   * @return translated "Preview"
  
   */
  @DefaultMessage("Preview")
  String preview();

  /**
   * Translated "Below lists collections of logically grouped Pentaho folders and documents called Solutions. By default, the "Sample" Solution is installed and contains working examples demonstrating the functionality of the platform. Once a Solution is chosen, use the file browser to locate and launch a document. To learn more about how to modify the examples or build a Solution, read the <A target="_blank" HREF="http://wiki.pentaho.org/display/PentahoDoc/Creating+Pentaho+Solutions">'Creating Pentaho Solutions'</A> document available at wiki.pentaho.org.<BR><BR>The 'Classic View' is provided for nostalgic purposes only and is not supported by Pentaho.".
   * 
   * @return translated "Below lists collections of logically grouped Pentaho folders and documents called Solutions. By default, the "Sample" Solution is installed and contains working examples demonstrating the functionality of the platform. Once a Solution is chosen, use the file browser to locate and launch a document. To learn more about how to modify the examples or build a Solution, read the <A target="_blank" HREF="http://wiki.pentaho.org/display/PentahoDoc/Creating+Pentaho+Solutions">'Creating Pentaho Solutions'</A> document available at wiki.pentaho.org.<BR><BR>The 'Classic View' is provided for nostalgic purposes only and is not supported by Pentaho."
  
   */
  @DefaultMessage("Below lists collections of logically grouped Pentaho folders and documents called Solutions. By default, the \"Sample\" Solution is installed and contains working examples demonstrating the functionality of the platform. Once a Solution is chosen, use the file browser to locate and launch a document. To learn more about how to modify the examples or build a Solution, read the <A target=\"_blank\" HREF=\"http://wiki.pentaho.org/display/PentahoDoc/Creating+Pentaho+Solutions\">'Creating Pentaho Solutions'</A> document available at wiki.pentaho.org.<BR><BR>The 'Classic View' is provided for nostalgic purposes only and is not supported by Pentaho.")
  String classicSolutionBrowserDescription();

  /**
   * Translated "Grant Permissions".
   * 
   * @return translated "Grant Permissions"
  
   */
  @DefaultMessage("Grant Permissions")
  String grantPermissions();

  /**
   * Translated "Schedule/Date".
   * 
   * @return translated "Schedule/Date"
  
   */
  @DefaultMessage("Schedule/Date")
  String scheduleDate();

  /**
   * Translated "New".
   * 
   * @return translated "New"
  
   */
  @DefaultMessage("New")
  String _new();

  /**
   * Translated "Cube".
   * 
   * @return translated "Cube"
  
   */
  @DefaultMessage("Cube")
  String cube();

  /**
   * Translated "View".
   * 
   * @return translated "View"
  
   */
  @DefaultMessage("View")
  String view();

  /**
   * Translated "Open...".
   * 
   * @return translated "Open..."
  
   */
  @DefaultMessage("Open...")
  String openEllipsis();

  /**
   * Translated "Revert to Default Setting".
   * 
   * @return translated "Revert to Default Setting"
  
   */
  @DefaultMessage("Revert to Default Setting")
  String revertToDefault();

  /**
   * Translated "Use Public Schedules".
   * 
   * @return translated "Use Public Schedules"
  
   */
  @DefaultMessage("Use Public Schedules")
  String enableSubscription();

  /**
   * Translated "All Permissions".
   * 
   * @return translated "All Permissions"
  
   */
  @DefaultMessage("All Permissions")
  String allPermissions();

  /**
   * Translated "Group Name".
   * 
   * @return translated "Group Name"
  
   */
  @DefaultMessage("Group Name")
  String groupName();

  /**
   * Translated "Public Schedules".
   * 
   * @return translated "Public Schedules"
  
   */
  @DefaultMessage("Public Schedules")
  String publicSchedules();

  /**
   * Translated "You must select a cube".
   * 
   * @return translated "You must select a cube"
  
   */
  @DefaultMessage("You must select a cube")
  String selectCube();

  /**
   * Translated "Remove".
   * 
   * @return translated "Remove"
  
   */
  @DefaultMessage("Remove")
  String remove();

  /**
   * Translated "Could not get repository document".
   * 
   * @return translated "Could not get repository document"
  
   */
  @DefaultMessage("Could not get repository document")
  String couldNotGetRepositoryDocument();

  /**
   * Translated "Link".
   * 
   * @return translated "Link"
  
   */
  @DefaultMessage("Link")
  String link();

  /**
   * Translated "Purge Mondrian Data Cache".
   * 
   * @return translated "Purge Mondrian Data Cache"
  
   */
  @DefaultMessage("Purge Mondrian Data Cache")
  String purgeMondrianDataCache();

  /**
   * Translated "Users and Roles:".
   * 
   * @return translated "Users and Roles:"
  
   */
  @DefaultMessage("Users and Roles:")
  String usersAndRoles();

  /**
   * Translated "Run Now".
   * 
   * @return translated "Run Now"
  
   */
  @DefaultMessage("Run Now")
  String run();

  /**
   * Translated "Global Variables".
   * 
   * @return translated "Global Variables"
  
   */
  @DefaultMessage("Global Variables")
  String executeGlobalActions();

  /**
   * Translated "An analysis view, {0}, is already open.<BR>Select OK to close this view and continue?".
   * 
   * @return translated "An analysis view, {0}, is already open.<BR>Select OK to close this view and continue?"
  
   */
  @DefaultMessage("An analysis view, {0}, is already open.<BR>Select OK to close this view and continue?")
  String analysisViewIsOpen(String arg0);

  /**
   * Translated "Version".
   * 
   * @return translated "Version"
  
   */
  @DefaultMessage("Version")
  String version();

  /**
   * Translated "Reporting Metadata".
   * 
   * @return translated "Reporting Metadata"
  
   */
  @DefaultMessage("Reporting Metadata")
  String refreshReportingMetadata();

  /**
   * Translated "Mondrian Data Cache Flushed Successfully".
   * 
   * @return translated "Mondrian Data Cache Flushed Successfully"
  
   */
  @DefaultMessage("Mondrian Data Cache Flushed Successfully")
  String mondrianDataCacheFlushedSuccessfully();

  /**
   * Translated "Loading...".
   * 
   * @return translated "Loading..."
  
   */
  @DefaultMessage("Loading...")
  String loadingEllipsis();

  /**
   * Translated "Hide Hidden Files".
   * 
   * @return translated "Hide Hidden Files"
  
   */
  @DefaultMessage("Hide Hidden Files")
  String hideHiddenFiles();

  /**
   * Translated "Revert".
   * 
   * @return translated "Revert"
  
   */
  @DefaultMessage("Revert")
  String revert();

  /**
   * Translated "Add...".
   * 
   * @return translated "Add..."
  
   */
  @DefaultMessage("Add...")
  String addPeriods();

  /**
   * Translated "Current:".
   * 
   * @return translated "Current:"
  
   */
  @DefaultMessage("Current:")
  String current();

  /**
   * Translated "Delete".
   * 
   * @return translated "Delete"
  
   */
  @DefaultMessage("Delete")
  String delete();

  /**
   * Translated "Show Hidden Files".
   * 
   * @return translated "Show Hidden Files"
  
   */
  @DefaultMessage("Show Hidden Files")
  String showHiddenFiles();

  /**
   * Translated "Revert to Global Setting".
   * 
   * @return translated "Revert to Global Setting"
  
   */
  @DefaultMessage("Revert to Global Setting")
  String revertToGlobal();

  /**
   * Translated "New Folder".
   * 
   * @return translated "New Folder"
  
   */
  @DefaultMessage("New Folder")
  String newFolder();

  /**
   * Translated "Bookmark Tab".
   * 
   * @return translated "Bookmark Tab"
  
   */
  @DefaultMessage("Bookmark Tab")
  String bookmarkTab();

  /**
   * Translated "Updates...".
   * 
   * @return translated "Updates..."
  
   */
  @DefaultMessage("Updates...")
  String softwareUpdates();

  /**
   * Translated "Could not retrieve mondrian schema and cube info".
   * 
   * @return translated "Could not retrieve mondrian schema and cube info"
  
   */
  @DefaultMessage("Could not retrieve mondrian schema and cube info")
  String noMondrianSchemas();

  /**
   * Translated "Options".
   * 
   * @return translated "Options"
  
   */
  @DefaultMessage("Options")
  String options();

  /**
   * Translated "Remove All".
   * 
   * @return translated "Remove All"
  
   */
  @DefaultMessage("Remove All")
  String removeAll();

  /**
   * Translated "Could not load bookmarks".
   * 
   * @return translated "Could not load bookmarks"
  
   */
  @DefaultMessage("Could not load bookmarks")
  String couldNotLoadBookmarks();

  /**
   * Translated "Files".
   * 
   * @return translated "Files"
  
   */
  @DefaultMessage("Files")
  String files();

  /**
   * Translated "Last Modified".
   * 
   * @return translated "Last Modified"
  
   */
  @DefaultMessage("Last Modified")
  String lastModified();

  /**
   * Translated "Close Other Tabs".
   * 
   * @return translated "Close Other Tabs"
  
   */
  @DefaultMessage("Close Other Tabs")
  String closeOtherTabs();

  /**
   * Translated "Repository Cache".
   * 
   * @return translated "Repository Cache"
  
   */
  @DefaultMessage("Repository Cache")
  String refreshRepository();

  /**
   * Translated "Back".
   * 
   * @return translated "Back"
  
   */
  @DefaultMessage("Back")
  String back();

  /**
   * Translated "User Preferences...".
   * 
   * @return translated "User Preferences..."
  
   */
  @DefaultMessage("User Preferences...")
  String userPreferencesEllipsis();

  /**
   * Translated "Operation in progress...".
   * 
   * @return translated "Operation in progress..."
  
   */
  @DefaultMessage("Operation in progress...")
  String waitMessage();

  /**
   * Translated "Folder".
   * 
   * @return translated "Folder"
  
   */
  @DefaultMessage("Folder")
  String folder();

  /**
   * Translated "Yes".
   * 
   * @return translated "Yes"
  
   */
  @DefaultMessage("Yes")
  String yes();

  /**
   * Translated "Logout".
   * 
   * @return translated "Logout"
  
   */
  @DefaultMessage("Logout")
  String logout();

  /**
   * Translated "Admin".
   * 
   * @return translated "Admin"
  
   */
  @DefaultMessage("Admin")
  String admin();

  /**
   * Translated "Permissions:".
   * 
   * @return translated "Permissions:"
  
   */
  @DefaultMessage("Permissions:")
  String permissionsColon();

  /**
   * Translated "Show Actual File Names".
   * 
   * @return translated "Show Actual File Names"
  
   */
  @DefaultMessage("Show Actual File Names")
  String showActualFileNames();

  /**
   * Translated "Documentation".
   * 
   * @return translated "Documentation"
  
   */
  @DefaultMessage("Documentation")
  String documentation();

  /**
   * Translated "Switch to Explorer View".
   * 
   * @return translated "Switch to Explorer View"
  
   */
  @DefaultMessage("Switch to Explorer View")
  String showExplorerView();

  /**
   * Translated "Close Tab".
   * 
   * @return translated "Close Tab"
  
   */
  @DefaultMessage("Close Tab")
  String closeTab();

  /**
   * Translated "Run and Archive".
   * 
   * @return translated "Run and Archive"
  
   */
  @DefaultMessage("Run and Archive")
  String archive();

  /**
   * Translated "No updates are available.".
   * 
   * @return translated "No updates are available."
  
   */
  @DefaultMessage("No updates are available.")
  String noUpdatesAvailable();

  /**
   * Translated "Actions".
   * 
   * @return translated "Actions"
  
   */
  @DefaultMessage("Actions")
  String actions();

  /**
   * Translated "My Desktop".
   * 
   * @return translated "My Desktop"
  
   */
  @DefaultMessage("My Desktop")
  String myDesktop();

  /**
   * Translated "Apply".
   * 
   * @return translated "Apply"
  
   */
  @DefaultMessage("Apply")
  String apply();

  /**
   * Translated "Effective Value".
   * 
   * @return translated "Effective Value"
  
   */
  @DefaultMessage("Effective Value")
  String effectiveValue();

  /**
   * Translated "Save".
   * 
   * @return translated "Save"
  
   */
  @DefaultMessage("Save")
  String save();

  /**
   * Translated "Location".
   * 
   * @return translated "Location"
  
   */
  @DefaultMessage("Location")
  String location();

  /**
   * Translated "Cancel".
   * 
   * @return translated "Cancel"
  
   */
  @DefaultMessage("Cancel")
  String cancel();

  /**
   * Translated "Download".
   * 
   * @return translated "Download"
  
   */
  @DefaultMessage("Download")
  String download();

  /**
   * Translated "New Analysis View".
   * 
   * @return translated "New Analysis View"
  
   */
  @DefaultMessage("New Analysis View")
  String newAnalysisView();

  /**
   * Translated "Mondrian Schema Cache".
   * 
   * @return translated "Mondrian Schema Cache"
  
   */
  @DefaultMessage("Mondrian Schema Cache")
  String purgeMondrianSchemaCache();

  /**
   * Translated "Type".
   * 
   * @return translated "Type"
  
   */
  @DefaultMessage("Type")
  String type();

  /**
   * Translated "My Schedules".
   * 
   * @return translated "My Schedules"
  
   */
  @DefaultMessage("My Schedules")
  String mySchedules();

  /**
   * Translated "Close".
   * 
   * @return translated "Close"
  
   */
  @DefaultMessage("Close")
  String close();

  /**
   * Translated "Please wait.".
   * 
   * @return translated "Please wait."
  
   */
  @DefaultMessage("Please wait.")
  String pleaseWait();

  /**
   * Translated "Create New Folder...".
   * 
   * @return translated "Create New Folder..."
  
   */
  @DefaultMessage("Create New Folder...")
  String createNewFolderEllipsis();

  /**
   * Translated "Tools".
   * 
   * @return translated "Tools"
  
   */
  @DefaultMessage("Tools")
  String tools();

  /**
   * Translated "Browse".
   * 
   * @return translated "Browse"
  
   */
  @DefaultMessage("Browse")
  String browse();

  /**
   * Translated "Default Value".
   * 
   * @return translated "Default Value"
  
   */
  @DefaultMessage("Default Value")
  String defaultValue();

  /**
   * Translated "Content repository cleaned successfully.".
   * 
   * @return translated "Content repository cleaned successfully."
  
   */
  @DefaultMessage("Content repository cleaned successfully.")
  String cleanContentRepositorySuccess();

  /**
   * Translated "Welcome".
   * 
   * @return translated "Welcome"
  
   */
  @DefaultMessage("Welcome")
  String welcome();

  /**
   * Translated "Global actions executed successfully.".
   * 
   * @return translated "Global actions executed successfully."
  
   */
  @DefaultMessage("Global actions executed successfully.")
  String globalActionsExecutedSuccessfully();

  /**
   * Translated "System settings refreshed successfully".
   * 
   * @return translated "System settings refreshed successfully"
  
   */
  @DefaultMessage("System settings refreshed successfully")
  String refreshSystemSettingsSuccess();

  /**
   * Translated "Run In Background".
   * 
   * @return translated "Run In Background"
  
   */
  @DefaultMessage("Run In Background")
  String runInBackground();

  /**
   * Translated "Browser".
   * 
   * @return translated "Browser"
  
   */
  @DefaultMessage("Browser")
  String showSolutionBrowser();

  /**
   * Translated "Could not create schedule".
   * 
   * @return translated "Could not create schedule"
  
   */
  @DefaultMessage("Could not create schedule")
  String couldNotCreateSchedule();

  /**
   * Translated "Do you want to delete {0}?".
   * 
   * @return translated "Do you want to delete {0}?"
  
   */
  @DefaultMessage("Do you want to delete {0}?")
  String deleteQuestion(String arg0);

  /**
   * Translated "Execute".
   * 
   * @return translated "Execute"
  
   */
  @DefaultMessage("Execute")
  String execute();

  /**
   * Translated "Permissions for {0}:".
   * 
   * @return translated "Permissions for {0}:"
  
   */
  @DefaultMessage("Permissions for {0}:")
  String permissionsFor(String arg0);

  /**
   * Translated "Add All".
   * 
   * @return translated "Add All"
  
   */
  @DefaultMessage("Add All")
  String addAll();

  /**
   * Translated "Deep Link".
   * 
   * @return translated "Deep Link"
  
   */
  @DefaultMessage("Deep Link")
  String deepLink();

  /**
   * Translated "Edit Action".
   * 
   * @return translated "Edit Action"
  
   */
  @DefaultMessage("Edit Action")
  String editAction();

  /**
   * Translated "Save As...".
   * 
   * @return translated "Save As..."
  
   */
  @DefaultMessage("Save As...")
  String saveAsEllipsis();

  /**
   * Translated "New Report...".
   * 
   * @return translated "New Report..."
  
   */
  @DefaultMessage("New Report...")
  String newAdhocReportEllipsis();

  /**
   * Translated "Favorites".
   * 
   * @return translated "Favorites"
  
   */
  @DefaultMessage("Favorites")
  String favorites();

  /**
   * Translated "Properties".
   * 
   * @return translated "Properties"
  
   */
  @DefaultMessage("Properties")
  String properties();

  /**
   * Translated "Size".
   * 
   * @return translated "Size"
  
   */
  @DefaultMessage("Size")
  String size();

  /**
   * Translated "Analysis View".
   * 
   * @return translated "Analysis View"
  
   */
  @DefaultMessage("Analysis View")
  String analysisView();

  /**
   * Translated "Preferences were not set successfully".
   * 
   * @return translated "Preferences were not set successfully"
  
   */
  @DefaultMessage("Preferences were not set successfully")
  String preferencesSetFailed();

  /**
   * Translated "Could not get file properties".
   * 
   * @return translated "Could not get file properties"
  
   */
  @DefaultMessage("Could not get file properties")
  String couldNotGetFileProperties();

  /**
   * Translated "All Schedules (Admin Only)".
   * 
   * @return translated "All Schedules (Admin Only)"
  
   */
  @DefaultMessage("All Schedules (Admin Only)")
  String allSchedulesAdminOnly();

  /**
   * Translated "Reload Tab".
   * 
   * @return translated "Reload Tab"
  
   */
  @DefaultMessage("Reload Tab")
  String reloadTab();

  /**
   * Translated "Update".
   * 
   * @return translated "Update"
  
   */
  @DefaultMessage("Update")
  String update();

  /**
   * Translated "Confirm Delete".
   * 
   * @return translated "Confirm Delete"
  
   */
  @DefaultMessage("Confirm Delete")
  String deleteConfirm();

  /**
   * Translated "New Folder Description".
   * 
   * @return translated "New Folder Description"
  
   */
  @DefaultMessage("New Folder Description")
  String newFolderDesc();

  /**
   * Translated "Workspace".
   * 
   * @return translated "Workspace"
  
   */
  @DefaultMessage("Workspace")
  String workspace();

  /**
   * Translated "About".
   * 
   * @return translated "About"
  
   */
  @DefaultMessage("About")
  String about();

  /**
   * Translated "Do you want to delete this public schedule?".
   * 
   * @return translated "Do you want to delete this public schedule?"
  
   */
  @DefaultMessage("Do you want to delete this public schedule?")
  String deletePublicSchedule();

  /**
   * Translated "Add".
   * 
   * @return translated "Add"
  
   */
  @DefaultMessage("Add")
  String add();

  /**
   * Translated "Browser".
   * 
   * @return translated "Browser"
  
   */
  @DefaultMessage("Browser")
  String solutionBrowser();

  /**
   * Translated "Save As".
   * 
   * @return translated "Save As"
  
   */
  @DefaultMessage("Save As")
  String saveAs();

  /**
   * Translated "Share".
   * 
   * @return translated "Share"
  
   */
  @DefaultMessage("Share")
  String share();

  /**
   * Translated "Preferences set successfully.<BR>You must restart the application for the settings to take effect.".
   * 
   * @return translated "Preferences set successfully.<BR>You must restart the application for the settings to take effect."
  
   */
  @DefaultMessage("Preferences set successfully.<BR>You must restart the application for the settings to take effect.")
  String preferencesSetSuccess();

  /**
   * Translated "Repository".
   * 
   * @return translated "Repository"
  
   */
  @DefaultMessage("Repository")
  String repository();

  /**
   * Translated "Complete".
   * 
   * @return translated "Complete"
  
   */
  @DefaultMessage("Complete")
  String complete();

  /**
   * Translated "Select the Manage function to perform".
   * 
   * @return translated "Select the Manage function to perform"
  
   */
  @DefaultMessage("Select the Manage function to perform")
  String manageContentSelectFunction();

  /**
   * Translated "Toggle Browser".
   * 
   * @return translated "Toggle Browser"
  
   */
  @DefaultMessage("Toggle Browser")
  String toggleSolutionBrowser();

  /**
   * Translated "Name".
   * 
   * @return translated "Name"
  
   */
  @DefaultMessage("Name")
  String name();

  /**
   * Translated "Quesion".
   * 
   * @return translated "Quesion"
  
   */
  @DefaultMessage("Quesion")
  String question();

  /**
   * Translated "New Report".
   * 
   * @return translated "New Report"
  
   */
  @DefaultMessage("New Report")
  String newAdhocReport();

  /**
   * Translated "This feature is disabled".
   * 
   * @return translated "This feature is disabled"
  
   */
  @DefaultMessage("This feature is disabled")
  String featureDisabled();

  /**
   * Translated "Error".
   * 
   * @return translated "Error"
  
   */
  @DefaultMessage("Error")
  String error();

  /**
   * Translated "Error: The file {0} does not exist. Check the spelling and try again or browse to the file and select it directly.".
   * 
   * @return translated "Error: The file {0} does not exist. Check the spelling and try again or browse to the file and select it directly."
  
   */
  @DefaultMessage("Error: The file {0} does not exist. Check the spelling and try again or browse to the file and select it directly.")
  String fileDoesNotExist(String arg0);

  /**
   * Translated "New Analysis View...".
   * 
   * @return translated "New Analysis View..."
  
   */
  @DefaultMessage("New Analysis View...")
  String newAnalysisViewEllipsis();

  /**
   * Translated "Schedule...".
   * 
   * @return translated "Schedule..."
  
   */
  @DefaultMessage("Schedule...")
  String scheduleEllipsis();

  /**
   * Translated "Edit".
   * 
   * @return translated "Edit"
  
   */
  @DefaultMessage("Edit")
  String edit();

  /**
   * Translated "Pentaho.com...".
   * 
   * @return translated "Pentaho.com..."
  
   */
  @DefaultMessage("Pentaho.com...")
  String pentahoHomePageName();

  /**
   * Translated "Repository cache refresh failed".
   * 
   * @return translated "Repository cache refresh failed"
  
   */
  @DefaultMessage("Repository cache refresh failed")
  String refreshRepositoryFailed();

  /**
   * Translated "General".
   * 
   * @return translated "General"
  
   */
  @DefaultMessage("General")
  String general();

  /**
   * Translated "Print...".
   * 
   * @return translated "Print..."
  
   */
  @DefaultMessage("Print...")
  String print();

  /**
   * Translated "Schedule".
   * 
   * @return translated "Schedule"
  
   */
  @DefaultMessage("Schedule")
  String schedule();

  /**
   * Translated "Source".
   * 
   * @return translated "Source"
  
   */
  @DefaultMessage("Source")
  String source();

  /**
   * Translated "New Folder Name".
   * 
   * @return translated "New Folder Name"
  
   */
  @DefaultMessage("New Folder Name")
  String newFolderName();

  /**
   * Translated "New Schedule".
   * 
   * @return translated "New Schedule"
  
   */
  @DefaultMessage("New Schedule")
  String newSchedule();

  /**
   * Translated "Manage Groups".
   * 
   * @return translated "Manage Groups"
  
   */
  @DefaultMessage("Manage Groups")
  String manageGroups();

  /**
   * Translated "Report scheduled. Note: Only last scheduled report run will be displayed from the Workspace.<BR><BR> To see subsequent scheduled report runs, please use public schedules.".
   * 
   * @return translated "Report scheduled. Note: Only last scheduled report run will be displayed from the Workspace.<BR><BR> To see subsequent scheduled report runs, please use public schedules."
  
   */
  @DefaultMessage("Report scheduled. Note: Only last scheduled report run will be displayed from the Workspace.<BR><BR> To see subsequent scheduled report runs, please use public schedules.")
  String actionSequenceScheduledSuccess();

  /**
   * Translated "{0} (User)".
   * 
   * @return translated "{0} (User)"
  
   */
  @DefaultMessage("{0} (User)")
  String user(String arg0);

  /**
   * Translated "Pentaho User Console".
   * 
   * @return translated "Pentaho User Console"
  
   */
  @DefaultMessage("Pentaho User Console")
  String productName();
}
