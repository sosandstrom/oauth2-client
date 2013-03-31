/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.oauth2.itest.exclude;

import com.wadpam.oauth2.dao.DConnectionDao;
import com.wadpam.oauth2.domain.DConnection;
import com.wadpam.oauth2.service.ConnectionService;
import java.util.Date;

/**
 *
 * @author sosandstrom
 */
public class MockConnectionService extends ConnectionService {

            @Override
            public DConnection get(String parentKeyString, String id) {
                LOG.info("getting DConnection for {}", id);
                if ("itest".equals(id)) {
                    DConnection conn = new DConnection();
                    conn.setId(id);
                    conn.setExpireTime(new Date(System.currentTimeMillis()+1000L));
                    conn.setUserId(id);
//                    conn.setUserRoles(" ROLE_USER , ROLE_ITEST");
                    
                    return conn;
                }
//                if ("itest_duplicate".equals(id)) {
//                    DConnection conn = new DConnection();
//                    conn.setId(id);
//                    conn.setExpireTime(new Date(System.currentTimeMillis()+1000L));
//                    
//                    return conn;
//                }
                return null;
            }

            @Override
            public void setDConnectionDao(DConnectionDao dConnectionDao) {
            }

            @Override
            public void setDao(DConnectionDao dao) {
            }

}
