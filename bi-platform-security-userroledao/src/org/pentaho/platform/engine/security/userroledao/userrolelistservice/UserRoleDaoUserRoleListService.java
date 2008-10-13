package org.pentaho.platform.engine.security.userroledao.userrolelistservice;

import java.util.ArrayList;
import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.springframework.dao.DataAccessException;

/**
 * An {@link IUserRoleListService} that delegates to an {@link IUserRoleDao}.
 * 
 * @author mlowery
 */
public class UserRoleDaoUserRoleListService implements IUserRoleListService {

  // ~ Static fields/initializers ====================================================================================== 

  // ~ Instance fields =================================================================================================

  private IUserRoleDao userRoleDao;
  
  private UserDetailsService userDetailsService;

  // ~ Constructors ====================================================================================================

  public UserRoleDaoUserRoleListService() {
    super();
  }

  // ~ Methods =========================================================================================================

  public GrantedAuthority[] getAllAuthorities() {
    List<IPentahoRole> roles = userRoleDao.getRoles();

    List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();

    for (IPentahoRole role : roles) {
      auths.add(new GrantedAuthorityImpl(role.getName()));
    }

    return auths.toArray(new GrantedAuthority[0]);
  }

  public String[] getAllUsernames() {
    List<IPentahoUser> users = userRoleDao.getUsers();

    List<String> usernames = new ArrayList<String>();

    for (IPentahoUser user : users) {
      usernames.add(user.getUsername());
    }

    return usernames.toArray(new String[0]);
  }

  public GrantedAuthority[] getAuthoritiesForUser(String username) throws UsernameNotFoundException,
  DataAccessException {
  UserDetails user = userDetailsService.loadUserByUsername(username);
  return user.getAuthorities();
  }

  public String[] getUsernamesInRole(GrantedAuthority authority) {
    IPentahoRole role = userRoleDao.getRole(authority.getAuthority());
    if (role == null) {
      String[] rtn = {};
      return rtn;
    }

    List<String> usernames = new ArrayList<String>();

    for (IPentahoUser user : role.getUsers()) {
      usernames.add(user.getUsername());
    }

    return usernames.toArray(new String[0]);
  }

  public void setUserRoleDao(IUserRoleDao userRoleDao) {
    this.userRoleDao = userRoleDao;
  }

  public void setUserDetailsService(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

}
