/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.oauth2.dao.DConnectionDao;
import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.open.mvc.MardaoCrudService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sosandstrom
 */
public class ConnectionService extends MardaoCrudService<DConnection, Long, DConnectionDao> {

    @Autowired
    public void setDConnectionDao(DConnectionDao dConnectionDao) {
        this.dao = dConnectionDao;
    }
}
