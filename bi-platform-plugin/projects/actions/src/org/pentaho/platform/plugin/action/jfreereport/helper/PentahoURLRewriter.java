package org.pentaho.platform.plugin.action.jfreereport.helper;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.jfree.report.modules.output.table.html.URLRewriteException;
import org.jfree.report.modules.output.table.html.URLRewriter;
import org.jfree.repository.ContentEntity;
import org.jfree.repository.ContentIOException;
import org.jfree.repository.ContentLocation;

/**
 * Creation-Date: 05.07.2007, 19:16:13
 *
 * @author Thomas Morgner
 */
public class PentahoURLRewriter implements URLRewriter {
  private String pattern;

  public PentahoURLRewriter(final String pattern) {
    this.pattern = pattern;
  }

  public String rewrite(final ContentEntity contentEntry, final ContentEntity dataEntity) throws URLRewriteException {
    try {
      final ArrayList<String> entityNames = new ArrayList<String>();
      entityNames.add(dataEntity.getName());

      ContentLocation location = dataEntity.getParent();
      while (location != null) {
        entityNames.add(location.getName());
        location = location.getParent();
      }

      final ArrayList<String> contentNames = new ArrayList<String>();
      location = dataEntity.getRepository().getRoot();

      while (location != null) {
        contentNames.add(location.getName());
        location = location.getParent();
      }

      // now remove all path elements that are equal ..
      while ((contentNames.isEmpty() == false) && (entityNames.isEmpty() == false)) {
        final String lastEntity = (String) entityNames.get(entityNames.size() - 1);
        final String lastContent = (String) contentNames.get(contentNames.size() - 1);
        if (lastContent.equals(lastEntity) == false) {
          break;
        }
        entityNames.remove(entityNames.size() - 1);
        contentNames.remove(contentNames.size() - 1);
      }

      final StringBuffer b = new StringBuffer();
      for (int i = entityNames.size() - 1; i >= 0; i--) {
        final String name = (String) entityNames.get(i);
        b.append(name);
        if (i != 0) {
          b.append("/");//$NON-NLS-1$
        }
      }

      if (pattern == null) {
        return b.toString();
      }

      return MessageFormat.format(pattern, new Object[] { b.toString() });
    } catch (ContentIOException cioe) {
      throw new URLRewriteException();
    }

  }
}
