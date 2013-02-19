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

import com.emc.documentum.fs.datamodel.core.*;
import com.emc.documentum.fs.datamodel.core.profiles.ContentTransferProfile;
import com.emc.documentum.fs.datamodel.core.profiles.ContentProfile;
import com.emc.documentum.fs.datamodel.core.profiles.DeleteProfile;
import com.emc.documentum.fs.datamodel.core.profiles.FormatFilter;
import com.emc.documentum.fs.datamodel.core.profiles.PropertyFilterMode;
import com.emc.documentum.fs.datamodel.core.profiles.PropertyProfile;
import com.emc.documentum.fs.datamodel.core.content.*;
import com.emc.documentum.fs.datamodel.core.properties.PropertySet;
import com.emc.documentum.fs.datamodel.core.properties.StringProperty;
import com.emc.documentum.fs.datamodel.core.context.RepositoryIdentity;
import com.emc.documentum.fs.datamodel.core.context.ServiceContext;
import com.emc.documentum.fs.services.core.ObjectService;
import com.emc.documentum.fs.services.core.ObjectServicePort;
import com.emc.documentum.fs.services.core.SerializableException;

import javax.xml.bind.JAXBException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.MTOMFeature;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ObjectUtil extends Util {
    
    private String repositoryName;
    private ObjectServicePort port;
    private ContentTransferMode transferMode;

    public ObjectUtil(ServiceContext serviceContext, ContentTransferMode transferMode, String target) throws MalformedURLException, JAXBException {
        this.transferMode = transferMode;
        setObjectServicePort(serviceContext, target);
        this.repositoryName = ((RepositoryIdentity) (serviceContext.getIdentities().get(0))).getRepositoryName();
    }
        
    public ObjectIdentity createObject(String type, String filePath, String name, String folderPath) throws IOException, SerializableException {
        
        byte[] byteArray = null;
        Map<String, String> properties = new HashMap<String, String>();
        
        ObjectIdentity objIdentity = createObjectIdentity();
        DataObject dataObject = createDataObject(objIdentity, type);
        DataPackage dataPackage = createDataPackage(dataObject);
        
        if (type.equals("dm_document")) {
            File file = new File(filePath);
            byteArray = new byte[(int) file.length()];
            
            properties.put("object_name", file.getName());
            
            if (transferMode == ContentTransferMode.MTOM) {
                dataObject.getContents().add(getDataHandlerContent(byteArray));
            }
            else if (transferMode == ContentTransferMode.BASE_64) {
                dataObject.getContents().add(getBinaryContent(byteArray));
            }
        }
        else { 
            properties.put("object_name", name);
        }
        
        addProperties(dataObject, properties);

        ObjectPath objectPath = createObjectPath(folderPath);
        
        ObjectIdentity folderIdentity = createObjectIdentity();
        folderIdentity.setObjectPath(objectPath);
        folderIdentity.setValueType(ObjectIdentityType.OBJECT_PATH);
        
        ReferenceRelationship folderRelationship = createRelationship(folderIdentity);
        dataObject.getRelationships().add(folderRelationship);
        
        dataPackage = port.create(dataPackage, null);
        
        return dataPackage.getDataObjects().get(0).getIdentity();
        
    }
    
    public ObjectIdentity createPath(String folderPath) throws SerializableException {
        ObjectPath objectPath = createObjectPath(folderPath);
        
        return port.createPath(objectPath, repositoryName);
    }
    
    public File getObject(ObjectIdentity objectIdentity, String outputPath) throws IOException, SerializableException {
        File outputFile = new File(outputPath);
        
        ObjectIdentitySet objectIdentitySet = new ObjectIdentitySet();
        objectIdentitySet.getIdentities().add(objectIdentity);

        ContentTransferProfile contentTransferProfile = new ContentTransferProfile();
        contentTransferProfile.setTransferMode(transferMode);

        ContentProfile contentProfile = new ContentProfile();
        contentProfile.setFormatFilter(FormatFilter.ANY);

        OperationOptions operationOptions = new OperationOptions();
        operationOptions.getProfiles().add(contentTransferProfile);
        operationOptions.getProfiles().add(contentProfile);

        DataPackage dp = port.get(objectIdentitySet, operationOptions);

        Content content = dp.getDataObjects().get(0).getContents().get(0);
        OutputStream os = new FileOutputStream(outputFile);
        
        if (content instanceof UrlContent) {
            UrlContent urlContent = (UrlContent) content;
            downloadContent(urlContent.getUrl(), os);
        }
        else if (content instanceof BinaryContent) {
            BinaryContent binaryContent = (BinaryContent) content;
            os.write(binaryContent.getValue());
        }
        else if (content instanceof DataHandlerContent) {
            DataHandlerContent dataHandlerContent = (DataHandlerContent) content;
            InputStream inputStream = dataHandlerContent.getValue().getInputStream();
            if (inputStream != null) {
                int byteRead;
                while ((byteRead = inputStream.read()) != -1) {
                    os.write(byteRead);
                }
                inputStream.close();
            }
        }
        os.close();
        return outputFile;
    }
    
    public DataPackage updateObject(ObjectIdentity objectIdentity, String type, String newContentFilePath, 
            Map<String, String> newProperties, ObjectIdentity oldParentFolder, ObjectIdentity newParentFolder) throws SerializableException {
        
        byte[] byteArray = null;
        
        DataObject dataObject = createDataObject(objectIdentity, type);
        DataPackage dataPackage = createDataPackage(dataObject);
        
        PropertyProfile propertyProfile = new PropertyProfile();
        propertyProfile.setFilterMode(PropertyFilterMode.ALL_NON_SYSTEM);
        OperationOptions operationOptions = new OperationOptions();
        operationOptions.getProfiles().add(propertyProfile);
        
        if (newContentFilePath != null) {
            File file = new File(newContentFilePath);
            byteArray = new byte[(int) file.length()];
            newProperties.put("object_name", file.getName());
            
            if (transferMode == ContentTransferMode.MTOM) {
                dataObject.getContents().add(getDataHandlerContent(byteArray));
            }
            else if (transferMode == ContentTransferMode.BASE_64) {
                dataObject.getContents().add(getBinaryContent(byteArray));
            }
        }
        
        if (newProperties != null) {
            addProperties(dataObject, newProperties);
        }

        if (oldParentFolder != null && newParentFolder != null) {
            ReferenceRelationship removeRelationship = createRelationship(oldParentFolder);
            dataObject.getRelationships().add(removeRelationship);
            removeRelationship.setIntentModifier(RelationshipIntentModifier.REMOVE);
    
            ReferenceRelationship addRelationship = createRelationship(newParentFolder);
            dataObject.getRelationships().add(addRelationship);
        }
        
        return port.update(dataPackage, operationOptions);
    }
    
    public boolean deleteObject(String path) {
        ObjectPath docPath = createObjectPath(path);
        
        ObjectIdentity objIdentity = createObjectIdentity();
        objIdentity.setObjectPath(docPath);
        objIdentity.setValueType(ObjectIdentityType.OBJECT_PATH);
        
        ObjectIdentitySet objectIdSet = new ObjectIdentitySet();
        objectIdSet.getIdentities().add(objIdentity);
        
        DeleteProfile deleteProfile = new DeleteProfile();
        deleteProfile.setIsDeepDeleteFolders(true);
        deleteProfile.setIsDeepDeleteChildrenInFolders(true);
        
        OperationOptions operationOptions = new OperationOptions();
        operationOptions.getProfiles().add(deleteProfile);
        
        try {
            port.delete(objectIdSet, operationOptions);
            return true;
        } catch (SerializableException e) {
            return false;
        }
    }
    
    public DataPackage copyObject(String sourceObjectPathString, String targetLocPathString) throws SerializableException {
        ObjectPath objPath = createObjectPath(sourceObjectPathString);
        
        ObjectIdentity docToCopy = createObjectIdentity();
        docToCopy.setObjectPath(objPath);
        docToCopy.setValueType(ObjectIdentityType.OBJECT_PATH);

        ObjectPath folderPath = createObjectPath(targetLocPathString);

        ObjectIdentity toFolderIdentity = createObjectIdentity();
        toFolderIdentity.setObjectPath(folderPath);
        toFolderIdentity.setValueType(ObjectIdentityType.OBJECT_PATH);
        
        ObjectLocation toLocation = new ObjectLocation();
        toLocation.setIdentity(toFolderIdentity);
        
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(docToCopy);
        
        OperationOptions operationOptions = null;
        
        return port.copy(objIdSet, toLocation, new DataPackage(), operationOptions);
    }
    
    public DataPackage moveObject(String sourceObjectPathString, String targetLocPathString, String sourceLocPathString) throws SerializableException {
        ObjectPath objPath = createObjectPath(sourceObjectPathString);
        ObjectIdentity docToCopy = createObjectIdentity();
        docToCopy.setObjectPath(objPath);
        docToCopy.setValueType(ObjectIdentityType.OBJECT_PATH);

        ObjectPath fromFolderPath = createObjectPath(sourceLocPathString);
        ObjectIdentity fromFolderIdentity = createObjectIdentity();
        fromFolderIdentity.setObjectPath(fromFolderPath);
        fromFolderIdentity.setValueType(ObjectIdentityType.OBJECT_PATH);
        ObjectLocation fromLocation = new ObjectLocation();
        fromLocation.setIdentity(fromFolderIdentity);

        ObjectPath folderPath = createObjectPath(targetLocPathString);
        ObjectIdentity toFolderIdentity = createObjectIdentity();
        toFolderIdentity.setObjectPath(folderPath);
        toFolderIdentity.setValueType(ObjectIdentityType.OBJECT_PATH);
        ObjectLocation toLocation = new ObjectLocation();
        toLocation.setIdentity(toFolderIdentity);
        
        ObjectIdentitySet objIdSet = new ObjectIdentitySet();
        objIdSet.getIdentities().add(docToCopy);

        return port.move(objIdSet, fromLocation, toLocation, new DataPackage(), null);
    }
    
    private ReferenceRelationship createRelationship(ObjectIdentity folderIdentity) {        
        ReferenceRelationship folderRelationship = new ReferenceRelationship();
        folderRelationship.setName("folder");
        folderRelationship.setTarget(folderIdentity);
        folderRelationship.setTargetRole("parent");
        
        return folderRelationship;
    }
    
    private DataObject createDataObject(ObjectIdentity objIdentity, String type) {        
        DataObject dataObject = new DataObject();
        dataObject.setIdentity(objIdentity);
        dataObject.setType(type);
        
        return dataObject;
    }
    
    private void addProperties(DataObject dataObject, Map<String, String> properties) {
        PropertySet propertySet = new PropertySet();
        for(Entry<String, String> myEntry: properties.entrySet()) {
            StringProperty objNameProperty = new StringProperty();
            objNameProperty.setName(myEntry.getKey());
            objNameProperty.setValue(myEntry.getValue());
            propertySet.getProperties().add(objNameProperty);
        }
        dataObject.setProperties(propertySet);
    }
    
    private ObjectIdentity createObjectIdentity() {
        ObjectIdentity objIdentity = new ObjectIdentity();
        objIdentity.setRepositoryName(repositoryName);
        
        return objIdentity;
    }
    
    private DataPackage createDataPackage(DataObject dataObject) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.getDataObjects().add(dataObject);

        return dataPackage;
    }
    
    private ObjectPath createObjectPath(String folderPath) {
        ObjectPath objectPath = new ObjectPath();
        objectPath.setPath(folderPath);
        
        return objectPath;
    }

    private void downloadContent(String url, OutputStream os) throws IOException {
        InputStream inputStream;
        inputStream = new BufferedInputStream(new URL(url).openConnection().getInputStream());

        int bytesRead;
        byte[] buffer = new byte[16384];
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            os.write(buffer, 0, bytesRead);
        }
    }
    
    private void setObjectServicePort(final ServiceContext context, String target) throws MalformedURLException, JAXBException {
        ObjectService objectService = new ObjectService();
        objectService.setHandlerResolver(new HandlerResolver() {
                    @SuppressWarnings("rawtypes")
                    public List<Handler> getHandlerChain(PortInfo info) {
                        List<Handler> handlerList = new ArrayList<Handler>();
                        Handler handler = new DfsSoapHeaderHandler(context);
                        handlerList.add(handler);
                        return handlerList;
                    }
        });
        
//        String objectServiceURL = target + "core/ObjectService?wsdl";
//        QName qName = new QName("http://core.services.fs.documentum.emc.com/", "ObjectService");
//        ObjectService objectService = new ObjectService(new URL(objectServiceURL), qName);
        
        if (transferMode == ContentTransferMode.MTOM) {
            port = objectService.getObjectServicePort(new MTOMFeature());
        }
        else {
            port = objectService.getObjectServicePort();
        }
        
//        List<Header> headers = new ArrayList<Header>();
//        Header header = new Header(qName, context, new JAXBDataBinding(ServiceContext.class));
//        headers.add(header);
//        ((BindingProvider)port).getRequestContext().put(Header.HEADER_LIST, headers);
    }
    
}