package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.UserRegistrationRequestDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserRegistrationRequest;
import com.sismics.docs.core.util.authentication.AuthenticationUtil;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;
import com.google.common.base.Strings;

/**
 * User registration request REST resource.
 */
@Path("/user/registration")
public class UserRegistrationRequestResource extends BaseResource {
    /**
     * Creates a new user registration request.
     *
     * @api {post} /user/registration Create a new user registration request
     * @apiName PostUserRegistration
     * @apiGroup User
     * @apiParam {String{3..50}} username Username
     * @apiParam {String{8..50}} password Password
     * @apiParam {String{1..100}} email E-mail
     * @apiSuccess {String} status Status OK
     * @apiError (client) ValidationError Validation error
     * @apiError (client) AlreadyExistingUsername Login already used
     * @apiVersion 1.5.0
     *
     * @param username User's username
     * @param password Password
     * @param email E-Mail
     * @return Response
     */
    @POST
    public Response create(
        @FormParam("username") String username,
        @FormParam("password") String password,
        @FormParam("email") String email) {
        
        // Validate the input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        ValidationUtil.validateEmail(email, "email");
        
        // Create the registration request
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setEmail(email);
        
        UserRegistrationRequestDao requestDao = new UserRegistrationRequestDao();
        try {
            requestDao.create(request);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ClientException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown server error", e);
            }
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Gets all pending user registration requests.
     *
     * @api {get} /user/registration Get all pending user registration requests
     * @apiName GetUserRegistration
     * @apiGroup User
     * @apiSuccess {Object[]} requests List of registration requests
     * @apiSuccess {String} requests.id Request ID
     * @apiSuccess {String} requests.username Username
     * @apiSuccess {String} requests.email E-mail
     * @apiSuccess {String} requests.create_date Creation date
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    public Response get() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        UserRegistrationRequestDao requestDao = new UserRegistrationRequestDao();
        List<UserRegistrationRequest> requestList = requestDao.getPendingRequests();
        
        JsonArrayBuilder requests = Json.createArrayBuilder();
        for (UserRegistrationRequest request : requestList) {
            requests.add(Json.createObjectBuilder()
                    .add("id", request.getId())
                    .add("username", request.getUsername())
                    .add("email", request.getEmail())
                    .add("create_date", request.getCreateDate().getTime()));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("requests", requests);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Processes a user registration request.
     *
     * @api {post} /user/registration/:id Process a user registration request
     * @apiName PostUserRegistrationProcess
     * @apiGroup User
     * @apiParam {String} id Request ID
     * @apiParam {String} action Action (approve/reject)
     * @apiParam {String} comment Process comment
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param id Request ID
     * @param action Action
     * @param comment Process comment
     * @return Response
     */
    @POST
    @Path("{id: [a-zA-Z0-9-]+}")
    public Response process(
        @PathParam("id") String id,
        @FormParam("action") String action,
        @FormParam("comment") String comment) {
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate the input data
        action = ValidationUtil.validateLength(action, "action", 1, 20);
        comment = ValidationUtil.validateLength(comment, "comment", 0, 500, true);
        
        // Get the request
        UserRegistrationRequestDao requestDao = new UserRegistrationRequestDao();
        UserRegistrationRequest request = requestDao.getById(id);
        if (request == null) {
            throw new ClientException("RequestNotFound", "The request does not exist");
        }
        
        // Process the request
        if ("approve".equals(action)) {
            // Create the user
            User user = new User();
            user.setRoleId(Constants.DEFAULT_USER_ROLE);
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setEmail(request.getEmail());
            String globalQuotaStr = System.getenv(Constants.GLOBAL_QUOTA_ENV);
            user.setStorageQuota(Strings.isNullOrEmpty(globalQuotaStr) ? 1073741824L : Long.parseLong(globalQuotaStr));
            user.setOnboarding(true);
            
            UserDao userDao = new UserDao();
            try {
                userDao.create(user, principal.getId());
            } catch (Exception e) {
                throw new ServerException("UnknownError", "Unknown server error", e);
            }
            
            request.setStatus("APPROVED");
        } else if ("reject".equals(action)) {
            request.setStatus("REJECTED");
        } else {
            throw new ClientException("ValidationError", "Invalid action");
        }
        
        requestDao.update(request, principal.getId(), comment);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
} 