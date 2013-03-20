/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.web;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.open.exceptions.RestException;
import com.wadpam.open.security.SecurityDetailsService;
import com.wadpam.open.security.SecurityInterceptor;
import com.wadpam.open.user.domain.DOpenUser;
import com.wadpam.open.web.BasicAuthenticationInterceptor;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * @author sosandstrom
 */
public class OAuth2InterceptorTest extends TestCase {
    static final String TOKEN = "a.valid.federated.token";
    static final Logger LOG = LoggerFactory.getLogger(OAuth2InterceptorTest.class);

    static final String URI = "/api/domain/resource/v10";
    OAuth2Interceptor instance;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        instance = new OAuth2Interceptor();
        instance.setSecurityDetailsService(new SecurityDetailsService() {
            @Override
            public Object loadUserDetailsByUsername(HttpServletRequest request, 
                    HttpServletResponse response, 
                    String uri, String authValue, String username) {
                if (TOKEN.equals(authValue)) {
                    DConnection conn = new DConnection();
                    conn.setId(TOKEN);
                    conn.setUserId("username");

                    return conn;
                }
                return null;
            }

            @Override
            public Collection<String> getRolesFromUserDetails(Object details) {
                return BasicAuthenticationInterceptor.getRolesFromUserDetailsImpl(details);
            }
        });
        LOG.info("-----           setUp() {}           -----", getName());
    }

    @Override
    protected void tearDown() throws Exception {
        LOG.info("+++++           tearDown() {}        +++++", getName());
        super.tearDown();
    }

    public void testIsAuthenticatedNoCredentials() {
        final String authValue = null;
        try {
            String actual = instance.isAuthenticated(null, null, null, 
                    URI, "GET", authValue);
        }
        catch (RestException expected) {}
    }
    
    public void testIsWhitelisted() {
        Entry<String, Collection<String>> forUri = new AbstractMap.SimpleImmutableEntry(
                String.format("\\A%s\\z", URI),
                Arrays.asList("GET"));
        Collection<Entry<String, Collection<String>>> whitelistedMethods = Arrays.asList(forUri);
        instance.setWhitelistedMethods(whitelistedMethods);
        final String authValue = null;
        MockHttpServletRequest request = new MockHttpServletRequest();
        String actual = instance.isAuthenticated(request, null, null, 
                URI, "GET", authValue);
        assertEquals("whitelisted", SecurityInterceptor.USERNAME_ANONYMOUS, actual);
        assertNull(request.getAttribute(SecurityInterceptor.ATTR_NAME_USERNAME));
    }
    
    public void testIsAuthenticatedNoSuchUser() {
        final String authValue = "test:test";
        try {
            String actual = instance.isAuthenticated(null, null, null, 
                    URI, "GET", authValue);
            fail("Expected 403 Forbidden (exception)");
        }
        catch (RestException expected) {}
    }
    
    public void testIsAuthenticatedWrongPassword() {
        final String authValue = "username:test";
        try {
        String actual = instance.isAuthenticated(null, null, null, 
                URI, "GET", authValue);
        }
        catch (RestException expected) {}
    }
    
    public void testIsAuthenticated() {
        final String authValue = TOKEN;
        MockHttpServletRequest request = new MockHttpServletRequest();
        String actual = instance.isAuthenticated(request, null, null, 
                URI, "GET", authValue);
        assertEquals("OK", "username", actual);
        assertEquals("ATTR", "username", request.getAttribute(SecurityInterceptor.ATTR_NAME_USERNAME));
    }
    
}
