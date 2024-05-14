package com.epam.rd.autotasks.springemployeecatalog.data;

import com.epam.rd.autotasks.springemployeecatalog.domain.Employee;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository{
    List<Employee> findAll(int page, int size, String sort);

    Optional<Employee> findById(Long id, boolean fullChain);

    List<Employee> findEmployeesByManager(Long managerId, int page, int size, String sort);

    List<Employee> findEmployeesByDepartmentId(Long departmentId, int page, int size, String sort);

    Long findDepartmentIdByName(String departmentName);
}
