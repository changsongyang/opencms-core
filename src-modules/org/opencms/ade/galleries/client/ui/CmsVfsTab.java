/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsVfsTab.java,v $
 * Date   : $Date: 2011/03/02 14:24:09 $
 * Version: $Revision: 1.11 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.A_CmsTabHandler;
import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.CmsVfsTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.upload.client.ui.CmsUploadButton;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.tree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The tab widget for selecting folders from the VFS tree.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.11 $
 * 
 * @since 8.0.0
 */
public class CmsVfsTab extends A_CmsListTab {

    /** Text metrics key. */
    private static final String TM_CATEGORY_TAB = "VfsTab";

    /** TODO: set the controller! */
    protected CmsGalleryController m_controller;

    /** A map from tree items to the corresponding data beans. */
    protected IdentityHashMap<CmsLazyTreeItem, CmsVfsEntryBean> m_entryMap = new IdentityHashMap<CmsLazyTreeItem, CmsVfsEntryBean>();

    /** The tab handler. */
    protected CmsVfsTabHandler m_tabHandler;

    /** A map of tree items indexed by VFS path. */
    private Map<String, CmsLazyTreeItem> m_itemsByPath = new HashMap<String, CmsLazyTreeItem>();

    /** The search parameter panel for this tab. */
    private CmsSearchParamPanel m_paramPanel;

    /**
     * Constructor.<p>
     * 
     * @param tabHandler the tab handler 
     */
    public CmsVfsTab(CmsVfsTabHandler tabHandler) {

        super(GalleryTabId.cms_tab_vfstree);
        m_scrollList.truncate(TM_CATEGORY_TAB, CmsGalleryDialog.DIALOG_WIDTH);
        m_tabHandler = tabHandler;
        addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().listOnlyTab());
    }

    /**
     * Sets the initial folders in the VFS tab.<p>
     * 
     * @param entries the root folders to display 
     */
    public void fillInitially(List<CmsVfsEntryBean> entries) {

        clear();
        for (CmsVfsEntryBean entry : entries) {
            CmsLazyTreeItem item = createItem(entry);
            addWidgetToList(item);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanel(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public CmsSearchParamPanel getParamPanel(CmsGallerySearchBean searchObj) {

        if (m_paramPanel == null) {
            m_paramPanel = new CmsSearchParamPanel(Messages.get().key(Messages.GUI_PARAMS_LABEL_VFS_0), this);
        }
        String content = getVfsParams(searchObj.getFolders());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
            m_paramPanel.setContent(content);
            return m_paramPanel;
        }
        return null;
    }

    /**
     * Returns a user-readable string representing the selected VFS folders.<p>
     * 
     * @param selectedFolders the list of selected folders 
     * 
     * @return a user-readable string representing the selected VFS folder 
     */
    public String getVfsParams(List<String> selectedFolders) {

        if (CmsCollectionUtil.isEmptyOrNull(selectedFolders)) {
            return null;
        }
        return CmsStringUtil.listAsString(selectedFolders, ", ");
    }

    /**
     * Unchecks the checkboxes for each folder passed in the <code>folders</code> parameter.<p>
     * 
     * @param folders the folders for which the checkboxes should be unchecked 
     */
    public void uncheckFolders(List<String> folders) {

        for (String folder : folders) {
            CmsLazyTreeItem item = m_itemsByPath.get(folder);
            item.getCheckBox().setChecked(false);
        }
    }

    /**
     * Helper method for creating a VFS tree item widget from a VFS entry bean.<p>
     * 
     * @param vfsEntry the VFS entry bean 
     * 
     * @return the tree item widget
     */
    protected CmsLazyTreeItem createItem(final CmsVfsEntryBean vfsEntry) {

        CmsListInfoBean info = new CmsListInfoBean();
        info.setTitle(vfsEntry.getDisplayName());

        // info.setSubTitle("...");
        CmsListItemWidget liWidget = new CmsListItemWidget(info);
        liWidget.setIcon(CmsIconUtil.getResourceIconClasses("folder", false));
        if (vfsEntry.isEditable()) {
            CmsUploadButton uploadButton = new CmsUploadButton();
            uploadButton.setTargetFolder(vfsEntry.getSitePath());
            uploadButton.setTitle(Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_0));
            uploadButton.setText(null);
            uploadButton.setShowBorder(false);
            uploadButton.setImageClass(I_CmsImageBundle.INSTANCE.style().uploadIcon());
            liWidget.addButton(uploadButton);
        }

        final CmsCheckBox checkbox = new CmsCheckBox();
        CmsLazyTreeItem result = new CmsLazyTreeItem(checkbox, liWidget, true);
        checkbox.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                m_tabHandler.onSelectFolder(vfsEntry.getSitePath(), checkbox.isChecked());

            }
        });
        m_entryMap.put(result, vfsEntry);
        m_itemsByPath.put(vfsEntry.getSitePath(), result);
        result.setLeafStyle(false);
        return result;

    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#createScrollList()
     */
    @Override
    protected CmsList<? extends I_CmsListItem> createScrollList() {

        return new CmsLazyTree<CmsLazyTreeItem>(new A_CmsLazyOpenHandler<CmsLazyTreeItem>() {

            /**
             * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
             */
            public void load(final CmsLazyTreeItem target) {

                CmsVfsEntryBean entry = m_entryMap.get(target);
                String path = entry.getSitePath();
                AsyncCallback<List<CmsVfsEntryBean>> callback = new AsyncCallback<List<CmsVfsEntryBean>>() {

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                     */
                    public void onFailure(Throwable caught) {

                        // should never be called 

                    }

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
                     */
                    public void onSuccess(List<CmsVfsEntryBean> result) {

                        for (CmsVfsEntryBean childEntry : result) {
                            CmsLazyTreeItem item = createItem(childEntry);
                            target.addChild(item);
                        }
                        target.onFinishLoading();
                        // 
                    }
                };

                m_tabHandler.getSubFolders(path, callback);

            }
        });
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected List<CmsPair<String, String>> getSortList() {

        return null;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    protected A_CmsTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * Clears the contents of the tab and resets the mapping from tree items to VFS beans.<p>
     */
    void clear() {

        clearList();
        m_entryMap = new IdentityHashMap<CmsLazyTreeItem, CmsVfsEntryBean>();

    }

}
