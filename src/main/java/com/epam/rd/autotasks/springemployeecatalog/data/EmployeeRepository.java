package com.epam.rd.autotasks.springemployeecatalog.data;

import com.epam.rd.autotasks.springemployeecatalog.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    @Query("SELECT e FROM EMPLOYEE e WHERE e.manager.id = :managerId")
    Page<Employee> findEmployeesByManager(Long managerId, Pageable pageable);

    @Query("SELECT e FROM EMPLOYEE e WHERE e.department.id = :departmentId")
    Page<Employee> findEmployeesByDepartmentId(Long departmentId, Pageable pageable);
}
