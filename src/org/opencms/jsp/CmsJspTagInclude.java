/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagInclude.java,v $
 * Date   : $Date: 2005/10/19 09:42:54 $
 * Version: $Revision: 1.35.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.I_CmsEditorActionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Used to include another OpenCms managed resource in a JSP.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.35.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagInclude extends BodyTagSupport implements I_CmsJspTagParamParent {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 705978510743164951L;

    /** Debugging on / off. */
    private static final boolean DEBUG = false;

    /** The value of the "attribute" attribute. */
    private String m_attribute;

    /** The value of the "editable" attribute. */
    private boolean m_editable;

    /** The value of the "element" attribute. */
    private String m_element;

    /** Hashmap to save parameters to the include in. */
    private HashMap m_parameterMap;

    /** The value of the "property" attribute. */
    private String m_property;

    /** The value of the "suffix" attribute. */
    private String m_suffix;

    /** The value of the "page" attribute. */
    private String m_target;

    /**
     * Adds parameters to a parameter Map that can be used for a http request.<p>
     * 
     * @param parameters the Map to add the parameters to
     * @param name the name to add
     * @param value the value to add
     * @param overwrite if <code>true</code>, a parameter in the map will be overwritten by
     *      a parameter with the same name, otherwise the request will have multiple parameters 
     *      with the same name (which is possible in http requests)
     */
    public static void addParameter(Map parameters, String name, String value, boolean overwrite) {

        // No null values allowed in parameters
        if ((parameters == null) || (name == null) || (value == null)) {
            return;
        }

        // Check if the parameter name (key) exists
        if (parameters.containsKey(name) && (!overwrite)) {
            // Yes: Check name values if value exists, if so do nothing, else add new value
            String[] values = (String[])parameters.get(name);
            String[] newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            parameters.put(name, newValues);
        } else {
            // No: Add new parameter name / value pair
            String[] values = new String[] {value};
            parameters.put(name, values);
        }
    }

    /**
     * Include action method.<p>
     * 
     * The logic in this mehod is more complex than it should be.
     * This is because of the XMLTemplate integration, which requires some settings 
     * to the parameters understandable only to XMLTemplate gurus.
     * By putting this logic here it is not required to care about these issues
     * on JSP pages, and you end up with considerable less JSP code.
     * Also JSP developers need not to know the intrinsics of XMLTemplates this way.<p>
     * 
     * @param context the current JSP page context
     * @param target the target for the include, might be <code>null</code>
     * @param element the element to select form the target might be <code>null</code>
     * @param editable the flag to indicate if the target is editable
     * @param paramMap a map of parameters for the include, will be merged with the request 
     *      parameters, might be <code>null</code>
     * @param req the current request
     * @param res the current response
     * @throws JspException in case something goes wrong
     */
    public static void includeTagAction(
        PageContext context,
        String target,
        String element,
        boolean editable,
        Map paramMap,
        ServletRequest req,
        ServletResponse res) throws JspException {

        // the Flex controller provides access to the interal OpenCms structures
        CmsFlexController controller = CmsFlexController.getController(req);

        if (target == null) {
            // set target to default
            target = controller.getCmsObject().getRequestContext().getUri();
        }

        // each include will have it's unique map of parameters
        Map parameterMap = new HashMap();
        if (paramMap != null) {
            // add all parameters from the parent elements
            parameterMap.putAll(paramMap);
        }

        if (element != null) {
            // add template element selector for JSP templates
            addParameter(parameterMap, I_CmsResourceLoader.PARAMETER_ELEMENT, element, true);
        }

        // resolve possible relative URI
        target = CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri());

        try {
            // now resolve additional include extensions that might be required for special loader implementations
            target = OpenCms.getResourceManager().resolveIncludeExtensions(
                target,
                element,
                editable,
                paramMap,
                req,
                res);
        } catch (CmsException e) {
            // localization: we should catch a localized exception!
            controller.setThrowable(e, target);
            throw new JspException(e);
        }

        // save old parameters from request
        Map oldParameterMap = req.getParameterMap();

        try {
            // include direct edit "start" element (if enabled)
            String directEditPermissions = null;
            if (editable) {
                directEditPermissions = CmsJspTagEditable.includeDirectEditElement(
                    context,
                    I_CmsEditorActionHandler.DIRECT_EDIT_AREA_START,
                    target,
                    element,
                    null,
                    null,
                    null);
            }

            // add parameters (again) to set the correct element
            controller.getCurrentRequest().addParameterMap(parameterMap);

            // write out a C_FLEX_CACHE_DELIMITER char on the page, this is used as a parsing delimeter later
            context.getOut().print(CmsFlexResponse.FLEX_CACHE_DELIMITER);

            // add the target to the include list (the list will be initialized if it is currently empty)
            controller.getCurrentResponse().addToIncludeList(target, parameterMap);

            // now use the Flex dispatcher to include the target (this will also work for targets in the OpenCms VFS)
            controller.getCurrentRequest().getRequestDispatcher(target).include(req, res);

            // include direct edit "end" element (if required)
            if (directEditPermissions != null) {
                CmsJspTagEditable.includeDirectEditElement(
                    context,
                    I_CmsEditorActionHandler.DIRECT_EDIT_AREA_END,
                    target,
                    element,
                    null,
                    directEditPermissions,
                    null);
            }

        } catch (ServletException e) {

            Throwable t;
            if (e.getRootCause() != null) {
                t = e.getRootCause();
            } else {
                t = e;
            }
            t = controller.setThrowable(t, target);
            throw new JspException(t);
        } catch (IOException e) {

            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        } finally {

            // restore old parameter map (if required)
            if (oldParameterMap != null) {
                controller.getCurrentRequest().setParameterMap(oldParameterMap);
            }
        }
    }

    /**
     * This methods adds parameters to the current request.
     * Parameters added here will be treated like parameters from the 
     * HttpRequest on included pages.<p>
     * 
     * Remember that the value for a parameter in a HttpRequest is a 
     * String array, not just a simple String. If a parameter added here does
     * not already exist in the HttpRequest, it will be added. If a parameter 
     * exists, another value will be added to the array of values. If the 
     * value already exists for the parameter, nothing will be added, since a 
     * value can appear only once per parameter.<p>
     * 
     * @param name the name to add
     * @param value the value to add
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(String, String)
     */
    public void addParameter(String name, String value) {

        // No null values allowed in parameters
        if ((name == null) || (value == null)) {
            return;
        }

        if (DEBUG) {
            System.err.println("CmsJspIncludeTag.addParameter: param=" + name + " value=" + value);
        }

        // Check if internal map exists, create new one if not
        if (m_parameterMap == null) {
            m_parameterMap = new HashMap();
        }

        addParameter(m_parameterMap, name, value, false);
    }

    /**
     * @return <code>EVAL_PAGE</code>
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     * @throws JspException by interface default
     */
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {
            CmsObject cms = CmsFlexController.getCmsObject(req);
            String target = null;

            // Try to find out what to do
            if (m_target != null) {
                // Option 1: target is set with "page" or "file" parameter
                target = m_target + getSuffix();
            } else if (m_property != null) {
                // Option 2: target is set with "property" parameter
                if (DEBUG) {
                    System.err.println("IncludeTag: property=" + m_property);
                }
                try {
                    String prop = cms.readPropertyObject(cms.getRequestContext().getUri(), m_property, true).getValue();
                    if (DEBUG) {
                        System.err.println("IncludeTag: property=" + m_property + " is " + prop);
                    }
                    if (prop != null) {
                        target = prop + getSuffix();
                    }
                } catch (Exception e) {
                    // target will be null
                }
            } else if (m_attribute != null) {
                // Option 3: target is set in "attribute" parameter
                try {
                    String attr = (String)req.getAttribute(m_attribute);
                    if (attr != null) {
                        target = attr + getSuffix();
                    }
                } catch (Exception e) {
                    // target will be null
                }
            } else {
                // Option 4: target might be set in body
                String body = null;
                if (getBodyContent() != null) {
                    body = getBodyContent().getString();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(body)) {
                        // target IS set in body
                        target = body + getSuffix();
                    }
                    // else target is not set at all, default will be used 
                }
            }

            // now perform the include action
            includeTagAction(pageContext, target, m_element, m_editable, m_parameterMap, req, res);

            // must call release here manually to make sure m_parameterMap is cleared
            release();
        }

        return EVAL_PAGE;
    }

    /**
     * @return <code>EVAL_BODY_BUFFERED</code>
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {

        return EVAL_BODY_BUFFERED;
    }

    /**
     * Returns the attribute.<p>
     * 
     * @return the attribute
     */
    public String getAttribute() {

        return m_attribute != null ? m_attribute : "";
    }

    /**
     * Returns the editable flag.<p>
     * 
     * @return the editable flag
     */
    public String getEditable() {

        return String.valueOf(m_editable);
    }

    /**
     * Returns the element.<p>
     * 
     * @return the element
     */
    public String getElement() {

        return m_element;
    }

    /**
     * Returns the value of <code>getPage()</code>.<p>
     * 
     * @return the value of <code>getPage()</code>
     * @see #getPage()
     */
    public String getFile() {

        return getPage();
    }

    /**
     * Returns the include page target.<p>
     * 
     * @return the include page target
     */
    public String getPage() {

        return m_target != null ? m_target : "";
    }

    /**
     * Returns the property.<p>
     * 
     * @return the property
     */
    public String getProperty() {

        return m_property != null ? m_property : "";
    }

    /**
     * Returns the suffix.<p>
     * 
     * @return the suffix
     */
    public String getSuffix() {

        return m_suffix != null ? m_suffix : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        super.release();
        m_target = null;
        m_suffix = null;
        m_property = null;
        m_element = null;
        m_parameterMap = null;
        m_editable = false;
    }

    /**
     * Sets the attribute.<p>
     * 
     * @param attribute the attribute to set
     */
    public void setAttribute(String attribute) {

        if (attribute != null) {
            this.m_attribute = attribute;
        }
    }

    /**
     * Sets the editable flag.<p>
     * 
     * @param editable the flag to set
     */
    public void setEditable(String editable) {

        m_editable = Boolean.valueOf(editable).booleanValue();
    }

    /**
     * Sets the element.<p>
     * 
     * @param element the element to set
     */
    public void setElement(String element) {

        if (element != null) {
            this.m_element = element;
        }
    }

    /**
     * Sets the file, same as using <code>setPage()</code>.<p>
     * 
     * @param file the file to set
     * @see #setPage(String)
     */
    public void setFile(String file) {

        setPage(file);
    }

    /**
     * Sets the include page target.<p>
     * 
     * @param target the target to set
     */
    public void setPage(String target) {

        if (target != null) {
            m_target = target;
        }
    }

    /**
     * Sets the property.<p>
     * 
     * @param property the property to set
     */
    public void setProperty(String property) {

        if (property != null) {
            this.m_property = property;
        }
    }

    /**
     * Sets the suffix.<p>
     * 
     * @param suffix the suffix to set
     */
    public void setSuffix(String suffix) {

        if (suffix != null) {
            this.m_suffix = suffix.toLowerCase();
        }
    }
}