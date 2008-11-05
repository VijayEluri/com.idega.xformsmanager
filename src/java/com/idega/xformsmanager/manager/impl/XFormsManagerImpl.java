package com.idega.xformsmanager.manager.impl;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chiba.xml.dom.DOMUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idega.block.process.variables.Variable;
import com.idega.chiba.web.xml.xforms.validation.ErrorType;
import com.idega.util.StringUtil;
import com.idega.util.xml.XPathUtil;
import com.idega.xformsmanager.business.component.properties.PropertiesComponent;
import com.idega.xformsmanager.component.FormComponent;
import com.idega.xformsmanager.component.FormComponentButtonArea;
import com.idega.xformsmanager.component.FormComponentContainer;
import com.idega.xformsmanager.component.FormComponentPage;
import com.idega.xformsmanager.component.FormComponentType;
import com.idega.xformsmanager.component.beans.ComponentDataBean;
import com.idega.xformsmanager.component.beans.ErrorStringBean;
import com.idega.xformsmanager.component.beans.LocalizedStringBean;
import com.idega.xformsmanager.component.properties.impl.ConstUpdateType;
import com.idega.xformsmanager.manager.XFormsManager;
import com.idega.xformsmanager.util.FormManagerUtil;
import com.idega.xformsmanager.xform.Bind;
import com.idega.xformsmanager.xform.Nodeset;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.14 $
 * 
 *          Last modified: $Date: 2008/11/05 19:42:42 $ by $Author: civilis $
 */
@FormComponentType(FormComponentType.base)
@Service
@Scope("singleton")
public class XFormsManagerImpl implements XFormsManager {

	// private static Logger logger =
	// Logger.getLogger(XFormsManagerImpl.class.getName());

	private XPathUtil bindsByNodesetXPath;
	private static final String nodesetVariable = "nodeset";
	private static final String autofill_attr = "autofillkey";

//	TODO: don't forget to remove the || true (i.e. use caching)
	public void loadComponentFromTemplate(FormComponent component) {

		final String componentType = component.getType();
		final CacheManager cacheManager = component.getFormDocument()
				.getContext().getCacheManager();
		// cacheManager.checkForComponentType(componentType);

		ComponentDataBean xformsComponentDataBean = cacheManager
				.getXformsComponentTemplate(componentType);

		if (xformsComponentDataBean == null || true) {

			synchronized (this) {

				xformsComponentDataBean = cacheManager
						.getXformsComponentTemplate(componentType);

				if (xformsComponentDataBean == null || true) {

					// Document componentsTemplate =
					// cacheManager.getComponentsTemplate();
					Element componentTemplateElement = FormManagerUtil
							.getElementById(component.getFormDocument()
									.getXformsDocument(), componentType);

					if (componentTemplateElement != null || true) {

						// loadXFormsComponentDataBean(component,
						// componentsTemplate, componentTemplateElement);

						// this is template component data bean
						xformsComponentDataBean = newXFormsComponentDataBeanInstance();
						xformsComponentDataBean
								.setElement(componentTemplateElement);
						component.setComponentDataBean(xformsComponentDataBean);

						// the component now serves as the templated component

						// TODO: load other stuff from the template, like
						// instances, or references or or or
						loadBindsAndNodesets(component);
						loadExtKeyElements(component);

						Bind bind = component.getComponentDataBean().getBind();

						if (bind != null)
							bind.setFormComponent(null);

						cacheManager.cacheXformsComponent(componentType,
								component.getComponentDataBean()/* .clone() */);

					} else {

						Logger.getLogger(getClass().getName()).log(
								Level.SEVERE,
								"Component not found in components template document by provided type: "
										+ componentType);
					}
				}
			}

			// if(componentXFormsElement == null) {
			// String msg =
			// "Component cannot be found in components xforms document by provided type: "+componentType;
			// Logger.getLogger(getClass().getName()).log(Level.SEVERE, msg+
			// " Should not happen. Take a look, why component is registered in components_types, but is not present in components xforms document.");
			// throw new NullPointerException(msg);
			// }

			if (xformsComponentDataBean == null) {
				throw new RuntimeException(
						"Component not found in components template document by provided type: "
								+ componentType
								+ ", check the componentst template document.");
			}
		}

		if (xformsComponentDataBean != null) {
			xformsComponentDataBean = (ComponentDataBean) xformsComponentDataBean
					.clone();
			component.setId(xformsComponentDataBean.getElement().getAttribute(
					FormManagerUtil.id_att));
			Bind bind = xformsComponentDataBean.getBind();

			if (bind != null)
				bind.setFormComponent(component);

			component.setComponentDataBean(xformsComponentDataBean);
		}

		// xformsComponentDataBean =
		// (ComponentDataBean)xformsComponentDataBean.clone();
		// component.setXformsComponentDataBean(xformsComponentDataBean);
		//		
		// loadAndCacheXFormsComponentByTypeFromComponentsXForm(component,
		// componentType);
	}

	/*
	 * protected synchronized void
	 * loadAndCacheXFormsComponentByTypeFromComponentsXForm(FormComponent
	 * component, String componentType) {
	 * 
	 * CacheManager cacheManager =
	 * component.getFormDocument().getContext().getCacheManager();
	 * ComponentDataBean xformsComponentDataBean =
	 * cacheManager.getXformsComponentTemplate(componentType);
	 * 
	 * if(xformsComponentDataBean != null) { xformsComponentDataBean =
	 * (ComponentDataBean)xformsComponentDataBean.clone();
	 * component.setXformsComponentDataBean(xformsComponentDataBean); return; }
	 * 
	 * Document componentsXFormsXml = cacheManager.getComponentsTemplate();
	 * Element componentXFormsElement =
	 * FormManagerUtil.getElementByIdFromDocument(componentsXFormsXml,
	 * FormManagerUtil.body_tag, componentType);
	 * 
	 * if(componentXFormsElement == null) { String msg =
	 * "Component cannot be found in components xforms document by provided type: "
	 * +componentType; Logger.getLogger(getClass().getName()).log(Level.SEVERE,
	 * msg+
	 * " Should not happen. Take a look, why component is registered in components_types, but is not present in components xforms document."
	 * ); throw new NullPointerException(msg); }
	 * 
	 * loadXFormsComponentDataBean(component, componentsXFormsXml,
	 * componentXFormsElement); cacheManager.cacheXformsComponent(componentType,
	 * (ComponentDataBean)component.getXformsComponentDataBean().clone()); }
	 */

	public void loadComponentFromDocument(FormComponent component) {

		Document xform = component.getFormDocument().getXformsDocument();
		Element componentElement = FormManagerUtil.getElementById(xform,
				component.getId());
		ComponentDataBean xformsComponentDataBean = newXFormsComponentDataBeanInstance();
		component.setComponentDataBean(xformsComponentDataBean);
		xformsComponentDataBean.setElement(componentElement);

		loadBindsAndNodesets(component);
		loadExtKeyElements(component);
	}

	protected ComponentDataBean newXFormsComponentDataBeanInstance() {
		return new ComponentDataBean();
	}

	/**
	 * this is called when loading component from xforms document and from
	 * component xforms document loads up the ComponentDataBean
	 * 
	 * @param component
	 * @param xform
	 *            - needed, as it can be components template document
	 * @param componentElement
	 */
	// protected void loadXFormsComponentDataBean(FormComponent component,
	// Document xform, Element componentElement) {
	//		
	// loadBindsAndNodesets(component, xform);
	// loadExtKeyElements(component, xform);
	// }
	protected void loadExtKeyElements(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();
		Document xform = component.getFormDocument().getXformsDocument();

		xformsComponentDataBean.setKeyExtInstance(FormManagerUtil
				.getElementById(xform, component.getId()
						+ FormManagerUtil.autofill_instance_ending));
		xformsComponentDataBean.setKeySetvalue(FormManagerUtil.getElementById(
				xform, component.getId()
						+ FormManagerUtil.autofill_setvalue_ending));
	}

	/*
	 * protected void addBindingsAndNodesets(FormComponent component) {
	 * 
	 * ComponentDataBean xformsComponentDataBean =
	 * component.getXformsComponentDataBean();
	 * 
	 * if(xformsComponentDataBean.getBind() != null) {
	 * 
	 * // insert bind element String componentId = component.getId(); Document
	 * xformsDoc = component.getFormDocument().getXformsDocument();
	 * 
	 * String bindId = FormManagerUtil.bind_att+'.'+componentId;
	 * xformsComponentDataBean
	 * .getElement().setAttribute(FormManagerUtil.bind_att, bindId);
	 * 
	 * Element bindElement =
	 * (Element)xformsDoc.importNode(xformsComponentDataBean
	 * .getBind().getBindElement(), true);
	 * 
	 * String newFormSchemaType = insertBindElement(component, bindElement,
	 * bindId);
	 * 
	 * if(newFormSchemaType != null)
	 * copySchemaType(component.getFormDocument().getContext
	 * ().getCacheManager().getComponentsXsd(), xformsDoc, newFormSchemaType,
	 * componentId+newFormSchemaType);
	 * 
	 * Nodeset nodeset = insertNodesetElement(component, bindId); Bind bind =
	 * Bind.load(bindElement); bind.setNodeset(nodeset);
	 * xformsComponentDataBean.setBind(bind); } }
	 */

	protected void loadBindsAndNodesets(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		String bindId = xformsComponentDataBean.getElement().getAttribute(
				FormManagerUtil.bind_att);

		if (!StringUtil.isEmpty(bindId)) {

			Bind bind = Bind.locate(component, bindId);

			if (bind == null)
				throw new NullPointerException("Binding not found by bind id: "
						+ bindId + " in the form "
						+ component.getFormDocument().getId());

			// bind.setFormComponent(component);
			xformsComponentDataBean.setBind(bind);
		}
	}

	/**
	 * import component from template component
	 */
	public void addComponentToDocument(FormComponent component) {

		ComponentDataBean componentDataBean = component
				.getComponentDataBean();
		Document xform = component.getFormDocument().getXformsDocument();

		// resolving template element and importing to xform document
		Element componentElement = componentDataBean.getElement();
		componentElement = (Element) xform.importNode(componentElement, true);
		componentDataBean.setElement(componentElement);

		String componentId = component.getId();
		componentElement.setAttribute(FormManagerUtil.id_att, componentId);

		FormManagerUtil.replaceAttributesByExpression(componentElement,
				"componentId", componentId);

		localizeComponent(componentId, componentElement, xform, component
				.getFormDocument().getContext().getCacheManager()
				.getComponentsTemplate());

		// TODO: is this something we need?
		if (removeTextNodes())
			FormManagerUtil.removeTextNodes(componentElement);

		if (componentDataBean.getBind() != null) {

			Bind bind = Bind.createFromTemplate(componentDataBean
					.getBind());
			componentDataBean.putBind(bind);
		}
		// addBindingsAndNodesets(component);

		FormComponentContainer parent = component.getParent();
		parent.getXFormsManager().addChild(parent, component);
		componentElement = componentDataBean.getElement();

		if (componentDataBean.getElement()
				.getAttributeNode(autofill_attr) != null) {

			component.getProperties().setAutofillKey(
					componentDataBean.getElement().getAttributeNode(
							autofill_attr).getValue());
			componentElement.removeAttribute(autofill_attr);

			updateAutofillKey(component);
		}
	}

	protected boolean removeTextNodes() {
		return true;
	}

	// protected Element getInsertBeforeComponentElement(FormComponent
	// component_after_this) {
	//		
	// return component_after_this.getXformsComponentDataBean().getElement();
	// }

	protected void localizeComponent(String componentId, Element context,
			Document xform, Document componentsTemplate) {

		// TODO: remake this, localizations could be alrady there in the xform
		// document, just keys would be changed
		NodeList elements = FormManagerUtil.getLocalizableElements(context);

		for (int i = 0; i < elements.getLength(); i++) {

			Element localizableElement = (Element) elements.item(i);

			final String ref = localizableElement
					.getAttribute(FormManagerUtil.ref_s_att);
			final String value = localizableElement
					.getAttribute(FormManagerUtil.value_att);
			final String attributeValue;
			final String attributeName;

			if (FormManagerUtil.isLocalizableExpressionCorrect(ref)) {
				attributeName = FormManagerUtil.ref_s_att;
				attributeValue = ref;
			} else if (FormManagerUtil.isLocalizableExpressionCorrect(value)) {
				attributeName = FormManagerUtil.value_att;
				attributeValue = value;
			} else {
				attributeName = null;
				attributeValue = null;
			}

			if (attributeName != null) {

				String key = FormManagerUtil.getKeyFromRef(attributeValue);
				FormManagerUtil.putLocalizedText(attributeName, FormManagerUtil
						.getComponentLocalizationKey(componentId, key), null,
						localizableElement, xform, FormManagerUtil
								.getLocalizedStrings(key, componentsTemplate));
			}
		}
	}

	/**
	 * <p>
	 * Copies schema type from one schema document to another by provided type
	 * name.
	 * </p>
	 * <p>
	 * <b><i>WARNING: </i></b>currently doesn't support cascading types copying,
	 * i.e., when one type depends on another
	 * </p>
	 * 
	 * @param src
	 *            - schema document to copy from
	 * @param dest
	 *            - schema document to copy to
	 * @param src_type_name
	 *            - name of type to copy
	 * @throws NullPointerException
	 *             - some params were null or such type was not found in src
	 *             document
	 */
	/*
	 * protected void copySchemaType(Document src, Document dest, String
	 * src_type_name, String dest_type_name) throws NullPointerException {
	 * 
	 * if(src == null || dest == null || src_type_name == null) {
	 * 
	 * String err_msg = new StringBuilder("\nEither parameter is not provided:")
	 * .append("\nsrc: ") .append(String.valueOf(src)) .append("\ndest: ")
	 * .append(String.valueOf(dest)) .append("\ntype_name: ")
	 * .append(src_type_name) .toString();
	 * 
	 * throw new NullPointerException(err_msg); }
	 * 
	 * Element root = src.getDocumentElement();
	 * 
	 * // check among simple types
	 * 
	 * Element type_to_copy =
	 * getSchemaTypeToCopy(root.getElementsByTagName(simple_type),
	 * src_type_name);
	 * 
	 * if(type_to_copy == null) { // check among complex types
	 * 
	 * type_to_copy =
	 * getSchemaTypeToCopy(root.getElementsByTagName(complex_type),
	 * src_type_name); }
	 * 
	 * if(type_to_copy == null) throw new
	 * NullPointerException("Schema type was not found by provided name: "
	 * +src_type_name);
	 * 
	 * type_to_copy = (Element)dest.importNode(type_to_copy, true);
	 * type_to_copy.setAttribute(FormManagerUtil.name_att, dest_type_name);
	 * 
	 * ((Element)dest.getElementsByTagName(FormManagerUtil.schema_tag).item(0)).
	 * appendChild(type_to_copy); }
	 * 
	 * private Element getSchemaTypeToCopy(NodeList types, String
	 * type_name_required) {
	 * 
	 * for (int i = 0; i < types.getLength(); i++) {
	 * 
	 * Element simple_type = (Element)types.item(i); String name_att =
	 * simple_type.getAttribute(FormManagerUtil.name_att);
	 * 
	 * if(name_att != null && name_att.equals(type_name_required)) return
	 * simple_type; }
	 * 
	 * return null; }
	 */

	protected void updateConstraintRequired(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		PropertiesComponent props = component.getProperties();
		Bind bind = xformsComponentDataBean.getBind();

		if (bind == null) {
			Logger
					.getLogger(getClass().getName())
					.log(
							Level.SEVERE,
							"Bind element not set in xforms_component data bean. See where component is rendered for cause.");
			throw new NullPointerException("Bind element is not set");
		}

		bind.setIsRequired(props.isRequired());

		if (props.isRequired())
			checkUpdateComponentValidation(component, ErrorType.required);
	}

	protected void checkUpdateComponentValidation(FormComponent component,
			ErrorType errType) {

		PropertiesComponent props = component.getProperties();

		LocalizedStringBean locStr = props.getErrorMsg(errType);

		if (locStr == null) {

			locStr = FormManagerUtil.getDefaultErrorMessage(errType, component
					.getFormDocument().getContext().getComponentsTemplate());
			// create default one and set to properties
			props.setErrorMsg(errType, locStr);
		}
	}

	public void update(FormComponent component, ConstUpdateType what,
			Object property) {

		// TODO: perhaps implement it another way: register updatables, which
		// would listen to update event and update themselves if neccessary
		switch (what) {
		case LABEL:
			updateLabel(component);
			break;

		case ERROR_MSG:

			ErrorStringBean errString = (ErrorStringBean) property;
			updateErrorMsg(component, errString);
			break;

		case HELP_TEXT:
			updateHelpText(component);
			break;

		case CONSTRAINT_REQUIRED:
			updateConstraintRequired(component);
			break;

		case P3P_TYPE:
			updateP3pType(component);
			break;

		case AUTOFILL_KEY:
			updateAutofillKey(component);
			break;

		case VARIABLE_NAME:
			updateVariableName(component);
			break;

		// case READ_ONLY:
		// updateReadonly(component);
		// break;
		// case VALIDATION:
		// updateValidationText(component);
		default:
			break;
		}
	}

	protected void updateLabel(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		PropertiesComponent props = component.getProperties();
		LocalizedStringBean locStr = props.getLabel();

		NodeList labels = xformsComponentDataBean.getElement()
				.getElementsByTagName(FormManagerUtil.label_tag);

		if (labels == null || labels.getLength() == 0)
			return;

		Element label = (Element) labels.item(0);

		FormManagerUtil.putLocalizedText(FormManagerUtil.ref_s_att, null, null,
				label, component.getFormDocument().getXformsDocument(), locStr);
	}

	// public void setReadonly(FormComponent component, boolean readonly) {
	//	
	// ComponentDataBean xformsComponentDataBean =
	// component.getXformsComponentDataBean();
	// Bind bind = xformsComponentDataBean.getBind();
	//		
	// if(bind != null)
	// bind.setReadonly(readonly);
	// }
	//		
	// protected void updateReadonly(FormComponent component) {
	//		
	// setReadonly(component, component.getProperties().isReadonly());
	// }

	protected void updateErrorMsg(FormComponent component,
			ErrorStringBean errString) {

		// public static void setErrorLabelLocalizedStrings(Element
		// componentElement, String componentId, String componentKey,
		// ErrorStringBean errString, Document componentsTemplate) {

		FormManagerUtil.setErrorLabelLocalizedStrings(component
				.getComponentDataBean().getElement(), component.getId(),
				component.getId(), errString, component.getFormDocument()
						.getContext().getComponentsTemplate());
	}

	protected void updateHelpText(FormComponent component) {

		PropertiesComponent properties = component.getProperties();
		LocalizedStringBean helpMsg = properties.getHelpText();

		FormManagerUtil.setHelpTextLocalizedStrings(component
				.getComponentDataBean().getElement(), component.getId(),
				helpMsg, component.getFormDocument().getContext()
						.getComponentsTemplate());
	}

	// TODO: remove
	/*
	 * protected void updateValidationText(FormComponent component) {
	 * 
	 * ComponentDataBean xformsComponentDataBean =
	 * component.getComponentDataBean();
	 * 
	 * PropertiesComponent properties = component.getProperties();
	 * 
	 * Element element = xformsComponentDataBean.getElement(); NodeList helps =
	 * element.getElementsByTagName(FormManagerUtil.help_tag);
	 * 
	 * Element helpFormDoc =
	 * FormManagerUtil.getItemElementById(component.getFormDocument
	 * ().getContext().getCacheManager().getComponentsTemplate(), "help");
	 * 
	 * XPathUtil outputXPUT= new
	 * XPathUtil(".//xf:output[@helptype='validationtext']");
	 * 
	 * String localizedKey = new
	 * StringBuilder(component.getId()).append(".info").toString();
	 * 
	 * 
	 * if(helps == null || helps.getLength() == 0) {
	 * 
	 * Document xform = component.getFormDocument().getXformsDocument();
	 * 
	 * helpFormDoc = (Element)xform.importNode(helpFormDoc, true);
	 * element.appendChild(helpFormDoc);
	 * 
	 * Element output = (Element) outputXPUT.getNode(helpFormDoc);
	 * 
	 * FormManagerUtil.putLocalizedText(FormManagerUtil.ref_s_att, localizedKey,
	 * FormManagerUtil.localized_entries, output, xform,
	 * properties.getValidationText());
	 * 
	 * Bind bind = xformsComponentDataBean.getBind();
	 * 
	 * // TODO validation type String value = newStringBuffer(
	 * "if(instance('control-instance')/validation_event='submit' and instance('data-instance')/"
	 * ) .append(bind.getNodeset().getPath()) .append(" ='' , ")
	 * .append(output.getAttribute(FormManagerUtil.ref_s_att)) .append(", '')")
	 * .toString();
	 * 
	 * output.setAttribute(FormManagerUtil.value_att, value);
	 * output.removeAttribute(FormManagerUtil.ref_s_att);
	 * 
	 * } else {
	 * 
	 * Element help = (Element)helps.item(0);
	 * 
	 * Element output = (Element) outputXPUT.getNode(help);
	 * 
	 * Element outputFromDoc=
	 * (Element)helpFormDoc.getElementsByTagName(FormManagerUtil
	 * .output_tag).item(0);
	 * 
	 * output.setAttribute(FormManagerUtil.ref_s_att,
	 * outputFromDoc.getAttribute(FormManagerUtil.ref_s_att));
	 * 
	 * FormManagerUtil.putLocalizedText(FormManagerUtil.ref_s_att, localizedKey,
	 * null, output, component.getFormDocument().getXformsDocument(),
	 * properties.getValidationText());
	 * output.removeAttribute(FormManagerUtil.ref_s_att);
	 * 
	 * } }
	 */

	public void moveComponent(FormComponent component, String nextSiblingId) {

		ComponentDataBean componentDataBean = component.getComponentDataBean();

		Document xform = component.getFormDocument().getXformsDocument();

		Element elementToMove = componentDataBean.getElement();
		// TODO: resolve this from parent
		Element nextSiblingElement = null;
		Element componentContainer = (Element) elementToMove.getParentNode();

		if (nextSiblingId != null) {

			nextSiblingElement = FormManagerUtil.getElementById(xform,
					nextSiblingId);
		} else {

			nextSiblingElement = DOMUtil
					.getLastChildElement(componentContainer);
		}

		componentDataBean.setElement((Element) componentContainer.insertBefore(
				elementToMove, nextSiblingElement));

		changePreviewElementOrder(component);
	}

	protected void changePreviewElementOrder(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		Element preview_element = xformsComponentDataBean.getPreviewElement();

		if (preview_element == null)
			return;

		FormComponent comp_after_this = component.getNextSibling();

		if (comp_after_this != null) {

			Element comp_after_preview = comp_after_this.getComponentDataBean()
					.getPreviewElement();

			if (comp_after_preview == null)
				return;

			xformsComponentDataBean
					.setPreviewElement((Element) comp_after_preview
							.getParentNode().insertBefore(preview_element,
									comp_after_preview));

		} else {
			FormComponentPage confirmation_page = component.getFormDocument()
					.getFormConfirmationPage();

			if (confirmation_page == null)
				throw new NullPointerException(
						"Confirmation page not found, but preview element exists.");

			FormComponentButtonArea button_area = (FormComponentButtonArea) confirmation_page
					.getButtonArea();

			appendPreviewElement(component, confirmation_page
					.getComponentDataBean().getElement(),
					button_area == null ? null : button_area
							.getComponentDataBean().getElement());
		}
	}

	public void removeComponentFromXFormsDocument(FormComponent component) {

		removeComponentLocalization(component);
		removeComponentBindings(component);

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		Element remove_this = xformsComponentDataBean.getPreviewElement();
		if (remove_this != null)
			remove_this.getParentNode().removeChild(remove_this);

		remove_this = xformsComponentDataBean.getKeyExtInstance();
		if (remove_this != null)
			remove_this.getParentNode().removeChild(remove_this);

		remove_this = xformsComponentDataBean.getKeySetvalue();
		if (remove_this != null)
			remove_this.getParentNode().removeChild(remove_this);

		remove_this = xformsComponentDataBean.getElement();
		if (remove_this != null)
			remove_this.getParentNode().removeChild(remove_this);
	}

	protected void removeComponentBindings(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();
		Bind bind = xformsComponentDataBean.getBind();

		if (bind == null)
			return;

		Node bindElementParent = bind.getBindElement().getParentNode();
		Nodeset nodeset = bind.getNodeset();
		bind.remove();

		if (nodeset != null) {

			// we don't remove nodeset if there exists any bind elements
			// pointing to this nodeset
			NodeList bindsByNodeset = getBindsByNodeset(bindElementParent,
					nodeset.getPath());

			if (bindsByNodeset == null || bindsByNodeset.getLength() == 0)
				nodeset.remove();
		}
	}

	protected void removeComponentLocalization(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		NodeList children = xformsComponentDataBean.getElement()
				.getElementsByTagName("*");

		Element loc_model = FormManagerUtil.getElementById(component
				.getFormDocument().getXformsDocument(),
				FormManagerUtil.data_mod);

		Element loc_strings = (Element) loc_model.getElementsByTagName(
				FormManagerUtil.loc_tag).item(0);

		for (int i = 0; i < children.getLength(); i++) {

			Element child = (Element) children.item(i);

			String ref = child.getAttribute(FormManagerUtil.ref_s_att);

			if (FormManagerUtil.isLocalizableExpressionCorrect(ref)) {

				String key = FormManagerUtil.getKeyFromRef(ref);

				// those elements should be the child nodes
				NodeList localization_elements = loc_strings
						.getElementsByTagName(key);

				if (localization_elements != null) {

					int elements_count = localization_elements.getLength();

					for (int j = 0; j < elements_count; j++) {

						loc_strings.removeChild(localization_elements.item(0));
					}
				}
			}
		}
	}

	/*
	 * public String insertBindElement(FormComponent component, Element
	 * new_bind_element, String bind_id) {
	 * 
	 * new_bind_element.setAttribute(FormManagerUtil.id_att, bind_id);
	 * 
	 * Element model = component.getFormDocument().getFormDataModelElement();
	 * model.appendChild(new_bind_element);
	 * 
	 * String type_att =
	 * new_bind_element.getAttribute(FormManagerUtil.type_att);
	 * 
	 * if(type_att != null && type_att.startsWith(FormManagerUtil.fb_)) {
	 * 
	 * new_bind_element.setAttribute(FormManagerUtil.type_att,
	 * component.getId()+type_att); return type_att; } return null; }
	 * 
	 * 
	 * protected Nodeset insertNodesetElement(FormComponent component, String
	 * bindId) {
	 * 
	 * ComponentDataBean xformsComponentDataBean =
	 * component.getXformsComponentDataBean();
	 * 
	 * Bind bind = xformsComponentDataBean.getBind(); Nodeset nodeset =
	 * bind.getNodeset();
	 * 
	 * if(nodeset != null) {
	 * 
	 * Document xform = component.getFormDocument().getXformsDocument(); nodeset
	 * =
	 * Nodeset.importNodeset(FormManagerUtil.getFormInstanceModelElement(xform),
	 * nodeset, bindId); }
	 * 
	 * return nodeset; }
	 */

	/*
	 * public void changeBindName(FormComponent component, String newBindName) {
	 * 
	 * ComponentDataBean xformsComponentDataBean =
	 * component.getXformsComponentDataBean();
	 * 
	 * Bind bind = xformsComponentDataBean.getBind();
	 * 
	 * if(bind == null) return;
	 * 
	 * newBindName =
	 * FormManagerUtil.escapeNonXmlTagSymbols(newBindName.replace(' ', '_'));
	 * bind.rename(newBindName);
	 * 
	 * if(xformsComponentDataBean.getPreviewElement() != null)
	 * xformsComponentDataBean.getPreviewElement().setAttribute(
	 * FormManagerUtil.ref_s_att,
	 * bind.getBindElement().getAttribute(FormManagerUtil.nodeset_att) ); }
	 */

	protected void updateP3pType(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();
		PropertiesComponent props = component.getProperties();
		xformsComponentDataBean.getBind().setP3pType(props.getP3ptype());
	}

	protected void updateVariableName(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();
		Bind bind = xformsComponentDataBean.getBind();

		if (bind == null)
			return;

		PropertiesComponent properties = component.getProperties();
		String variableStringRepresentation = properties.getVariable() == null ? null
				: properties.getVariable().getDefaultStringRepresentation();

		if (variableStringRepresentation == null) {

			// remove variable

			Nodeset nodeset = bind.getNodeset();

			if (nodeset != null) {

				// check if there are any bindings that are pointing to this
				// nodeset
				NodeList bindsByNodeset = getBindsByNodeset(bind
						.getBindElement().getParentNode(), nodeset.getPath());

				if (bindsByNodeset != null && bindsByNodeset.getLength() == 1) {
					// this bind is the only one pointing to the nodeset, so we
					// are free to remove variable for it
					nodeset.setMapping(null);

				} else {

					// create separate nodeset
					nodeset = Nodeset.create(FormManagerUtil
							.getFormInstanceModelElement(component
									.getFormDocument().getXformsDocument()),
							bind.getId());
					bind.setNodeset(nodeset);
				}
			}

		} else {

			// create new or assing to existing nodeset

			Nodeset currentNodeset = bind.getNodeset();

			if (currentNodeset == null) {

				Nodeset nodeset = Nodeset.locateByMapping(FormManagerUtil
						.getFormInstanceModelElement(component
								.getFormDocument().getXformsDocument()),
						variableStringRepresentation);

				if (nodeset == null) {

					nodeset = Nodeset.create(FormManagerUtil
							.getFormInstanceModelElement(component
									.getFormDocument().getXformsDocument()),
							bind.getId());
					nodeset.setMapping(variableStringRepresentation);
				}

				bind.setNodeset(nodeset);

			} else {

				if (!variableStringRepresentation.equals(currentNodeset
						.getMapping())) {

					NodeList bindsByNodeset = getBindsByNodeset(bind
							.getBindElement().getParentNode(), currentNodeset
							.getPath());

					if (bindsByNodeset != null
							&& bindsByNodeset.getLength() == 1) {

						// there're no additional binds pointing to current
						// nodeset

						Nodeset nodeset = Nodeset.locateByMapping(
								FormManagerUtil
										.getFormInstanceModelElement(component
												.getFormDocument()
												.getXformsDocument()),
								variableStringRepresentation);

						if (nodeset != null) {

							// removing current and assiging to existing one
							currentNodeset.remove();
							bind.setNodeset(nodeset);

						} else {

							currentNodeset
									.setMapping(variableStringRepresentation);
						}

					} else {

						// someone else is pointing at current nodeset too, so
						// we can't rename variable and cannot remove current
						// nodeset. either create new nodeset or assing to
						// existing (if any)
						Nodeset nodeset = Nodeset.locateByMapping(
								FormManagerUtil
										.getFormInstanceModelElement(component
												.getFormDocument()
												.getXformsDocument()),
								variableStringRepresentation);

						if (nodeset == null) {

							nodeset = Nodeset
									.create(
											FormManagerUtil
													.getFormInstanceModelElement(component
															.getFormDocument()
															.getXformsDocument()),
											bind.getId());
							nodeset.setMapping(variableStringRepresentation);
						}

						bind.setNodeset(nodeset);
					}
				}
			}
		}
	}

	protected void updateAutofillKey(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		PropertiesComponent props = component.getProperties();
		String autofillKey = props.getAutofillKey();

		if (autofillKey == null
				&& (xformsComponentDataBean.getKeyExtInstance() != null || xformsComponentDataBean
						.getKeySetvalue() != null)) {

			Element rem_el = xformsComponentDataBean.getKeyExtInstance();
			if (rem_el != null)
				rem_el.getParentNode().removeChild(rem_el);
			xformsComponentDataBean.setKeyExtInstance(null);

			rem_el = xformsComponentDataBean.getKeySetvalue();
			if (rem_el != null)
				rem_el.getParentNode().removeChild(rem_el);
			xformsComponentDataBean.setKeySetvalue(null);

		} else if (autofillKey != null) {

			autofillKey = FormManagerUtil.autofill_key_prefix + autofillKey;

			String src = FormManagerUtil.context_att_pref + autofillKey;

			if (xformsComponentDataBean.getKeyExtInstance() != null) {

				xformsComponentDataBean.getKeyExtInstance().setAttribute(
						FormManagerUtil.src_att, src);

			} else {

				Element model = component.getFormDocument()
						.getFormDataModelElement();
				Element instanceElement = model.getOwnerDocument()
						.createElementNS(model.getNamespaceURI(),
								FormManagerUtil.instance_tag);
				instanceElement.setAttribute(FormManagerUtil.relevant_att,
						FormManagerUtil.xpath_false);

				instanceElement = (Element) model.appendChild(instanceElement);
				instanceElement.setAttribute(FormManagerUtil.src_att, src);
				instanceElement.setAttribute(FormManagerUtil.id_att, component
						.getId()
						+ FormManagerUtil.autofill_instance_ending);
				xformsComponentDataBean.setKeyExtInstance(instanceElement);
			}

			String value = new StringBuilder(FormManagerUtil.inst_start)
					.append(
							xformsComponentDataBean.getKeyExtInstance()
									.getAttribute(FormManagerUtil.id_att))
					.append(FormManagerUtil.inst_end).append(
							FormManagerUtil.slash).append(autofillKey)
					.toString();

			if (xformsComponentDataBean.getKeySetvalue() != null) {

				xformsComponentDataBean.getKeySetvalue().setAttribute(
						FormManagerUtil.value_att, value);

			} else {
				Element autofill_model = component.getFormDocument()
						.getAutofillModelElement();
				Element setval_el = autofill_model.getOwnerDocument()
						.createElementNS(autofill_model.getNamespaceURI(),
								FormManagerUtil.setvalue_tag);
				setval_el = (Element) autofill_model.appendChild(setval_el);
				setval_el.setAttribute(FormManagerUtil.bind_att,
						xformsComponentDataBean.getBind().getId());
				setval_el.setAttribute(FormManagerUtil.value_att, value);
				setval_el.setAttribute(FormManagerUtil.model_att, component
						.getFormDocument().getFormDataModelElement()
						.getAttribute(FormManagerUtil.id_att));
				setval_el.setAttribute(FormManagerUtil.id_att, component
						.getId()
						+ FormManagerUtil.autofill_setvalue_ending);
				xformsComponentDataBean.setKeySetvalue(setval_el);
			}
		}
	}

	public LocalizedStringBean getLocalizedStrings(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		return FormManagerUtil.getLabelLocalizedStrings(xformsComponentDataBean
				.getElement(), component.getFormDocument().getXformsDocument());
	}

	public boolean isRequired(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();
		Bind bind = xformsComponentDataBean.getBind();

		return bind != null && bind.isRequired();
	}

	public boolean isReadonly(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();
		Bind bind = xformsComponentDataBean.getBind();

		return bind != null && bind.isReadonly();
	}

	public Map<ErrorType, LocalizedStringBean> getErrorLabelLocalizedStrings(
			FormComponent component) {
		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();
		return FormManagerUtil
				.getErrorLabelLocalizedStrings(xformsComponentDataBean
						.getElement());
	}

	public LocalizedStringBean getHelpText(FormComponent component) {
		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		return FormManagerUtil
				.getHelpTextLocalizedStrings(xformsComponentDataBean
						.getElement());
	}

	/*
	 * public LocalizedStringBean getValidationText(FormComponent component) {
	 * ComponentDataBean xformsComponentDataBean =
	 * component.getComponentDataBean();
	 * 
	 * return
	 * FormManagerUtil.getValidationTextLocalizedStrings(xformsComponentDataBean
	 * .getElement(), component.getFormDocument().getXformsDocument()); }
	 */

	public Variable getVariable(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		Bind bind = xformsComponentDataBean.getBind();

		if (bind == null)
			return null;

		Nodeset nodeset = bind.getNodeset();

		if (nodeset == null)
			return null;

		String mapping = nodeset.getMapping();

		return StringUtil.isEmpty(mapping) ? null : Variable
				.parseDefaultStringRepresentation(mapping);
	}

	public void loadConfirmationElement(FormComponent component,
			FormComponentPage confirmation_page) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		Element preview_element = FormManagerUtil.getElementById(component
				.getFormDocument().getXformsDocument(), FormManagerUtil.preview
				+ '.' + component.getId());

		if (preview_element != null) {
			xformsComponentDataBean.setPreviewElement(preview_element);
			return;
		}

		if (confirmation_page == null)
			return;

		Bind bind = xformsComponentDataBean.getBind();

		if (bind == null)
			return;

		// creating new preview element
		FormComponent component_after_this = component.getNextSibling();
		Element page_element = confirmation_page.getComponentDataBean()
				.getElement();

		if (component_after_this != null) {

			Element preview_after = null;

			// if preview_after == null, that could mean 2 things:
			// - errornous form xforms document (ignore)
			// - form component is not "normal" component (default), taking next
			// if exists
			while (component_after_this != null
					&& (preview_after = component_after_this
							.getComponentDataBean().getPreviewElement()) == null)
				component_after_this = component_after_this.getNextSibling();

			if (preview_after == null)
				appendPreviewElement(component, page_element, confirmation_page
						.getButtonArea() == null ? null
						: ((FormComponent) confirmation_page.getButtonArea())
								.getComponentDataBean().getElement());
			else {

				Element output_element = createPreviewElement(component);
				output_element = (Element) preview_after.getParentNode()
						.insertBefore(output_element, preview_after);
				xformsComponentDataBean.setPreviewElement(output_element);
			}

		} else
			appendPreviewElement(component, page_element, confirmation_page
					.getButtonArea() == null ? null
					: ((FormComponent) confirmation_page.getButtonArea())
							.getComponentDataBean().getElement());
	}

	protected Element createPreviewElement(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		Element output_element = component.getFormDocument()
				.getXformsDocument().createElementNS(
						xformsComponentDataBean.getElement().getNamespaceURI(),
						FormManagerUtil.output_tag);

		output_element.setAttribute(FormManagerUtil.id_att,
				FormManagerUtil.preview + '.' + component.getId());
		output_element.setAttribute(FormManagerUtil.bind_att,
				xformsComponentDataBean.getBind().getId());

		Element component_element = xformsComponentDataBean.getElement();
		Element component_label = DOMUtil.getChildElement(component_element,
				FormManagerUtil.label_tag);

		if (component_label != null) {

			Element cloned_label = (Element) component_label.cloneNode(true);
			output_element.appendChild(cloned_label);
		}
		return output_element;
	}

	protected void appendPreviewElement(FormComponent component,
			Element page_element, Element button_area) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		Element output_element = createPreviewElement(component);

		if (button_area == null)
			output_element = (Element) page_element.appendChild(output_element);
		else
			output_element = (Element) button_area.getParentNode()
					.insertBefore(output_element, button_area);

		xformsComponentDataBean.setPreviewElement(output_element);
	}

	public String getAutofillKey(FormComponent component) {

		ComponentDataBean xformsComponentDataBean = component
				.getComponentDataBean();

		Element inst = xformsComponentDataBean.getKeyExtInstance();

		if (inst == null)
			return null;
		String src = inst.getAttribute(FormManagerUtil.src_att);
		String key = src.substring(FormManagerUtil.context_att_pref.length());

		return key.startsWith(FormManagerUtil.autofill_key_prefix) ? key
				.substring(FormManagerUtil.autofill_key_prefix.length()) : key;
	}

	protected synchronized NodeList getBindsByNodeset(Node context,
			String nodeset) {

		if (bindsByNodesetXPath == null)
			bindsByNodesetXPath = new XPathUtil(".//xf:bind[@nodeset=$nodeset]");

		bindsByNodesetXPath.clearVariables();
		bindsByNodesetXPath.setVariable(nodesetVariable, nodeset);

		return bindsByNodesetXPath.getNodeset(context);
	}
}