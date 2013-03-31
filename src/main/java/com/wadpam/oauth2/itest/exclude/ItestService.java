/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.itest.exclude;

import com.wadpam.oauth2.itest.ITestTemplate;
import com.wadpam.oauth2.service.OAuth2Service;
import com.wadpam.open.domain.DAppDomain;
import com.wadpam.open.exceptions.ConflictException;
import com.wadpam.open.mvc.CrudController;
import com.wadpam.open.mvc.CrudListenerAdapter;
import com.wadpam.open.mvc.CrudService;
import com.wadpam.open.service.DomainService;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.UserProfile;

/**
 *
 * @author sosandstrom
 */
public class ItestService extends CrudListenerAdapter {
    
    static final Logger LOG = LoggerFactory.getLogger(ItestService.class);
    
    @Autowired
    private DomainService domainService;
    
    private OAuth2Service oauth2Service;
    
    public void init() {
        oauth2Service.addListener(this);
        
        DAppDomain itest = new DAppDomain();
        itest.setId("itest");
        itest.setUsername("itest");
        itest.setPassword("itest");
        domainService.create(itest);
    }

    @Override
    public void postService(CrudController controller, CrudService service, HttpServletRequest request, String namespace, int operation, Object json, Serializable id, Object serviceResponse) {
        if (OAuth2Service.OPERATION_REGISTER_FEDERATED == operation) {
            UserProfile profile = (UserProfile) serviceResponse;
            LOG.info("postService profile.email={}, username={}", profile.getEmail(), profile.getUsername());
            if (ITestTemplate.ITEST_PROVIDER_USER_ID_DUPLICATE.equals(profile.getUsername())) {
                throw new ConflictException(409, "User email conflict");
            }
        }
    }
    
    public void setDomainService(DomainService domainService) {
        this.domainService = domainService;
    }

    public void setOauth2Service(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
    }
    
}
