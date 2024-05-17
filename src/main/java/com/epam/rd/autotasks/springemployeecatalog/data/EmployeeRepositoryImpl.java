package com.epam.rd.autotasks.springemployeecatalog.data;

import com.epam.rd.autotasks.springemployeecatalog.domain.Department;
import com.epam.rd.autotasks.springemployeecatalog.domain.Employee;
import com.epam.rd.autotasks.springemployeecatalog.domain.FullName;
import com.epam.rd.autotasks.springemployeecatalog.domain.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository{

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EmployeeRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Employee> findAll(Integer page, Integer size, String sort) {
        if (sort != null) {
            if(sort.equals("HIRED")|| sort.equals("hired")) sort += "ATE";
        }
        String query = "SELECT * FROM EMPLOYEE ORDER BY " + sort;
        List<Employee> employees = jdbcTemplate.query(query, new EmployeeRowMapper(jdbcTemplate, false));
        return getList(employees, page, size);
    }



    @Override
    public Optional<Employee> findById(Long id, boolean fullChain) {
        String query = "SELECT * FROM EMPLOYEE WHERE ID = ?";
        Employee employee = jdbcTemplate.queryForObject(query, new Object[]{id}, new EmployeeRowMapper(jdbcTemplate, fullChain));
        return Optional.ofNullable(employee);
    }


    @Override
    public List<Employee> findEmployeesByManager(Long managerId, Integer page, Integer size, String sort) {
        if(sort.equals("HIRED")|| sort.equals("hired")) sort += "ATE";
        String query = "SELECT * FROM EMPLOYEE WHERE MANAGER = ? ORDER BY " + sort;
        List<Employee> employees = jdbcTemplate.query(query, new Object[]{managerId}, new EmployeeRowMapper(jdbcTemplate, false));
        return getList(employees, page, size);
    }


    @Override
    public List<Employee> findEmployeesByDepartmentId(Long departmentId, Integer page, Integer size, String sort) {
        if(sort.equals("HIRED")|| sort.equals("hired")) sort += "ATE";
        String query = "SELECT * FROM EMPLOYEE WHERE DEPARTMENT = ? ORDER BY " + sort;
        List<Employee> employees = jdbcTemplate.query(query, new Object[]{departmentId}, new EmployeeRowMapper(jdbcTemplate, false));
        return getList(employees, page, size);
    }

    @Override
    public Long findDepartmentIdByName(String departmentName) {
        String query = "SELECT ID FROM DEPARTMENT WHERE NAME = ?";
        try {
            return jdbcTemplate.queryForObject(query, new Object[]{departmentName}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Department with name '" + departmentName + "' not found");
        }
    }

    private List<Employee> getList(List<Employee> employees, Integer page, Integer size) {
        if (page == null || size == null) {
            return employees;
        }
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, employees.size());
        if (startIndex > endIndex) {
            return Collections.emptyList();
        }
        return employees.subList(startIndex, endIndex);
    }

    private static class EmployeeRowMapper implements RowMapper<Employee> {
        private final JdbcTemplate jdbcTemplate;
        private final boolean fullChain;

        private EmployeeRowMapper(JdbcTemplate jdbcTemplate, boolean fullChain) {
            this.jdbcTemplate = jdbcTemplate;
            this.fullChain = fullChain;
        }

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
            Long departmentId = resultSet.getLong("DEPARTMENT");
            Department department = findDepartment(departmentId);
            Employee manager = null;
            if (fullChain) {
                manager = findFullManagerChain(managerId, fullChain);
            } else {
                manager = findImmediateManager(managerId);
            }
            return new Employee(id, fullName, position, hired, salary, manager, department);
        }

        private Employee findImmediateManager(Long managerId) {
            if (managerId == 0) return null;
            String query = "SELECT * FROM EMPLOYEE WHERE ID = ?";
            return (Employee) jdbcTemplate.queryForObject(query, new Object[]{managerId}, new EmployeeWithNoManagerRowMapper(jdbcTemplate, false));
        }

        private Employee findFullManagerChain(Long managerId, boolean fullChain) {
            if (managerId == 0) return null;
            String query = "SELECT * FROM EMPLOYEE WHERE ID = ?";
            return jdbcTemplate.queryForObject(query, new Object[]{managerId}, new EmployeeRowMapper(jdbcTemplate, fullChain));
        }

        private Department findDepartment(Long departmentId) {
            if (departmentId == 0) return null;
            String query = "SELECT * FROM DEPARTMENT WHERE ID = ?";
            return jdbcTemplate.queryForObject(query, new Object[]{departmentId}, new DepartmentRowMapper());
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

        private class EmployeeWithNoManagerRowMapper implements RowMapper{
            public EmployeeWithNoManagerRowMapper(JdbcTemplate jdbcTemplate, boolean b) {
            }

            @Override
            public Object mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                Long id = resultSet.getLong("ID");
                String firstName = resultSet.getString("FIRSTNAME");
                String lastName = resultSet.getString("LASTNAME");
                String middleName = resultSet.getString("MIDDLENAME");
                FullName fullName = new FullName(firstName, lastName, middleName);
                Position position = Position.valueOf(resultSet.getString("POSITION"));
                LocalDate hired = resultSet.getDate("HIREDATE").toLocalDate();
                BigDecimal salary = BigDecimal.valueOf(resultSet.getDouble("SALARY"));
                Long managerId = resultSet.getLong("MANAGER");
                Long departmentId = resultSet.getLong("DEPARTMENT");
                Department department = findDepartment(departmentId);
                Employee manager = null;
                return new Employee(id, fullName, position, hired, salary, manager, department);
            }
        }
    }
}
