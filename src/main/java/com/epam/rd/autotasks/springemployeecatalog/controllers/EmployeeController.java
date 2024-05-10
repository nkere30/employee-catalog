package com.epam.rd.autotasks.springemployeecatalog.controllers;

import com.epam.rd.autotasks.springemployeecatalog.domain.Employee;
import com.epam.rd.autotasks.springemployeecatalog.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public Page<Employee> getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam int size,
                                          @RequestParam String sort) {
        return employeeService.getAllEmployees(page, size, sort);
    }

    @GetMapping("/{employee_id}")
    public Optional<Employee> getEmployeeById(@PathVariable("employee_id") Long employeeId,
                                              @RequestParam(defaultValue = "false") boolean full_chain) {
        return employeeService.getEmployeeById(employeeId, full_chain);
    }

    @GetMapping("/by_manager/{managerId}")
    public Page<Employee> getEmployeeByManager(@PathVariable("managerId") Long managerId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam int size,
                                               @RequestParam String sort) {
        return employeeService.getEmployeeByManager(managerId, page, size, sort);
    }

    @GetMapping("/by_department/{departmentId}")
    public Page<Employee> getEmployeeByDepartment(@PathVariable("departmentId") Long departmentId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam int size,
                                                  @RequestParam String sort) {
        return employeeService.getEmployeeByDepartment(departmentId, page, size, sort);
    }
}
