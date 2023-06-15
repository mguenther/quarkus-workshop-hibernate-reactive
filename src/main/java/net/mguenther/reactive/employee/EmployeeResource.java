package net.mguenther.reactive.employee;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.mguenther.reactive.ExceptionMapper;
import net.mguenther.reactive.department.Department;
import net.mguenther.reactive.department.DepartmentManager;

import java.util.ArrayList;
import java.util.List;


@Path("/employees")
@ApplicationScoped
public class EmployeeResource {

    private final DepartmentManager departments;

    private final ExceptionMapper exceptionMapper;

    private final EmployeeRepository repository;

    @Inject
    public EmployeeResource(final DepartmentManager departments, final ExceptionMapper exceptionMapper, EmployeeRepository repository) {
        this.departments = departments;
        this.exceptionMapper = exceptionMapper;
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Employee>> getEmployees() {
        // Replace/extend this
        return Employee.listAll();
    }

    @GET
    @Path("/{employeeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getEmployee(@PathParam("employeeId") final String employeeId) {
        // Replace/extend this
        return Uni.createFrom().failure(new EmployeeNotFoundException(employeeId))
                .onItem().transform(employee -> Response.ok(employee).build())
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getEmployeeCount(@QueryParam("lastName") final String lastName) {
        // Replace/extend this
        return Uni.createFrom().item(0)
                .onItem().transform(counter -> Response.ok(counter).build())
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }

    @GET
    @Path("/filter/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getEmployeeByEmail(@QueryParam("email") final String email) {
        // Replace/extend this
        return Uni.createFrom().failure(new EmployeeNotFoundException(email))
                .onItem().transform(employee -> Response.ok(employee).build())
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createEmployee(final CreateEmployeeCommand command) {
        return Uni.combine()
                .all()
                .unis(Employee.accept(command), departments.findByName(command.getDepartment()))
                .combinedWith(this::merge)
                .map(outgoingEmployee -> Response.ok(outgoingEmployee).status(Response.Status.CREATED).build())
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }

    @PUT
    @Path("/{employeeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> updateEmployee(final UpdateEmployeeCommand command,
                                        @PathParam("employeeId") String employeeId) {
        // Replace/extend this.
        return Uni.createFrom().failure(new EmployeeNotFoundException(employeeId))
                .onItem().transform(outgoingEmployee -> Response.ok(outgoingEmployee).status(Response.Status.OK).build())
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }


    private OutgoingEmployee merge(final Employee employee, final Department department) {
        return new OutgoingEmployee(
                employee.employeeId,
                employee.givenName,
                employee.lastName,
                employee.email,
                department.getName(),
                department.getDescription(),
                department.getCompany()
        );
    }

    @DELETE
    @Path("/{employeeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> deleteEmployee(@PathParam("employeeId") final String employeeId) {
        // Replace/extend this
        return Uni.createFrom().item(true)
                .map(deleted -> Response.ok().status(Response.Status.NO_CONTENT).build())
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }
}
