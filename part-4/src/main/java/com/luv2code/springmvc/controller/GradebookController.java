package com.luv2code.springmvc.controller;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GradebookController {

    @Autowired
    private Gradebook gradebook;

    @Autowired
    private StudentAndGradeService studentAndGradeService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getStudents(Model model) {
        Iterable<CollegeStudent> studentIterable = studentAndGradeService.getGradebook();

        model.addAttribute("students", studentIterable);

        return "index";
    }

    @PostMapping("/")
    public String createStudent(@ModelAttribute CollegeStudent student, Model model) {
        studentAndGradeService.createStudent(student.getFirstname(), student.getLastname(), student.getEmailAddress());

        model.addAttribute("students", studentAndGradeService.getGradebook());

        return "index";
    }

    @GetMapping("/delete/student/{id}")
    public String deleteStudent(@PathVariable int id, Model model) {
        if (!studentAndGradeService.checkIfStudentIsNull(id)) {
            return "error";
        }

        studentAndGradeService.deleteStudent(id);

        model.addAttribute("students", studentAndGradeService.getGradebook());

        return "index";
    }


    @GetMapping("/studentInformation/{id}")
    public String studentInformation(@PathVariable int id, Model m) {
        if (!studentAndGradeService.checkIfStudentIsNull(id)) {
            return "error";
        }

        populateModelWithStudentInformation(id, m);

        return "studentInformation";
    }

    @PostMapping("/grades")
    public String createGrade(@RequestParam double grade, @RequestParam String gradeType, @RequestParam int studentId,
                              Model model) {
        if (!studentAndGradeService.checkIfStudentIsNull(studentId)) {
            return "error";
        }

        boolean success = studentAndGradeService.createGrade(grade, studentId, gradeType);

        if (!success) {
            return "error";
        }

        populateModelWithStudentInformation(studentId, model);

        return "studentInformation";
    }

    @GetMapping("/grades/{id}/{gradeType}")
    public String deleteGrade(@PathVariable int id, @PathVariable String gradeType, Model model) {
        int studentId = studentAndGradeService.deleteGrade(id, gradeType);

        if (studentId == 0) {
            return "error";
        }

        populateModelWithStudentInformation(studentId, model);

        return "studentInformation";
    }

    private void populateModelWithStudentInformation(int studentId, Model m) {
        GradebookCollegeStudent student = studentAndGradeService.studentInformation(studentId);

        m.addAttribute("student", student);

        StudentGrades studentGrades = student.getStudentGrades();
        if (studentGrades.getMathGradeResults().size() > 0) {
            m.addAttribute("mathAverage", studentGrades.findGradePointAverage(studentGrades.getMathGradeResults()));
        } else {
            m.addAttribute("mathAverage", "N/A");
        }

        if (studentGrades.getScienceGradeResults().size() > 0) {
            m.addAttribute("scienceAverage", studentGrades.findGradePointAverage(studentGrades.getScienceGradeResults()));
        } else {
            m.addAttribute("scienceAverage", "N/A");
        }

        if (studentGrades.getHistoryGradeResults().size() > 0) {
            m.addAttribute("historyAverage", studentGrades.findGradePointAverage(studentGrades.getHistoryGradeResults()));
        } else {
            m.addAttribute("historyAverage", "N/A");
        }
    }

}
