/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */

package org.pentaho.platform.engine.security.acls.voter;

import java.util.List;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.acl.AclEntry;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.repository.IRepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public abstract class AbstractPentahoAclVoter implements IAclVoter, IPentahoInitializer {
  protected GrantedAuthority adminRole;

  public abstract Authentication getAuthentication(IPentahoSession session);

  public GrantedAuthority getAdminRole() {
    return this.adminRole;
  }

  public void setAdminRole(final GrantedAuthority value) {
    this.adminRole = value;
  }

  public void init(final IPentahoSession session) {
    ISystemSettings settings = PentahoSystem.getSystemSettings();
    String roleName = settings.getSystemSetting("acl-voter/admin-role", "Admin"); //$NON-NLS-1$ //$NON-NLS-2$
    adminRole = new GrantedAuthorityImpl(roleName);
  }

  public boolean isPentahoAdministrator(final IPentahoSession session) {
    // A user is considered a manager if they're authenticated,
    // and a member of the adminRole specified.
    return isGranted(session, adminRole);
  }

  public boolean isGranted(final IPentahoSession session, final GrantedAuthority role) {
    Authentication auth = getAuthentication(session);
    if ((auth != null) && auth.isAuthenticated()) {
      GrantedAuthority[] userAuths = auth.getAuthorities();
      if (userAuths == null) {
        return false;
      }
      for (GrantedAuthority element : userAuths) {
        if (element.equals(role)) {
          return true;
        }
      }
      return false;
    } else {
      return false;
    }
  }

  protected AclEntry[] getEffectiveAccessControls(final Object domainInstance) {
    List acls = ((IAclHolder) domainInstance).getAccessControls();
    if (domainInstance instanceof IAclHolder) {
      if (acls.size() == 0) {
        ISolutionFile chain = (ISolutionFile) domainInstance;
        while (!chain.isRoot() && (acls.size() == 0)) {
          chain = (ISolutionFile) chain.retrieveParent();
          if (chain instanceof IAclSolutionFile) {
            acls = ((IAclSolutionFile)chain).getAccessControls();
          }
        }
      }
    } else {
      // bail
      throw new RuntimeException("cannot get effective access controls for instances of type " + domainInstance.getClass());
    }
    return (AclEntry[]) acls.toArray(new AclEntry[0]);
  }
}
