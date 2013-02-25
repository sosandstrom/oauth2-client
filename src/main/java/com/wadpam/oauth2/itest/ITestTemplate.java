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

    @Override
    public JOpenUser getUser() {
        final JOpenUser user = new JOpenUser();
        user.setId(ITestApiAdapter.ITEST_PROVIDER_USER_ID);
        user.setUsername(ITestApiAdapter.ITEST_PROVIDER_USER_ID);
        user.setDisplayName("ITest User");
        user.setEmail("test@example.com");
        return user;
    }

    @Override
    public UserOperations userOperations() {
        return this;
    }

}
