package com.example.coursebe.pattern.strategy;

import com.example.coursebe.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CourseSearchStrategy {
    /**
     * Searches for courses based on a keyword using a specific search strategy.
     *
     * @param keyword  The search term to match against courses according to the strategy implementation
     * @param pageable Pagination information (page number, size, and sorting options)
     * @return A paginated list of courses matching the search criteria
     */
    Page<Course> search(String keyword, Pageable pageable);

    /**
     * Searches for courses that a specific user is enrolled in, filtered by a keyword.
     *
     * @param userId   The ID of the student/user whose enrolled courses to search
     * @param keyword  The search term to match against courses according to the strategy implementation
     * @param pageable Pagination information (page number, size, and sorting options)
     * @return A paginated list of enrolled courses matching the search criteria
     */
    Page<Course> searchForUser(UUID userId, String keyword, Pageable pageable);
}

