package.com.awscognito.service;

import com.example.myapp.model.UserProfile;
import com.example.myapp.repository.UserProfileRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserProfileRepository repository;

    public ProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    /**
     * Gets the profile for the current user, or creates one if it does not exist yet.
     *
     * The first time a user calls /api/profile after registering, there is no row
     * for them in the database. This method creates it on the fly so callers
     * never have to think about whether the user is new or returning.
     */
    public UserProfile getOrCreate(Jwt jwt) {
        String sub = jwt.getSubject();         // The unique Cognito user ID from the token
        String email = jwt.getClaimAsString("email");

        return repository.findByCognitoSub(sub)
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setCognitoSub(sub);
                    profile.setEmail(email);
                    profile.setTodos("[]");        // Start with an empty JSON array
                    return repository.save(profile);
                });
    }

    public UserProfile updateTodos(Jwt jwt, String todos) {
        UserProfile profile = getOrCreate(jwt);
        profile.setTodos(todos);
        return repository.save(profile);
    }

    // @Transactional wraps the delete in a database transaction.
    // If the operation fails halfway, the database rolls back automatically.
    @Transactional
    public void deleteAllUserData(Jwt jwt) {
        repository.deleteByCognitoSub(jwt.getSubject());
    }
}