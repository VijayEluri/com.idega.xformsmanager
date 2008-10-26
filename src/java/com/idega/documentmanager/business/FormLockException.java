package com.idega.documentmanager.business;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/10/26 17:01:16 $ by $Author: civilis $
 */
public class FormLockException extends Exception {

	private static final long serialVersionUID = 1704760759202849950L;

	private String form_id;
	
	public String getFormId() {
		
		return form_id;
	}
	
    public FormLockException(String form_id, String msg) {
    	
    	super(msg);
    	this.form_id = form_id;
    }

    public FormLockException(String form_id, String msg, Throwable throwable) {
        super(msg, throwable);
        this.form_id = form_id;
    }
}