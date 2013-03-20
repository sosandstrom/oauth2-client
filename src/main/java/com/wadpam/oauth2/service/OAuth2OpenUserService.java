/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.open.security.SecurityDetailsService;
import com.wadpam.open.user.domain.DOpenUser;
import com.wadpam.open.user.service.OpenUserService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sosandstrom
 */
public class OAuth2OpenUserService implements OAuth2UserService {
    
    private OpenUserService openUserService;

    @Override
    public String createUser(String email, String firstName, String lastName, 
            String name, String providerId, String providerUserId, String domain) {
        DOpenUser user = openUserService.createDomain();
        user.setDisplayName(name);
        user.setEmail(email);
        ArrayList roles = new ArrayList();
        roles.add(SecurityDetailsService.ROLE_USER);
        final String role = String.format("ROLE_%s", providerId.toUpperCase());
        roles.add(role);
        user.setRoles(roles);
        final Long userId = openUserService.create(user);
        return null != userId ? userId.toString() : null;
    }

    @Override
    public Collection<String> getRolesFromUserDetails(Object details) {
        return null != details ? ((DOpenUser)details).getRoles() : Collections.EMPTY_LIST;
    }

    @Override
    public Object loadUserDetailsByUsername(HttpServletRequest request, HttpServletResponse response, String uri, String authValue, String username) {
        return openUserService.get(null, Long.parseLong(username));
    }

    @Autowired
    public void setOpenUserService(OpenUserService openUserService) {
        this.openUserService = openUserService;
    }
    
}
