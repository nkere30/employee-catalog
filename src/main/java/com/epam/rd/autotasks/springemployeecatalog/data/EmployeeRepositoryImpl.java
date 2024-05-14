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
    public List<Employee> findAll(int page, int size, String sort) {
        if(sort.equals("HIRED")|| sort.equals("hired")) sort += "ATE";
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
    public List<Employee> findEmployeesByManager(Long managerId, int page, int size, String sort) {
        if(sort.equals("HIRED")|| sort.equals("hired")) sort += "ATE";
        String query = "SELECT * FROM EMPLOYEE WHERE MANAGER = ? ORDER BY " + sort;
        List<Employee> employees = jdbcTemplate.query(query, new Object[]{managerId}, new EmployeeRowMapper(jdbcTemplate, false));
        return getList(employees, page, size);
    }


    @Override
    public List<Employee> findEmployeesByDepartmentId(Long departmentId, int page, int size, String sort) {
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
            throw new IllegalArgumentException();
        }
    }

    private List<Employee> getList(List<Employee> employees, int page, int size) {
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, employees.size());
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
            Employee manager = null;
            if (fullChain) {
                manager = findFullManagerChain(managerId).get(0);
            } else {
                manager = findImmediateManager(managerId);
            }
            Long departmentId = resultSet.getLong("DEPARTMENT");
            Department department = findDepartment(departmentId);
            return new Employee(id, fullName, position, hired, salary, manager, department);
        }

        private Employee findImmediateManager(Long managerId) {
            if (managerId == 0) return null;
            String query = "SELECT * FROM EMPLOYEE WHERE ID = ?";
            return jdbcTemplate.queryForObject(query, new Object[]{managerId}, new EmployeeRowMapper(jdbcTemplate, false));
        }

        private List<Employee> findFullManagerChain(Long managerId) {
            List<Employee> managerChain = new ArrayList<>();
            while (managerId != 0) {
                Employee manager = findImmediateManager(managerId);
                if (manager != null) {
                    managerChain.add(manager);
                    managerId = manager.getManager().getId();
                } else {
                    break;
                }
            }
            return managerChain;
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

    }
}
