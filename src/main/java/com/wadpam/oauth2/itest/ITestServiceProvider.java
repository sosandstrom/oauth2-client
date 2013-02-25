/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.itest;

import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;

/**
 *
 * @author sosandstrom
 */
public class ITestServiceProvider extends AbstractOAuth2ServiceProvider<ITest> {

    public ITestServiceProvider() {
//        OAuth2Operations oauth2Operations;
        super(null);
    }


    @Override
    public ITest getApi(String accessToken) {
        return new ITestTemplate();
    }

}
