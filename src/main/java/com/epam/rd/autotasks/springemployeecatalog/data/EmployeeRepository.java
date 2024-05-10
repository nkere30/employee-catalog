package com.epam.rd.autotasks.springemployeecatalog.data;

import com.epam.rd.autotasks.springemployeecatalog.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository{
    Page<Employee> findAll(int page, int size, String sort);

    Optional<Employee> findById(Long id, boolean fullChain);

    Page<Employee> findEmployeesByManager(Long managerId, int page, int size, String sort);

    Page<Employee> findEmployeesByDepartmentId(Long departmentId, int page, int size, String sort);
}
