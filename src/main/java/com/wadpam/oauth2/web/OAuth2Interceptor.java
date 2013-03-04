/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.web;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.service.ConnectionService;
import com.wadpam.oauth2.service.OAuth2Service;
import com.wadpam.open.exceptions.RestException;
import com.wadpam.open.security.SecurityDetailsService;
import com.wadpam.open.web.DomainInterceptor;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

/**
 *
 * @author sosandstrom
 */
public class OAuth2Interceptor extends DomainInterceptor implements SecurityDetailsService {
    
    private boolean verifyLocally = true;
    private boolean verifyRemotely = false;
    private String providerId = OAuth2Service.PROVIDER_ID_FACEBOOK;
    
    private ConnectionService connectionService;
    
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
            LOG.info("No token/user found for {}", authValue);
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
                
                String providerUserId = OAuth2Service.getProviderUserId(accessToken, providerId, null);
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

    public void setVerifyLocally(boolean verifyLocally) {
        this.verifyLocally = verifyLocally;
    }

    public void setVerifyRemotely(boolean verifyRemotely) {
        this.verifyRemotely = verifyRemotely;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }
    
}
