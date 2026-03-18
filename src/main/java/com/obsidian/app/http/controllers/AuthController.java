package com.obsidian.app.http.controllers;

import com.obsidian.app.models.User;
import com.obsidian.app.repository.UserRepository;
import com.obsidian.core.di.annotations.Inject;
import com.obsidian.core.http.controller.BaseController;
import com.obsidian.core.http.controller.annotations.Controller;
import com.obsidian.core.routing.methods.GET;
import com.obsidian.core.routing.methods.POST;
import com.obsidian.core.security.auth.Auth;
import com.obsidian.core.security.auth.LoginLockedException;
import com.obsidian.core.security.csrf.annotations.CsrfProtect;
import com.obsidian.core.security.user.RequireLogin;
import com.obsidian.core.validation.RequestValidator;
import com.obsidian.core.validation.ValidationResult;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

import static spark.Spark.halt;

@Controller
public class AuthController extends BaseController {

    @Inject
    UserRepository userRepository;

    /**
     * Shows the login form.
     */
    @GET("/login")
    public String loginForm(Request req, Response res) {
        return render("auth/login.html", null);
    }

    /**
     * Handles login form submission.
     * On success, redirects to the originally requested URL
     * On failure, redirects back with an error flash message.
     * Throws LoginLockedException after 5 failed attempts.
     */
    @POST("/login")
    @CsrfProtect
    public String login(Request req, Response res) {
        try {
            if (Auth.login(req.queryParams("username"), req.queryParams("password"), req)) {
                res.redirect(Auth.getRedirectAfterLogin(req, "/"));
                halt();
            }
            return redirectWithFlash(req, res, "error", "Invalid username or password.", "/login").toString();
        } catch (LoginLockedException e) {
            return redirectWithFlash(req, res, "error", e.getMessage(), "/login").toString();
        }
    }

    /**
     * Shows the registration form.
     */
    @GET("/register")
    public String registerForm(Request req, Response res) {
        return render("auth/register.html", null);
    }

    /**
     * Handles registration form submission.
     * Validates input, hashes the password, saves the user, then logs them in.
     * On failure, redirects back with validation errors.
     */
    @POST("/register")
    @CsrfProtect
    public String register(Request req, Response res) {

        String username = req.queryParams("username");
        String password = req.queryParams("password");

        ValidationResult result = RequestValidator.validateSafe(req, Map.of(
                "username", "required|min:3|max:32",
                "password", "required|min:8|confirmed"
        ));

        if (result.fails()) {
            return render("auth/register.html", Map.of(
                    "errors", result.getErrors(),
                    "old",    Map.of("username", username)
            ));
        }

        if (userRepository.findByUsername(username) != null) {
            return render("auth/register.html", Map.of(
                    "errors", List.of("This username is already taken."),
                    "old",    Map.of("username", username)
            ));
        }

        userRepository.create(username, Auth.hashPassword(password));

        Auth.login(username, password, req);
        res.redirect("/");
        halt();
        return null;
    }

    /**
     * Logs out the current user and redirects to the login page.
     */
    @GET("/logout")
    @RequireLogin
    public String logout(Request req, Response res) {
        Auth.logout(req.session());
        res.redirect("/login");
        halt();
        return null;
    }
}