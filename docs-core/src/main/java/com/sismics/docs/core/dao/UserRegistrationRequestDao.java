package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.UserRegistrationRequest;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User registration request DAO.
 */
public class UserRegistrationRequestDao {
    /**
     * Creates a new user registration request.
     * 
     * @param request User registration request to create
     * @return Request ID
     */
    public String create(UserRegistrationRequest request) {
        // Create the request UUID
        request.setId(UUID.randomUUID().toString());
        
        // Checks for username unicity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        q.setParameter("username", request.getUsername());
        List<?> l = q.getResultList();
        if (l.size() > 0) {
            throw new RuntimeException("AlreadyExistingUsername");
        }
        
        // Create the request
        request.setCreateDate(new Date());
        request.setStatus("PENDING");
        em.persist(request);
        
        return request.getId();
    }
    
    /**
     * Gets a user registration request by ID.
     * 
     * @param id Request ID
     * @return User registration request
     */
    public UserRegistrationRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select u from UserRegistrationRequest u where u.id = :id");
        q.setParameter("id", id);
        try {
            return (UserRegistrationRequest) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets all pending user registration requests.
     * 
     * @return List of pending requests
     */
    public List<UserRegistrationRequest> getPendingRequests() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select u from UserRegistrationRequest u where u.status = 'PENDING' order by u.createDate");
        return q.getResultList();
    }
    
    /**
     * Updates a user registration request.
     * 
     * @param request Request to update
     * @param userId User ID who processed the request
     * @param comment Process comment
     */
    public void update(UserRegistrationRequest request, String userId, String comment) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        request.setProcessDate(new Date());
        request.setProcessUserId(userId);
        request.setProcessComment(comment);
        em.merge(request);
    }
} 