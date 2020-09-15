package org.unicon.lex.services.external;

import org.unicon.lex.db.model.Content;
import org.unicon.lex.db.model.Course;
import org.unicon.lex.db.model.Enrollment;
import org.unicon.lex.db.model.Policy;
import org.unicon.lex.db.model.Student;

import java.util.List;

public interface RDSService {

    Student getStudentByUserName(String userName);

    Course getCourseByName(String courseName);

    List<Enrollment> getEnrollmentsByStudent(String userName);

    List<Enrollment> getEnrollmentsByCourse(String courseName);

    List<Enrollment> getEnrollmentsByStudentAndCourse(String userName, String courseName);

    Policy getPolicy(String courseName, Integer state);

    Content getContentById(int id);

    List<Content> getContentByCoursename(String courseName);
}
