package com.example.coursebe.dto;

import java.util.List;
import java.util.UUID;

public class EnrolledStudentResponse {
    private UUID studentId;
    private String studentName;
    private String studentEmail;

    public EnrolledStudentResponse() {}

    public EnrolledStudentResponse(UUID studentId, String studentName, String studentEmail) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
}
