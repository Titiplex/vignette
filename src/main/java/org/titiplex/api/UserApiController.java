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

    @GetMapping("/me/profile")
    public UserProfileResponse myProfile(Principal principal) {
        User user = users.getUserByUsername(principal.getName());
        if (user == null) throw new IllegalStateException("user not found");
        return toPrivateProfile(user);
    }

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
