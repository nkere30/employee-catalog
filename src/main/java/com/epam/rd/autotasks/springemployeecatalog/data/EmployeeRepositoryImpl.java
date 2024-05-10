package com.epam.rd.autotasks.springemployeecatalog.data;

import com.epam.rd.autotasks.springemployeecatalog.domain.Department;
import com.epam.rd.autotasks.springemployeecatalog.domain.Employee;
import com.epam.rd.autotasks.springemployeecatalog.domain.FullName;
import com.epam.rd.autotasks.springemployeecatalog.domain.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository{

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EmployeeRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<Employee> findAll(int page, int size, String sort) {
        String query = "SELECT * FROM EMPLOYEE ORDER BY " + sort;
        List<Employee> employees = jdbcTemplate.query(query, new EmployeeRowMapper(jdbcTemplate));
        return getPage(employees, page, size);
    }



    @Override
    public Optional<Employee> findById(Long id, boolean fullChain) {
        String query = "SELECT * FROM EMPLOYEE WHERE ID = ?";
        Employee employee = jdbcTemplate.queryForObject(query, new Object[]{id}, new EmployeeRowMapper(jdbcTemplate));
        return Optional.ofNullable(employee);
    }


    @Override
    public Page<Employee> findEmployeesByManager(Long managerId, int page, int size, String sort) {
        String query = "SELECT * FROM EMPLOYEE WHERE MANAGER = ? ORDER BY " + sort;
        List<Employee> employees = jdbcTemplate.query(query, new Object[]{managerId}, new EmployeeRowMapper(jdbcTemplate));
        return getPage(employees, page, size);
    }


    @Override
    public Page<Employee> findEmployeesByDepartmentId(Long departmentId, int page, int size, String sort) {
        String query = "SELECT * FROM EMPLOYEE WHERE DEPARTMENT = ? ORDER BY " + sort;
        List<Employee> employees = jdbcTemplate.query(query, new Object[]{departmentId}, new EmployeeRowMapper(jdbcTemplate));
        return getPage(employees, page, size);
    }
    private Page<Employee> getPage(List<Employee> employees, int page, int size) {
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, employees.size());
        List<Employee> employeeSubList = employees.subList(startIndex, endIndex);
        return new PageImpl<>(employeeSubList, PageRequest.of(page, size), employees.size());
    }

    private static class EmployeeRowMapper implements RowMapper<Employee> {
        private final JdbcTemplate jdbcTemplate;

        private EmployeeRowMapper(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        private static List<Long> employeeManager = new ArrayList<>();

        @Override
        public Employee mapRow(ResultSet resultSet, int i) throws SQLException {
            Long id = resultSet.getLong("ID");
            String firstName = resultSet.getString("FIRSTNAME");
            String lastName = resultSet.getString("LASTNAME");
            String middleName = resultSet.getString("MIDDLENAME");
            FullName fullName = new FullName(firstName, lastName, middleName);
            Position position = Position.valueOf(resultSet.getString("POSITION"));
            LocalDate hired = resultSet.getDate("HIREDATE").toLocalDate();
            BigDecimal salary = BigDecimal.valueOf(resultSet.getDouble("SALARY"));
            Long managerId = resultSet.getLong("MANAGER");
            Employee manager = null;
            if (employeeManager.isEmpty()) {
                manager = findEmployee(managerId);
            }
            Long departmentId = resultSet.getLong("DEPARTMENT");
            Department department = findDepartment(departmentId);
            Employee employee = new Employee(id, fullName, position, hired, salary, manager, department);
            return employee;
        }

        private Department findDepartment(Long departmentId) {
            if (departmentId == 0) return null;
            String query = "SELECT * FROM DEPARTMENT WHERE ID = ?";
            return jdbcTemplate.queryForObject(query, new Object[]{departmentId}, new DepartmentRowMapper());
        }

        private Employee findEmployee(Long managerId) {
            if (managerId == 0) return null;
            employeeManager.add(managerId);
            String query = "SELECT * FROM EMPLOYEE WHERE ID = ?";
            return jdbcTemplate.queryForObject(query, new Object[]{managerId}, new EmployeeRowMapper(jdbcTemplate));
        }

        private static class DepartmentRowMapper implements RowMapper<Department> {

            @Override
            public Department mapRow(ResultSet resultSet, int i) throws SQLException {
                Long id = resultSet.getLong("ID");
                String name = resultSet.getString("NAME");
                String location = resultSet.getString("LOCATION");
                return new Department(id, name, location);
            }
        }

    }
}
