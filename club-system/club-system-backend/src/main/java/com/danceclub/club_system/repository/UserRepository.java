package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * Find user by email address
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user exists with the given email
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by name
     * @param name the name to search for
     * @return list of users with the given name
     */
    List<User> findByName(String name);
    
    /**
     * Find users by name containing (partial match)
     * @param name the name keyword to search for
     * @return list of users whose name contains the keyword
     */
    List<User> findByNameContaining(String name);
    
    /**
     * Find users by role
     * @param role the role to search for (member or admin)
     * @return list of users with the given role
     */
    List<User> findByRole(String role);
    
    /**
     * Find the maximum user ID for ID generation
     * @return the maximum user ID or null if no users exist
     */
    @Query("SELECT u.id FROM User u ORDER BY u.id DESC LIMIT 1")
    String findMaxId();
}
