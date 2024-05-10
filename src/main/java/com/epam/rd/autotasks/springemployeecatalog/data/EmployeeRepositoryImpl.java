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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        List<Employee> employees = jdbcTemplate.query(query, new EmployeeRowMapper());
        return getPage(employees, page, size);
    }



    @Override
    public Optional<Employee> findById(Long id, boolean fullChain) {
        return Optional.empty();
    }

    @Override
    public Page<Employee> findEmployeesByManager(Long managerId, int page, int size, String sort) {
        String query = "SELECT * FROM EMPLOYEE WHERE MANAGER = ? ORDER BY " + sort;
        List<Employee> employees = jdbcTemplate.query(query, new Object[]{managerId}, new EmployeeRowMapper());
        return getPage(employees, page, size);
    }


    @Override
    public Page<Employee> findEmployeesByDepartmentId(Long departmentId, int page, int size, String sort) {
        String query = "SELECT * FROM EMPLOYEE WHERE DEPARTMENT = ? ORDER BY " + sort;
        List<Employee> employees = jdbcTemplate.query(query, new Object[]{departmentId}, new EmployeeRowMapper());
        return getPage(employees, page, size);
    }
    private Page<Employee> getPage(List<Employee> employees, int page, int size) {
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, employees.size());
        List<Employee> employeeSubList = employees.subList(startIndex, endIndex);
        return new PageImpl<>(employeeSubList, PageRequest.of(page, size), employees.size());
    }
    private static class EmployeeRowMapper implements RowMapper<Employee> {

        @Override
        public Employee mapRow(ResultSet resultSet, int i) throws SQLException {
            Map<Long, Employee> employees = new HashMap<>();
            Map<Long, Long> employeeToManagerMap = new HashMap<>();
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
            Long departmentId = resultSet.getLong("DEPARTMENT");
            Department department = (departmentId != 0) ? new Department(departmentId, null, null) : null;
            if (managerId != 0) {
                employeeToManagerMap.put(id, managerId);
            }
            Employee employee = new Employee(id, fullName, position, hired, salary, null, department);
            employees.put(id, employee);
            employeeToManagerMap.forEach((key, value) -> {
                employees.put(key, findManager(key, employees, employeeToManagerMap));
            });
            return employee;
        }

        private Employee findManager(Long id, Map<Long, Employee> employees, Map<Long, Long> employeeToManagerMap) {
            Employee employee = employees.get(id);
            Employee manager = employees.get(employeeToManagerMap.get(id));
            Employee employeeWithManager;
            if (manager != null) {
                employeeWithManager = new Employee(employee.getId(), employee.getFullName(), employee.getPosition(), employee.getHired(), employee.getSalary(),
                        findManager(manager.getId(), employees, employeeToManagerMap), employee.getDepartment());
            } else {
                employeeWithManager = employee;
            }
            return employeeWithManager;
        }
    }
}
