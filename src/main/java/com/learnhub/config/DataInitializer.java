package com.learnhub.config;

import com.learnhub.entity.*;
import com.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public void run(String... args) {
        // Ensure upload directory exists
        try {
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
                log.info("Created local uploads directory at {}", uploadDirectory.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Could not create uploads directory: {}", e.getMessage());
        }

        if (userRepository.count() > 0) {
            log.info("Database already initialized with users. Skipping data seeding.");
            return;
        }

        log.info("Starting database seeding for LearnHub LMS...");

        // 1. Seed Users
        User admin = User.builder()
                .name("System Administrator")
                .email("admin@learnhub.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN)
                .active(true)
                .bio("Platform Administrator with full access to manage courses, enrollments, and users.")
                .build();
        userRepository.save(admin);

        User instructor1 = User.builder()
                .name("Dr. Sarah Jenkins")
                .email("instructor@learnhub.com")
                .passwordHash(passwordEncoder.encode("Instructor@123"))
                .role(Role.ROLE_INSTRUCTOR)
                .active(true)
                .bio("Lead Architect & Professor with 12+ years building enterprise Java systems.")
                .build();
        userRepository.save(instructor1);

        User instructor2 = User.builder()
                .name("Marcus Vance")
                .email("marcus@learnhub.com")
                .passwordHash(passwordEncoder.encode("Instructor@123"))
                .role(Role.ROLE_INSTRUCTOR)
                .active(true)
                .bio("Senior Full-Stack Developer specializing in React, TypeScript, and Cloud Architecture.")
                .build();
        userRepository.save(instructor2);

        User student1 = User.builder()
                .name("Alex Rivera")
                .email("student@learnhub.com")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .role(Role.ROLE_STUDENT)
                .active(true)
                .bio("Computer Science undergraduate passionate about full-stack web engineering.")
                .build();
        userRepository.save(student1);

        User student2 = User.builder()
                .name("Jane Smith")
                .email("jane@learnhub.com")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .role(Role.ROLE_STUDENT)
                .active(true)
                .bio("Aspiring data scientist and software developer.")
                .build();
        userRepository.save(student2);

        // 2. Seed Course 1: Full-Stack Java & Spring Boot
        Course course1 = Course.builder()
                .title("Full-Stack Enterprise Java with Spring Boot & React")
                .shortDescription("Master modern enterprise application development with Java 21, Spring Boot 3, Hibernate JPA, and React.")
                .description("In this comprehensive course, you will learn to build enterprise-grade full-stack web applications from scratch. We cover Spring Boot REST APIs, Spring Security with JWT tokens, MySQL database design with Hibernate JPA, and seamless integration with a modern React front end. Ideal for software developers looking to build robust and scalable systems.")
                .category("Web Development")
                .difficulty("Intermediate")
                .instructor(instructor1)
                .thumbnailUrl("https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&auto=format&fit=crop&q=60")
                .learningObjectives("Design RESTful APIs with Spring Boot 3;Implement JWT security and role-based authorization;Master JPA/Hibernate entity relationships and indexing;Build dynamic React interfaces connecting to Spring Boot")
                .prerequisites("Basic understanding of Java syntax and core Object-Oriented Programming principles.")
                .estimatedDuration("18 Hours")
                .published(true)
                .build();
        courseRepository.save(course1);

        // Course 1 Modules & Lessons
        CourseModule c1m1 = CourseModule.builder()
                .course(course1)
                .title("Module 1: Spring Boot 3 Foundations & REST APIs")
                .description("Explore project setup, dependency injection, architecture layers, and RESTful controller design.")
                .displayOrder(1)
                .build();
        moduleRepository.save(c1m1);

        Lesson c1m1l1 = Lesson.builder()
                .module(c1m1)
                .title("1.1 Introduction to Spring Boot & Architecture Overview")
                .description("Overview of the Spring ecosystem, Spring Boot starters, auto-configuration, and layered architecture.")
                .videoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                .duration("14:20")
                .displayOrder(1)
                .published(true)
                .build();
        lessonRepository.save(c1m1l1);

        Lesson c1m1l2 = Lesson.builder()
                .module(c1m1)
                .title("1.2 Designing Clean REST Controllers & DTOs")
                .description("Building robust endpoints with @RestController, request validation with Bean Validation, and DTO mapping.")
                .videoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
                .duration("18:45")
                .displayOrder(2)
                .published(true)
                .build();
        lessonRepository.save(c1m1l2);

        CourseModule c1m2 = CourseModule.builder()
                .course(course1)
                .title("Module 2: Persistence with MySQL & Spring Data JPA")
                .description("Deep dive into Hibernate entities, repositories, relationships, and queries.")
                .displayOrder(2)
                .build();
        moduleRepository.save(c1m2);

        Lesson c1m2l1 = Lesson.builder()
                .module(c1m2)
                .title("2.1 JPA Entities and Relational Mapping")
                .description("Learn entity annotations, primary key generation, OneToMany and ManyToOne mappings, and cascade operations.")
                .videoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
                .duration("22:10")
                .displayOrder(1)
                .published(true)
                .build();
        lessonRepository.save(c1m2l1);

        Lesson c1m2l2 = Lesson.builder()
                .module(c1m2)
                .title("2.2 Spring Security & JWT Token Authentication")
                .description("Configure stateless authentication, JWT filters, BCrypt password hashing, and role checks.")
                .videoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4")
                .duration("25:30")
                .displayOrder(2)
                .published(true)
                .build();
        lessonRepository.save(c1m2l2);

        // Course 1 Assignments
        Assignment c1a1 = Assignment.builder()
                .course(course1)
                .title("Assignment 1: Building a Layered Spring Boot REST API")
                .description("Implement a complete CRUD service with validation, DTOs, and global exception handling.")
                .instructions("1. Create your entities and repositories.\n2. Add service methods with appropriate business logic.\n3. Create controller endpoints with @Valid.\n4. Submit either code snippets or a zip archive.")
                .maxMarks(BigDecimal.valueOf(100.00))
                .dueDate(LocalDateTime.now().plusDays(7))
                .allowedSubmissionType("BOTH")
                .allowLateSubmissions(true)
                .published(true)
                .build();
        assignmentRepository.save(c1a1);

        Assignment c1a2 = Assignment.builder()
                .course(course1)
                .title("Assignment 2: JWT Security & Custom Role Filtering")
                .description("Secure your REST endpoints with Spring Security and verify role-based authorization.")
                .instructions("Implement JWT generation, validation filter, and secure student and instructor routes.")
                .maxMarks(BigDecimal.valueOf(50.00))
                .dueDate(LocalDateTime.now().plusDays(14))
                .allowedSubmissionType("BOTH")
                .allowLateSubmissions(false)
                .published(true)
                .build();
        assignmentRepository.save(c1a2);

        // 3. Seed Course 2: Modern React & Front-End Architecture
        Course course2 = Course.builder()
                .title("Modern React with Vite, Hooks, and Component Patterns")
                .shortDescription("Build high-performance, responsive Single Page Applications with React 18, Vite, and Bootstrap.")
                .description("Become proficient in modern React development. Learn component lifecycles, hooks (useState, useEffect, useContext), routing with React Router v6, Axios API consumption, and clean state management.")
                .category("Web Development")
                .difficulty("Beginner")
                .instructor(instructor2)
                .thumbnailUrl("https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=800&auto=format&fit=crop&q=60")
                .learningObjectives("Understand React functional components and hooks;Manage shared state using Context API;Build responsive UI layouts with Bootstrap and custom CSS;Handle asynchronous API calls with Axios")
                .prerequisites("Basic familiarity with HTML, CSS, and modern JavaScript (ES6+).")
                .estimatedDuration("14 Hours")
                .published(true)
                .build();
        courseRepository.save(course2);

        CourseModule c2m1 = CourseModule.builder()
                .course(course2)
                .title("Module 1: React Fundamentals & Hooks")
                .description("Introduction to JSX, props, state, and standard React hooks.")
                .displayOrder(1)
                .build();
        moduleRepository.save(c2m1);

        Lesson c2m1l1 = Lesson.builder()
                .module(c2m1)
                .title("1.1 Getting Started with React & Vite")
                .description("Setting up modern React applications with Vite build tooling and project structuring.")
                .videoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4")
                .duration("12:15")
                .displayOrder(1)
                .published(true)
                .build();
        lessonRepository.save(c2m1l1);

        Lesson c2m1l2 = Lesson.builder()
                .module(c2m1)
                .title("1.2 State Management with useState & useEffect")
                .description("Handling dynamic state updates, form inputs, side effects, and clean-up functions.")
                .videoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4")
                .duration("16:40")
                .displayOrder(2)
                .published(true)
                .build();
        lessonRepository.save(c2m1l2);

        Assignment c2a1 = Assignment.builder()
                .course(course2)
                .title("Assignment 1: Responsive Dashboard UI Component")
                .description("Build a responsive metrics dashboard with charts, stats cards, and theme toggling.")
                .instructions("Create modular React components that accept props for dynamic metric values.")
                .maxMarks(BigDecimal.valueOf(100.00))
                .dueDate(LocalDateTime.now().plusDays(10))
                .allowedSubmissionType("BOTH")
                .allowLateSubmissions(true)
                .published(true)
                .build();
        assignmentRepository.save(c2a1);

        // 4. Seed Course 3: Data Science & ML (Intermediate)
        Course course3 = Course.builder()
                .title("Applied Data Science with Python & Machine Learning")
                .shortDescription("Hands-on data analysis, predictive modeling, pandas, scikit-learn, and model evaluation.")
                .description("Learn statistical modeling, data wrangling with pandas, exploratory analysis, and supervised machine learning algorithms.")
                .category("Data Science")
                .difficulty("Intermediate")
                .instructor(instructor1)
                .thumbnailUrl("https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&auto=format&fit=crop&q=60")
                .learningObjectives("Perform exploratory data analysis with pandas and numpy;Train regression and classification models;Evaluate models using cross-validation and ROC curves")
                .prerequisites("Basic Python programming experience.")
                .estimatedDuration("20 Hours")
                .published(true)
                .build();
        courseRepository.save(course3);

        CourseModule c3m1 = CourseModule.builder()
                .course(course3)
                .title("Module 1: Exploratory Data Analysis")
                .description("Data cleaning, visualization, and feature engineering.")
                .displayOrder(1)
                .build();
        moduleRepository.save(c3m1);

        Lesson c3m1l1 = Lesson.builder()
                .module(c3m1)
                .title("1.1 Data Wrangling with Pandas")
                .description("Working with Series, DataFrames, indexing, filtering, and missing data imputation.")
                .videoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4")
                .duration("19:00")
                .displayOrder(1)
                .published(true)
                .build();
        lessonRepository.save(c3m1l1);

        // 5. Seed Course 4: Cloud Native DevOps (Draft/Unpublished course to test instructor & admin publishing features)
        Course course4 = Course.builder()
                .title("Cloud-Native Architecture with Docker & Kubernetes")
                .shortDescription("Deploy, scale, and manage containerized microservices in cloud environments.")
                .description("Deep dive into Docker containerization, multi-stage builds, Kubernetes pods, deployments, services, and ingress controllers.")
                .category("Cloud & DevOps")
                .difficulty("Advanced")
                .instructor(instructor2)
                .thumbnailUrl("https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=800&auto=format&fit=crop&q=60")
                .learningObjectives("Containerize applications with Docker;Author production Kubernetes manifests;Implement CI/CD pipeline automation")
                .prerequisites("Basic Linux shell commands and networking fundamentals.")
                .estimatedDuration("16 Hours")
                .published(false) // Unpublished Draft!
                .build();
        courseRepository.save(course4);

        // 6. Seed Student Enrollments for Alex Rivera (student1)
        Enrollment e1 = Enrollment.builder()
                .student(student1)
                .course(course1)
                .status("ACTIVE")
                .build();
        enrollmentRepository.save(e1);

        Enrollment e2 = Enrollment.builder()
                .student(student1)
                .course(course2)
                .status("ACTIVE")
                .build();
        enrollmentRepository.save(e2);

        // 7. Seed Lesson Progress for student1 on course1
        LessonProgress lp1 = LessonProgress.builder()
                .student(student1)
                .lesson(c1m1l1)
                .completed(true)
                .completedAt(LocalDateTime.now().minusDays(2))
                .lastAccessedAt(LocalDateTime.now().minusDays(2))
                .build();
        lessonProgressRepository.save(lp1);

        LessonProgress lp2 = LessonProgress.builder()
                .student(student1)
                .lesson(c1m1l2)
                .completed(true)
                .completedAt(LocalDateTime.now().minusDays(1))
                .lastAccessedAt(LocalDateTime.now().minusDays(1))
                .build();
        lessonProgressRepository.save(lp2);

        LessonProgress lp3 = LessonProgress.builder()
                .student(student1)
                .lesson(c1m2l1)
                .completed(false)
                .lastAccessedAt(LocalDateTime.now().minusHours(3))
                .build();
        lessonProgressRepository.save(lp3);

        // 8. Seed Graded Submission for student1 on c1a1
        Submission sub1 = Submission.builder()
                .assignment(c1a1)
                .student(student1)
                .textResponse("Completed the implementation of Student and Course REST controllers with JPA repositories and Bean Validation. Added custom exceptions and response DTOs as requested.")
                .status("GRADED")
                .marksAwarded(BigDecimal.valueOf(95.00))
                .feedback("Outstanding work Alex! Excellent architecture layering, proper exception handling, and clean code formatting.")
                .gradedAt(LocalDateTime.now().minusHours(12))
                .gradedBy(instructor1)
                .build();
        submissionRepository.save(sub1);

        log.info("Database seeding completed successfully!");
        log.info("Default Accounts Created:");
        log.info(" - ADMIN:      admin@learnhub.com / Admin@123");
        log.info(" - INSTRUCTOR: instructor@learnhub.com / Instructor@123");
        log.info(" - INSTRUCTOR: marcus@learnhub.com / Instructor@123");
        log.info(" - STUDENT:    student@learnhub.com / Student@123");
        log.info(" - STUDENT:    jane@learnhub.com / Student@123");
    }
}
