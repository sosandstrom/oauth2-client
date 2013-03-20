/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.open.security.SecurityDetailsService;

/**
 *
 * @author sosandstrom
 */
public interface OAuth2UserService extends SecurityDetailsService {

    /**
     * Creates a new user based on Connection details
     * @param email
     * @param firstName
     * @param lastName
     * @param name
     * @param providerId 
     * @param providerUserId
     * @return the ID for the created user
     */
    String createUser(String email, String firstName, String lastName, 
            String name, String providerId, String providerUserId, String domain);
}
