/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.open.mvc.CrudListener;
import com.wadpam.open.transaction.Idempotent;
import org.springframework.http.ResponseEntity;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author sosandstrom
 */
public interface OAuth2Service extends ConnectionFactoryLocator {
    
    public static final String PROVIDER_ID_ITEST = "itest";
    public static final String PROVIDER_ID_FACEBOOK = "facebook";
//    public static final String PROVIDER_ID_GEKKO = "gekko";
    public static final String PROVIDER_ID_GOOGLE = "google";
    public static final String PROVIDER_ID_SALESFORCE = "salesforce";
    public static final String PROVIDER_ID_TWITTER = "twitter";
    
    public static final int OPERATION_REGISTER_FEDERATED = 1001;
    
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
            String domain);    
    
    public void addListener(CrudListener listener);
    public void removeListener(CrudListener listener);
    
    String getProviderUserId(String access_token, String providerId, String appArg0);
    
    void setCustomProvider(ProviderFactory customProvider);
}
