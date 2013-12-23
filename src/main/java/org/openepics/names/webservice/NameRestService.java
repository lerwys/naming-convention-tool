/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.names.webservice;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@javax.ws.rs.ApplicationPath("/rest/v0")
public class NameRestService extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return getRestResourceClasses();
    }

    /**
     * Do not modify this method. It is automatically generated by NetBeans REST support.
     */
    private Set<Class<?>> getRestResourceClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        resources.add(org.openepics.names.webservice.NameElementCategoryResource.class);
        resources.add(org.openepics.names.webservice.NameElementResource.class);
        resources.add(org.openepics.names.webservice.NameEventResource.class);
        resources.add(org.openepics.names.webservice.NameReleaseResource.class);
        resources.add(org.openepics.names.webservice.NcNameResource.class);
        // following code can be used to customize Jersey 1.x JSON provider:
        try {
            Class jacksonProvider = Class.forName("org.codehaus.jackson.jaxrs.JacksonJsonProvider");
            resources.add(jacksonProvider);
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return resources;
    }
    
}
