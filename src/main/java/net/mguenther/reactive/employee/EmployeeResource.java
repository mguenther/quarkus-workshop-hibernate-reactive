package net.mguenther.reactive.employee;

import io.quarkus.hibernate.reactive.panache.Panache;
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
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;


@Path("/employees")
@ApplicationScoped
public class EmployeeResource {

    private final DepartmentManager departments;

    private final ExceptionMapper exceptionMapper;

    @Inject
    public EmployeeResource(final DepartmentManager departments, final ExceptionMapper exceptionMapper) {
        this.departments = departments;
        this.exceptionMapper = exceptionMapper;
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
    public Uni<RestResponse<Employee>> getEmployee(@PathParam("employeeId") final String employeeId) {
        return Employee.<Employee>findById(employeeId)
                .onItem().ifNull().failWith(() -> new EmployeeNotFoundException(employeeId))
                .onItem().transform(RestResponse::ok)
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<Long>> getEmployeeCount(@QueryParam("lastName") final String lastName) {
        // Replace/extend this
        return Uni.createFrom().item(lastName)
                .onItem().transformToUni(item -> (item == null)
                        ? Employee.count()
                        : Employee.countByName(item))
                .onItem().transform(RestResponse::ok)
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }

    @GET
    @Path("/filter/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<Employee>> getEmployeeByEmail(@QueryParam("email") final String email) {
        return Uni.createFrom().item(email)
                .onItem().ifNull().failWith(() -> new MissingParameterException("Missing mandatory parameter E-Mail"))
                .chain(() -> Employee.findByEmail(email))
                .onItem().ifNull().failWith(() -> new EmployeeNotFoundException(email))
                .onItem().transform(RestResponse::ok)
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<OutgoingEmployee>> createEmployee(final CreateEmployeeCommand command) {
        return Uni.combine()
                .all()
                .unis(Employee.accept(command), departments.findByName(command.getDepartment()))
                .combinedWith(EmployeeResource::buildOutgoingEmployee)
                .map(outgoingEmployee -> RestResponse.status(Response.Status.CREATED, outgoingEmployee))
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }

    @PUT
    @Path("/{employeeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @WithTransaction
    public Uni<RestResponse<OutgoingEmployee>> updateEmployee(final UpdateEmployeeCommand command,
                                                              @PathParam("employeeId") String employeeId) {
        return Uni.combine()
                .all()
                .unis(Employee.<Employee>findById(employeeId)
                                .onItem().ifNull()
                                .failWith(() -> new EmployeeNotFoundException(employeeId)),
                        departments.findByName(command.getDepartment()))
                .asTuple()
                .onItem().transformToUni(tuple ->
                        tuple.getItem1()
                                .accept(command).onItem()
                                .transform((updatedEmployee) -> buildOutgoingEmployee(updatedEmployee, tuple.getItem2())))
                .onItem().transform(RestResponse::ok)
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }


    private static OutgoingEmployee buildOutgoingEmployee(final Employee employee, final Department department) {
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
    public Uni<RestResponse<Object>> deleteEmployee(@PathParam("employeeId") final String employeeId) {
        // Replace/extend this
        return Panache
                .withTransaction(() -> Employee.deleteById(employeeId))
                .map(deleted -> RestResponse.noContent())
                .onFailure().recoverWithItem(exceptionMapper::handle);
    }
}
