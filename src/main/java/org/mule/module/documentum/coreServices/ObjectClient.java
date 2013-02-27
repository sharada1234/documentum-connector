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
import java.io.IOException;
import java.util.Map;

import com.emc.documentum.fs.datamodel.core.ObjectIdentity;
import com.emc.documentum.fs.datamodel.core.acl.AclIdentity;
import com.emc.documentum.fs.datamodel.core.content.ContentTransferMode;
import com.emc.documentum.fs.services.core.SerializableException;
import com.emc.documentum.fs.services.core.acl.ServiceException;

public interface ObjectClient {
    
    public ObjectIdentity createObject(String type, String filePath, String name, String folderPath, ContentTransferMode transferMode) throws IOException, SerializableException;
    
    public ObjectIdentity createPath(String folderPath) throws SerializableException;
    
    public File getObject(ObjectIdentity objectIdentity, String outputPath, ContentTransferMode transferMode) throws IOException, SerializableException;
    
    public ObjectIdentity updateObject(ObjectIdentity objectIdentity, String type, String newContentFilePath, Map<String, String> newProperties, ObjectIdentity oldParentFolder, ObjectIdentity newParentFolder, ContentTransferMode transferMode) throws SerializableException, IOException;
    
    public ObjectIdentity deleteObject(ObjectIdentity objectIdentity) throws SerializableException;
    
    public ObjectIdentity copyObject(ObjectIdentity objectIdentity, ObjectIdentity folderIdentity) throws SerializableException;
    
    public ObjectIdentity moveObject(ObjectIdentity objectIdentity, ObjectIdentity toFolderIdentity, ObjectIdentity fromFolderIdentity) throws SerializableException;
    
    public ObjectIdentity applyAcl(ObjectIdentity objectIdentity, AclIdentity aclIdentity) throws ServiceException, SerializableException;

}