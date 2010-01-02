package org.pentaho.platform.api.repository;

import java.util.EnumSet;
import java.util.List;

/**
 * Entry point into the content repository.
 * 
 * <p>
 * With the exception of the {@code create} methods, all {@code RepositoryFile} instances should be retrieved 
 * from this service and not created outside of this service. For example, to get a stream for read, use
 * <pre>{@code
 * InputStream stream = getContentForRead(getFile("/pentaho/acme/public/myfile.xml"));
 * }</pre>
 * </p>
 * 
 * @author mlowery
 */
public interface IRepositoryService {

  /**
   * Gets file. Use this method to test for file existence too.
   * 
   * @param absPath absolute path to file
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final String absPath);

  /**
   * Gets content for read.
   * 
   * @param file to read
   * @param contentClass class that implements {@link IRepositoryFileData}
   * @return content
   */
  <T extends IRepositoryFileData> T getContentForRead(final RepositoryFile file, final Class<T> contentClass);

  /**
   * Gets content for execute.
   * 
   * @param file to execute
   * @param contentClass class that implements {@link IRepositoryFileData}
   * @return content
   */
  <T extends IRepositoryFileData> T getContentForExecute(final RepositoryFile file, final Class<T> contentClass);

  /**
   * Creates a file.
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @param content file content
   * @param versionMessageAndLabel optional version comment [0] and label [1] to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileData content, final String... versionMessageAndLabel);

  /**
   * Creates a folder.
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @param versionMessageAndLabel optional version comment [0] and label [1] to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final String... versionMessageAndLabel);

  /**
   * Returns the children of this folder.
   * 
   * @param folder folder whose children to fetch
   * @return list of children (never {@code null})
   */
  List<RepositoryFile> getChildren(final RepositoryFile folder);

  /**
   * Updates a file and/or the content of a file.
   * 
   * @param file updated file (not a folder)
   * @param content updated content
   * @param versionMessageAndLabel optional version comment [0] and label [1]
   * @return updated file (possible with new version number)
   */
  RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData content,
      final String... versionMessageAndLabel);

  /**
   * Deletes a file or folder.
   * 
   * @param file file to delete
   * @param versionMessageAndLabel optional version comment [0] and label [1]
   */
  void deleteFile(final RepositoryFile file, final String... versionMessageAndLabel);

  // ~ Lock methods ====================================================================================================

  /**
   * Locks a file.
   * 
   * @param file file to lock
   * @param lock message
   */
  void lockFile(final RepositoryFile file, final String message);

  /**
   * Unlocks a file.
   * 
   * @param file file to unlock
   */
  void unlockFile(final RepositoryFile file);

  // ~ Access read/write methods =======================================================================================

  /**
   * Returns ACL for file.
   * 
   * @param file file whose ACL to get
   * @return access control list
   */
  RepositoryFileAcl getAcl(final RepositoryFile file);
  
  /**
   * Sets an ACL.
   * 
   * @param acl ACL to set
   */
  void setAcl(final RepositoryFileAcl acl);
  
  /**
   * Returns {@code true} if user has all permissions given.
   * 
   * @param absPath absolute path to file or folder
   * @param permissions permissions to check
   * @return {@code true} if user has all permissions given
   */
  boolean hasAccess(final String absPath, final EnumSet<RepositoryFilePermission> permissions);
  
  /**
   * Returns the list of access control entries that will be used to make an access control decision.
   * 
   * @param file file whose effective ACEs to get
   * @return list of ACEs
   */
  List<RepositoryFileAcl.Ace> getEffectiveAces(final RepositoryFile file);

  // ~ Version methods =================================================================================================

  /**
   * Returns a list of version summary instances. The first version in the list is the root version. The last version
   * in the list is the base version. Branching and merging are not supported so this is a simple list.
   * 
   * @param file file whose versions to get
   * @return list of version summaries (never {@code null})
   */
  List<VersionSummary> getVersionSummaries(final RepositoryFile file);
  
  /**
   * Gets file as it was at the given version. Use this method to test for file existence too.
   * 
   * @param versionSummary version of file to retrieve
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final VersionSummary versionSummary);

  /**
   * Returns the associated {@link IRepositoryEventHandler}.
   * @return repository event handler
   */
  IRepositoryEventHandler getRepositoryEventHandler();

  /**
   * Handles various events like startup and new user. 
   * 
   * <p>
   * Methods in this class are not called by the {@link IRepositoryService} implementation; they must be called 
   * by an external caller. A caller can get a reference to the {@link IRepositoryEventHandler} by calling 
   * {@link IRepositoryService#getRepositoryEventHandler()}. Methods should be able to be called more than once 
   * with the same arguments with no adverse effects.
   * </p>
   * 
   * <p>
   * Example: When a servlet-based application starts up, a {@code ServletContextListener} calls {@link #onStartup()}. 
   * When a user logs in, {@link #onNewTenant(String)} and {@link #onNewUser(String)} are called. Finally, the 
   * {@code ServletContextListener} calls {@link #onShutdown()}.
   * </p>
   */
  interface IRepositoryEventHandler {

    /**
     * To be called before any users call into the {@link IRepositoryService}.
     */
    void onStartup();

    /**
     * To be called on repository shutdown.
     */
    void onShutdown();

    /**
     * To be called before any users belonging to a particular tenant call into the {@link IRepositoryService}.
     * @param tenantId new tenant id
     */
    void onNewTenant(final String tenantId);
    
    /**
     * To be called before any users belonging to the current tenant call into the {@link IRepositoryService}. 
     */
    void onNewTenant();

    /**
     * To be called before user indicated by {@code username} calls into the {@link IRepositoryService}.
     * @param tenantId tenant to which the user belongs
     * @param username new username
     */
    void onNewUser(final String tenantId, final String username);

    /**
     * To be called before current user calls into the {@link IRepositoryService}.
     */
    void onNewUser();
  }
}
