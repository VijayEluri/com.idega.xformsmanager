package com.idega.documentmanager.manager;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/10/26 17:01:17 $ by $Author: civilis $
 */
public class HtmlManagerFactory {

	private HtmlManager 		htmlManager;
	
	public HtmlManager getHtmlManager() {
		return htmlManager;
	}
	public void setHtmlManager(HtmlManager htmlManager) {
		this.htmlManager = htmlManager;
	}
}