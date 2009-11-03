package org.pentaho.platform.repository.pcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.repository.pcr.springsecurity.RepositoryFilePermission;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.MutableAclService;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../bi-platform-sample-solution/system/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" })
public class PentahoContentRepositoryTests implements ApplicationContextAware {
  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(PentahoContentRepositoryTests.class);

  private static final Authentication AUTHENTICATION_JOE;

  private static final Authentication AUTHENTICATION_SUZY;

  private static final Authentication AUTHENTICATION_TIFFANY;

  static {
    final String password = "password";
    final GrantedAuthority[] adminAuthorities = new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_Admin"),
        new GrantedAuthorityImpl("ROLE_Authenticated") };
    final GrantedAuthority[] regularAuthorities = new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_Authenticated") };

    UserDetails joe = new User("joe", password, true, true, true, true, adminAuthorities);
    UserDetails suzy = new User("suzy", password, true, true, true, true, regularAuthorities);
    UserDetails tiffany = new User("tiffany", password, true, true, true, true, regularAuthorities);

    AUTHENTICATION_JOE = new UsernamePasswordAuthenticationToken(joe, password, adminAuthorities);
    AUTHENTICATION_SUZY = new UsernamePasswordAuthenticationToken(suzy, password, regularAuthorities);
    AUTHENTICATION_TIFFANY = new UsernamePasswordAuthenticationToken(tiffany, password, regularAuthorities);
  }

  // ~ Instance fields =================================================================================================

  private IPentahoContentRepository pentahoContentRepository;

  /**
   * Used for state verification and test cleanup.
   */
  private SimpleJdbcTemplate testJdbcTemplate;

  /**
   * Used for state verification and test cleanup.
   */
  private JcrTemplate testJcrTemplate;

  private MutableAclService mutableAclService;

  private String systemUsername;

  private Sid systemUserSid;

  private Sid regularUserAuthoritySid;

  // ~ Constructors ==================================================================================================== 

  public PentahoContentRepositoryTests() throws Exception {
    super();
  }

  // ~ Methods =========================================================================================================

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    testJdbcTemplate
        .update("DROP TABLE IF EXISTS acl_entry; DROP TABLE IF EXISTS acl_object_identity; DROP TABLE IF EXISTS acl_sid; DROP TABLE IF EXISTS acl_class;");
    SecurityContextHolder.getContext().setAuthentication(createAnonymousAuthentication());
    SimpleJcrTestUtils.deleteItem(testJcrTemplate, "/pentaho");
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testNotStartedUp() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    pentahoContentRepository.createUserHomeFolderIfNecessary();
  }

  @Test
  public void testStartup() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(createAnonymousAuthentication());
    final String rootFolderPath = "/pentaho";
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, rootFolderPath));
    assertAceExists(getNodeId(rootFolderPath), systemUserSid, RepositoryFilePermission.WRITE);
    assertAceExists(getNodeId(rootFolderPath), regularUserAuthoritySid, RepositoryFilePermission.READ);
    assertOwner(getNodeId(rootFolderPath), systemUserSid);
    final String publicFolderPath = "/pentaho/public";
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, publicFolderPath));
    assertAclEmpty(getNodeId(publicFolderPath));
    assertOwner(getNodeId(publicFolderPath), systemUserSid);
    final String homeFolderPath = "/pentaho/home";
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, homeFolderPath));
    assertAclEmpty(getNodeId(homeFolderPath));
    assertOwner(getNodeId(homeFolderPath), systemUserSid);

  }

  @Test
  public void testCreateUserHomeFolderIfNecessary() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    RepositoryFile suzyHomeFolder = pentahoContentRepository.createUserHomeFolderIfNecessary();
    assertNotNull(suzyHomeFolder);
    System.out.println("\n\n\n" + suzyHomeFolder);
    final String suzyFolderPath = "/pentaho/home/suzy";
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, suzyFolderPath));
    Sid suzySid = new PrincipalSid("suzy");
    assertAceExists(getNodeId(suzyFolderPath), suzySid, RepositoryFilePermission.WRITE);
    assertOwner(getNodeId(suzyFolderPath), suzySid);
  }

  @Test(expected = AccessDeniedException.class)
  public void testGetFileAccessDenied() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    RepositoryFile tiffanyHomeFolder = pentahoContentRepository.createUserHomeFolderIfNecessary();
    pentahoContentRepository.createFolder(tiffanyHomeFolder, new RepositoryFile.Builder("test").folder(true).build());
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    // no execute access for suzy on folder called tiffany
    pentahoContentRepository.getFile("/pentaho/home/tiffany/test");
  }

  @Test
  public void testGetFileAdmin() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    RepositoryFile tiffanyHomeFolder = pentahoContentRepository.createUserHomeFolderIfNecessary();
    pentahoContentRepository.createFolder(tiffanyHomeFolder, new RepositoryFile.Builder("test").folder(true).build());
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_JOE);
    pentahoContentRepository.getFile("/pentaho/home/tiffany/test");
  }

  @Test
  public void testGetFile() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    RepositoryFile file = pentahoContentRepository.getFile("/pentaho/home");
    assertNotNull(file);
    RepositoryFile file2 = pentahoContentRepository.getFile("/pentaho/home2");
    assertNull(file2);
  }

  @Test
  public void testGetUserHomeFolder() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    pentahoContentRepository.createUserHomeFolderIfNecessary();
    RepositoryFile tiffanyHome = pentahoContentRepository.getFile("/pentaho/home/tiffany");
    assertNotNull(tiffanyHome);
  }

//  @Test
//  public void testGetStreamForExecute() throws Exception {
//    pentahoContentRepository.startup();
//    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
//    RepositoryFile file = pentahoContentRepository.getStreamForExecute("/pentaho/home");
//    assertNotNull(file);
//  }

  @Test
  public void testStartupTwice() throws Exception {
    pentahoContentRepository.startup();
    pentahoContentRepository.startup();
  }

  @Test
  public void testCreateHomeUserHomeFolderIfNecessaryTwice() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    pentahoContentRepository.createUserHomeFolderIfNecessary();
    pentahoContentRepository.createUserHomeFolderIfNecessary();
  }
  
  @Test
  public void testCreateFolder() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    pentahoContentRepository.createUserHomeFolderIfNecessary();
    RepositoryFile parentFolder = pentahoContentRepository.getFile("/pentaho/home/suzy");
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    newFolder = pentahoContentRepository.createFolder(parentFolder, newFolder);
    assertNotNull(newFolder);
    assertNotNull(newFolder.getId());
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy/test"));
  }
  
  @Test(expected = AccessDeniedException.class)
  public void testCreateFolderAccessDenied() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    RepositoryFile parentFolder = pentahoContentRepository.getFile("/pentaho");
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    pentahoContentRepository.createFolder(parentFolder, newFolder);
  }

  @Test
  public void testCreateFile() throws Exception {
    pentahoContentRepository.startup();
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    pentahoContentRepository.createUserHomeFolderIfNecessary();
    RepositoryFile parentFolder = pentahoContentRepository.getFile("/pentaho/home/suzy");
    final String expectedDataString = "Hello World!";
    final String expectedEncoding = "UTF-8";
    byte[] data = expectedDataString.getBytes(expectedEncoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String expectedMimeType = "text/plain";
    final String expectedName = "helloworld.xaction";
    final String expectedAbsolutePath = "/pentaho/home/suzy/helloworld.xaction"; 
    RepositoryFile newFile = pentahoContentRepository.createFile(parentFolder, new RepositoryFile.Builder(
        expectedName).encoding("UTF-8").mimeType(expectedMimeType).build(), dataStream);
    assertNotNull(newFile.getId());
    RepositoryFile foundFile = pentahoContentRepository.getFile(expectedAbsolutePath);
    assertNotNull(foundFile);
    assertEquals(expectedName, foundFile.getName());
    assertEquals(expectedAbsolutePath, foundFile.getAbsolutePath());
//    assertTrue(foundFile.length() > 0);
//    assertNotNull(foundFile.getData());
    assertEquals(expectedEncoding, foundFile.getEncoding());
    assertEquals(expectedMimeType, foundFile.getMimeType());
    assertNotNull(foundFile.getCreatedDate());
//    String dataString = new String(foundFile.getData(), expectedEncoding);
//    assertEquals(expectedDataString, dataString);
    assertNotNull(foundFile.getLastModifiedDate());
  }

  private void assertAceExists(final Serializable id, final Sid sid, final Permission permission) {
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, id);
    Acl acl = mutableAclService.readAclById(oid);
    assertTrue(acl.isGranted(new Permission[] { permission }, new Sid[] { sid }, true));
  }

  private void assertAclEmpty(final Serializable id) {
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, id);
    Acl acl = mutableAclService.readAclById(oid);
    assertTrue(acl.getEntries().length == 0);
  }

  private void assertOwner(final Serializable id, final Sid sid) {
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, id);
    Acl acl = mutableAclService.readAclById(oid);
    assertTrue(sid.equals(acl.getOwner()));
  }

  private Serializable getNodeId(final String absPath) throws Exception {
    return SimpleJcrTestUtils.getNodeId(testJcrTemplate, absPath);
  }

  private Authentication createAnonymousAuthentication() {
    // create "anonymous" authentication
    final GrantedAuthority[] anonymousUserAuthorities = new GrantedAuthority[1];
    anonymousUserAuthorities[0] = new GrantedAuthorityImpl("ROLE_ANONYMOUS");
    final String password = "ignored";
    UserDetails anonymousUserDetails = new User("anonymousUser", password, true, true, true, true,
        anonymousUserAuthorities);
    Authentication anonymousAuthentication = new UsernamePasswordAuthenticationToken(anonymousUserDetails, password,
        anonymousUserAuthorities);
    return anonymousAuthentication;
  }

  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    pentahoContentRepository = (IPentahoContentRepository) applicationContext.getBean("pentahoContentRepository");
    DataSource aclDataSource = (DataSource) applicationContext.getBean("aclDataSource");
    testJdbcTemplate = new SimpleJdbcTemplate(aclDataSource);
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean("jcrSessionFactory");
    testJcrTemplate = new JcrTemplate(jcrSessionFactory);
    testJcrTemplate.setAllowCreate(true);
    mutableAclService = (MutableAclService) applicationContext.getBean("aclService");
    systemUsername = (String) applicationContext.getBean("systemUsername");
    systemUserSid = new PrincipalSid(systemUsername);
    regularUserAuthoritySid = new GrantedAuthoritySid((String) applicationContext.getBean("regularUserAuthorityName"));
  }

}
