/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.oauth2.dao.DConnectionDao;
import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.open.mvc.MardaoCrudService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sosandstrom
 */
public class ConnectionService extends MardaoCrudService<DConnection, String, DConnectionDao> {
    public static final String ROLE_SEPARATOR = ",";
    
    public Iterable<DConnection> queryByProviderIdProviderUserId(String providerId, String providerUserId) {
        return dao.queryByProviderIdProviderUserId(providerId, providerUserId);
    }
    
    public static String convertRoles(Iterable<String> from) {
        if (null == from) {
            return null;
        }
        final StringBuffer to = new StringBuffer();
        boolean first = true;
        for (String s : from) {
            if (!first) {
                to.append(ROLE_SEPARATOR);
            }
            to.append(s);
            first = false;
        }
        return to.toString();
    }
    
    public static List<String> convertRoles(String from) {
        if (null == from) {
            return Collections.EMPTY_LIST;
        }
        String roles[] = from.split(ROLE_SEPARATOR);
        return Arrays.asList(roles);
    }

    @Autowired
    public void setDConnectionDao(DConnectionDao dConnectionDao) {
        this.dao = dConnectionDao;
    }

}
