package com.idega.xformsmanager.xform;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.idega.xformsmanager.component.FormComponent;
import com.idega.xformsmanager.util.FormManagerUtil;

/**
 * represents component (not nodeset) mapping handling - i.e. handling situation, when more than one
 * component is referencing nodesets with the same mapping
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $ Last modified: $Date: 2009/04/23 14:17:14 $ by $Author: civilis $
 */
public class FormComponentMapping {
	
	private FormComponent formComponent;
	private Nodeset nodeset;
	
	public FormComponentMapping(FormComponent formComponent, Nodeset nodeset) {
		
		this.formComponent = formComponent;
		this.nodeset = nodeset;
	}
	
	public void setMapping(String mappingExpression) {
		
		Element updaterElement = getMappingSiblingsUpdaterElement();
		
		// check if we need updater
		boolean needUpdater = isHasMappingSiblings(mappingExpression);
		
		if (!needUpdater) {
			
			// just remove updater if exists
			
			if (updaterElement != null) {
				
				updaterElement.getParentNode().removeChild(updaterElement);
			}
		} else {
			
			if (updaterElement == null) {
				
				updaterElement = createUpdaterElement();
				updaterElement = (Element) getFormComponent().appendChildNode(
				    updaterElement);
			}
			
			updaterElement.setAttribute(FormManagerUtil.ref_s_att,
			    "instance('data-instance')/*[@mapping='" + mappingExpression
			            + "']");
		}
	}
	
	private Element createUpdaterElement() {
		
		// create updater element
		Element updaterElement = getFormComponent().getFormDocument()
		        .createXFormsElement(FormManagerUtil.setvalue_tag);
		
		updaterElement.setAttribute(FormManagerUtil.event_att,
		    FormManagerUtil.XFormEvent.XFORMS_VALUE_CHANGED);
		
		String noodesetPath = getNodeset().getXPathPath();
		
		updaterElement.setAttribute(FormManagerUtil.value_att, noodesetPath);
		
		return updaterElement;
	}
	
	private boolean isHasMappingSiblings(String mappingExpression) {
		
		Element dataInstanceElement = getFormComponent().getFormDocument()
		        .getFormMainDataInstanceElement();
		
		org.chiba.xml.dom.DOMUtil.prettyPrintDOM(dataInstanceElement);
		
		NodeList nl = FormManagerUtil.getElementsContainingAttribute(
		    dataInstanceElement, null, FormManagerUtil.mapping_att,
		    mappingExpression);
		
		return nl.getLength() > 1;
	}
	
	private Element getMappingSiblingsUpdaterElement() {
		
		NodeList nl = FormManagerUtil.getElementsContainingAttribute(
		    getFormComponentElement(), "name", "mappingSiblingsUpdater");
		Element el;
		
		if (nl.getLength() != 0) {
			
			el = (Element) nl.item(0);
			
		} else {
			
			el = null;
		}
		
		return el;
	}
	
	private Element getFormComponentElement() {
		
		return getFormComponent().getComponentDataBean().getElement();
	}
	
	public void removeMapping() {
		
		Element updaterElement = getMappingSiblingsUpdaterElement();
		
		if (updaterElement != null) {
			
			updaterElement.getParentNode().removeChild(updaterElement);
		}
	}
	
	FormComponent getFormComponent() {
		return formComponent;
	}
	
	Nodeset getNodeset() {
		return nodeset;
	}
}