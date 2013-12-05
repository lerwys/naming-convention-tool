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

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameRelease;
import org.openepics.names.model.Privilege;
import org.openepics.names.nc.NamingConventionEJBLocal;

// import org.openepics.auth.japi.*;

/**
 * The process layer for Naming.
 * 
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Stateless
public class NamesEJB implements NamesEJBLocal {

	// Add business logic below. (Right-click in editor and choose
	// "Insert Code > Add Business Method")
	private static final Logger logger = Logger
			.getLogger("org.openepics.names");
	// TODO: Remove the injection. Not a good way to authorize.
	@Inject
	private UserManager userManager;
	@PersistenceContext(unitName = "org.openepics.names.punit")
	private EntityManager em;
	@EJB
	private NamingConventionEJBLocal ncEJB;

	// private AuthServ authService = null; //* Authentication service

	/**
	 * Create a new event i.e. name creation, modification, deletion etc.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	public NameEvent createNewEvent(String name, String fullName,
			int nameCategoryID, int parentNameID, char eventType,
			String comment) throws Exception {
		logger.log(Level.INFO, "creating...");
		Date curdate = new Date();

		if (userManager == null) {
			throw new Exception("userManager is null. Cannot inject it");
		}

		if (!userManager.isLoggedIn()) {
			throw new Exception(
					"You are not authorized to perform this operation.");
		}
		
		// NameCategory ncat = new NameCategory(category, category,0);
		NameCategory ncat;
		ncat = em.find(NameCategory.class, nameCategoryID);
		if (ncat == null) {
			logger.log(Level.SEVERE, "Invalid categroy: " + nameCategoryID);
			return null;
		}
		
		if(ncEJB.isNamePartValid(name, ncat)) {
			NameEvent mEvent = new NameEvent(0, '?', userManager.getUser(), curdate, '?', name, fullName, 0);
			logger.log(Level.INFO, "new created:" + name + ":" + fullName);

			mEvent.setRequestorComment(comment);
			mEvent.setNameCategory(ncat);
			logger.log(Level.INFO, "set properties...");
			em.persist(mEvent);
			logger.log(Level.INFO, "persisted...");
			return mEvent;
		}
		
		//TODO really null?
		return null;
	}

	/**
	 * Publish a new release of the naming system.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	public NameRelease createNewRelease(NameRelease newRelease)
			throws Exception {
		logger.log(Level.INFO, "creating release...");

		if (!userManager.isEditor()) {
			throw new Exception(
					"You are not authorized to perform this operation.");
		}
		newRelease.setReleaseDate(new Date());
		// logger.log(Level.INFO, "set properties...");
		em.persist(newRelease);
		logger.log(Level.INFO, "published new release ...");
		return newRelease;
	}

	/**
	 * Find all events that are not processed yet
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	// @TransactionAttribute(TransactionAttributeType.SUPPORTS) // No
	// transaction as it is read-only query
	public List<NameEvent> getUnprocessedEvents() {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em.createQuery(
				"SELECT n FROM NameEvent n WHERE n.status = 'p'",
				NameEvent.class); // TODO: convert to criteria query
		nameEvents = query.getResultList();
		logger.log(Level.INFO,
				"retreived pending requests: " + nameEvents.size());
		return nameEvents;
	}

	/*
	 * Get names that are approved, and are new ('i') or modified ('m').
	 */
	@Override
	public List<NameEvent> getValidNames() {
		return getStandardNames("%", false);
	}

	/*
	 * Is name being changed?
	 */
	@Override
	// @TransactionAttribute(TransactionAttributeType.SUPPORTS) // No
	// transaction as it is read-only query
	public boolean isUnderChange(NameEvent nevent) {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em
				.createQuery(
						"SELECT n FROM NameEvent n WHERE n.name = :name AND n.status != 'r' AND  n.requestDate > (SELECT MAX(r.releaseDate) FROM NameRelease r)",
						NameEvent.class).setParameter("name", nevent.getName());
		nameEvents = query.getResultList();
		logger.log(Level.INFO, "change requests: " + nameEvents.size());
		return !nameEvents.isEmpty();
	}

	/*
	 * Get all requests of the current user
	 */
	@Override
	// @TransactionAttribute(TransactionAttributeType.SUPPORTS) // No
	// transaction as it is read-only query
	public List<NameEvent> getUserRequests() {
		List<NameEvent> nameEvents;
		Integer userId = userManager.getUser().getId();
		// String user = "system";

		if (userId == null) {
			return null;
		}

		TypedQuery<NameEvent> query = em.createQuery(
				"SELECT n FROM NameEvent n WHERE n.requestedBy = :userId",
				NameEvent.class).setParameter("userId", userId);

		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Results for requests: " + nameEvents.size());
		return nameEvents;
	}

	/**
	 * Retrieve all event.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	// @TransactionAttribute(TransactionAttributeType.SUPPORTS) // No
	// transaction as it is read-only query
	public List<NameEvent> getAllEvents() {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em.createQuery(
				"SELECT n FROM NameEvent n ORDER BY n.requestDate DESC",
				NameEvent.class); // TODO: convert to criteria query.
		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Results for all events: " + nameEvents.size());
		return nameEvents;
	}

	/**
	 * Retrieve all releases.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	// @TransactionAttribute(TransactionAttributeType.SUPPORTS) // No
	// transaction as it is read-only query
	public List<NameRelease> getAllReleases() {
		List<NameRelease> releases;

		TypedQuery<NameRelease> query = em.createQuery(
				"SELECT n FROM NameRelease n ORDER BY n.releaseDate DESC",
				NameRelease.class); // TODO: convert to criteria query.
		releases = query.getResultList();
		logger.log(Level.INFO, "Results for all events: " + releases.size());
		return releases;
	}

	/**
	 * Retrieve all events of a given name.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	// @TransactionAttribute(TransactionAttributeType.SUPPORTS) // No
	// transaction as it is read-only query
	public List<NameEvent> findEventsByName(String name) {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em
				.createQuery(
						"SELECT n FROM NameEvent n WHERE n.name = :name ORDER BY n.requestDate DESC",
						NameEvent.class).setParameter("name", name); // TODO:
																			// convert
																			// to
																			// criteria
																			// query.
		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Events for " + name + nameEvents.size());
		return nameEvents;
	}
	
	@Override
	public List<NameEvent> findEventsByCategory(NameCategory category) {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em
				.createQuery(
						"SELECT n FROM NameEvent n WHERE n.nameCategory = :nameCategory ORDER BY n.name",
						NameEvent.class).setParameter("nameCategory", category.getId()); // TODO:
																			// convert
																			// to
																			// criteria
																			// query.
		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Events for category " + category.getName() + nameEvents.size());
		return nameEvents;
	}

	/**
	 * Find the latest event related to the given name.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	// @TransactionAttribute(TransactionAttributeType.SUPPORTS) // No
	// transaction as it is read-only query
	public NameEvent findLatestEvent(String name) {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em
				.createQuery(
						"SELECT n FROM NameEvent n WHERE n.name = :name  AND n.status != 'r' ORDER BY n.requestDate DESC",
						NameEvent.class).setParameter("name", name); // TODO:
																			// convert
																			// to
																			// criteria
																			// query.
		nameEvents = query.getResultList();
		// logger.log(Level.INFO, "Events for " + nameId + nameEvents.size());
		if (nameEvents.isEmpty()) {
			return null;
		} else {
			return nameEvents.get(0);
		}
	}
	
	@Override
	public List<NameEvent> findEventsByParent(NameEvent parent) {
		List<NameEvent> childEvents;
		
		TypedQuery<NameEvent> query = em
				.createQuery(
						"SELECT n FROM NameEvent n WHERE n.parentNameId = :parentId ORDER BY n.name",
						NameEvent.class).setParameter("parentNameId", parent.getId()); // TODO:
																			// convert
																			// to
																			// criteria
																			// query.
		childEvents = query.getResultList();
		logger.log(Level.INFO, "Child events for " + parent.getName() + childEvents.size());
		return childEvents;
	}

	/**
	 * Find events matching given criteria
	 * 
	 * @param eventType
	 *            an event type
	 * @param eventStatus
	 *            event status
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	// @TransactionAttribute(TransactionAttributeType.SUPPORTS) // No
	// transaction as it is read-only query
	public List<NameEvent> findEvents(char eventType, char eventStatus) {
		List<NameEvent> nameEvents;

		String queryStr = "SELECT n FROM NameEvent n ";
		String cons = "";

		if (eventType != 0) {
			cons += " n.eventType = '" + String.valueOf(eventType) + "' "; // TODO:
																			// Bad
																			// idea!
																			// change
																			// to
																			// criteria
																			// query
		}

		if (eventStatus != 0) {
			if (!"".equals(cons)) {
				cons += " AND ";
			}
			cons += " n.status = '" + String.valueOf(eventStatus) + "' "; // TODO:
																			// Bad
																			// idea!
																			// change
																			// to
																			// criteria
																			// query
		}

		if (!"".equals(cons)) {
			queryStr += "WHERE " + cons;
		}

		logger.log(Level.INFO, "Search query is: " + queryStr);

		TypedQuery<NameEvent> query = em.createQuery(queryStr, NameEvent.class); // TODO:
																					// convert
																					// to
																					// criteria
																					// query.

		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Search hits: " + nameEvents.size());
		return nameEvents;
	}

	/*
	 * Get name elements that have been approved.
	 * 
	 * @param category Restrict names to the given name-element category
	 * 
	 * @param includeDeleted Don't discard deleted name-elements.
	 */
	@Override
	// @TransactionAttribute(TransactionAttributeType.SUPPORTS) // No
	// transaction as it is read-only query
	public List<NameEvent> getStandardNames(String category,
			boolean includeDeleted) {
		List<NameEvent> nameEvents;
		TypedQuery<NameEvent> query;

		// TypedQuery<NameEvent> query =
		// em.createQuery("SELECT n FROM NameEvent n WHERE n.status = 'a' AND n.eventType IN (?1, ?2) AND n.nameCategoryId.id LIKE ?3 AND n.processDate <= (SELECT MAX(r.releaseDate) FROM NameRelease r) ORDER BY n.nameCategoryId.id, n.nameCode",
		// NameEvent.class)
		if (includeDeleted) {
			query = em
					.createQuery(
							"SELECT n FROM NameEvent n WHERE n.nameCategoryId.name LIKE :categ AND n.requestDate = (SELECT MAX(r.requestDate) FROM NameEvent r WHERE r.name = n.name AND (r.status = 'a' OR r.status = 'p')) ORDER BY n.nameCategoryId.id, n.name",
							NameEvent.class).setParameter("categ", category); // TODO:
																				// convert
																				// to
																				// criteria
																				// query.
		} else {
			query = em
					.createQuery(
							"SELECT n FROM NameEvent n WHERE n.nameCategoryId.name LIKE :categ AND n.requestDate = (SELECT MAX(r.requestDate) FROM NameEvent r WHERE r.name = n.name AND (r.status = 'a' OR r.status = 'p')) AND NOT (n.eventType = 'd' AND n.status = 'a') ORDER BY n.nameCategoryId.id, n.name",
							NameEvent.class).setParameter("categ", category); // TODO:
																				// convert
																				// to
																				// criteria
																				// query.
		}
		// TODO: check category values
		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Results for category " + category + ":"
				+ nameEvents.size());
		return nameEvents;
	}

	/**
	 * Update the status of a set of events.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	public void processEvents(NameEvent[] nevents, char status, String comment)
			throws Exception {
		NameEvent mEvent;

		// TODO; check if user has privs to processEvents.
		if (!userManager.isEditor()) {
			throw new Exception(
					"You are not authorized to perform this operation.");
		}
		logger.log(Level.INFO, "Processing events " + nevents.length);
		for (NameEvent event : nevents) {
			logger.log(Level.INFO, "Processing  " + event.getName());
			mEvent = em.find(NameEvent.class, event.getId(),
					LockModeType.OPTIMISTIC); // TODO: better to merge or extend
												// the persistence context?
			mEvent.setStatus(status);
			mEvent.setProcessDate(new java.util.Date());
			mEvent.setProcessorComment(comment);
			mEvent.setProcessedBy(userManager.getUser());
			if (status == 'r') { // rejected. ToDO: use enums.
				continue;
			}
			// TODO what's up with this?
			// if (event.getEventType() == 'i') { // initiated. ToDO: use enums.
			// mEvent.setNameId(UUID.randomUUID().toString());
			// }
		}
	}

	/**
	 * Cancel a change request
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	public void cancelRequest(int eventId, String comment) throws Exception {
		NameEvent mEvent;

		// TODO; check if user has privs to processEvents.
		mEvent = em.find(NameEvent.class, eventId, LockModeType.OPTIMISTIC); // TODO:
																				// better
																				// to
																				// merge
																				// or
																				// extend
																				// the
																				// persistence
																				// context?

		if (mEvent == null) {
			throw new Exception("Event not found.");
		}
		if (!userManager.isEditor()
				&& !mEvent.getRequestedBy().equals(userManager.getUser())) {
			throw new Exception("Unauthorized access");
		}
		logger.log(Level.INFO, "Processing  " + mEvent.getName());
		mEvent.setStatus('c'); // cancelled
		mEvent.setProcessDate(new java.util.Date());
		mEvent.setProcessorComment(comment);
		mEvent.setProcessedBy(userManager.getUser());

	}

	/**
	 * Is the current user an Editor?
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	// TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isEditor(Privilege user) {
		if (user != null) {
			return "E".equals(user.getOperation());
		} else {
			return false;
		}
	}

	/**
	 * Retrieve all the name categories
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	@Override
	public List<NameCategory> getCategories() {
		List<NameCategory> cats;

		TypedQuery<NameCategory> query = em.createNamedQuery(
				"NameCategory.findAll", NameCategory.class);
		cats = query.getResultList();
		logger.log(Level.INFO, "Total number of categories: " + cats.size());

		return cats;
	}

	/*
	 * private int validate(String ticket) throws Exception { findAuthService();
	 * 
	 * if (authService == null) { logger.log(Level.WARNING,
	 * "Cannot access Auth Service."); return -1; //TODO: This is not good. Use
	 * exceptions. }
	 * 
	 * MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	 * AuthResponse resp;
	 * 
	 * params.add("ticket", ticket);
	 * 
	 * resp = authService.validate(params);
	 * 
	 * return resp.getStatus(); }
	 * 
	 * @Override public AuthResponse authenticate (String userid, String
	 * password) throws Exception { AuthResponse response; findAuthService();
	 * 
	 * if (authService == null) { logger.log(Level.WARNING,
	 * "Cannot access Auth Service."); return null; //TODO: Use exceptions. }
	 * 
	 * // RequestContext context = RequestContext.getCurrentInstance(); //
	 * AuthServ auth = new
	 * AuthServ("http://qa01.hlc.nscl.msu.edu:8080/auth/rs/v0");
	 * MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	 * 
	 * params.add("user", userid); params.add("password", password); response =
	 * authService.authenticate(params); password = "xxxxxxxx"; //TODO implement
	 * a better way destroy the password (from JVM)
	 * 
	 * return response; }
	 * 
	 * private void findAuthService() throws Exception { Properties prop =
	 * getProperties("AuthDomain"); // defined via JNDI String serviceURL;
	 * 
	 * if (prop == null || !"true".equals(prop.getProperty("Enabled"))) {
	 * authService = null; logger.log(Level.INFO,
	 * "Auth Domain not enabled or defined"); return; }
	 * 
	 * if (authService == null) { serviceURL = prop.getProperty("ServiceURL");
	 * if (serviceURL == null || serviceURL.isEmpty()) {
	 * logger.log(Level.SEVERE, "ServiceURL not set"); authService = null; }
	 * else { authService = new AuthServ(serviceURL); } } }
	 * 
	 * private Properties getProperties(String jndiName) throws Exception {
	 * Properties properties;
	 * 
	 * InitialContext context = new InitialContext(); properties = (Properties)
	 * context.lookup(jndiName); context.close();
	 * 
	 * if (properties == null) { logger.log(Level.SEVERE,
	 * "Error occurred while getting properties from JNDI."); }
	 * 
	 * return properties; }
	 */
}
