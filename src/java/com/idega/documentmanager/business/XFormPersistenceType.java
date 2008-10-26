package com.idega.documentmanager.business;import java.lang.annotation.ElementType;import java.lang.annotation.Retention;import java.lang.annotation.RetentionPolicy;import java.lang.annotation.Target;import org.springframework.beans.factory.annotation.Qualifier;/** * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a> * @version $Revision: 1.1 $ * * Last modified: $Date: 2008/10/26 17:01:16 $ by $Author: civilis $ */@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})@Retention(RetentionPolicy.RUNTIME)@Qualifierpublic @interface XFormPersistenceType {    String value();}