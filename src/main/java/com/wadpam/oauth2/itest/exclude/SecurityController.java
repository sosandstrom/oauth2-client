/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.itest.exclude;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.service.ConnectionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author sosandstrom
 */
@Controller
@RequestMapping("{domain}")
public class SecurityController {
    
    private ConnectionService connectionService;

    @RequestMapping(value = "security")
    @ResponseBody
    public String getHello() {
        return "Hello Security";
    }

    @RequestMapping(value = "security/{accessToken}")
    @ResponseBody
    public DConnection getConnection(@PathVariable String accessToken) {
        return connectionService.get(null, accessToken);
    }

    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }
    
    
}
