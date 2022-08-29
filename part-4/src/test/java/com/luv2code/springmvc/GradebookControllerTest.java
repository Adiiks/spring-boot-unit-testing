package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource("/application.properties")
@AutoConfigureMockMvc
@SpringBootTest
public class GradebookControllerTest {

    static MockHttpServletRequest request;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StudentDao studentDao;
    
    @Autowired
    MathGradesDao mathGradesDao;

    @Mock
    StudentAndGradeService studentAndGradeServiceMock;

    @Autowired
    StudentAndGradeService studentService;

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

    @BeforeAll
    static void setup() {
        request = new MockHttpServletRequest();
        request.setParameter("firstname", "Chad");
        request.setParameter("lastname", "Darby");
        request.setParameter("emailAddress", "chad.darby@luv2code_school.com");
    }

    @BeforeEach
    void beforeEach() {
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
    void getStudentHttpRequest() throws Exception {
        CollegeStudent studentOne = new GradebookCollegeStudent("Eric", "Roby",
                "eric.roby@luv2code_school.com");

        CollegeStudent studentTwo = new GradebookCollegeStudent("Chad", "Darby",
                "chad.darby@luv2code_school.com");

        List<CollegeStudent> students = Arrays.asList(studentOne, studentTwo);

        when(studentAndGradeServiceMock.getGradebook()).thenReturn(students);

        assertIterableEquals(students, studentAndGradeServiceMock.getGradebook());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void createStudentHttpRequest() throws Exception {
        CollegeStudent studentOne = new CollegeStudent("Eric", "Roby",
                "eric.roby@luv2code_school.com");

        List<CollegeStudent> studentList = List.of(studentOne);

        when(studentAndGradeServiceMock.getGradebook()).thenReturn(studentList);

        assertIterableEquals(studentList, studentAndGradeServiceMock.getGradebook());

        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstname", request.getParameter("firstname"))
                .param("lastname", request.getParameter("lastname"))
                .param("emailAddress", request.getParameter("emailAddress")))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        CollegeStudent verifyStudent = studentDao.findByEmailAddress("chad.darby@luv2code_school.com");

        assertNotNull(verifyStudent, "Student should be found");
    }

    @Test
    void deleteStudentHttpRequest() throws Exception {
        assertTrue(studentDao.existsById(1));

        mockMvc.perform(get("/delete/student/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        assertFalse(studentDao.existsById(1));
    }

    @Test
    void deleteStudentHttpRequestErrorPage() throws Exception {
        mockMvc.perform(get("/delete/student/{id}", 0))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    void studentInformationHttpRequest() throws Exception {
        assertTrue(studentDao.existsById(1));

        mockMvc.perform(get("/studentInformation/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("studentInformation"));
    }

    @Test
    void studentInformationHttpStudentDoesNotExistsRequest() throws Exception {
        assertFalse(studentDao.existsById(0));

        mockMvc.perform(get("/studentInformation/{id}", 0))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    void createValidGradeHttpRequest() throws Exception {
        assertTrue(studentDao.existsById(1));

        GradebookCollegeStudent student = studentService.studentInformation(1);

        assertEquals(1, student.getStudentGrades().getMathGradeResults().size());

        mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "math")
                .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("studentInformation"));

        student = studentService.studentInformation(1);

        assertEquals(2, student.getStudentGrades().getMathGradeResults().size());
    }

    @Test
    void createValidStudentHttpRequestStudentDoesNotExistEmptyResponse() throws Exception {
        mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "history")
                .param("studentId", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    void createNonValidStudentHttpRequestTypeDoesNotExistEmptyResponse() throws Exception {
        mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("grade", "85.00")
                        .param("gradeType", "literature")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    void deleteValidGradeHttpRequest() throws Exception {
        assertTrue(mathGradesDao.existsById(1));

        mockMvc.perform(get("/grades/{id}/{gradeType}", 1, "math"))
                .andExpect(status().isOk())
                .andExpect(view().name("studentInformation"));

        assertFalse(mathGradesDao.existsById(1));
    }

    @Test
    void deleteValidGradeHttpRequestGradeIdDoesNotExistEmptyResponse() throws Exception {
        assertFalse(mathGradesDao.existsById(2));

        mockMvc.perform(get("/grades/{id}/{gradeType}", 2, "math"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    void deleteNonValidGradeHttpRequest() throws Exception {
        mockMvc.perform(get("/grades/{id}/{gradeType}", 1, "literature"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }
}
