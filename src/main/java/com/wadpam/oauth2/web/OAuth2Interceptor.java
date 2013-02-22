/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.web;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.service.ConnectionService;
import com.wadpam.oauth2.service.OAuth2Service;
import com.wadpam.open.exceptions.RestException;
import com.wadpam.open.web.DomainInterceptor;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

/**
 *
 * @author sosandstrom
 */
public class OAuth2Interceptor extends DomainInterceptor {
    
    public static final String NAME_ACCESS_TOKEN = "access_token";
    public static final String PREFIX_OAUTH = "OAuth ";
    
    private boolean verifyLocally = true;
    private boolean verifyRemotely = false;
    private String providerId = OAuth2Service.PROVIDER_ID_FACEBOOK;
    
    private ConnectionService connectionService;

    @Override
    protected String doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = null;
        
        // check Authorization: OAuth header first
        final String authorization = request.getHeader(NAME_AUTHORIZATION);
        if (null != authorization && authorization.startsWith(PREFIX_OAUTH)) {
            accessToken = authorization.substring(PREFIX_OAUTH.length());
        }
        
        // param is backup
        if (null == accessToken) {
            accessToken = request.getParameter(NAME_ACCESS_TOKEN);
        }
        
        return verifyAccessToken(accessToken);
    }

    protected String verifyAccessToken(String accessToken) {
        // missing means Unauthorized
        if (null != accessToken) {
            
            // no verification at all?
            if (!verifyLocally && !verifyRemotely) {
                return USERNAME_ANONYMOUS;
            }
            
            DConnection conn = null;
            
            // only verify in local database if configured
            if (verifyLocally) {
                conn = connectionService.getByAccessToken(accessToken);
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
                
                String providerUserId = OAuth2Service.getProviderUserId(accessToken, providerId);
                if (null == providerUserId) {
                    return null;
                }
                
                // double-check userId
                if (providerUserId.equals(conn.getProviderUserId())) {
                    return conn.getUserId();
                }
            }
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
