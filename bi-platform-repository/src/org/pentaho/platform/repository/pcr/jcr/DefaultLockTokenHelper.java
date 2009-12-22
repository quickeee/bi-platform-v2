package org.pentaho.platform.repository.pcr.jcr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository.pcr.jcr.JcrRepositoryFileDao.ILockTokenHelper;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link ILockTokenHelper}. If user {@code suzy} in tenant {@code acme} locks a file with 
 * UUID {@code abc} then this implementation will store the lock token {@code xyz} as 
 * {@code /pentaho/acme/home/suzy/.lockTokens/abc/xyz}.
 * 
 * @author mlowery
 */
public class DefaultLockTokenHelper implements ILockTokenHelper {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultLockTokenHelper.class);

  private final String PATTERN_USER_HOME_FOLDER_PATH = "/pentaho/{0}/home/{1}"; //$NON-NLS-1$

  private final String FOLDER_NAME_LOCK_TOKENS = ".lockTokens"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public DefaultLockTokenHelper() {
    super();
  }

  // ~ Methods =========================================================================================================

  public void addLockToken(final Session session, final NodeIdStrategy nodeIdStrategy, final Lock lock)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, nodeIdStrategy);
    JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, nodeIdStrategy, lockTokensNode);
    Node newLockTokenNode = lockTokensNode.addNode(lock.getNode().getUUID(), JcrRepositoryFileUtils.addPentahoPrefix(
        session, PentahoJcrConstants.PENTAHO_LOCKTOKENSTORAGE));
    newLockTokenNode.setProperty(JcrRepositoryFileUtils.addPentahoPrefix(session,
        PentahoJcrConstants.PENTAHO_LOCKEDNODEREF), lock.getNode());
    newLockTokenNode.setProperty(JcrRepositoryFileUtils
        .addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_LOCKTOKEN), lock.getLockToken());
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, nodeIdStrategy, lockTokensNode);
  }

  public List<String> getLockTokens(final Session session, final NodeIdStrategy nodeIdStrategy)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, nodeIdStrategy);
    NodeIterator nodes = lockTokensNode.getNodes();
    List<String> lockTokens = new ArrayList<String>();
    while (nodes.hasNext()) {
      lockTokens.add(nodes.nextNode().getProperty(
          JcrRepositoryFileUtils.addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_LOCKTOKEN)).getString());
    }
    return lockTokens;
  }

  public void removeLockToken(final Session session, final NodeIdStrategy nodeIdStrategy, final Lock lock)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, nodeIdStrategy);
    NodeIterator nodes = lockTokensNode.getNodes();
    JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, nodeIdStrategy, lockTokensNode);
    while (nodes.hasNext()) {
      Node node = nodes.nextNode();
      if (node.getName().equals(lock.getLockToken())) {
        node.remove();
      }
    }
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, nodeIdStrategy, lockTokensNode);
  }
  
  private String internalGetTenantId() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null, "this method cannot be called with a null IPentahoSession");
    return (String) pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY);
  }

  private String internalGetUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null, "this method cannot be called with a null IPentahoSession");
    return pentahoSession.getName();
  }

  private Node getOrCreateLockTokensNode(final Session session, final NodeIdStrategy nodeIdStrategy)
      throws RepositoryException {
    String tenantId = internalGetTenantId();
    String username = internalGetUsername();
    Item item = session.getItem(MessageFormat.format(PATTERN_USER_HOME_FOLDER_PATH, tenantId, username));
    Assert.isTrue(item.isNode());
    Node userHomeFolderNode = (Node) item;
    if (userHomeFolderNode.hasNode(FOLDER_NAME_LOCK_TOKENS)) {
      return userHomeFolderNode.getNode(FOLDER_NAME_LOCK_TOKENS);
    } else {
      JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, nodeIdStrategy, userHomeFolderNode);
      Node lockTokensNode = userHomeFolderNode.addNode(FOLDER_NAME_LOCK_TOKENS, JcrRepositoryFileUtils
          .addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_INTERNALFOLDER));
      session.save();
      JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, nodeIdStrategy, userHomeFolderNode);
      return lockTokensNode;
    }
  }
}