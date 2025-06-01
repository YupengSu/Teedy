package com.sismics.docs.core.dao.criteria;

import java.util.Date;

/**
 * Audit log criteria.
 *
 * @author bgamard 
 */
public class AuditLogCriteria {
    /**
     * Document ID.
     */
    private String documentId;

    /**
     * User ID.
     */
    private String userId;

    /**
     * The search is done for an admin user.
     */
    private boolean isAdmin = false;

    /**
     * Start date.
     */
    private Date startDate;

    /**
     * End date.
     */
    private Date endDate;
    
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public AuditLogCriteria setAdmin(boolean admin) {
        isAdmin = admin;
        return this;
    }

    public Date getStartDate() {
        return startDate;
    }

    public AuditLogCriteria setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public Date getEndDate() {
        return endDate;
    }

    public AuditLogCriteria setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }
}
