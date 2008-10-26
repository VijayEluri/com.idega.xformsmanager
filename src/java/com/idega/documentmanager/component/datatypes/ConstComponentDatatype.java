package com.idega.documentmanager.component.datatypes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/10/26 17:01:18 $ by $Author: civilis $
 */
public class ConstComponentDatatype {
	
	public static final String STRING = "string";
	public static final String LIST = "list";
	public static final String FILE = "file";
	public static final String FILES = "files";
	private static List<String> components_datatypes = new ArrayList<String>();
	
	private String datatype;
	
	static {
		components_datatypes.add(STRING);
		components_datatypes.add(LIST);
		components_datatypes.add(FILE);
		components_datatypes.add(FILES);
	}
	
	public ConstComponentDatatype(String datatype) {
		
		if(!components_datatypes.contains(datatype))
			throw new NullPointerException("Provided datatype not supported: " + datatype);
		
		this.datatype = datatype;
	}
	
	public String getDatatype() {
		return datatype;
	}


	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}


	@Override
	public String toString() {
		return "components category set: " + getDatatype();
	}

}
