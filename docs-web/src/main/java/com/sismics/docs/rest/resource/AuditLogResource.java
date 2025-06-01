package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.AclDao;
import com.sismics.docs.core.dao.AuditLogDao;
import com.sismics.docs.core.dao.criteria.AuditLogCriteria;
import com.sismics.docs.core.dao.dto.AuditLogDto;
import com.sismics.docs.core.util.SecurityUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.util.JsonUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

import com.sismics.docs.rest.constant.BaseFunction;

/**
 * Audit log REST resources.
 * 
 * @author bgamard
 */
@Path("/auditlog")
public class AuditLogResource extends BaseResource {
    /**
     * Returns the list of all logs for a document or user.
     *
     * @api {get} /auditlog Get audit logs
     * @apiDescription If no document ID is provided, logs for the current user will be returned.
     * @apiName GetAuditlog
     * @apiGroup Auditlog
     * @apiParam {String} [document] Document ID
     * @apiParam {String} [startDate] Start date (YYYY-MM-DD)
     * @apiParam {String} [endDate] End date (YYYY-MM-DD)
     * @apiSuccess {String} total Total number of logs
     * @apiSuccess {Object[]} logs List of logs
     * @apiSuccess {String} logs.id ID
     * @apiSuccess {String} logs.username Username
     * @apiSuccess {String} logs.target Entity ID
     * @apiSuccess {String="Acl","Comment","Document","File","Group","Tag","User","RouteModel","Route"} logs.class Entity type
     * @apiSuccess {String="CREATE","UPDATE","DELETE"} logs.type Type
     * @apiSuccess {String} logs.message Message
     * @apiSuccess {Number} logs.create_date Create date (timestamp)
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Document not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    public Response list(
            @QueryParam("document") String documentId,
            @QueryParam("startDate") String startDateStr,
            @QueryParam("endDate") String endDateStr) {
        System.out.println("AuditLogResource.list called");
        System.out.println("documentId: " + documentId);
        System.out.println("startDateStr: " + startDateStr);
        System.out.println("endDateStr: " + endDateStr);
        System.out.println("Principal: " + principal);

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        System.out.println("Authenticated principal: " + principal);
        System.out.println("Is Admin: " + hasBaseFunction(BaseFunction.ADMIN));

        // Parse date parameters
        Date startDate = null;
        Date endDate = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH); // Explicitly set locale for consistent parsing
        try {
            if (!Strings.isNullOrEmpty(startDateStr)) {
                startDate = dateFormat.parse(startDateStr);
                System.out.println("Parsed startDate: " + startDate);
            }
            if (!Strings.isNullOrEmpty(endDateStr)) {
                endDate = dateFormat.parse(endDateStr);
                System.out.println("Parsed endDate: " + endDate);
                // Set time to the end of the day
                if (endDate != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(endDate);
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999);
                    endDate = calendar.getTime();
                    System.out.println("Adjusted endDate: " + endDate);
                }
            }
        } catch (ParseException e) {
            // Handle parsing error if necessary, perhaps return a client error
            // For simplicity, we'll ignore invalid date strings for now
            System.err.println("Date parsing failed: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for more details
        }

        // On a document or a user?
        PaginatedList<AuditLogDto> paginatedList = PaginatedLists.create(20, 0);
        SortCriteria sortCriteria = new SortCriteria(1, false);
        AuditLogCriteria criteria = new AuditLogCriteria();
        
        if (Strings.isNullOrEmpty(documentId)) {
            // Search logs for a user or all users (if admin)
            if (!hasBaseFunction(BaseFunction.ADMIN)) {
                // Not admin, restrict to current user logs
                criteria.setUserId(principal.getId());
            } else {
                // Admin, allow searching all user logs (don't set userId)
                criteria.setAdmin(true);
            }
        } else {
            // Check ACL on the document
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(null))) {
                throw new NotFoundException();
            }
            criteria.setDocumentId(documentId);
            criteria.setAdmin(hasBaseFunction(BaseFunction.ADMIN)); // Admins can see all logs for a document
        }
        
        System.out.println("AuditLogCriteria isAdmin: " + criteria.isAdmin());
        System.out.println("AuditLogCriteria userId: " + criteria.getUserId());
        System.out.println("AuditLogCriteria startDate: " + criteria.getStartDate());
        System.out.println("AuditLogCriteria endDate: " + criteria.getEndDate());

        // Set date range criteria
        criteria.setStartDate(startDate);
        criteria.setEndDate(endDate);

        // Search the logs
        AuditLogDao auditLogDao = new AuditLogDao();
        auditLogDao.findByCriteria(paginatedList, criteria, sortCriteria);
        
        // Assemble the results
        JsonArrayBuilder logs = Json.createArrayBuilder();
        for (AuditLogDto auditLogDto : paginatedList.getResultList()) {
            logs.add(Json.createObjectBuilder()
                    .add("id", auditLogDto.getId())
                    .add("username", auditLogDto.getUsername())
                    .add("target", auditLogDto.getEntityId())
                    .add("class", auditLogDto.getEntityClass())
                    .add("type", auditLogDto.getType().name())
                    .add("message", JsonUtil.nullable(auditLogDto.getMessage()))
                    .add("create_date", auditLogDto.getCreateTimestamp()));
        }

        // Send the response
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("logs", logs)
                .add("total", paginatedList.getResultCount());
        return Response.ok().entity(response.build()).build();
    }
}
