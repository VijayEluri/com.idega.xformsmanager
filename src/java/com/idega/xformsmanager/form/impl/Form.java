package com.idega.xformsmanager.form.impl;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.idega.util.xml.XmlUtil;
import com.idega.xformsmanager.business.PersistedFormDocument;
import com.idega.xformsmanager.business.PersistenceManager;
import com.idega.xformsmanager.component.FormComponent;
import com.idega.xformsmanager.component.beans.LocalizedStringBean;
import com.idega.xformsmanager.component.impl.FormDocumentImpl;
import com.idega.xformsmanager.context.DMContext;
import com.idega.xformsmanager.util.FormManagerUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/10/27 10:27:43 $ by $Author: civilis $
 */
public class Form {
	
	protected static Logger logger = Logger.getLogger(Form.class.getName());
	
	protected FormDocumentImpl formDocument;
	
	private Locale defaultDocumentLocale;
	private Map<String, FormComponent> formComponents;
	private int lastComponentId = 0;
	private boolean documentChanged = true;
	private DocumentBuilder builder;
	private DMContext context;
	
	public Form(DMContext context) {
		
		formDocument = new FormDocumentImpl();
		formDocument.setContext(context);
		formDocument.setForm(this);
		formDocument.setFormDocument(formDocument);
	}
	
	public static Form createDocument(LocalizedStringBean formTitle, DMContext context, String formType) {

		Form form = new Form(context);
		form.setContext(context);
//		form.formDocument.setLoad(true);
		
		context.setXformsXmlDoc(context.getCacheManager().getFormXformsTemplateCopy());
		form.loadDocumentInternal(null);
		
		if(formTitle != null)
			form.formDocument.setFormTitle(formTitle);
		
		form.formDocument.setFormId(null);
		form.formDocument.setFormType(formType);

		return form;
	}
	
	public com.idega.xformsmanager.business.Document getDocument() {
		
		return formDocument;
	}
	
	public String generateNewComponentId() {
		
		if(lastComponentId == 0) {
			
			Set<String> allIds = FormManagerUtil.getAllComponentsIds(formDocument.getXformsDocument());
			
			for (String id : allIds) {
				int idNr = FormManagerUtil.parseIdNumber(id);
				
				if(idNr > lastComponentId)
					lastComponentId = idNr;
			}
		}
		
		return FormManagerUtil.CTID+(++lastComponentId);
	}
	
	protected Map<String, FormComponent> getFormComponents() {
		
		if(formComponents == null)
			formComponents = new HashMap<String, FormComponent>();
		
		return formComponents;
	}
	
	public void setFormDocumentModified(boolean changed) {
		documentChanged = changed;
	}
	
	public boolean isFormDocumentModified() {
		return documentChanged;
	}
	public Document getFormXFormsDocumentClone() {
		return (Document)getContext().getXformsXmlDoc().cloneNode(true);
	}
	
	protected void loadDocumentInternal(PersistedFormDocument persistedForm) {
		
		Document xformsXmlDoc = getContext().getXformsXmlDoc();
		
		if(persistedForm != null) {
			formDocument.setFormId(persistedForm.getFormId());
			formDocument.setFormType(persistedForm.getFormType());
			
		} else {
			
			try {
				formDocument.setFormId(new Long(FormManagerUtil.getFormId(xformsXmlDoc)));
			} catch (Exception e) {
				formDocument.setFormId(null);
			}
		}
		
		formDocument.setContainerElement(FormManagerUtil.getComponentsContainerElement(xformsXmlDoc));
		formDocument.loadContainerComponents();
		formDocument.setProperties();
	}
	
	public static Form loadDocument(Long formId, DMContext context) {
		
		if(formId == null)
			throw new NullPointerException("Form id was not provided");
		
		Form form = new Form(context);
		form.setContext(context);
//		form.formDocument.setLoad(true);
		
		PersistedFormDocument persistedFormDocument = form.loadPersistedFormDocument(formId);

		context.setXformsXmlDoc(persistedFormDocument.getXformsDocument());
		form.loadDocumentInternal(persistedFormDocument);
		
		return form;
	}
	
	public static Form takeAndLoadDocument(Long formIdToTakeFrom, DMContext context) {
		
		if(formIdToTakeFrom == null)
			throw new NullPointerException("Form id was not provided");
		
		Form form = new Form(context);
		form.setContext(context);
//		form.formDocument.setLoad(true);
		
		PersistedFormDocument persistedFormDocument = form.takeAndloadFormDocument(formIdToTakeFrom);

		context.setXformsXmlDoc(persistedFormDocument.getXformsDocument());
		form.loadDocumentInternal(persistedFormDocument);
		
		return form;
	}
	
	public static Form loadDocument(Document xformsXmlDoc, DMContext context) {
		
		Form form = new Form(context);
//		form.formDocument.setLoad(true);
		form.setContext(context);
		context.setXformsXmlDoc(xformsXmlDoc);
		
		form.loadDocumentInternal(null);
		form.formDocument.setFormId(null);
		
		return form;
	}
	
	/*
	public static Form loadDocument(Long formId, Document xformsXmlDoc, DMContext context) throws InitializationException, Exception {
		
		Form form = loadDocument(xformsXmlDoc, context);
		form.formDocument.setFormId(formId);
		
		return form;
	}
	
	public static Form loadDocumentAndGenerateId(Document xformsXmlDoc, DMContext context) throws InitializationException, Exception {
		
		if(true)
			throw new UnsupportedOperationException();
		Form form = loadDocument(xformsXmlDoc, context);
		
		String defaultFormName = form.getDocument().getFormTitle().getString(form.getDefaultLocale());
		//form.formDocument.setFormId(context.getPersistenceManager().generateFormId(defaultFormName));
		
		return form;
	}
	*/
	
	protected PersistedFormDocument loadPersistedFormDocument(Long formId) {
		
		PersistenceManager persistenceManager = getContext().getPersistenceManager();
		return persistenceManager.loadForm(formId);
	}
	
	protected PersistedFormDocument takeAndloadFormDocument(Long formIdToTakeFrom) {
		
		PersistenceManager persistenceManager = getContext().getPersistenceManager();
		return persistenceManager.takeForm(formIdToTakeFrom);
	}
	
	public String getXFormsDocumentSourceCode() throws Exception {
		
		return FormManagerUtil.serializeDocument(getContext().getXformsXmlDoc());
	}
	public void setXFormsDocumentSourceCode(String srcXml) throws Exception {
		
		if(builder == null)
			builder = XmlUtil.getDocumentBuilder();
		
		clear();
//		formDocument.setLoad(true);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(srcXml.getBytes("UTF-8"));
		Document xformsXmlDoc = builder.parse(new InputSource(bais));
		getContext().setXformsXmlDoc(xformsXmlDoc);
		loadDocumentInternal(null);
	}
	
	public Locale getDefaultLocale() {
		
		if(defaultDocumentLocale == null)
			defaultDocumentLocale = FormManagerUtil.getDefaultFormLocale(getContext().getXformsXmlDoc());
		
		return defaultDocumentLocale;
	}
	
//	public void tellComponentId(String component_id) {
//		
//		int id_number = FormManagerUtil.parseIdNumber(component_id);
//		
//		if(id_number > lastComponentId)
//			lastComponentId = id_number;
//	}
	
	public void clear() {
		
		getContext().setXformsXmlDoc(null);
		defaultDocumentLocale = null;
		formComponents = null;
		lastComponentId = 0;
		documentChanged = true;
		formDocument.clear();
		formDocument.setForm(this);
		formDocument.setFormDocument(formDocument);
	}
	
	public DMContext getContext() {
		return context;
	}

	public void setContext(DMContext context) {
		this.context = context;
	}
}