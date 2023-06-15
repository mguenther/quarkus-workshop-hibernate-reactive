# Hints

**Spoiler Alert**

We encourage you to work on the assignment yourself or together with your peers. However, situations may present themselves to you where you're stuck on a specific assignment. Thus, this document contains a couple of hints/solutions that ought to guide you through a specific task of the lab assignment.

In any case, don't hesitate to talk to us if you're stuck on a given problem!


## Task 1.1

As the data types (just the mutiny primitives) are compatible between Panache and what we want to return from the REST interface we can just return the Panache-call directly:

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public Uni<List<Employee>> getEmployees() {
    return Employee.listAll();
}
```

## Task 1.2

We can use the Panache `findById()` method to fetch the entity - this method will return the object or `null`, if the object does not exist. So all that's left to do is to check if the item in the `Uni` is `null` and propagate the correct exception.

```java
@GET
@Path("/{employeeId}")
@Produces(MediaType.APPLICATION_JSON)
public Uni<Response> getEmployee(@PathParam("employeeId") final String employeeId) {
    return Employee.findById(employeeId)
        .onItem().ifNull().failWith(() -> new EmployeeNotFoundException(employeeId))
        .onItem().transform(employee -> Response.ok(employee).build())
        .onFailure().recoverWithItem(exceptionMapper::handle);
}
```

## Task 1.3

We can use the Panache `deleteById(String id)` method to delete the entity. Take care, that you will need to open a transaction, otherwise the operation will not be written to the database.

```java
@DELETE
@Path("/{employeeId}")
@Produces(MediaType.APPLICATION_JSON)
public Uni<Response> deleteEmployee(@PathParam("employeeId") final String employeeId) {
    return Panache
        .withTransaction(() -> Employee.deleteById(employeeId))
        .map(deleted -> Response.ok().status(Response.Status.NO_CONTENT).build())
        .onFailure().recoverWithItem(exceptionMapper::handle);
}
```

## Task 2.1

That is pretty straight-forward. The only tricky part is, that we need to call different functions, depending on if our parameter lastName is null or not. This can be done as part of a transformation, where we decide with which Uni to continue. Additionally, we need a method that can count entities based on the last name. This can be either done in the active record or within the repository.

```java
public static Uni<Long> countByName(String name) {
    return count("lastName = ?1", name);
}
```

```java 
@GET
@Path("/count")
@Produces(MediaType.APPLICATION_JSON)
public Uni<Response> getEmployeeCount(@QueryParam("lastName") final String lastName) {
    return Uni.createFrom().item(lastName)
            .onItem().transformToUni(item -> {
                if (item == null) {
                    return Employee.count();
                } else {
                    return Employee.countByName(item);
                }
            })
            .onItem().transform(counter -> Response.ok(counter).build())
            .onFailure().recoverWithItem(exceptionMapper::handle);
}
```

## Task 2.2

This is quite similar to Task 2.1 - but this time we want to throw an `Exception` if the e-mail is not set as part of the request. We can use the `onItem().ifNull().failWith()` syntax to achieve this - otherwise we either chain or transform to the `Uni` that fetches us the correct `Employee` (or `null` if this `Employee` does not exist).

Additionally, we need a method to search for an employee by e-Mail. Again: this can be done in the active record or the repository:

```java
public Uni<Employee> findByEmail(String email) {
    return find("email", email).firstResult();
}
```

```java
@GET
@Path("/filter/")
@Produces(MediaType.APPLICATION_JSON)
public Uni<Response> getEmployeeByEmail(@QueryParam("email") final String email) {
    return Uni.createFrom().item(email)
            .onItem().ifNull().failWith(() -> new MissingParameterException("Missing mandatory parameter E-Mail"))
            .chain(() -> repository.findByEmail(email))
            .onItem().ifNull().failWith(() -> new EmployeeNotFoundException(email))
            .onItem().transform(employee -> Response.ok(employee).build())
            .onFailure().recoverWithItem(exceptionMapper::handle);
    }
```

## Task 3

The problematic part is, that we need to do several things here. We need to make sure that the `Employee` **and** `Department` exist, update the `Employee`, merge the resulting entities and return the result back (or the correct error otherwise).

Additionally, we need to use the `@WithTransaction` annotation to ensure that the changes will be propagated to the database. Otherwise, it will return the correct entity just fine, but it will not be updated in the data store.

This leads to quite a long chain that can be done like this:

```java
@PUT
@Path("/{employeeId}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@WithTransaction
public Uni<Response> updateEmployee(final UpdateEmployeeCommand command,
                                    @PathParam("employeeId") String employeeId) {
    return Uni.combine()
            .all()
            .unis(Employee.<Employee>findById(employeeId)
                            .onItem().ifNull().failWith(() -> new EmployeeNotFoundException(employeeId)),
                    departments.findByName(command.getDepartment()))
            .asTuple()
            .onItem().transformToUni(tuple ->
                    tuple.getItem1().accept(command)
                            .onItem().transform((updatedEmployee) -> this.merge(updatedEmployee, tuple.getItem2())))
            .onItem().transform(outgoingEmployee -> Response.ok(outgoingEmployee).status(Response.Status.OK).build())
            .onFailure().recoverWithItem(exceptionMapper::handle);
    }
```