package com.example.coursebe.model.state;

import com.example.coursebe.model.TutorApplication;

public interface TutorState {
    /**
     * Handles the logic for submitting an application.
     * Behavior depends on the current state.
     * @param context The TutorApplication instance.
     */
    void submitApplication(TutorApplication context);

    /**
     * Handles the logic for a student deleting their tutor application.
     * @param context The TutorApplication instance.
     */
    void deleteApplication(TutorApplication context);

    /**
     * Returns the corresponding Status enum for the current state.
     * @return The TutorApplication.Status enum value.
     */
    TutorApplication.Status getStatus();

    /**
     * Handles the logic for creating a course.
     * Only allowed if the application is in an accepted state.
     * @param context The TutorApplication instance.
     */
    void createCourse(TutorApplication context);

    /**
     * Handles the logic for editing a course.
     * Only allowed if the application is in an accepted state.
     * @param context The TutorApplication instance.
     */
    void editCourse(TutorApplication context);

    /**
     * Handles the logic for deleting a course.
     * Only allowed if the application is in an accepted state.
     * @param context The TutorApplication instance.
     */
    void deleteCourse(TutorApplication context);
}
