/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.itest;

import com.wadpam.open.user.json.JOpenUser;
import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;

/**
 *
 * @author sosandstrom
 */
public class ITestTemplate extends AbstractOAuth2ApiBinding implements ITest, UserOperations{
    public static final String ITEST_PROVIDER_USER_ID = "1000244";
    public static final String ITEST_PROVIDER_USER_ID_DUPLICATE = "2000245";
    
    private final String accessToken;

    public ITestTemplate(String accessToken) {
        super(accessToken);
        this.accessToken = accessToken;
    }
    
    @Override
    public JOpenUser getUser() {
        final JOpenUser user = new JOpenUser();
        user.setId("itest_duplicate".equals(accessToken) ? ITEST_PROVIDER_USER_ID_DUPLICATE: ITEST_PROVIDER_USER_ID);
        user.setUsername(accessToken);
        user.setDisplayName("ITest User");
        user.setEmail("test@example.com");
        return user;
    }

    @Override
    public UserOperations userOperations() {
        return this;
    }

}
