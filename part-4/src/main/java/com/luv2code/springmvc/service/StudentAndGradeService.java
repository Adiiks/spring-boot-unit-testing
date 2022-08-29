package com.luv2code.springmvc.service;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class StudentAndGradeService {

    private final StudentDao studentDao;
    private final MathGradesDao mathGradesDao;
    private final ScienceGradesDao scienceGradesDao;
    private final HistoryGradesDao historyGradesDao;
    private final MathGrade mathGrade;
    private final ScienceGrade scienceGrade;
    private final HistoryGrade historyGrade;
    private final StudentGrades studentGrades;

    public StudentAndGradeService(StudentDao studentDao, MathGradesDao mathGradesDao, ScienceGradesDao scienceGradesDao,
                                  HistoryGradesDao historyGradesDao, MathGrade mathGrade, ScienceGrade scienceGrade,
                                  HistoryGrade historyGrade, StudentGrades studentGrades) {
        this.studentDao = studentDao;
        this.mathGradesDao = mathGradesDao;
        this.scienceGradesDao = scienceGradesDao;
        this.historyGradesDao = historyGradesDao;
        this.mathGrade = mathGrade;
        this.scienceGrade = scienceGrade;
        this.historyGrade = historyGrade;
        this.studentGrades = studentGrades;
    }

    public void createStudent(String firstname, String lastname, String emailAddress) {
        CollegeStudent student = new CollegeStudent(firstname, lastname, emailAddress);
        student.setId(0);
        studentDao.save(student);
    }

    public boolean checkIfStudentIsNull(int id) {
        Optional<CollegeStudent> student = studentDao.findById(id);

        return student.isPresent();
    }

    public void deleteStudent(int studentId) {
        if (studentDao.existsById(studentId)) {
            studentDao.deleteById(studentId);
            mathGradesDao.deleteByStudentId(studentId);
            scienceGradesDao.deleteByStudentId(studentId);
            historyGradesDao.deleteByStudentId(studentId);
        }
    }

    public Iterable<CollegeStudent> getGradebook() {
        return studentDao.findAll();
    }

    public boolean createGrade(double grade, int studentId, String gradeType) {
        if (!studentDao.existsById(studentId)) {
            return false;
        }

        if (grade >= 0 && grade <= 100) {
            if (gradeType.equals("math")) {
                mathGrade.setId(0);
                mathGrade.setGrade(grade);
                mathGrade.setStudentId(studentId);

                mathGradesDao.save(mathGrade);
                return true;
            }
            else if (gradeType.equals("science")) {
                scienceGrade.setId(0);
                scienceGrade.setGrade(grade);
                scienceGrade.setStudentId(studentId);

                scienceGradesDao.save(scienceGrade);
                return true;
            }
            else if (gradeType.equals("history")) {
                historyGrade.setId(0);
                historyGrade.setGrade(grade);
                historyGrade.setStudentId(studentId);

                historyGradesDao.save(historyGrade);
                return true;
            }
        }

        return false;
    }

    public int deleteGrade(int gradeId, String gradeType) {
        AtomicInteger studentId = new AtomicInteger();

        switch (gradeType) {
            case "math":
                Optional<MathGrade> mathGradeOptional = mathGradesDao.findById(gradeId);
                mathGradeOptional.ifPresent(mathGradeLocal -> {
                    studentId.set(mathGradeLocal.getStudentId());
                    mathGradesDao.deleteById(gradeId);
                });
                break;
            case "science":
                Optional<ScienceGrade> scienceGradeOptional = scienceGradesDao.findById(gradeId);
                scienceGradeOptional.ifPresent(scienceGrade -> {
                    studentId.set(scienceGrade.getStudentId());
                    scienceGradesDao.deleteById(gradeId);
                });
                break;
            case "history":
                Optional<HistoryGrade> historyGradeOptional = historyGradesDao.findById(gradeId);
                historyGradeOptional.ifPresent(historyGrade -> {
                    studentId.set(historyGrade.getStudentId());
                    historyGradesDao.deleteById(gradeId);
                });
                break;
        }

        return studentId.get();
    }

    public GradebookCollegeStudent studentInformation(int studentId) {
        if (!studentDao.existsById(studentId)) {
            return null;
        }

        CollegeStudent collegeStudent = studentDao.findById(studentId).get();

        Iterable<MathGrade> mathGrades = mathGradesDao.findGradeByStudentId(studentId);
        Iterable<ScienceGrade> scienceGrades = scienceGradesDao.findGradeByStudentId(studentId);
        Iterable<HistoryGrade> historyGrades = historyGradesDao.findGradeByStudentId(studentId);

        List<Grade> mathGradesList = new ArrayList<>();
        mathGrades.forEach(mathGradesList::add);

        List<Grade> scienceGradesList = new ArrayList<>();
        scienceGrades.forEach(scienceGradesList::add);

        List<Grade> historyGradesList = new ArrayList<>();
        historyGrades.forEach(historyGradesList::add);

        studentGrades.setMathGradeResults(mathGradesList);
        studentGrades.setScienceGradeResults(scienceGradesList);
        studentGrades.setHistoryGradeResults(historyGradesList);

        return new GradebookCollegeStudent(collegeStudent.getId(), collegeStudent.getFirstname(),
                collegeStudent.getLastname(), collegeStudent.getEmailAddress(), studentGrades);
    }
}
