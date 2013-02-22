/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.oauth2.dao.DConnectionDao;
import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.domain.DFactory;
import com.wadpam.open.exceptions.AuthenticationFailedException;
import com.wadpam.open.exceptions.NotFoundException;
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
import org.springframework.social.facebook.api.UserOperations;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;
import org.springframework.web.client.HttpClientErrorException;

/**
 *
 * @author sosandstrom
 */
public class OAuth2Service implements ConnectionFactoryLocator {
    
    public static final String PROVIDER_ID_FACEBOOK = "facebook";
//    public static final String PROVIDER_ID_GEKKO = "gekko";
    public static final String PROVIDER_ID_GOOGLE = "google";
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
    public ResponseEntity<String> registerFederated(String access_token, 
            String providerId,
            String providerUserId,
            String secret,
            Integer expiresInSeconds) {
        
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
        
        // load connection from db
        DConnection conn = dConnectionDao.findByProviderIdProviderUserId(providerId, providerUserId);
        final boolean isNewConnection = (null == conn);
        
        // create connection?
        if (isNewConnection) {
            conn = new DConnection();
            conn.setDisplayName(profile.getName());
            conn.setProviderId(providerId);
            conn.setProviderUserId(providerUserId);
            conn.setSecret(secret);
            dConnectionDao.persist(conn);
        }
        
        String userId = conn.getUserId();
        
        // create user?
        final boolean isNewUser = (null == userId);
        if (isNewUser && autoCreateUser && null != oauth2UserService) {
            userId = oauth2UserService.createUser(profile.getEmail(), profile.getFirstName(), profile.getLastName(),
                    profile.getName(), profile.getUsername());
            conn.setUserId(userId);
        }
        
        // update connection values
        conn.setAccessToken(access_token);
        if (null != expiresInSeconds) {
            conn.setExpireTime(new Date(System.currentTimeMillis() + expiresInSeconds*1000L));
        }
        dConnectionDao.update(conn);
        
        
        return new ResponseEntity<String>(userId, 
                isNewUser ? HttpStatus.CREATED : HttpStatus.OK);
    }
    
    protected Connection<?> createConnection(String token, String secret, String providerId, String providerUserId)
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

        if (secret == null) {
            /** Twitter need to authenticate with token and secret **/
            // create connectionData
            DConnection conn = dConnectionDao.findByProviderIdProviderUserId(providerId, providerUserId);
            // duConnectionData.setExpireTime(toDate(System.currentTimeMillis() + 90L * 60L * 1000L));
            if (conn != null) {
                secret = conn.getSecret();
            }
        }

        final ConnectionData data = new ConnectionData(providerId, providerUserId, null, null, null, token, secret, null,
                System.currentTimeMillis() + 90L * 60L * 1000L);

        // use the connectionFactory
        final Connection<?> connection = cf.createConnection(data);

        return connection;
    }

    // ------------    implements ConnectionFactoryLocator --------- 
    
    public static ConnectionFactory<?> createFromFactory(DFactory factory) {

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
        else if (PROVIDER_ID_TWITTER.equals(factory.getId())) {

            // LOG.debug("=================================================================Twitter : {}:{}",
            // factory.getClientId(),
            // factory.getClientSecret());
            cf = new TwitterConnectionFactory(factory.getClientId(), factory.getClientSecret());
        }
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
    
    public String unregisterFederated(String providerId, String providerUserId) {
        DConnection conn = dConnectionDao.findByProviderIdProviderUserId(providerId, providerUserId);
        if (null == conn) {
            throw new NotFoundException(500404, "Not Connected");
        }
        
        conn.setAccessToken(null);
        conn.setRefreshToken(null);
        dConnectionDao.update(conn);
        return conn.getUserId();
    }
    
    public static String getProviderUserId(String access_token, String providerId) {
        if (PROVIDER_ID_FACEBOOK.equals(providerId)) {
            FacebookTemplate template = new FacebookTemplate(access_token);
            UserOperations userOps = template.userOperations();
            FacebookProfile profile = userOps.getUserProfile();
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
