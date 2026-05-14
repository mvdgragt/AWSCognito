package com.awscognito.repository;

import com.example.myapp.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByCognitoSub(String cognitoSub);
    void deleteByCognitoSub(String cognitoSub);
}