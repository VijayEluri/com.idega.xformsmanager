package com.idega.documentmanager.component.properties.impl;

import com.idega.documentmanager.business.component.properties.PropertiesButton;
import com.idega.util.CoreConstants;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/10/26 17:01:17 $ by $Author: civilis $
 */
public class ComponentPropertiesButton extends ComponentProperties implements PropertiesButton {
	
	private String referAction;
	
	public String getReferAction() {
		return referAction;
	}
	public void setReferAction(String referAction) {
		setReferActionPlain(referAction);
		component.update(ConstUpdateType.BUTTON_REFER_TO_ACTION);
	}
	public void setReferActionPlain(String referAction) {
		this.referAction = CoreConstants.EMPTY.equals(referAction) ? null : referAction;
	}
}