/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/upload/shared/Attic/I_CmsUploadConstants.java,v $
 * Date   : $Date: 2011/03/02 14:24:06 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.upload.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An interface that holds some constants for the upload dialog.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsUploadConstants extends IsSerializable {

    /** The request attribute name for the dialog mode. */
    String ATTR_DIALOG_MODE = "dialogMode";

    /** The request attribute name for the close link. */
    String ATTR_CLOSE_LINK = "closeLink";

    /** The request attribute name for the current folder. */
    String ATTR_CURRENT_FOLDER = "currentFolder";

    /** The explorer URI for the back link. */
    String EXPLORER_URI = "/system/workplace/views/explorer/explorer_files.jsp?mode=explorerview";

    /** Key for the JSON object. */
    String KEY_MESSAGE = "message";

    /** Key for the JSON object. */
    String KEY_REQUEST_SIZE = "requestsize";

    /** Key for the JSON object. */
    String KEY_STACKTRACE = "stacktrace";

    /** Key for the JSON object. */
    String KEY_SUCCESS = "success";

    /** Upload action JSP URI. */
    String UPLOAD_ACTION_JSP_URI = "/system/modules/org.opencms.ade.upload/uploadAction.jsp";

    /** The name of the form field that stores the flag that signals if the filename is URL encoded. */
    String UPLOAD_FILE_NAME_URL_ENCODED_FLAG = "upload_encoding_flag";

    /** Upload JSP URI. */
    String UPLOAD_JSP_URI = "/system/modules/org.opencms.ade.upload/upload.jsp";

    /** The name of the form field that stores the target folder for the upload. */
    String UPLOAD_TARGET_FOLDER_FIELD_NAME = "upload_target_folder";

    /** The javascript variable name for the upload target folder. */
    String VAR_TARGET_FOLDER = "targetFolder";
}
