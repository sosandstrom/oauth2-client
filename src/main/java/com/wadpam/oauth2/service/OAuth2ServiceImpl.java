/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.oauth2.dao.DConnectionDao;
import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.domain.DFactory;
import com.wadpam.oauth2.itest.ITestTemplate;
import com.wadpam.oauth2.itest.IntegrationTestConnectionFactory;
import com.wadpam.open.exceptions.AuthenticationFailedException;
import com.wadpam.open.exceptions.ConflictException;
import com.wadpam.open.exceptions.NotFoundException;
import com.wadpam.open.mvc.CrudListener;
import com.wadpam.open.mvc.CrudObservable;
import com.wadpam.open.mvc.CrudService;
import com.wadpam.open.transaction.Idempotent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import net.sf.mardao.core.CursorPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.salesforce.api.SalesforceProfile;
import org.springframework.social.salesforce.api.impl.SalesforceTemplate;
import org.springframework.social.salesforce.connect.SalesforceConnectionFactory;
import org.springframework.social.salesforce.connect.SalesforceServiceProvider;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.HttpClientErrorException;

/**
 *
 * @author sosandstrom
 */
public class OAuth2ServiceImpl implements OAuth2Service, CrudObservable {
    
    static final Logger LOG = LoggerFactory.getLogger(OAuth2ServiceImpl.class);
    
    private boolean autoCreateUser = true;
    
    private ConnectionFactoryRegistry   registry = null;
    
    private CrudService<DFactory, String> factoryService;
    
    private DConnectionDao dConnectionDao;
    
    private OAuth2UserService oauth2UserService;
    
    protected final ArrayList<CrudListener> listeners = new ArrayList<CrudListener>();
    
    private ProviderFactory customProvider;
    
    private AbstractPlatformTransactionManager transactionManager = null;
    protected final TransactionDefinition TRANSACTION_DEFINITION = new DefaultTransactionDefinition();
    
    protected TransactionStatus getTransaction() {
        if (null == transactionManager) {
            return null;
        }
        
        
        final TransactionStatus status = transactionManager.getTransaction(TRANSACTION_DEFINITION);
        return status;
    }
    
    protected void commitTransaction(TransactionStatus status) {
        if (null != transactionManager) {
            transactionManager.commit(status);
        }
    }
    
    protected void rollbackTransaction(TransactionStatus status) {
        if (null != transactionManager && !status.isCompleted()) {
            transactionManager.rollback(status);
        }
    }
    
    /**
     * 
     * @param access_token
     * @param providerId
     * @param providerUserId
     * @param secret
     * @param expires_in
     * @return the userId associated with the Connection, null if new Connection
     */
    @Idempotent
    @Transactional
    public ResponseEntity<DConnection> registerFederated(
            String access_token, 
            String providerId,
            String providerUserId,
            String secret,
            Integer expiresInSeconds,
            String appArg0,
            String domain) {
        
        // providerUserId is optional, fetch it if necessary:
        final String realProviderUserId = getProviderUserId(access_token, providerId, appArg0);
        if (null == providerUserId) {
            providerUserId = realProviderUserId;
        }
        else if (!providerUserId.equals(realProviderUserId)) {
            throw new AuthenticationFailedException(503403, "Unauthorized federated side mismatch");
        }
        
        final ArrayList<String> expiredTokens = new ArrayList<String>();
        // load connection from db async style (likely case is new token for existing user)
        final Iterable<DConnection> conns = dConnectionDao.queryByProviderUserId(providerUserId);
        final TransactionStatus transactionStatus = getTransaction();
        
        try {
            // use the connectionFactory
            final Connection<?> connection = createConnection(access_token, secret, providerId, providerUserId, appArg0);

            UserProfile profile = null;
            try {
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
            final Date now = new Date();

            // create connection?
            if (isNewConnection) {

                // find other connections for this user, discard expired
                for (DConnection dc : conns) {
                    if (providerId.equals(dc.getProviderId())) {
                        userId = dc.getUserId();

                        // expired?
                        if (null != dc.getExpireTime() && now.after(dc.getExpireTime())) {
                            expiredTokens.add(dc.getId());
                        }
                    }
                }

                // create user?
                isNewUser = (null == userId);
                if (isNewUser && autoCreateUser && null != oauth2UserService) {
                    userId = oauth2UserService.createUser(profile.getEmail(), profile.getFirstName(), profile.getLastName(),
                            profile.getName(), providerId, providerUserId, domain);
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
                if (null != conn.getExpireTime() && now.after(conn.getExpireTime())) {
                    throw new ConflictException(503410, "Existing token expired");
                }
                userId = conn.getUserId();
            }

            // update connection values
            conn.setAppArg0(appArg0);
            if (null != oauth2UserService) {
                Object user = oauth2UserService.loadUserDetailsByUsername(null, null, null, access_token, userId);
                if (null != user) {
                    Collection<String> userRoles = oauth2UserService.getRolesFromUserDetails(user);
                    conn.setUserRoles(ConnectionServiceImpl.convertRoles(userRoles));
                }
            }
            dConnectionDao.update(conn);

            // notify listeners
            postService(null, domain, OPERATION_REGISTER_FEDERATED, conn, userId, profile);

            commitTransaction(transactionStatus);
            
            dConnectionDao.delete(null, expiredTokens);
            
            return new ResponseEntity<DConnection>(conn, 
                    isNewUser ? HttpStatus.CREATED : HttpStatus.OK);
        }
        finally {
            rollbackTransaction(transactionStatus);
        }
    }
    
    protected Connection<?> createConnection(String accessToken, String secret, 
            String providerId, String providerUserId, String appArg0)
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
        
        // salesforce needs instanceUrl
        if (PROVIDER_ID_SALESFORCE.equals(providerId) && null != appArg0) {
            SalesforceServiceProvider.setInstanceUrl(appArg0);
        }

        final ConnectionData data = new ConnectionData(providerId, providerUserId, null, null, null, accessToken, secret, null,
                System.currentTimeMillis() + 90L * 60L * 1000L);

        // use the connectionFactory
        final Connection<?> connection = cf.createConnection(data);

        return connection;
    }

    // ------------    implements ConnectionFactoryLocator --------- 
    
    public ConnectionFactory<?> createFromFactory(DFactory factory) {
        LOG.debug("creating factory for {}", factory.getId());

        ConnectionFactory<?> cf = null;

        if (null != customProvider && customProvider.supports(factory.getId())) {
            cf = customProvider.createFactory(factory.getId(), factory.getClientId(), factory.getClientSecret(),
                    factory.getBaseUrl());
        }
        else if (PROVIDER_ID_FACEBOOK.equals(factory.getId())) {
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
    
    public String getProviderUserId(String access_token, String providerId, String appArg0) {
        if (null != customProvider && customProvider.supports(providerId)) {
            return customProvider.getUserId(access_token);
        }
        else if (PROVIDER_ID_FACEBOOK.equals(providerId)) {
            FacebookTemplate template = new FacebookTemplate(access_token);
            org.springframework.social.facebook.api.UserOperations userOps = template.userOperations();
            FacebookProfile profile = userOps.getUserProfile();
            return profile.getId();
        }
        else if (PROVIDER_ID_ITEST.equals(providerId)) {
            return ITestTemplate.ITEST_PROVIDER_USER_ID;
        }
        else if (PROVIDER_ID_SALESFORCE.equals(providerId)) {
            SalesforceTemplate template = (null != appArg0) ? 
                    new SalesforceTemplate(access_token, appArg0) : new SalesforceTemplate(access_token);
            LOG.warn("get providerUserId for {}", access_token);
            org.springframework.social.salesforce.api.BasicOperations basicOps = template.basicOperations();
            SalesforceProfile profile = basicOps.getUserProfile();
            return profile.getId();
        }
        throw new IllegalArgumentException("No registered provider " + providerId);
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

    public void setFactoryService(CrudService factoryService) {
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

    public void setCustomProvider(ProviderFactory customProvider) {
        this.customProvider = customProvider;
    }

    public void setTransactionManager(AbstractPlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

}
