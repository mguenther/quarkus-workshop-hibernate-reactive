package net.mguenther.reactive.department;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.mguenther.reactive.ExceptionMapper;


@Path("/departments")
public class DepartmentResource {

    private final DepartmentManager departments;

    private final ExceptionMapper exceptionMapper;

    @Inject
    public DepartmentResource(final DepartmentManager departments, final ExceptionMapper exceptionMapper) {
        this.departments = departments;
        this.exceptionMapper = exceptionMapper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Department> getDepartments() {
        return departments.findAll();
    }

    @GET
    @Path("/{departmentName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getDepartment(@PathParam("departmentName") final String departmentId) {
        return departments.findByName(departmentId)
                .onItem().transform(department -> Response.ok(department).build())
                .onFailure().recoverWithItem(exceptionMapper::handle);}
}
