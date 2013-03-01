/**
 *
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */

/**
 * This file was automatically generated by the Mule Development Kit
 */

package org.mule.module.documentum;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.ConnectionException;
import org.mule.api.MuleException;

import com.emc.documentum.fs.services.core.SerializableException;
import com.emc.documentum.fs.services.core.acl.CoreServiceException_Exception;
import com.emc.documentum.fs.services.core.acl.ServiceException;

public class DocumentumConnectorTestDriver {
    
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String REPOSITORY = "repository";
    private static final String SERVER = "http://localhost:9080/";
    private DocumentumConnector connector;
    
    @Before
    public void init() throws ConnectionException, MuleException {
        connector = new DocumentumConnector();
        connector.connect(USER, PASSWORD, REPOSITORY, SERVER);
    }
    
    @Test
    public void testGetAcls() throws ServiceException, CoreServiceException_Exception, SerializableException {
        connector.getAcls();
    }
    
}