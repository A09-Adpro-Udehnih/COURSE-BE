package com.example.coursebe.model.state;

import com.example.coursebe.model.TutorApplication;

public class TutorDeniedState implements TutorState {

    @Override
    public void submitApplication(TutorApplication context) {
        throw new IllegalStateException("Application is DENIED. Please submit a new application if you wish to reapply.");
    }

    @Override
    public void deleteApplication(TutorApplication context) {
        System.out.println("Deleting DENIED application.");
    }

    @Override
    public TutorApplication.Status getStatus() {
        return TutorApplication.Status.DENIED;
    }

    @Override
    public void createCourse(TutorApplication context) {
        throw new IllegalStateException("Cannot create course when application is DENIED.");
    }

    @Override
    public void editCourse(TutorApplication context) {
        throw new IllegalStateException("Cannot edit course when application is DENIED.");
    }

    @Override
    public void deleteCourse(TutorApplication context) {
        throw new IllegalStateException("Cannot delete course when application is DENIED.");
    }
}
