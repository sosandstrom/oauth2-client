/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.web;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.json.JConnection;
import com.wadpam.oauth2.service.OAuth2Service;
import com.wadpam.open.security.SecurityInterceptor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.CookieGenerator;

/**
 *
 * @author sosandstrom
 */
@Controller
@RequestMapping("{domain}")
public class OAuth2Controller {
    
    private OAuth2Service service;
    
    private final CookieGenerator COOKIE_GENERATOR;
    
    private boolean supportCookie = true;

    public OAuth2Controller() {
        // set cookie name to access_token
        COOKIE_GENERATOR = new CookieGenerator();
        COOKIE_GENERATOR.setCookieName(SecurityInterceptor.AUTH_PARAM_OAUTH);
    }
    
    @RequestMapping(value="federated/v11", method={RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<JConnection> registerFederated(
            HttpServletResponse response,
            @PathVariable String domain,
            @RequestParam String access_token, 
            @RequestParam String providerId,
            @RequestParam String providerUserId,
            @RequestParam(required=false) String secret,
            @RequestParam(defaultValue="3600") Integer expires_in,
            @RequestParam(required=false) String appArg0
            ) {
        
        ResponseEntity<DConnection> res = service.registerFederated(access_token, 
                providerId, providerUserId, 
                secret, expires_in, appArg0);
        
        // set a cookie if supported
        if (supportCookie) {
            synchronized (COOKIE_GENERATOR) {
                COOKIE_GENERATOR.setCookiePath(String.format("/api/%s", domain));
                COOKIE_GENERATOR.setCookieMaxAge(expires_in);
                COOKIE_GENERATOR.addCookie(response, access_token);
            }
        }
        
        final JConnection body = new JConnection();
        ConnectionController.convertDConnection(res.getBody(), body);
        
        // clear a few values
        body.setId(null);
        body.setSecret(null);
        return new ResponseEntity<JConnection>(body, res.getStatusCode());
    }

    @RequestMapping(value="federated/v11/{providerId}", method={RequestMethod.DELETE})
    public ResponseEntity unregisterFederated(
            HttpServletResponse response,
            @PathVariable String domain,
            @PathVariable String providerId) {
        
        // delete the cookie client-side:
        synchronized (COOKIE_GENERATOR) {
            COOKIE_GENERATOR.setCookiePath(String.format("/api/%s", domain));
            COOKIE_GENERATOR.removeCookie(response);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @Autowired
    public void setService(OAuth2Service oauth2Service) {
        this.service = oauth2Service;
    }

    public boolean isSupportCookie() {
        return supportCookie;
    }

    public void setSupportCookie(boolean supportCookie) {
        this.supportCookie = supportCookie;
    }
    
}
