package com.epam.rd.autotasks.springemployeecatalog.service;

import com.epam.rd.autotasks.springemployeecatalog.data.EmployeeRepository;
import com.epam.rd.autotasks.springemployeecatalog.domain.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<Employee> getAllEmployees(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findAll(pageable);
    }

    public Optional<Employee> getEmployeeById(Long employeeId, boolean fullChain) {
        Optional<Employee> employeeOptional = employeeRepository.findById(String.valueOf(employeeId));
        if (employeeOptional.isPresent() && fullChain) {
            Employee employee = employeeOptional.get();
            fetchFullManagerChain(employee);

        }
        return employeeOptional;
    }

    private void fetchFullManagerChain(Employee employee) {
//        if (employee.getManager() != null) {
//            Optional<Employee> managerOptional = employeeRepository.findById(String.valueOf(employee.getManager().getId()));
//            managerOptional.ifPresent(manager -> {
//            });
//        }
    }


    public Page<Employee> getEmployeeByManager(Long managerId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findEmployeesByManager(managerId, pageable);
    }

    public Page<Employee> getEmployeeByDepartment(Long departmentId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findEmployeesByDepartmentId(departmentId, pageable);
    }
}
