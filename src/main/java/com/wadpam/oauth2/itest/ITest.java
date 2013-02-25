/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.itest;

import org.springframework.social.ApiBinding;

/**
 *
 * @author sosandstrom
 */
public interface ITest extends ApiBinding{
    UserOperations userOperations();
}
