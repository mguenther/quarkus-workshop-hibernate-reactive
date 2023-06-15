package net.mguenther.reactive.employee;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class EmployeeRepository implements PanacheRepositoryBase<Employee, String> {
}
