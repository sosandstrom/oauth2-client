/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.web;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.json.JConnection;
import com.wadpam.oauth2.service.ConnectionService;
import com.wadpam.open.mvc.CrudController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author sosandstrom
 */
@Controller
@RequestMapping("{domain}/_admin/connection")
public class ConnectionController extends CrudController<JConnection, DConnection, String, ConnectionService> {

    public ConnectionController() {
        super(JConnection.class);
    }
    
    @Override
    public void convertDomain(DConnection from, JConnection to) {
        convertStringEntity(from, to);
        to.setDisplayName(from.getDisplayName());
        to.setExpireTime(toLong(from.getExpireTime()));
        to.setImageUrl(from.getImageUrl());
        to.setProfileUrl(from.getProfileUrl());
        to.setProviderId(from.getProviderId());
        to.setProviderUserId(from.getProviderUserId());
        to.setRefreshToken(from.getRefreshToken());
        to.setSecret(from.getSecret());
        to.setUserId(from.getUserId());
    }

    @Override
    public void convertJson(JConnection from, DConnection to) {
        convertJString(from, to);
        to.setDisplayName(from.getDisplayName());
        to.setExpireTime(toDate(from.getExpireTime()));
        to.setImageUrl(from.getImageUrl());
        to.setProfileUrl(from.getProfileUrl());
        to.setProviderId(from.getProviderId());
        to.setProviderUserId(from.getProviderUserId());
        to.setRefreshToken(from.getRefreshToken());
        to.setSecret(from.getSecret());
        to.setUserId(from.getUserId());
    }

    @Autowired
    public void setConnectionService(ConnectionService connectionService) {
        this.service = connectionService;
    }
}
