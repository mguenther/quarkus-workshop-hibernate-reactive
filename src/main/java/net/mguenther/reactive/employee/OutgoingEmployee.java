package net.mguenther.reactive.employee;

public class OutgoingEmployee {

    private final String employeeId;
    private final String givenName;
    private final String lastName;
    private final String email;
    private final String departmentName;
    private final String departmentDescription;
    private final String company;

    public OutgoingEmployee(String employeeId, String givenName, String lastName, String email, String departmentName, String departmentDescription, String company) {
        this.employeeId = employeeId;
        this.givenName = givenName;
        this.lastName = lastName;
        this.email = email;
        this.departmentName = departmentName;
        this.departmentDescription = departmentDescription;
        this.company = company;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getDepartmentDescription() {
        return departmentDescription;
    }

    public String getCompany() {
        return company;
    }
}
