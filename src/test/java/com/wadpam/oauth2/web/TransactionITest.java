package com.wadpam.oauth2.web;

import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.itest.ITestTemplate;
import com.wadpam.oauth2.json.JConnection;
import com.wadpam.oauth2.service.OAuth2Service;
import com.wadpam.open.domain.DomainHelper;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Integration test for the monitor controller.
 * @author sosandtrom
 */
public class TransactionITest {
    static final Logger LOG = LoggerFactory.getLogger(TransactionITest.class);

    static final String                  BASE_URL       = "http://localhost:8345/api/itest/";

    RestTemplate                         template;
    public TransactionITest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        template = new RestTemplate();
        LOG.info("---------------------- setUp() ------------------------------");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testRegisterFederatedTransaction() {
        LOG.info("+ testRegisterFederatedTransaction");
        final String ACCESS_TOKEN = "itest_duplicate";
        
         LinkedMultiValueMap<String,String> request = new LinkedMultiValueMap<String,String>();
        
        request.set("access_token", ACCESS_TOKEN);
        request.set("providerId", OAuth2Service.PROVIDER_ID_ITEST);
        request.set("providerUserId", ITestTemplate.ITEST_PROVIDER_USER_ID);
        request.set("expires_in", "60");
        request.set("appArg0", "appArg0");
        
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", DomainHelper.J_BASIC_ITEST);
        HttpEntity requestEntity = new HttpEntity(request, headers);
        
        try {
            ResponseEntity<JConnection> entity = template.exchange(BASE_URL + "federated/v11", 
                HttpMethod.POST,
                requestEntity,
                JConnection.class);
            fail("Expected 409 Conflict");
        }
        catch (HttpClientErrorException expected) {
            assertEquals("409 Conflict", expected.getMessage());
        }
        
        requestEntity = new HttpEntity(headers);
        ResponseEntity<DConnection> conn = template.exchange(BASE_URL + "security/{access_token}", 
                HttpMethod.GET,
                requestEntity,
                DConnection.class, ACCESS_TOKEN);
        assertNull("Duplicate DConnection", conn.getBody());
    }

}
