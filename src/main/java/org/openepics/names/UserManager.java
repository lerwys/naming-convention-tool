/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 * 
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 * 
 * Contact Information:
 *   Facilitty for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 * 
 */
package org.openepics.names;

import java.io.Serializable;
import java.security.Principal;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.openepics.names.model.Privilege;

/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Named
@SessionScoped
public class UserManager implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@EJB
    private NamesEJBLocal namesEJB;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger("org.openepics.names");
    private Privilege User;
    private boolean LoggedIn = false;
    private boolean Editor = false;
    
    //TODO is this correctly here?
    @PersistenceContext(unitName = "org.openepics.names.punit")
    private EntityManager em;

    /**
     * Creates a new instance of UserManager
     */
    public UserManager() {
    }

    public void init() {
        Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        if (principal == null) {
            User = new Privilege();
            LoggedIn = false;
            Editor = false;
        } else {
            User = em.find(Privilege.class, principal.getName());
            LoggedIn = true;
            Editor = namesEJB.isEditor(User);
        }
    }

    /*
    public String onLogin() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String originalURL = (String) context.getExternalContext().getRequestMap().get(RequestDispatcher.FORWARD_REQUEST_URI);

        if (originalURL == null) {
            originalURL = "index.xhtml";
        }
        //TODO: find an alternative for this workaround
        // Sometimes the user is already logged in. This is a workaround for it. 
        try {
            request.logout();
        } catch (Exception e) {
            logger.log(Level.INFO, "Cannot log out during login");
        }

        try {
            // resp = namesEJB.authenticate(inputUserID, inputPassword);

            request.login(this.inputUserID, this.inputPassword);
            inputPassword = "xxxxxxxx"; //TODO implement a better way destroy the password (from JVM)
            LoggedIn = true;
            User = inputUserID;
            Editor = namesEJB.isEditor(User);
            showMessage(FacesMessage.SEVERITY_INFO, "You are logged in. Welcome to Proteus.", inputUserID);
            context.getExternalContext().redirect(originalURL);
        } catch (ServletException e) {
            Ticket = null;
            LoggedIn = false;
            User = null;
            Editor = false;
            showMessage(FacesMessage.SEVERITY_ERROR, "Login Failed! Please try again. ", "Status: ");

        } finally {
        }
        return null;
    }

    public String onLogout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            request.logout();
            LoggedIn = false;
            Ticket = "";
            inputUserID = "";
            Editor = false;
            showMessage(FacesMessage.SEVERITY_INFO, "You have been logged out.", "Thank you!");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Strangely, logout has failed", "That's odd!");
        }

        return "/index.xhtml";
    }
*/
    
    public Privilege getUser() {
        return User;
    }

    public boolean isLoggedIn() {
        return LoggedIn;
    }

    public boolean isEditor() {
        return Editor;
    }

    //TODO: Move it to a common utility class
    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();

        context.addMessage(null, new FacesMessage(severity, summary, message));
        // FacesMessage n = new FacesMessage();
    }
}
