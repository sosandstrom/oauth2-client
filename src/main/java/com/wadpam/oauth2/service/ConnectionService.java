/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.open.mvc.CrudService;

/**
 *
 * @author sosandstrom
 */
public interface ConnectionService extends CrudService<DConnection, String> {
    final String ROLE_SEPARATOR = ",";
    
    Iterable<DConnection> queryByProviderIdProviderUserId(String providerId, String providerUserId);

    Iterable<DConnection> queryByAppArg0(String appArg0);
    
}
