/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
/**
 * This file was automatically generated by the Mule Development Kit
 */

package org.mule.modules.documentum;

import java.security.SecureRandom;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import com.emc.documentum.fs.datamodel.core.context.ServiceContext;

public class HeaderHandler implements SOAPHandler<SOAPMessageContext> {

    ServiceContext context;

    public HeaderHandler(ServiceContext context) {
        this.context = context;
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        doIt(smc);
        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    public void close(MessageContext messageContext) {
    }

    private void doIt(SOAPMessageContext smc) {

        Boolean outboundProperty = (Boolean) false;
        outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundProperty) {
            SOAPMessage message = smc.getMessage();

            try {
                SOAPPart part = message.getSOAPPart();
                SOAPEnvelope envelope = part.getEnvelope();
                SOAPHeader soapHeader = envelope.addHeader();

                SOAPElement sc = soapHeader.addChildElement("ServiceContext", "", "http://context.core.datamodel.fs.documentum.emc.com/");
                sc.addNamespaceDeclaration("", "http://context.core.datamodel.fs.documentum.emc.com/");

                SecureRandom secureRandom = new SecureRandom();
                int seedByteCount = 20;
                byte seed[] = secureRandom.generateSeed(seedByteCount);
                secureRandom.setSeed(seed);
                String random = String.valueOf(secureRandom.nextLong());

                sc.addAttribute(new QName("token"), (new StringBuilder("temporary/127.0.0.1-")).append(String.valueOf(System.currentTimeMillis())).append("-").append(random)
                        .toString());

                SOAPElement identity = sc.addChildElement("Identities", "", "http://context.core.datamodel.fs.documentum.emc.com/");
                identity.addAttribute(new QName("xsi:type"), "RepositoryIdentity");
                identity.addAttribute(new QName("password"), Utils.getPassword(context));
                identity.addAttribute(new QName("repositoryName"), Utils.getRepositoryName(context));
                identity.addAttribute(new QName("userName"), Utils.getUserName(context));
                identity.addAttribute(new QName("xmlns:xsi"), "http://www.w3.org/2001/XMLSchema-instance");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}