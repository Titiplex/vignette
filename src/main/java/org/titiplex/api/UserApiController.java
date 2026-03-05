package org.titiplex.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.titiplex.api.dto.PublicUserProfileResponse;
import org.titiplex.api.dto.UpdateUserProfileRequest;
import org.titiplex.api.dto.UserProfileResponse;
import org.titiplex.persistence.model.Role;
import org.titiplex.persistence.model.User;
import org.titiplex.service.UserService;

import java.security.Principal;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService users;

    public UserApiController(UserService users) {
        this.users = users;
    }

    /**
     * Retrieves the private profile of the currently authenticated user.
     *
     * @param principal the security context {@link Principal} providing details about the currently authenticated user
     * @return the private user profile information encapsulated in a {@link UserProfileResponse}
     * @throws IllegalStateException if the authenticated user cannot be found in the system
     */
    @GetMapping("/me/profile")
    public UserProfileResponse myProfile(Principal principal) {
        User user = users.getUserByUsername(principal.getName());
        if (user == null) throw new IllegalStateException("user not found");
        return toPrivateProfile(user);
    }

    /**
     * Updates the profile information of the currently authenticated user.
     *
     * @param request   the details of the user's updated profile, including display name, bio,
     *                  institution, research interests, profile visibility, and academy affiliations; via {@link UpdateUserProfileRequest}.
     * @param principal the security context {@link Principal} providing details about the currently authenticated user
     * @return the updated private user profile information encapsulated in a {@link UserProfileResponse}
     */
    @PutMapping("/me/profile")
    public UserProfileResponse updateMyProfile(@RequestBody UpdateUserProfileRequest request, Principal principal) {
        User user = users.getUserByUsername(principal.getName());
        if (user == null) throw new IllegalStateException("user not found");

        User updated = users.updateProfile(
                user,
                request.displayName(),
                request.bio(),
                request.institution(),
                request.researchInterests(),
                request.profilePublic(),
                request.academyAffiliations()
        );

        return toPrivateProfile(updated);
    }

    /**
     * Retrieves the public profile of a user specified by their unique identifier.
     * <p>
     * The method first checks if the user exists and if their profile is marked as public.
     * If the user does not exist or their profile is not public, it throws a {@link UserProfileNotFoundException}.
     * Otherwise, it converts the user entity into a response object representing the public profile.
     *
     * @param id ({@link Long}) the unique identifier of the user whose public profile needs to be retrieved
     * @return the public profile information encapsulated in a {@link PublicUserProfileResponse} object
     * @throws UserProfileNotFoundException if the user does not exist or their profile is not public
     */
    @GetMapping("/{id}/profile")
    public PublicUserProfileResponse publicProfile(@PathVariable Long id) {
        User user = users.getExistingUserById(id);
        if (user == null || !user.isProfilePublic()) {
            throw new UserProfileNotFoundException();
        }
        return toPublicProfile(user);
    }

    private UserProfileResponse toPrivateProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getInstitution(),
                user.getResearchInterests(),
                user.isProfilePublic(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getAcademyAffiliations()
        );
    }

    private PublicUserProfileResponse toPublicProfile(User user) {
        return new PublicUserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getInstitution(),
                user.getResearchInterests(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getAcademyAffiliations()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static class UserProfileNotFoundException extends RuntimeException {
    }
}
