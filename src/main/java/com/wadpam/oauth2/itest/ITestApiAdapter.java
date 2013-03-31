/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.itest;

import com.wadpam.open.user.json.JOpenUser;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;

/**
 *
 * @author sosandstrom
 */
public class ITestApiAdapter implements ApiAdapter<ITest> {

    public ITestApiAdapter() {
    }

    @Override
    public boolean test(ITest a) {
        return true;
    }

    @Override
    public void setConnectionValues(ITest a, ConnectionValues cv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UserProfile fetchUserProfile(ITest a) {
        JOpenUser user = a.userOperations().getUser();
        return new UserProfileBuilder().setName(user.getDisplayName())
                .setUsername(user.getId())
                .setEmail(user.getEmail())
                .build();
    }

    @Override
    public void updateStatus(ITest a, String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
