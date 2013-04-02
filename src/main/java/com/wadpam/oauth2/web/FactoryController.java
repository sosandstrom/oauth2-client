/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.web;

import com.wadpam.oauth2.domain.DFactory;
import com.wadpam.oauth2.json.JFactory;
import com.wadpam.oauth2.service.FactoryService;
import com.wadpam.open.mvc.CrudController;
import com.wadpam.open.mvc.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author sosandstrom
 */
@Controller
@RequestMapping("{domain}/_admin/factory")
public class FactoryController extends CrudController<JFactory, DFactory, String, CrudService<DFactory, String>> {

    public FactoryController() {
        super(JFactory.class);
    }
    
    @Override
    public void convertDomain(DFactory from, JFactory to) {
        convertStringEntity(from, to);
        to.setBaseUrl(from.getBaseUrl());
        to.setClientId(from.getClientId());
        to.setClientSecret(from.getClientSecret());
    }

    @Override
    public void convertJson(JFactory from, DFactory to) {
        convertJString(from, to);
        to.setBaseUrl(from.getBaseUrl());
        to.setClientId(from.getClientId());
        to.setClientSecret(from.getClientSecret());
    }

    @Autowired
    public void setFactoryService(CrudService factoryService) {
        this.service = factoryService;
    }
}
