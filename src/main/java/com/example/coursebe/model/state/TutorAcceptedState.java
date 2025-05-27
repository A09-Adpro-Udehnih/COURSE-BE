package com.example.coursebe.model.state;

import com.example.coursebe.model.TutorApplication;

public class TutorAcceptedState implements TutorState {

    @Override
    public void submitApplication(TutorApplication context) {
        throw new IllegalStateException("Cannot submit an application that is already ACCEPTED.");
    }

    @Override
    public void deleteApplication(TutorApplication context) {
        throw new IllegalStateException("Cannot delete an application that is ACCEPTED. Consider a 'withdraw' action.");
    }

    @Override
    public TutorApplication.Status getStatus() {
        return TutorApplication.Status.ACCEPTED;
    }

    @Override
    public void createCourse(TutorApplication context) {
        System.out.println("Tutor is ACCEPTED. Creating course...");
    }

    @Override
    public void editCourse(TutorApplication context) {
        System.out.println("Tutor is ACCEPTED. Editing course...");
    }

    @Override
    public void deleteCourse(TutorApplication context) {
        System.out.println("Tutor is ACCEPTED. Deleting course...");
    }
}
