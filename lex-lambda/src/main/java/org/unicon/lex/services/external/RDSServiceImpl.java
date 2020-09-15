package org.unicon.lex.services.external;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.db.model.Content;
import org.unicon.lex.db.model.Course;
import org.unicon.lex.db.model.CourseTopic;
import org.unicon.lex.db.model.CourseTopicState;
import org.unicon.lex.db.model.Enrollment;
import org.unicon.lex.db.model.Policy;
import org.unicon.lex.db.model.Student;
import org.unicon.lex.db.model.TopicState;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class RDSServiceImpl implements RDSService {
    private static final String URL = "rds.db.url";
    private static final String USER = "rds.db.user";
    private static final String PW = "rds.db.pw";

    private static final Logger LOGGER = LogManager.getLogger(RDSServiceImpl.class);

    private final Properties properties;

    public RDSServiceImpl(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Student getStudentByUserName(String userName) {
        Student student = null;
        try (Connection conn = DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER),
                properties.getProperty(PW));
             PreparedStatement pstmt = conn.prepareStatement("select * from student where user_name = ?")) {
            pstmt.setString(1, userName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    student = new Student();
                    student.setId(rs.getInt("id"));
                    student.setUserName(userName);
                    student.setFirstName(rs.getString("first_name"));
                    student.setLastName(rs.getString("last_name"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }
        return student;
    }

    @Override
    public Course getCourseByName(String courseName) {
        Course course = null;
        try (Connection conn = DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER),
                properties.getProperty(PW));
             PreparedStatement pstmt = conn.prepareStatement("select * from course where course_name = ?")) {
            pstmt.setString(1, courseName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    course = new Course();
                    course.setId(rs.getInt("id"));
                    course.setCourseName(courseName);
                    course.setUrl(rs.getString("url"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }

        if (course != null) {
            addCourseTopics(course);
        }
        return course;
    }

    private void addCourseTopics(Course course) {
        course.setTopics(new HashSet<>());
        try (Connection conn = DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER),
                properties.getProperty(PW));
             PreparedStatement pstmt = conn.prepareStatement("select * from course_topic where course_id = ?")) {
            pstmt.setInt(1, course.getId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CourseTopic topic = new CourseTopic();
                    course.getTopics().add(topic);
                    topic.setId(rs.getInt("id"));
                    topic.setName(rs.getString("name"));
                    topic.setTopicorder(rs.getInt("topicorder"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudent(String userName) {
        List<Enrollment> enrollments = new ArrayList<>();

        int studenId = -1;
        List<Integer> courseIds = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER),
                properties.getProperty(PW));
             PreparedStatement pstmt = conn.prepareStatement(
                     "select e.id, course_id, student_id, state from enrollment e, student s where s.user_name = ? and s.id = e.student_id")) {
            pstmt.setString(1, userName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollments.add(enrollment);
                    enrollment.setId(rs.getInt("id"));
                    enrollment.setState(rs.getInt("state"));

                    if (studenId == -1) {
                        studenId = rs.getInt("student_id");
                    }
                    courseIds.add(rs.getInt("course_id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }

        if (!enrollments.isEmpty()) {
            Student student = getStudentById(studenId);

            for (int i = 0; i < enrollments.size(); i++) {
                Enrollment current = enrollments.get(i);
                current.setStudent(student);
                //TODO: not very efficient
                Course course = getCourseById(courseIds.get(i));
                current.setCourse(course);

                setTopicStates(course, current);
            }
        }
        return enrollments;
    }

    private Student getStudentById(int id) {
        Student student = null;
        try (Connection conn = DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER),
                properties.getProperty(PW));
             PreparedStatement pstmt = conn.prepareStatement("select * from student where id = ?")) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    student = new Student();
                    student.setId(id);
                    student.setUserName(rs.getString("user_name"));
                    student.setFirstName(rs.getString("first_name"));
                    student.setLastName(rs.getString("last_name"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }
        return student;
    }

    private Course getCourseById(int id) {
        Course course = null;
        try (Connection conn = DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER),
                properties.getProperty(PW));
             PreparedStatement pstmt = conn.prepareStatement("select * from course where id = ?")) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    course = new Course();
                    course.setId(id);
                    course.setCourseName(rs.getString("course_name"));
                    course.setUrl(rs.getString("url"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }

        if (course != null) {
            addCourseTopics(course);
        }
        return course;
    }

    @Override
    public List<Enrollment> getEnrollmentsByCourse(String courseName) {
        List<Enrollment> enrollments = new ArrayList<>();

        int courseId = -1;
        List<Integer> studentIds = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER),
                properties.getProperty(PW));
             PreparedStatement pstmt = conn.prepareStatement(
                     "select  e.id, course_id, student_id, state  from enrollment e, course c where c.course_name = ? and c.id = e.course_id")) {
            pstmt.setString(1, courseName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollments.add(enrollment);
                    enrollment.setId(rs.getInt("id"));
                    enrollment.setState(rs.getInt("state"));

                    if (courseId == -1) {
                        courseId = rs.getInt("course_id");
                    }
                    studentIds.add(rs.getInt("student_id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }

        if (!enrollments.isEmpty()) {
            Course course = getCourseById(courseId);

            for (int i = 0; i < enrollments.size(); i++) {
                Enrollment current = enrollments.get(i);
                current.setCourse(course);
                //TODO: not very efficient
                Student student = getStudentById(studentIds.get(i));
                current.setStudent(student);
                setTopicStates(course, current);
            }
        }
        return enrollments;
    }

    private void setTopicStates(Course course, Enrollment enrollment) {
        Set<TopicState> states = new HashSet<>();
        enrollment.setTopicStates(states);

        for (CourseTopic courseTopic : course.getTopics()) {
            TopicState state = new TopicState();
            state.setTopic(courseTopic);

            CourseTopicState courseTopicState = new CourseTopicState();
            state.setCourseTopicState(courseTopicState);
            courseTopicState.setName(courseTopic.getName());
            courseTopicState.setValue(enrollment.getState());

            states.add(state);
        }
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudentAndCourse(String userName, String courseName) {
        List<Enrollment> enrollments = new ArrayList<>();

        int studenId = -1;
        int courseId = -1;
        try (Connection conn = DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER),
                properties.getProperty(PW));
             PreparedStatement pstmt = conn.prepareStatement(
                     "select e.id, course_id, student_id, state " +
                     "from enrollment e, student s, course c " +
                     "where s.user_name = ? " +
                     "and c.course_name = ? " +
                     "and e.course_id = c.id " +
                     "and s.id = e.student_id")) {
            pstmt.setString(1, userName);
            pstmt.setString(2,  courseName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollments.add(enrollment);
                    enrollment.setId(rs.getInt("id"));
                    enrollment.setState(rs.getInt("state"));

                    if (studenId == -1) {
                        studenId = rs.getInt("student_id");
                    }
                    if (courseId == -1) {
                        courseId = rs.getInt("course_id");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }

        if (!enrollments.isEmpty()) {
            Student student = getStudentById(studenId);
            Course course = getCourseById(courseId);
            for (int i = 0; i < enrollments.size(); i++) {
                Enrollment current = enrollments.get(i);
                current.setStudent(student);
                current.setCourse(course);

                setTopicStates(course, current);
            }
        }
        return enrollments;
    }

    @Override
    public Policy getPolicy(String courseName, Integer state) {
        Policy policy = null;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "select p.id, p.course_id, p.state, content " +
                     "from policy p, course c " +
                     "where p.state = ? " +
                     "and p.course_id = c.id " +
                     "and c.course_name = ?"
                     );) {
            pstmt.setLong(1, state);
            pstmt.setString(2, courseName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    policy = new Policy();
                    policy.setId(rs.getInt("id"));
                    policy.setCourseId(rs.getInt("course_id"));
                    policy.setContent(rs.getString("content"));
                    policy.setState(rs.getInt("state"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }

        LOGGER.error("Policy {}", policy);
        return policy;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER),
                properties.getProperty(PW));
    }

    @Override
    public Content getContentById(int id) {
        LOGGER.error("content ID [{}]",  id);
        Content content = null;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "select id, course_id, name, url " +
                     "from content c " +
                     "where id = ?");) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    content = new Content();
                    content.setId(rs.getInt("id"));
                    content.setCourseId(rs.getInt("course_id"));
                    content.setName(rs.getString("name"));
                    content.setUrl(rs.getString("url"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }

        LOGGER.error("Content {}", content);
        return content;
    }

    @Override
    public List<Content> getContentByCoursename(String courseName) {
        LOGGER.error("courseName [{}]", courseName);
        List<Content> contents = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "select c.id, course_id, name, c.url " +
                     "from content c, course cs " +
                     "where c.course_id = cs.id " +
                     "and cs.course_name = ?");) {
            pstmt.setString(1, courseName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Content content = new Content();
                    content.setId(rs.getInt("id"));
                    content.setCourseId(rs.getInt("course_id"));
                    content.setName(rs.getString("name"));
                    content.setUrl(rs.getString("url"));
                    contents.add(content);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage(), e);
        }

        LOGGER.error("Content {}", contents);
        return contents;
    }
}
