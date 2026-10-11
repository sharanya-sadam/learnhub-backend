package com.learnhub.dto.admin;

import com.learnhub.dto.course.CourseDto;
import com.learnhub.dto.user.UserDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDto {
    private long totalUsers;
    private long totalStudents;
    private long totalInstructors;
    private long totalAdmins;
    private long totalCourses;
    private long publishedCourses;
    private long unpublishedCourses;
    private long totalEnrollments;
    private long totalAssignments;
    private long totalSubmissions;
    private long pendingSubmissions;
    private long gradedSubmissions;
    private List<UserDto> recentUsers;
    private List<CourseDto> recentCourses;
}
