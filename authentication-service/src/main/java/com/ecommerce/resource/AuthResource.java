package com.ecommerce.resource;

import com.ecommerce.dto.*;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @Inject
    UserService userService;
    @Inject
    AuthService authService;
    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request){
        LOG.debugf("POST /auth/register request received: email=%s", request.email());
        UserResponse user = userService.register(request);
        LOG.infof("User registered: %s", user.email());
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @POST
    @Path("/activate")
    public Response activate(@Valid ActivationRequest request) {
        LOG.debugf("POST /auth/activate request received: token=%s", request.token());
        userService.activateUser(request);
        LOG.infof("User activated with token");
        return Response.ok(Map.of("message", "Account activated successfully")).build();
    }

    @POST
    @Path("/request-password-reset")
    public Response requestPasswordReset(@Valid PasswordResetRequest request) {
        LOG.debugf("POST /auth/request-password-reset request received: email=%s", request.email());
        userService.requestPasswordReset(request);
        LOG.infof("Password reset requested for: %s", request.email());
        return Response.ok(Map.of("message", "Password reset email sent if account exists")).build();
    }

    @POST
    @Path("/reset-password")
    public Response resetPassword(@Valid PasswordResetUpdateRequest request) {
        LOG.debugf("POST /auth/reset-password request received: token=%s", request.token());
        userService.resetPassword(request);
        LOG.infof("Password reset successfully");
        return Response.ok(Map.of("message", "Password reset successfully")).build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request){
        LOG.debugf("POST /auth/login request received: email=%s", request.email());

        TokenResponse tokens = authService.login(request);
        LOG.infof("User logged in: %s", request.email());

        return Response.ok(tokens).build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(@Valid RefreshRequest request){
        LOG.debugf("POST /auth/refresh request received: refreshToken=%s", request.refreshToken());
        TokenResponse newTokens = authService.refresh(request);
        LOG.infof("Token refreshed successfully");
        return Response.ok(newTokens).build();
    }
}
