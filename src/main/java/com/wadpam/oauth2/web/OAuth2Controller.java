/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.web;

import com.wadpam.docrest.domain.RestCode;
import com.wadpam.docrest.domain.RestReturn;
import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.json.JConnection;
import com.wadpam.oauth2.service.OAuth2Service;
import static com.wadpam.oauth2.service.OAuth2Service.OPERATION_REGISTER_FEDERATED;
import com.wadpam.open.mvc.CrudListener;
import com.wadpam.open.mvc.CrudObservable;
import com.wadpam.open.security.SecurityInterceptor;
import java.io.Serializable;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.CookieGenerator;

/**
 * Provides methods to register and unregister a federated OAuth2 access token.
 * @author sosandstrom
 */
@RestReturn(value = JConnection.class)
@Controller
@RequestMapping("{domain}")
public class OAuth2Controller implements CrudObservable {
    
    static final Logger LOG = LoggerFactory.getLogger(OAuth2Controller.class);
    
    private OAuth2Service service;
    
    private final CookieGenerator COOKIE_GENERATOR;
    
    private boolean supportCookie = true;

    protected final ArrayList<CrudListener> listeners = new ArrayList<CrudListener>();
    
    public OAuth2Controller() {
        // set cookie name to access_token
        COOKIE_GENERATOR = new CookieGenerator();
        COOKIE_GENERATOR.setCookieName(SecurityInterceptor.AUTH_PARAM_OAUTH);
    }
    
    /**
     * Registers an access token from a separate (federated) OAuth2 Provider.
     * @param response for adding cookie
     * @param domain for multi-tenancy
     * @param access_token the access token to register
     * @param providerId id of the OAuth2 provider
     * @param providerUserId user's id at the OAuth2 provider (optional since 1.5)
     * @param secret only used for twitter
     * @param expires_in seconds this access token is valid (from now)
     * @param appArg0 provider-specific. For Salesforce, this is instance_url
     * @return the JConnection, containing the user's id in this client app domain.
     */
    @RestReturn(value = JConnection.class, entity=JConnection.class ,code = {
        @RestCode(code = 200, description = "The token was registered for an existing user", message = "OK"),
        @RestCode(code = 201, description = "A User was created, and the token was registered", message = "Created")
    })
    @RequestMapping(value="federated/v11", method={RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<JConnection> registerFederated(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String domain,
            @RequestParam String access_token, 
            @RequestParam String providerId,
            @RequestParam(required=false) String providerUserId,
            @RequestParam(required=false) String secret,
            @RequestParam(defaultValue="3600") Integer expires_in,
            @RequestParam(required=false) String appArg0
            ) {
        
        // notify listeners
        preService(request, domain, OPERATION_REGISTER_FEDERATED, providerUserId, providerId, access_token);
        
        ResponseEntity<DConnection> res = service.registerFederated(access_token, 
                providerId, providerUserId, 
                secret, expires_in, appArg0,
                domain);
        
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
        
        // notify listeners
        postService(request, domain, OPERATION_REGISTER_FEDERATED, body, body.getUserId(), res.getBody());
        
        
        return new ResponseEntity<JConnection>(body, res.getStatusCode());
    }

    /**
     * Removes the cookie for this host and path.
     * @param response
     * @param domain used to construct the cookie path
     * @param providerId not used.
     * @return 200 on everything
     */
    @RestReturn(value = Void.class, code = {
        @RestCode(code = 200, description = "Cookie will be deleted.", message = "OK")
    })
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

    @Override
    public void addListener(CrudListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void removeListener(CrudListener listener) {
        listeners.remove(listener);
    }
    
    protected void preService(HttpServletRequest request, String namespace,
            int operation, Object json, Object domain, Serializable id) {
        for (CrudListener l : listeners) {
            l.preService(null, null, request, namespace, 
                    operation, json, domain, id);
        }
    }
    
    protected void postService(HttpServletRequest request, String namespace,
            int operation, Object json, Serializable id, Object serviceResponse) {
        for (CrudListener l : listeners) {
            l.postService(null, null, request, namespace, 
                    operation, json, id, serviceResponse);
        }
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
