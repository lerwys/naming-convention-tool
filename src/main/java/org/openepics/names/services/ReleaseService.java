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
package org.openepics.names.services;

import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.names.model.NameRelease;

// import org.openepics.auth.japi.*;
/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Stateless
public class ReleaseService {

    @Inject private SessionService sessionService;
    @PersistenceContext private EntityManager em;

    /**
     * Retrieve all releases.
     *
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public List<NameRelease> getAllReleases() {
        return em.createQuery("SELECT n FROM NameRelease n ORDER BY n.releaseDate DESC", NameRelease.class).getResultList();
    }

    /**
     * Publish a new release of the naming system.
     *
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public NameRelease createNewRelease(NameRelease newRelease) throws Exception {
        if (!sessionService.isEditor()) {
            throw new Exception("You are not authorized to perform this operation.");
        }
        newRelease.setReleaseDate(new Date());
        newRelease.setReleasedBy(sessionService.user());
        em.persist(newRelease);
        return newRelease;
    }
}
