package com.luv2code.springmvc;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application.properties")
@SpringBootTest
public class StudentAndGradeServiceTest {

    @Autowired
    StudentAndGradeService studentService;

    @Autowired
    StudentDao studentDao;

    @Autowired
    MathGradesDao mathGradesDao;

    @Autowired
    ScienceGradesDao scienceGradesDao;

    @Autowired
    HistoryGradesDao historyGradesDao;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grades}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grades}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grades}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grades}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grades}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grades}")
    private String sqlDeleteHistoryGrade;

    @BeforeEach
    void setupDatabase() {
        jdbcTemplate.execute(sqlAddStudent);
        jdbcTemplate.execute(sqlAddMathGrade);
        jdbcTemplate.execute(sqlAddScienceGrade);
        jdbcTemplate.execute(sqlAddHistoryGrade);
    }

    @AfterEach
    void setupAfterTransaction() {
        jdbcTemplate.execute(sqlDeleteStudent);
        jdbcTemplate.execute(sqlDeleteMathGrade);
        jdbcTemplate.execute(sqlDeleteScienceGrade);
        jdbcTemplate.execute(sqlDeleteHistoryGrade);
    }

    @Test
    void createStudentService() {
        studentService.createStudent("Chad", "Darby", "chad.darby@luv2code_school.com");

        CollegeStudent student = studentDao.findByEmailAddress("chad.darby@luv2code_school.com");

        assertEquals("chad.darby@luv2code_school.com", student.getEmailAddress(), "find by email");
    }

    @Test
    void isStudentNullCheck() {
        assertTrue(studentService.checkIfStudentIsNull(1));

        assertFalse(studentService.checkIfStudentIsNull(0));
    }

    @Test
    void deleteStudentService() {
        Optional<CollegeStudent> deletedStudent = studentDao.findById(1);
        Optional<MathGrade> deletedMathGrade = mathGradesDao.findById(1);
        Optional<ScienceGrade> deletedScienceGrade = scienceGradesDao.findById(1);
        Optional<HistoryGrade> deletedHistoryGrade = historyGradesDao.findById(1);

        assertTrue(deletedStudent.isPresent(), "return true");
        assertTrue(deletedMathGrade.isPresent(), "return true");
        assertTrue(deletedScienceGrade.isPresent(), "return true");
        assertTrue(deletedHistoryGrade.isPresent(), "return true");

        studentService.deleteStudent(1);

        deletedStudent = studentDao.findById(1);
        deletedMathGrade = mathGradesDao.findById(1);
        deletedScienceGrade = scienceGradesDao.findById(1);
        deletedHistoryGrade = historyGradesDao.findById(1);

        assertFalse(deletedStudent.isPresent(), "return false");
        assertFalse(deletedMathGrade.isPresent(), "return false");
        assertFalse(deletedScienceGrade.isPresent(), "return false");
        assertFalse(deletedHistoryGrade.isPresent(), "return false");
    }

    @Sql("/insert-data.sql")
    @Test
    void getGradebookService() {
        Iterable<CollegeStudent> students = studentService.getGradebook();

        List<CollegeStudent> studentList = new ArrayList<>();

        students.iterator().forEachRemaining(studentList::add);

        assertEquals(5, studentList.size());
    }

    @Test
    void createGradeService() {
        assertTrue(studentService.createGrade(80.50, 1, "math"));
        assertTrue(studentService.createGrade(80.50, 1, "science"));
        assertTrue(studentService.createGrade(80.50, 1, "history"));

        Iterable<MathGrade> mathGrades = mathGradesDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> scienceGrades = scienceGradesDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> historyGrades = historyGradesDao.findGradeByStudentId(1);

        assertEquals(2, ((Collection<MathGrade>) mathGrades).size(), "Student has math grades");
        assertEquals(2, ((Collection<ScienceGrade>) scienceGrades).size(), "Student has science grades");
        assertEquals(2, ((Collection<HistoryGrade>) historyGrades).size(), "Student has history grades");
    }

    @Test
    void createGradeServiceReturnFalse() {
        assertFalse(studentService.createGrade(105.00, 1, "math"));
        assertFalse(studentService.createGrade(-5, 1, "math"));
        assertFalse(studentService.createGrade(80.50, 2, "math"));
        assertFalse(studentService.createGrade(80.50, 1, "literature"));
    }

    @Test
    void deleteGradeService() {
        assertEquals(1, studentService.deleteGrade(1, "math"),
                "Returns student id after delete");

        assertEquals(1, studentService.deleteGrade(1, "science"),
                "Returns student id after delete");

        assertEquals(1, studentService.deleteGrade(1, "history"),
                "Returns student id after delete");
    }

    @Test
    void deleteGradeServiceReturnStudentIdOfZero() {
        assertEquals(0, studentService.deleteGrade(0, "science"),
                "No student should have 0 id");

        assertEquals(0, studentService.deleteGrade(0, "literature"),
                "No student should have a literature class");
    }

    @Test
    void studentInformation() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(1);

        assertNotNull(gradebookCollegeStudent);
        assertEquals(1, gradebookCollegeStudent.getId());
        assertEquals("Eric", gradebookCollegeStudent.getFirstname());
        assertEquals("Roby", gradebookCollegeStudent.getLastname());
        assertEquals("eric.roby@luv2code_school.com", gradebookCollegeStudent.getEmailAddress());
        assertEquals(1, gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size());
        assertEquals(1, gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size());
        assertEquals(1, gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size());
    }

    @Test
    void studentInformationReturnNull() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(0);

        assertNull(gradebookCollegeStudent);
    }
}
