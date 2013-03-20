/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.oauth2.dao.DConnectionDao;
import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.open.mvc.MardaoCrudService;
import java.util.ArrayList;
import java.util.Arrays;
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
            to.append(s.trim());
            first = false;
        }
        return to.toString();
    }
    
    public static ArrayList<String> convertRoles(String from) {
        final ArrayList<String> to = new ArrayList<String>();
        if (null != from) {
            final String roles[] = from.split(ROLE_SEPARATOR);
            for (String r : roles) {
                to.add(r.trim());
            }
        }
        return to;
    }

    @Autowired
    public void setDConnectionDao(DConnectionDao dConnectionDao) {
        this.dao = dConnectionDao;
    }

}
