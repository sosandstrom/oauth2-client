/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.service;

import com.wadpam.oauth2.dao.DFactoryDao;
import com.wadpam.oauth2.domain.DFactory;
import com.wadpam.open.mvc.MardaoCrudService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sosandstrom
 */
public class FactoryService extends MardaoCrudService<DFactory, String, DFactoryDao> {
    
    @Autowired
    public void setDFactoryDao(DFactoryDao dFactoryDao) {
        this.dao = dFactoryDao;
    }
}
