package com.idega.xformsmanager.manager;

import com.idega.xformsmanager.component.FormComponent;
import com.idega.xformsmanager.component.beans.LocalizedStringBean;

/**
 * @author <a href="mailto:arunas@idega.com">Arūnas Vasmanas</a>
 * @version $Revision: 1.4 $
 * 
 *          Last modified: $Date: 2008/11/05 17:07:06 $ by $Author: civilis $
 */
public interface XFormsManagerMultiUpload extends XFormsManager {

	public abstract LocalizedStringBean getRemoveButtonLabel(
			FormComponent component);

	public abstract LocalizedStringBean getAddButtonLabel(
			FormComponent component);

	public abstract LocalizedStringBean getDescriptionButtonLabel(
			FormComponent component);

	public abstract LocalizedStringBean getUploadingFileDescription(
			FormComponent component);
}