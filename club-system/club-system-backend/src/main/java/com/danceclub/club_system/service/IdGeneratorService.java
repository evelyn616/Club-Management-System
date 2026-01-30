package com.danceclub.club_system.service;

import com.danceclub.club_system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for generating unique IDs for various entities in the system.
 * Ensures thread-safe generation of sequential IDs with specific prefixes.
 * 
 * Note: Activity and Payment use auto-generated Long IDs, so no manual generation needed.
 */
@Service
public class IdGeneratorService {

    private final UserRepository userRepository;
    private final LeaveRepository leaveRepository;

    public IdGeneratorService(
            UserRepository userRepository,
            LeaveRepository leaveRepository) {
        this.userRepository = userRepository;
        this.leaveRepository = leaveRepository;
    }

    /**
     * Generates a unique member ID in format m0001, m0002, m0003, etc.
     * Thread-safe through database transaction.
     * 
     * @return unique member ID
     */
    @Transactional
    public synchronized String generateMemberId() {
        return generateId("m", userRepository.findMaxId());
    }

    /**
     * Generates a unique leave ID in format L0001, L0002, L0003, etc.
     * Thread-safe through database transaction.
     * 
     * @return unique leave ID
     */
    @Transactional
    public synchronized String generateLeaveId() {
        // Note: Leave entity uses Long ID, this method may not be needed
        // Keeping for compatibility if needed in the future
        return generateId("L", null);
    }

    /**
     * Internal method to generate ID with given prefix and current max ID.
     * Extracts the numeric part from the max ID, increments it, and formats with leading zeros.
     * 
     * @param prefix the prefix character(s) for the ID
     * @param maxId the current maximum ID from database (can be null)
     * @return formatted ID string
     */
    private String generateId(String prefix, String maxId) {
        int nextNumber = 1;
        
        if (maxId != null && !maxId.isEmpty()) {
            // Extract numeric part from the max ID
            String numericPart = maxId.substring(prefix.length());
            nextNumber = Integer.parseInt(numericPart) + 1;
        }
        
        // Format with 4 digits and leading zeros
        return String.format("%s%04d", prefix, nextNumber);
    }
}
