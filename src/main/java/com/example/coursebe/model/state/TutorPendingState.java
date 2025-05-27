package com.example.coursebe.model.state;

import com.example.coursebe.model.TutorApplication;

public class TutorPendingState implements TutorState {

    @Override
    public void submitApplication(TutorApplication context) {
        System.out.println("Application is already PENDING.");
    }

    @Override
    public void deleteApplication(TutorApplication context) {
        System.out.println("Deleting PENDING application.");
    }

    @Override
    public TutorApplication.Status getStatus() {
        return TutorApplication.Status.PENDING;
    }

    @Override
    public void createCourse(TutorApplication context) {
        throw new IllegalStateException("Cannot create course while application is PENDING.");
    }

    @Override
    public void editCourse(TutorApplication context) {
        throw new IllegalStateException("Cannot edit course while application is PENDING.");
    }

    @Override
    public void deleteCourse(TutorApplication context) {
        throw new IllegalStateException("Cannot delete course while application is PENDING.");
    }

    // Admin-only methods, should not be callable by student/tutor directly from this state
    // but are needed for the state transition by admin
    public void acceptApplication(TutorApplication context) {
        context.setState(new TutorAcceptedState());
        context.setRawStatus(TutorApplication.Status.ACCEPTED);
    }

    public void denyApplication(TutorApplication context) {
        context.setState(new TutorDeniedState());
        context.setRawStatus(TutorApplication.Status.DENIED);
    }
}
