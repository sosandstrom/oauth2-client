/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.itest;

import com.google.appengine.api.NamespaceManager;
import com.wadpam.oauth2.domain.DFactory;
import com.wadpam.oauth2.service.OAuth2Service;
import com.wadpam.open.mvc.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;

/**
 *
 * @author sosandstrom
 */
public class IntegrationTestConnectionFactory extends OAuth2ConnectionFactory<ITest> {
    
    private CrudService<DFactory,String> factoryService;

    public IntegrationTestConnectionFactory() {
        super(OAuth2Service.PROVIDER_ID_ITEST, new ITestServiceProvider(), new ITestApiAdapter());
    }

    public void init() {
        final String current = NamespaceManager.get();
        try {
            NamespaceManager.set("itest");
            DFactory itestFactory = new DFactory();
            itestFactory.setId(OAuth2Service.PROVIDER_ID_ITEST);

            factoryService.create(itestFactory);
        }
        finally {
            NamespaceManager.set(current);
        }
    }

    @Autowired
    public void setFactoryService(CrudService factoryService) {
        this.factoryService = factoryService;
    }
    
    
}
