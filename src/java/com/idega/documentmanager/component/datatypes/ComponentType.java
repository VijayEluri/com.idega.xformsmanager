package com.idega.documentmanager.component.datatypes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/10/26 17:01:18 $ by $Author: civilis $
 */
public class ComponentType {
	
	private String type;
	private List<String> datatypes = new ArrayList<String>();
	private String accessSupport;
	
	public ComponentType() {
		
	}
	
	public ComponentType(String id, String accessSupport) {
		this.type = id;
		this.accessSupport = accessSupport;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getDatatypes() {
		return datatypes;
	}
	public void setDatatypes(List<String> datatypes) {
		this.datatypes = datatypes;
	}
	public String getAccessSupport() {
		return accessSupport;
	}
	public void setAccessSupport(String accessSupport) {
		this.accessSupport = accessSupport;
	}

}
