package com.idega.xformsmanager.manager.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.idega.util.StringUtil;
import com.idega.util.xml.XPathUtil;
import com.idega.xformsmanager.business.component.properties.PropertiesMultiUpload;
import com.idega.xformsmanager.component.FormComponent;
import com.idega.xformsmanager.component.beans.ComponentDataBean;
import com.idega.xformsmanager.component.beans.ComponentMultiUploadBean;
import com.idega.xformsmanager.component.beans.LocalizedStringBean;
import com.idega.xformsmanager.component.properties.impl.ConstUpdateType;
import com.idega.xformsmanager.manager.XFormsManagerMultiUpload;
import com.idega.xformsmanager.util.FormManagerUtil;
import com.idega.xformsmanager.xform.Bind;

/**
 * @author <a href="mailto:arunas@idega.com">Arūnas Vasmanas</a>
 * @version $Revision: 1.5 $
 * 
 * Last modified: $Date: 2008/11/04 17:53:06 $ by $Author: civilis $
 */
//@Service
//@Scope("singleton")
public class XFormsManagerMultiUploadImpl extends XFormsManagerImpl implements
	XFormsManagerMultiUpload {

    private static final String INSTANCE = "instance('data-instance')/Multi_file_upload_";
    private static final String REPEAT = "upload_entries_";
    private static final String ENTRY = "/entry";
    private static final String AT_START = "index('";
    private static final String AT_END = "')";
  
    private static final String APPEND_TITLE = ".title";
    private static final String APPEND_REMOVE = ".remove";
    private static final String APPEND_NAME = ".name";
    private static final String APPEND_LABEL = ".label";
    
    private static final int TITLE_LABEL = 0;
    private static final int ADD_BUTTON_LABEL = 1;
    private static final int UPLOADING_FILE_DESC = 2;
    private static final int REMOVE_BUTTON_LABEL = 3;
       
    final private XPathUtil labelsXPathUT = new XPathUtil(".//xf:label[@model]");
    final private XPathUtil insertXPUT = new XPathUtil(".//xf:insert[@at='last()']");
    final private XPathUtil repeatXPUT = new XPathUtil(".//xf:repeat");
    final private XPathUtil deleteXPUT = new XPathUtil(".//xf:delete");
    final private XPathUtil bindXPUT = new XPathUtil(".//xf:bind[@id='multiupload']");

    @Override
    protected ComponentDataBean newXFormsComponentDataBeanInstance() {
	return new ComponentMultiUploadBean();
    }

    /*
    @Override
    protected void loadXFormsComponentDataBean(FormComponent component,
	    Document xform, Element componentElement) {
	super.loadXFormsComponentDataBean(component, xform, componentElement);
	    	XPathUtil util = new XPathUtil(".//xf:bind[@id='bind."+componentElement.getAttribute(FormManagerUtil.id_att)+"']");	  
		Element bindElement = (Element) util.getNode(xform.getFirstChild());
		Bind bind = Bind.load(bindElement);
		if (bind != null){
		    ComponentMultiUploadBean xforms_component = (ComponentMultiUploadBean) component.getXformsComponentDataBean();
		    xforms_component.setBind(bind);
		}
    }
    */

    @Override
    public void removeComponentFromXFormsDocument(FormComponent component) {

	ComponentMultiUploadBean xforms_component = (ComponentMultiUploadBean) component
		.getComponentDataBean();
	Element data_src_element = xforms_component.getMultiUploadInstance();

	if (data_src_element != null)
	    data_src_element.getParentNode().removeChild(data_src_element);

	super.removeComponentFromXFormsDocument(component);
    }

    // component attach to document

    @Override
    public void addComponentToDocument(FormComponent component) {

	super.addComponentToDocument(component);
	
	ComponentMultiUploadBean xfMultiUploadComponentBean = (ComponentMultiUploadBean) component.getComponentDataBean();

	Element nodeElement = (Element) insertXPUT.getNode(xfMultiUploadComponentBean.getElement());
	nodeElement.setAttribute(FormManagerUtil.nodeset_att,constructInsertNodeset(component));
	
	nodeElement = (Element) repeatXPUT.getNode(xfMultiUploadComponentBean.getElement());
	nodeElement.setAttribute(FormManagerUtil.bind_att, constructBindValue(component.getId()));
	nodeElement.setAttribute(FormManagerUtil.id_att, constructRepeatId(component.getId()));

	nodeElement = bindXPUT.getNode(xfMultiUploadComponentBean.getBind().getBindElement());
	nodeElement.setAttribute(FormManagerUtil.id_att, constructBindValue(component.getId()));
	
	nodeElement = (Element) deleteXPUT.getNode(xfMultiUploadComponentBean.getElement());
	nodeElement.setAttribute(FormManagerUtil.nodeset_att,
		constructInsertNodeset(component));
	nodeElement.setAttribute(FormManagerUtil.at_att,
		constructDeleteAt(component.getId()));
    }

    private String constructInsertNodeset(FormComponent component) {

	StringBuffer buf = new StringBuffer();
	
	buf.append(INSTANCE).append(component.getId()).append(ENTRY);
	return buf.toString();

    }

    private String constructBindValue(String component_id) {

	StringBuffer buf = new StringBuffer();
	buf.append("upload_").append(component_id);
	return buf.toString();

    }

    private String constructRepeatId(String component_id) {
	StringBuffer buf = new StringBuffer();
	buf.append(REPEAT).append(component_id);
	return buf.toString();
    }

    private String constructDeleteAt(String component_id) {
	StringBuffer buf = new StringBuffer();
	buf.append(AT_START).append(REPEAT).append(component_id).append(AT_END);
	return buf.toString();
    }
    
    @Override
    public void update(FormComponent component, ConstUpdateType what, Object prop) {
		
		switch (what) {
			
		case ADD_BUTTON_LABEL:
			updateAddButtonLabel(component);
			break;
			
		case REMOVE_BUTTON_LABEL:
			updateRemoveButtonLabel(component);
			break;
		case LABEL:
		    	updateLabel(component);
		    	break;
		case UPLOADING_FILE_DESC:
		    	updateUploadingFileDescription(component);
		    	break;
		
		default:
			break;
		}
	}
// 	getting all labels nodes from component   
    protected NodeList getLabelNodeList (FormComponent component){
	NodeList labels;
	ComponentMultiUploadBean xfMultiUploadComponent = (ComponentMultiUploadBean) component.getComponentDataBean();
	synchronized (labelsXPathUT) {
	    labels = labelsXPathUT.getNodeset(xfMultiUploadComponent.getElement());
	}
	return labels;
    }

   protected void updateAddButtonLabel(FormComponent component) {
	
	PropertiesMultiUpload properties = (PropertiesMultiUpload)component.getProperties();
	LocalizedStringBean localizedText = properties.getAddButtonLabel();
	NodeList labels = getLabelNodeList(component);
		
	if(labels == null || labels.getLength() == 0)
		return;
	
	Element addButtonlabel = (Element)labels.item(ADD_BUTTON_LABEL);
	
	String ref = addButtonlabel.getAttribute(FormManagerUtil.ref_s_att);
	
	FormManagerUtil.putLocalizedText(FormManagerUtil.ref_s_att, !StringUtil.isEmpty(ref) ? null : new StringBuilder(component.getId()).append(APPEND_LABEL).toString(), null, 
		addButtonlabel,
			component.getFormDocument().getXformsDocument(),
			localizedText
	);

   } 	
   
   protected void updateUploadingFileDescription(FormComponent component) {
       	PropertiesMultiUpload properties = (PropertiesMultiUpload)component.getProperties();
	LocalizedStringBean localizedText = properties.getUploadingFileDescription();
	NodeList labels = getLabelNodeList(component);
	
	if(labels == null || labels.getLength() == 0)
		return;
	
	Element label = (Element)labels.item(UPLOADING_FILE_DESC);
	String ref = label.getAttribute(FormManagerUtil.ref_s_att);
	
	FormManagerUtil.putLocalizedText(FormManagerUtil.ref_s_att, !StringUtil.isEmpty(ref) ? null : new StringBuilder(component.getId()).append(APPEND_NAME).toString(), null, 
		label,
		component.getFormDocument().getXformsDocument(),
		localizedText
	);
  }
   
   protected void updateRemoveButtonLabel(FormComponent component) {
	
	PropertiesMultiUpload properties = (PropertiesMultiUpload)component.getProperties();
	LocalizedStringBean localizedText = properties.getRemoveButtonLabel();
	NodeList labels = getLabelNodeList(component);
	
	if(labels == null || labels.getLength() == 0)
		return;
	
	Element removeButtonlabel = (Element)labels.item(REMOVE_BUTTON_LABEL);
	String ref = removeButtonlabel.getAttribute(FormManagerUtil.ref_s_att);
	
	FormManagerUtil.putLocalizedText(FormManagerUtil.ref_s_att, !StringUtil.isEmpty(ref) ? null : new StringBuilder(component.getId()).append(APPEND_REMOVE).toString(), null, 
		removeButtonlabel,
		component.getFormDocument().getXformsDocument(),
		localizedText
	);
   } 
   
   @Override
   protected void updateLabel(FormComponent component) {
       	PropertiesMultiUpload properties = (PropertiesMultiUpload)component.getProperties();
	LocalizedStringBean localizedText = properties.getLabel();
	NodeList labels = getLabelNodeList(component);
	
	if(labels == null || labels.getLength() == 0)
		return;
	
	Element title = (Element)labels.item(TITLE_LABEL);
	String ref = title.getAttribute(FormManagerUtil.ref_s_att);

	FormManagerUtil.putLocalizedText(FormManagerUtil.ref_s_att, !StringUtil.isEmpty(ref) ? null : new StringBuilder(component.getId()).append(APPEND_TITLE).toString(), null, 
		title,
		component.getFormDocument().getXformsDocument(),
		localizedText
	);
       
   }
    
    public LocalizedStringBean getAddButtonLabel(FormComponent component) {
	NodeList labels = getLabelNodeList(component);
	if(labels == null || labels.getLength() == 0)
		return null;
	Element label = (Element)labels.item(ADD_BUTTON_LABEL);
	
	if (label == null)
	    return null;
	
	return FormManagerUtil.getElementLocalizedStrings(label, component.getFormDocument().getXformsDocument());
	
    }

    public LocalizedStringBean getRemoveButtonLabel(FormComponent component) {
	
	NodeList labels = getLabelNodeList(component);
	if(labels == null || labels.getLength() == 0)
		return null;
	Element label = (Element)labels.item(REMOVE_BUTTON_LABEL);
	
	if (label == null)
	    return null;
	
	return FormManagerUtil.getElementLocalizedStrings(label, component.getFormDocument().getXformsDocument());
	
    }
    
    public LocalizedStringBean getUploadingFileDescription(FormComponent component) {
	NodeList labels = getLabelNodeList(component);
	if(labels == null || labels.getLength() == 0)
		return null;
	Element label = (Element)labels.item(UPLOADING_FILE_DESC);
	
	if (label == null)
	    return null;
	
	return FormManagerUtil.getElementLocalizedStrings(label, component.getFormDocument().getXformsDocument());
    }
    

}
