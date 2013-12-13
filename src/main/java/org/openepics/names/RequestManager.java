/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 * 
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 * 
 * Contact Information:
 *   Facility for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 * 
 */
package org.openepics.names;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.openepics.names.model.NameEvent;

/**
 * Manages Change Requests (backing bean for request-sub.xhtml)
 * 
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class RequestManager implements Serializable {

	@EJB
	private NamesEJBLocal namesEJB;
	private static final Logger logger = Logger
			.getLogger("org.openepics.names");
	private List<NameEvent> validNames;
	private NameEvent selectedName;
	private List<NameEvent> filteredNames;
	private List<NameEvent> historyEvents;
	private boolean myRequest = false; // user is looking at 'his'her' requests
										// i.e. 'option' param is 'user'
	private String option = null; // option parameter
	// Input parameters from input page
	private Integer newCategory;
	private Integer newParent;
	private String newCode;
	private String newDescription;
	private String newComment;
	private static final Map<String, String> requestTypeNames;

	static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("i", "Add");
		map.put("m", "Modify");
		map.put("d", "Delete");
		map.put("c", "Cancel");
		requestTypeNames = Collections.unmodifiableMap(map);
	}

	/**
	 * Creates a new instance of RequestManager
	 */
	public RequestManager() {
	}

	@PostConstruct
	public void init() {
		try {
			if (option == null) {
				option = (String) FacesContext.getCurrentInstance()
						.getExternalContext().getRequestParameterMap()
						.get("option");
			}

			if (option == null) {
				validNames = namesEJB.getValidNames();
				myRequest = false;
			} else if ("user".equals(option)) {
				validNames = namesEJB.getUserRequests();
				myRequest = true;
			}
			newCode = newDescription = newComment = null;
			newCategory = newParent = null;
			selectedName = (validNames == null || validNames.size() == 0) ? null : validNames.get(0);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not initialize Request Manager.");
			System.err.println(e);
		}
	}

	public void onModify() {
        NameEvent newRequest;

        try {
            logger.log(Level.INFO, "Modifying ");
            newRequest = namesEJB.createNewEvent(selectedName.getNameId(), newCode, newDescription, newCategory, newParent, 'm', newComment);
//			newRequest = namesEJB.createNewEvent('m', selectedName.getName(), newCategory, newCode, newDescription, newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error", e.getMessage());
            System.err.println(e);
        } finally {
            init();
        }
    }

	public void onAdd() {
		NameEvent newRequest;

		try {
			logger.log(Level.INFO, "Adding ");
			if (newCode == null || newCode.isEmpty()) {
				showMessage(FacesMessage.SEVERITY_ERROR, "Code is empty", " ");
			}
			newRequest = namesEJB.createNewEvent("", newCode, newDescription, newCategory, newParent, 'i', newComment);
//			newRequest = namesEJB.createNewEvent('i', "", newCategory, newCode,
//					newDescription, newComment);
			showMessage(FacesMessage.SEVERITY_INFO,
					"Your request was successfully submitted.",
					"Request Number: " + newRequest.getId());
		} catch (Exception e) {
			showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
					e.getMessage());
			System.err.println(e);
		} finally {
			init();
		}
	}

	/*
	 * Has the selectedName been processed?
	 */
	public boolean selectedEventProcessed() {
		return selectedName == null ? false : selectedName.getStatus() != 'p';
	}

	public void onDelete() {
		NameEvent newRequest;

		try {
			if (selectedName == null) {
				showMessage(FacesMessage.SEVERITY_ERROR, "Error:",
						"You did not select any name.");
				return;
			}

			logger.log(Level.INFO, "Deleting ");
			newRequest = namesEJB.createNewEvent(selectedName.getNameId(), selectedName.getName(), selectedName.getFullName(), selectedName.getNameCategory().getId(), selectedName.getParentName().getId(), 'd', newComment);
//			newRequest = namesEJB.createNewEvent('d', selectedName.getName(),
//					selectedName.getNameCategory().getId(),
//					selectedName.getName(), selectedName.getFullName(),
//					newComment);
			showMessage(FacesMessage.SEVERITY_INFO,
					"Your request was successfully submitted.",
					"Request Number: " + newRequest.getId());
		} catch (Exception e) {
			showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
					e.getMessage());
			System.err.println(e);
		} finally {
			init();
		}
	}

	public void onCancel() {

		try {
			if (selectedName == null) {
				showMessage(FacesMessage.SEVERITY_ERROR, "Error:",
						"You did not select any request.");
				return;
			}
			logger.log(Level.INFO, "Cancelling ");
			namesEJB.cancelRequest(selectedName.getId(), newComment);
			showMessage(FacesMessage.SEVERITY_INFO,
					"Your request has been cancelled.", "Request Number: ");
		} catch (Exception e) {
			showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
					e.getMessage());
			System.err.println(e);
		} finally {
			init();
		}
	}

	private void showMessage(FacesMessage.Severity severity, String summary,
			String message) {
		FacesContext context = FacesContext.getCurrentInstance();

		context.addMessage(null, new FacesMessage(severity, summary, message));
		FacesMessage n = new FacesMessage();

	}

	/*
	 * Convert a type code (p, a, r etc) to descriptive string
	 */
	public String requestType(char s) {
		String tname = requestTypeNames.get(String.valueOf(s));
		if (tname == null) {
			tname = "Invalid Request Type";
		}
		return tname;
	}

	// TODO: merge with same method in NamesManager
	public void findHistory() {
		try {
			if (selectedName == null) {
				showMessage(FacesMessage.SEVERITY_ERROR, "Error",
						"You must select a name first.");
				historyEvents = null;
				return;
			}
			logger.log(Level.INFO, "history ");
			historyEvents = namesEJB.findEventsByName(selectedName.getName());
			// showMessage(FacesMessage.SEVERITY_INFO,
			// "Your request was successfully submitted.", "Request Number: " +
			// newRequest.getId());
		} catch (Exception e) {
			showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
					e.getMessage());
			System.err.println(e);
		} finally {
		}
	}

	/* --------------------------- */

	public List<NameEvent> getValidNames() {
		return validNames;
	}

	public NameEvent getSelectedName() {
		return selectedName;
	}

	public void setSelectedName(NameEvent selectedName) {
		this.selectedName = selectedName;
	}

	public List<NameEvent> getFilteredNames() {
		return filteredNames;
	}

	public void setFilteredNames(List<NameEvent> filteredNames) {
		this.filteredNames = filteredNames;
	}

	public Integer getNewCategory() {
		return newCategory;
	}

	public void setNewCategory(Integer newCategory) {
		this.newCategory = newCategory;
	}
	
	public Integer getNewParent() {
		return newParent;
	}
	
	public void setNewParent(Integer newParent) {
		this.newParent = newParent;
	}

	public String getNewCode() {
		return newCode;
	}

	public void setNewCode(String newCode) {
		this.newCode = newCode;
	}

	public String getNewDescription() {
		return newDescription;
	}

	public void setNewDescription(String newDescription) {
		this.newDescription = newDescription;
	}

	public String getNewComment() {
		return newComment;
	}

	public void setNewComment(String newComment) {
		this.newComment = newComment;
	}

	public boolean isMyRequest() {
		return myRequest;
	}

	public List<NameEvent> getHistoryEvents() {
		return historyEvents;
	}
}
