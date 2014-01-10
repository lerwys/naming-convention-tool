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
package org.openepics.names.ui;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.model.NamePartRevisionType;
import org.openepics.names.services.NamePartService;

/**
 * Manages report generation.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class ReportManager implements Serializable {

	private static final long serialVersionUID = 1L;

    @Inject private NamePartService namePartService;

    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.ReportManager");
    private List<NamePartRevision> events;
    // Search Parameters
    private NamePartRevisionType revisionType;
    private NamePartRevisionStatus revisionStatus;
    private Date startDate, endDate;
    private String startRev, endRev;

    /**
     * Creates a new instance of ReportManager
     */
    public ReportManager() {
    }

    public void onGenReport() {
        try {
            logger.log(Level.FINE, "Action: generating report");
            events = namePartService.getNamePartReport(revisionType, revisionStatus);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            revisionType = null;
            revisionStatus = null;
            startRev = endRev = null;
            startDate = endDate = null;
        }
    }

    public List<NamePartRevision> getEvents() {
        return events;
    }

    public NamePartRevisionType getEventType() {
        return revisionType;
    }

    public void setEventType(NamePartRevisionType eventType) {
        this.revisionType = eventType;
    }

    public NamePartRevisionStatus getEventStatus() {
        return revisionStatus;
    }

    public void setEventStatus(NamePartRevisionStatus eventStatus) {
        this.revisionStatus = eventStatus;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStartRev() {
        return startRev;
    }

    public void setStartRev(String startRev) {
        this.startRev = startRev;
    }

    public String getEndRev() {
        return endRev;
    }

    public void setEndRev(String endRev) {
        this.endRev = endRev;
    }
}
