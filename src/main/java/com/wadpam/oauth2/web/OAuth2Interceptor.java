/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.web;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.service.ConnectionServiceImpl;
import com.wadpam.oauth2.service.OAuth2Service;
import com.wadpam.oauth2.service.OAuth2ServiceImpl;
import com.wadpam.open.exceptions.RestException;
import com.wadpam.open.mvc.CrudService;
import com.wadpam.open.security.SecurityDetailsService;
import com.wadpam.open.web.DomainInterceptor;
import com.wadpam.open.web.DomainNamespaceFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author sosandstrom
 */
public class OAuth2Interceptor extends DomainInterceptor implements SecurityDetailsService {
    
    private boolean autoRegister = true;
    private boolean verifyLocally = true;
    private boolean verifyRemotely = false;
    private String providerId = OAuth2Service.PROVIDER_ID_FACEBOOK;
    
    private CrudService<DConnection,String> connectionService;
    private OAuth2Service oauth2Service = null;
    
    public OAuth2Interceptor() {
        super();
        setAuthenticationMechanism(AUTH_TYPE_OAUTH);
        setSecurityDetailsService(this);
    }

    @Override
    protected String getRealmPassword(Object details) {
        final DConnection conn = (DConnection) details;
        return conn.getAccessToken();
    }

    @Override
    protected String getRealmUsername(String clientUsername, Object details) {
        final DConnection conn = (DConnection) details;
        return conn.getUserId();
    }

    /**
     * if specified details is defined, returns Details.roles[].
     * @param details a DConnection object
     * @return if specified details is defined, Details.roles[].
     */
    @Override
    public Collection<String> getRolesFromUserDetails(Object details) {
        final DConnection conn = (DConnection) details;
        return null != details ?
            ConnectionServiceImpl.convertRoles(conn.getUserRoles()) :
            Collections.EMPTY_LIST;
    }
    
    @Override
    public String isAuthenticated(HttpServletRequest request, HttpServletResponse response, Object handler, String uri, String method, String authValue) {
        
        // already registered (local check)?
        String username = super.isAuthenticated(request, response, handler, uri, method, authValue);
        
        // is this a different token, to be registered on-the-fly?
        if (null == username && autoRegister && null != oauth2Service && null != request) {
            
            // does request contain necessary parameters?
            String providerId = request.getParameter("providerId");
            String providerUserId = request.getParameter("providerUserId");
            String secret = request.getParameter("secret");
            String expiresIn = request.getParameter("expires_in");
            Integer expiresInSeconds = null != expiresIn ? Integer.parseInt(expiresIn) : 3600;
            String appArg0 = request.getParameter("appArg0");
            String domain = DomainNamespaceFilter.getDomain();
            
            if (null != providerId && null != expiresInSeconds) {
                
                // register and verify with federated provider
                ResponseEntity<DConnection> res = oauth2Service.registerFederated(
                        authValue, providerId, providerUserId, secret, expiresInSeconds, appArg0, domain);

                // if it looks good, try to authenticate again, to have all populated:
                if (null != res && null != res.getBody()) {
                    username = super.isAuthenticated(request, response, handler, uri, method, authValue);
                    LOG.info("auto registered: {} for {}", authValue, username);
                }
            }
        }
        
        // the SecurityInterceptor.preHandle will simply return false,
        // which leads to an empty 200 response.
        if (null == username) {
            throw new RestException(77403, HttpStatus.FORBIDDEN, authValue);
        }
        
        return username;
    }

    @Override
    public Object loadUserDetailsByUsername(HttpServletRequest request, 
            HttpServletResponse response, 
            String uri, 
            String authValue, 
            String clientUsername) {
        try {
            final String realmUsername = verifyAccessToken(authValue, request);
            if (null != realmUsername && null != request) {
                final DConnection conn = (DConnection) request.getAttribute(AUTH_PARAM_OAUTH);
                return conn;
            }
        }
        catch (RestException whenMissing) {
            LOG.info("No token/user found for {}, reason {}", authValue, whenMissing.getMessage());
        }
        return null;
    }

    protected String verifyAccessToken(String accessToken, HttpServletRequest request) {

        // missing means Unauthorized
        if (null != accessToken) {
            
            // no verification at all?
            if (!verifyLocally && !verifyRemotely) {
                return USERNAME_ANONYMOUS;
            }
            
            DConnection conn = null;
            
            // only verify in local database if configured
            if (verifyLocally) {
                conn = connectionService.get(null, accessToken);
                if (null == conn) {
                    throw new RestException(404, "No token found in realm", null, HttpStatus.FORBIDDEN, "Authentication required");
                }
                
                // check expired locally?
                if (!verifyRemotely && null != conn.getExpireTime() && conn.getExpireTime().before(new Date())) {
                    throw new RestException(410, null != conn.getExpireTime() ? conn.getExpireTime().toString() : "No expireTime", 
                            null, HttpStatus.FORBIDDEN, "Authentication expired");
                }
            }
            
            // verify remotely?
            if (verifyRemotely) {
                if (null == conn) {
                    throw new UnsupportedOperationException("For remote verification, local must be enabled too.");
                }
                
                String providerUserId = oauth2Service.getProviderUserId(accessToken, providerId, null);
                if (null == providerUserId) {
                    return null;
                }
                
                // double-check userId
                if (!providerUserId.equals(conn.getProviderUserId())) {
                    throw new RestException(409, "providerUserId mismatch", null, HttpStatus.FORBIDDEN, "Authentication mismatch");
                }
            }
            
            if(null != request) {
                request.setAttribute(AUTH_PARAM_OAUTH, conn);
            }
            return conn.getUserId();
        }

        throw new RestException(401, "No token found in request", null, HttpStatus.FORBIDDEN, "Authentication required");
    }

    public void setAutoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
    }

    public void setVerifyLocally(boolean verifyLocally) {
        this.verifyLocally = verifyLocally;
    }

    public void setVerifyRemotely(boolean verifyRemotely) {
        this.verifyRemotely = verifyRemotely;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    @Autowired
    public void setConnectionService(CrudService connectionService) {
        this.connectionService = connectionService;
    }

    public void setOauth2Service(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
    }
    
}
