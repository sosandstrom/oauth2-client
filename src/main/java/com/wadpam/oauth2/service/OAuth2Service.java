/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.oauth2.dao.DConnectionDao;
import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.domain.DFactory;
import com.wadpam.oauth2.itest.ITestApiAdapter;
import com.wadpam.oauth2.itest.IntegrationTestConnectionFactory;
import com.wadpam.open.exceptions.AuthenticationFailedException;
import com.wadpam.open.exceptions.NotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import net.sf.mardao.core.CursorPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.salesforce.api.SalesforceProfile;
import org.springframework.social.salesforce.api.impl.SalesforceTemplate;
import org.springframework.social.salesforce.connect.SalesforceConnectionFactory;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;
import org.springframework.web.client.HttpClientErrorException;

/**
 *
 * @author sosandstrom
 */
public class OAuth2Service implements ConnectionFactoryLocator {
    
    public static final String PROVIDER_ID_ITEST = "itest";
    public static final String PROVIDER_ID_FACEBOOK = "facebook";
//    public static final String PROVIDER_ID_GEKKO = "gekko";
    public static final String PROVIDER_ID_GOOGLE = "google";
    public static final String PROVIDER_ID_SALESFORCE = "salesforce";
    public static final String PROVIDER_ID_TWITTER = "twitter";
    
    static final Logger LOG = LoggerFactory.getLogger(OAuth2Service.class);
    
    private boolean autoCreateUser = true;
    
    private ConnectionFactoryRegistry   registry = null;
    
    private FactoryService factoryService;
    
    private DConnectionDao dConnectionDao;
    
    private OAuth2UserService oauth2UserService;
    
    /**
     * 
     * @param access_token
     * @param providerId
     * @param providerUserId
     * @param secret
     * @param expires_in
     * @return the userId associated with the Connection, null if new Connection
     */
    public ResponseEntity<String> registerFederated(
            String access_token, 
            String providerId,
            String providerUserId,
            String secret,
            Integer expiresInSeconds,
            String appArg0) {
        
        // load connection from db async style (likely case is new token for existing user)
        Iterable<DConnection> conns = dConnectionDao.queryByProviderUserId(providerUserId);

        // use the connectionFactory
        final Connection<?> connection = createConnection(access_token, secret, providerId, providerUserId);

        UserProfile profile = null;
        try {
            boolean valid = verifyConnection(connection);
            if (!valid) {
                throw new AuthenticationFailedException(503403, "Unauthorized federated side");
            }
            
            profile = connection.fetchUserProfile();
            
            // it provderId from twitter skip it,
            // WARNING: Authentication error: Unable to respond to any of these challenges: {oauth=WWW-Authenticate: OAuth
            // realm="https://api.twitter.com"}
            // if (!PROVIDER_ID_TWITTER.equals(providerId) && !valid) {
            if (null == profile) {
                throw new IllegalArgumentException("Invalid connection");
            }
        } catch (NotAuthorizedException unauthorized) {
            throw new AuthenticationFailedException(503401, "Unauthorized federated side");
        } catch (HttpClientErrorException deletedOnServerSide) {
            throw new NotFoundException(503404, "User deleted federated side");
        }
        
        // load existing conn for token
        DConnection conn = dConnectionDao.findByPrimaryKey(access_token);
        final boolean isNewConnection = (null == conn);
        boolean isNewUser = false;
        String userId = null;
        
        // create connection?
        if (isNewConnection) {
            
            // find other connections for this user, discard expired
            final Date now = new Date();
            final ArrayList<String> expiredTokens = new ArrayList<String>();
            for (DConnection dc : conns) {
                if (providerId.equals(dc.getProviderId())) {
                    userId = dc.getUserId();
                    
                    // expired?
                    if (null != dc.getExpireTime() && now.after(dc.getExpireTime())) {
                        expiredTokens.add(dc.getId());
                    }
                }
            }
            dConnectionDao.delete(null, expiredTokens);
            
            // create user?
            isNewUser = (null == userId);
            if (isNewUser && autoCreateUser && null != oauth2UserService) {
                userId = oauth2UserService.createUser(profile.getEmail(), profile.getFirstName(), profile.getLastName(),
                        profile.getName(), profile.getUsername());
            }
            
            conn = new DConnection();
            conn.setId(access_token);
            conn.setDisplayName(profile.getName());
            conn.setProviderId(providerId);
            conn.setProviderUserId(providerUserId);
            conn.setSecret(secret);
            conn.setUserId(userId);
            if (null != expiresInSeconds) {
                conn.setExpireTime(new Date(System.currentTimeMillis() + expiresInSeconds*1000L));
            }
            dConnectionDao.persist(conn);
        }
        else {
            userId = conn.getUserId();
        }
        
        // update connection values
        conn.setAppArg0(appArg0);
        dConnectionDao.update(conn);
        
        return new ResponseEntity<String>(userId, 
                isNewUser ? HttpStatus.CREATED : HttpStatus.OK);
    }
    
    protected Connection<?> createConnection(String accessToken, String secret, String providerId, String providerUserId)
            throws IllegalArgumentException {
        // load from database
        final DFactory factory = factoryService.get(null, providerId);
        if (null == factory) {
            throw new IllegalArgumentException("No configured provider " + providerId);
        }

        // create the factory
        final ConnectionFactory<?> cf = createFromFactory(factory);
        if (null == cf) {
            throw new IllegalArgumentException("No registered provider " + providerId);
        }

        // LOG.debug("=========secret : {}", secret);

        /** Twitter needs to authenticate with token and secret **/
        if (secret == null && PROVIDER_ID_TWITTER.equals(providerId)) {
            // create connectionData
            DConnection twitterConn = dConnectionDao.findByPrimaryKey(accessToken);
            if (twitterConn != null) {
                secret = twitterConn.getSecret();
            }
        }

        final ConnectionData data = new ConnectionData(providerId, providerUserId, null, null, null, accessToken, secret, null,
                System.currentTimeMillis() + 90L * 60L * 1000L);

        // use the connectionFactory
        final Connection<?> connection = cf.createConnection(data);

        return connection;
    }

    // ------------    implements ConnectionFactoryLocator --------- 
    
    public static ConnectionFactory<?> createFromFactory(DFactory factory) {
        LOG.debug("creating factory for {}", factory.getId());

        ConnectionFactory<?> cf = null;

        if (PROVIDER_ID_FACEBOOK.equals(factory.getId())) {
            cf = new FacebookConnectionFactory(factory.getClientId(), factory.getClientSecret());
        }
        else if (PROVIDER_ID_GOOGLE.equals(factory.getId())) {
            cf = new GoogleConnectionFactory(factory.getClientId(), factory.getClientSecret());
        }
//        else if (PROVIDER_ID_GEKKO.equals(factory.getProviderId())) {
//            cf = new GekkoConnectionFactory(factory.getBaseUrl(), factory.getClientId(), factory.getClientSecret());
//        }
        else if (PROVIDER_ID_SALESFORCE.equals(factory.getId())) {
            cf = new SalesforceConnectionFactory(factory.getClientId(), factory.getClientSecret());
        }
        else if (PROVIDER_ID_TWITTER.equals(factory.getId())) {
            cf = new TwitterConnectionFactory(factory.getClientId(), factory.getClientSecret());
        }
        else if (PROVIDER_ID_ITEST.equals(factory.getId())) {
            cf = new IntegrationTestConnectionFactory();
        }
        LOG.debug("created factory {} for {}", cf, factory.getId());
        return cf;
    }

    protected ConnectionFactoryRegistry getRegistry() {

        if (null == registry) {
            registry = new ConnectionFactoryRegistry();
            final CursorPage<DFactory, String> page = factoryService.getPage(100, null);
            for(DFactory factory : page.getItems()) {
                final ConnectionFactory<?> cf = createFromFactory(factory);
                if (null != cf) {
                    registry.addConnectionFactory(cf);
                }
            }

            LOG.debug("created registry with {} factories", registry.registeredProviderIds().size());
        }
        LOG.debug("returning registry {}", registry);

        return registry;
    }

    @Override
    public ConnectionFactory<?> getConnectionFactory(String providerId) {
        return getRegistry().getConnectionFactory(providerId);
    }

    @Override
    public <A> ConnectionFactory<A> getConnectionFactory(Class<A> apiType) {
        return getRegistry().getConnectionFactory(apiType);
    }

    @Override
    public Set<String> registeredProviderIds() {
        return getRegistry().registeredProviderIds();
    }
    
    public static String getProviderUserId(String access_token, String providerId) {
        if (PROVIDER_ID_FACEBOOK.equals(providerId)) {
            FacebookTemplate template = new FacebookTemplate(access_token);
            org.springframework.social.facebook.api.UserOperations userOps = template.userOperations();
            FacebookProfile profile = userOps.getUserProfile();
            return profile.getId();
        }
        else if (PROVIDER_ID_ITEST.equals(providerId)) {
            return ITestApiAdapter.ITEST_PROVIDER_USER_ID;
        }
        else if (PROVIDER_ID_SALESFORCE.equals(providerId)) {
            SalesforceTemplate template = new SalesforceTemplate(access_token);
            org.springframework.social.salesforce.api.ChatterOperations chatOps = template.chatterOperations();
            SalesforceProfile profile = chatOps.getUserProfile();
            return profile.getId();
        }
        throw new IllegalArgumentException("No registered provider " + providerId);
    }

    protected boolean verifyConnection(Connection connection) {
        ConnectionData data = connection.createData();
        String userId = getProviderUserId(data.getAccessToken(), data.getProviderId());
        return data.getProviderUserId().equals(userId);
    }

    public void setFactoryService(FactoryService factoryService) {
        this.factoryService = factoryService;
    }

    public void setdConnectionDao(DConnectionDao dConnectionDao) {
        this.dConnectionDao = dConnectionDao;
    }

    public void setAutoCreateUser(boolean autoCreateUser) {
        this.autoCreateUser = autoCreateUser;
    }

    public void setOauth2UserService(OAuth2UserService oauth2UserService) {
        this.oauth2UserService = oauth2UserService;
    }

}
