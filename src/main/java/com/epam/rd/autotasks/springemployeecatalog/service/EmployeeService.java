package com.epam.rd.autotasks.springemployeecatalog.service;

import com.epam.rd.autotasks.springemployeecatalog.data.EmployeeRepository;
import com.epam.rd.autotasks.springemployeecatalog.domain.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> getAllEmployees(int page, int size, String sort) {
        return employeeRepository.findAll(page, size, sort);
    }

    public Optional<Employee> getEmployeeById(Long employeeId, boolean fullChain) {
        return employeeRepository.findById(employeeId, fullChain);
    }
    public List<Employee> getEmployeeByManager(Long managerId, int page, int size, String sort) {
        return employeeRepository.findEmployeesByManager(managerId, page, size, sort);
    }

    public List<Employee> getEmployeeByDepartment(Long departmentId, int page, int size, String sort) {
        return employeeRepository.findEmployeesByDepartmentId(departmentId, page, size, sort);
    }
}
