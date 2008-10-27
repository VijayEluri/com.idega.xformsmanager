package com.idega.xformsmanager.component.impl;

import com.idega.xformsmanager.business.component.ButtonArea;
import com.idega.xformsmanager.business.component.Page;
import com.idega.xformsmanager.business.component.properties.PropertiesPage;
import com.idega.xformsmanager.component.FormComponentButtonArea;
import com.idega.xformsmanager.component.FormComponentPage;
import com.idega.xformsmanager.component.properties.impl.ComponentPropertiesPage;
import com.idega.xformsmanager.manager.XFormsManagerPage;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/10/27 10:27:40 $ by $Author: civilis $
 */
public class FormComponentPageImpl extends FormComponentContainerImpl implements Page, FormComponentPage {
	
	protected boolean first = false;
	protected String button_area_id;
	protected FormComponentPage previous_page;
	protected FormComponentPage next_page;
	
	public PropertiesPage getProperties() {
		
		if(properties == null) {
			ComponentPropertiesPage properties = new ComponentPropertiesPage();
			properties.setComponent(this);
			this.properties = properties;
		}
		
		return (PropertiesPage)properties;
	}
	
	@Override
	public XFormsManagerPage getXFormsManager() {
		
		return getFormDocument().getContext().getXformsManagerFactory().getXformsManagerPage();
	}
	
	@Override
	public void remove() {
		
		if(getType().equals(FormComponentFactory.page_type_thx)) {
			//TODO: log warning
			System.out.println("removing page thx");
		}
		
		super.remove();
		parent.componentsOrderChanged();
	}
	
	public ButtonArea getButtonArea() {
		
		return button_area_id == null ? null : (ButtonArea)getContainedComponent(button_area_id);
	}
	public ButtonArea createButtonArea(String nextSiblingId) throws NullPointerException {
		
		if(getButtonArea() != null)
			throw new IllegalArgumentException("Button area already exists in the page");
		
		return (ButtonArea)addComponent(FormComponentFactory.fbc_button_area, nextSiblingId);
	}
	public void setButtonAreaComponentId(String button_area_id) {
		this.button_area_id = button_area_id;
	}
	public void setPageSiblings(FormComponentPage previous, FormComponentPage next) {
		
		next_page = next;
		previous_page = previous;
	}
	public void pagesSiblingsChanged() {
		getXFormsManager().pageContextChanged(this);
		FormComponentButtonArea button_area = (FormComponentButtonArea)getButtonArea();
		if(button_area == null)
			return;
		
		button_area.setPageSiblings(previous_page, next_page);
	}
	public FormComponentPage getPreviousPage() {
		return previous_page;
	}
	public FormComponentPage getNextPage() {
		return next_page;
	}
	public void announceLastPage(String last_page_id) {
		FormComponentButtonArea button_area = (FormComponentButtonArea)getButtonArea();
		
		if(button_area == null)
			return;
		
		button_area.announceLastPage(last_page_id);
	}
}