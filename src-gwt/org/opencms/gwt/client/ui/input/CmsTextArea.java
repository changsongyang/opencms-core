/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Basic text area widget for forms.<p>
 * 
 * @since 8.0.0
 * 
 */
public class CmsTextArea extends Composite implements I_CmsFormWidget, I_CmsHasInit {

    /** The widget type identifier for this widget. */
    private static final String WIDGET_TYPE = "textarea";

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The root panel containing the other components of this widget. */
    Panel m_panel = new FlowPanel();

    /** The internal text area widget used by this widget. */
    TextArea m_textArea = new TextArea();

    /** The container for the text area. */
    CmsScrollPanel m_textAreaContainer = GWT.create(CmsScrollPanel.class);

    /** The default rows set. */
    int m_defaultRows;

    /** The faid panel. */
    Panel m_faidpanel = new SimplePanel();

    /**
     * Text area widgets for ADE forms.<p>
     */
    public CmsTextArea() {

        super();
        initWidget(m_panel);
        m_panel.add(m_textAreaContainer);
        m_textAreaContainer.setResizable(true);
        m_textAreaContainer.getElement().getStyle().setHeight(m_textArea.getOffsetHeight(), Unit.PX);
        m_faidpanel.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().inputTextAreaFaider());
        m_textAreaContainer.add(m_textArea);
        m_faidpanel.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_textArea.setFocus(true);
            }
        }, ClickEvent.getType());
        // TODO: add pastehandler!

        m_textArea.addKeyUpHandler(new KeyUpHandler() {

            public void onKeyUp(KeyUpEvent event) {

                String string = m_textArea.getText();
                String searchString = "\n";
                int occurences = 0;
                if (0 != searchString.length()) {
                    for (int index = string.indexOf(searchString, 0); index != -1; index = string.indexOf(
                        searchString,
                        index + 1)) {
                        occurences++;
                    }
                }
                String[] splittext = m_textArea.getText().split("\\n");
                for (int i = 0; i < splittext.length; i++) {
                    occurences += (splittext[i].length() * 6.77) / m_textArea.getOffsetWidth();
                }
                int height = occurences + 1;
                if (m_defaultRows > height) {
                    height = m_defaultRows;
                }

                m_textArea.setVisibleLines(height);
                m_textAreaContainer.onResize();
            }

        });

        m_panel.add(m_error);
        m_textAreaContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());

        m_textArea.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                m_panel.remove(m_faidpanel);
                m_panel.getElement().setTitle("");

            }
        });
        m_textArea.addBlurHandler(new BlurHandler() {

            public void onBlur(BlurEvent event) {

                String string = m_textArea.getText();
                String searchString = "\n";
                int occurences = 0;
                if (0 != searchString.length()) {
                    for (int index = string.indexOf(searchString, 0); index != -1; index = string.indexOf(
                        searchString,
                        index + 1)) {
                        occurences++;
                    }
                }
                String[] splittext = m_textArea.getText().split("\\n");
                for (int i = 0; i < splittext.length; i++) {
                    occurences += (splittext[i].length() * 6.88) / m_textArea.getOffsetWidth();
                }
                int height = occurences + 1;
                if (m_defaultRows < height) {
                    m_panel.add(m_faidpanel);
                    m_panel.getElement().setTitle(string);
                }
                m_textAreaContainer.scrollToTop();

            }
        });
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                String string = m_textArea.getText();
                String searchString = "\n";
                int occurences = 0;
                if (0 != searchString.length()) {
                    for (int index = string.indexOf(searchString, 0); index != -1; index = string.indexOf(
                        searchString,
                        index + 1)) {
                        occurences++;
                    }
                }
                String[] splittext = m_textArea.getText().split("\\n");
                for (int i = 0; i < splittext.length; i++) {
                    occurences += (splittext[i].length() * 6.88) / m_textArea.getOffsetWidth();
                }
                int height = occurences + 1;
                if (m_defaultRows > height) {
                    height = m_defaultRows;
                    m_panel.remove(m_faidpanel);
                    m_panel.getElement().setTitle("");
                }
                m_panel.add(m_faidpanel);
                m_panel.getElement().setTitle(string);
                m_textArea.setVisibleLines(height);
                m_textAreaContainer.onResize();
            }
        });
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsTextArea();
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        if (m_textArea.getText() == null) {
            return "";
        }
        return m_textArea.getText();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return (String)getFormValue();
    }

    /**
     * Returns the text contained in the text area.<p>
     * 
     * @return the text in the text area
     */
    public String getText() {

        return m_textArea.getText();
    }

    /**
     * Returns the textarea of this widget.<p>
     * 
     * @return the textarea
     */
    public TextArea getTextArea() {

        return m_textArea;
    }

    /**
     * Sets the height of this textarea.<p>
     * 
     * @param rows the value of rows should be shown
     */
    public void setRows(int rows) {

        m_defaultRows = rows;
        double height_scroll = (rows * 17.95) + 8;
        m_textArea.setVisibleLines(rows);
        m_textAreaContainer.setHeight(height_scroll + "px");
        m_textAreaContainer.setDefaultHeight(height_scroll);
        m_textAreaContainer.onResize();
    }

    /**
     * Returns the text area container of this widget.<p>
     * 
     * @return the text area container
     */
    public CmsScrollPanel getTextAreaContainer() {

        return m_textAreaContainer;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_textArea.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_textArea.setText("");
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_textArea.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);
    }

    /**
     * Sets the value of the widget.<p>
     * 
     * @param value the new value 
     */
    public void setFormValue(Object value) {

        if (value == null) {
            value = "";
        }
        if (value instanceof String) {
            String strValue = (String)value;
            m_textArea.setText(strValue);
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String newValue) {

        setFormValue(newValue);
    }

    /**
     * Sets the text in the text area.<p>
     * 
     * @param text the new text
     */
    public void setText(String text) {

        m_textArea.setText(text);
    }

    /**
     * @param handler
     */
    public void addValueChangeHandler(ValueChangeHandler<String> handler) {

        m_textArea.addValueChangeHandler(handler);
    }
}
