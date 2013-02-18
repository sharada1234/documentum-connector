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

package org.mule.module.documentum.coreServices;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;

import com.emc.documentum.fs.datamodel.core.CheckoutInfo;
import com.emc.documentum.fs.datamodel.core.DataObject;
import com.emc.documentum.fs.datamodel.core.DataPackage;
import com.emc.documentum.fs.datamodel.core.ObjectIdentity;
import com.emc.documentum.fs.datamodel.core.ObjectIdentitySet;
import com.emc.documentum.fs.datamodel.core.OperationOptions;
import com.emc.documentum.fs.datamodel.core.VersionInfo;
import com.emc.documentum.fs.datamodel.core.VersionStrategy;
import com.emc.documentum.fs.datamodel.core.content.ContentTransferMode;
import com.emc.documentum.fs.datamodel.core.context.ServiceContext;
import com.emc.documentum.fs.datamodel.core.profiles.ContentProfile;
import com.emc.documentum.fs.datamodel.core.profiles.FormatFilter;
import com.emc.documentum.fs.datamodel.core.profiles.PageFilter;
import com.emc.documentum.fs.datamodel.core.profiles.PageModifierFilter;
import com.emc.documentum.fs.services.core.SerializableException;
import com.emc.documentum.fs.services.core.VersionControlService;
import com.emc.documentum.fs.services.core.VersionControlServicePort;

import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;

public class VersionControlUtil extends Util {
    
    private VersionControlServicePort port;
    private ContentTransferMode transferMode;

    public VersionControlUtil(ServiceContext serviceContext, ContentTransferMode transferMode, String target) throws MalformedURLException, JAXBException {
        this.transferMode = transferMode;
        setVersionControlPort(serviceContext, target);
    }
    
    public CheckoutInfo getCheckoutInfo(ObjectIdentity objIdentity) throws SerializableException {
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(objIdentity);
        
        List<CheckoutInfo> objList;
        OperationOptions operationOptions = null;
        
        port.checkout(objIdSet, operationOptions);
        objList = port.getCheckoutInfo(objIdSet);
        CheckoutInfo checkoutInfo = objList.get(0);
        
        port.cancelCheckout(objIdSet);
        return checkoutInfo;
    }
    
    public DataPackage checkout(ObjectIdentity objIdentity) throws SerializableException {
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(objIdentity);
        
        OperationOptions operationOptions = null;
        DataPackage resultDp = port.checkout(objIdSet, operationOptions);
        
        port.cancelCheckout(objIdSet);
        return resultDp;
    }
    
    public DataPackage checkin(ObjectIdentity objIdentity, String newContentPath, String format, VersionStrategy versionStrategy, List<String> labels, boolean isRetainLock) throws SerializableException {
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(objIdentity);
        
        OperationOptions operationOptions = new OperationOptions();
        ContentProfile contentProfile = new ContentProfile();
        contentProfile.setFormatFilter(FormatFilter.ANY);
        contentProfile.setPageFilter(PageFilter.ANY);
        contentProfile.setPageNumber(-1);
        contentProfile.setPageModifierFilter(PageModifierFilter.ANY);
        operationOptions.getProfiles().add(contentProfile);
        
        DataPackage checkinPackage = port.checkout(objIdSet, operationOptions);
        DataObject checkinObj = checkinPackage.getDataObjects().get(0);
        checkinObj.getContents().clear();
        
        File file = new File(newContentPath);
        byte[] byteArray = new byte[(int) file.length()];
        
        if (transferMode == ContentTransferMode.MTOM) {
            checkinObj.getContents().add(getDataHandlerContent(byteArray, format));
        }
        else if (transferMode == ContentTransferMode.BASE_64) {
            checkinObj.getContents().add(getBinaryContent(byteArray, format));
        }
        
        DataPackage resultDp = port.checkin(checkinPackage, versionStrategy, isRetainLock, labels, operationOptions);
        return resultDp;
    }
    
    public boolean cancelCheckout(ObjectIdentity objIdentity) {
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(objIdentity);
        try {
            port.cancelCheckout(objIdSet);
            return true;
        } catch (SerializableException e) {
            return false;
        }
    }
    
    public boolean deleteVersion(ObjectIdentity objIdentity) {
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(objIdentity);
        try {
            port.deleteVersion(objIdSet);
            return true;
        } catch (SerializableException e) {
            return false;
        }
    }
    
    public boolean deleteAllVersions(ObjectIdentity objIdentity) {
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(objIdentity);
        try {
            port.deleteAllVersions(objIdSet);
            return true;
        } catch (SerializableException e) {
            return false;
        }
    }
    
    public DataObject getCurrent(ObjectIdentity objIdentity) throws SerializableException {
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(objIdentity);
        OperationOptions operationOptions = null;
        DataPackage resultDataPackage = port.getCurrent(objIdSet, operationOptions);
        return resultDataPackage.getDataObjects().get(0);
    }
    
    public VersionInfo getVersionInfo(ObjectIdentity objIdentity) throws SerializableException {
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(objIdentity);
        return port.getVersionInfo(objIdSet).get(0);
    }
    
    private void setVersionControlPort(ServiceContext context, String target) throws MalformedURLException, JAXBException {
        String versionControlServiceURL = target + "core/VersionControlService";
        QName qName = new QName("http://core.services.fs.documentum.emc.com/", "VersionControlService");
        VersionControlService versionControlService = new VersionControlService(new URL(versionControlServiceURL), qName);
        
        if (transferMode == ContentTransferMode.MTOM) {
            port = versionControlService.getVersionControlServicePort(new MTOMFeature());
        }
        else {
            port = versionControlService.getVersionControlServicePort();
        }
        
        List<Header> headers = new ArrayList<Header>();
        Header header = new Header(qName, context, new JAXBDataBinding(ServiceContext.class));
        headers.add(header);
        ((BindingProvider)port).getRequestContext().put(Header.HEADER_LIST, headers);
    }

}
