# Lab Assignment

We've provided an up-to-date Quarkus baseline with Hibernate Panache and a trivial entity `Employee` (which should look familiar from the slides) for you. As the focus of this workshop is on reactive, and not on JPA, the tasks will primarily be geared toward wiring the necessary Panache methods in our reactive data flow and less in writing (complex) JPA methods / queries.

We expose an endpoint with different methods in the `EmployeeResource` - it's an RESTeasy-reactive endpoint with all method signatures predefined. It will be your job to implement the necessary functionality in the method.

## Task #1: CR(U)D

1. We want to have an endpoint that returns all employees with their full department information. The method that needs to be implemented is `getEmployees()`.

In the `employee.http` file you have a request ready to rumble to test your endpoint:

```java
### Get all employees
GET http://localhost:8080/employees
```

There should be no error cases, the response should always be an HTTP 200 with the employee objects in the body if there are any.

2. We need another endpoint to fetch an employee by his/her ID - the predefined method for this is `EmployeeResource.getEmployee(@PathParam("employeeId") final String employeeId)` - take note that we want to see a 200 in case of a successful response, but a 404 if we did not find an employee with this ID. Take a look at the `ExceptionMapper` to see how this can be achieved without too much hassle.

The `employee.http` file again has requests ready to test your implementation manually:

```java 
### Get employee by ID (faulty)
# This should return a 404
GET http://localhost:8080/employees/does-not-exist

### Get employee by ID (correct)
# Replace theidtosearch with the id you were sent from the POST to the employees endpoint
GET http://localhost:8080/employees/theidtosearch
```

3. We also want to be able to delete employees by their ID. As deleting a non-existent object can still be seen as a successful operation we want to return a 204 - No Content in any case no error occurred. The method that needs to contain the logic to do this is `EmployeeResource.deleteEmployee(@PathParam("employeeId") final String employeeId)`

The `employee.http` file provides this HTTP request to test:

```java
### Delete employee by ID
# Should always return 204 - no matter if the id exists or not
DELETE http://localhost:8080/employees/theidtodelete
```

## Task #2: Count & Filter

1. Another common operation is to count the number of (filtered) entities in our database. Take a look at these requests in the `employee.http`:

```http request
### Get count of all employees
GET http://localhost:8080/employees/count

### Get count of all employees with a certain lastName
GET http://localhost:8080/employees/count?lastName=Spotter
```

The count endpoint is in the method `getEmployeeCount(@QueryParam("lastName") final String lastName)` - we want the count of all employees if the QueryParam is missing, the count of all employees with exactly this lastName otherwise.

2. Searching for a certain employee is your next task. We provided the endpoint to filter our employees for a specific `Employee` by e-mail. This argument is mandatory: if a client does not provide an e-mail to search for we want to send an HTTP 400.

If an e-mail is provided we want to get the employee with that e-mail address, or a 404 otherwise, if no such employee exists. The requests to test this in the `employee.http` file look like down below, the method to implement is `getEmployeeByEmail(@QueryParam("email") final String email)`

```http request
### Get employee by email (faulty)
# This should return a HTTP 400, as we're missing the mandatory QueryParam email
GET http://localhost:8080/employees/filter

### Get employee by email (correct)
# This should return the object created from the request above
GET http://localhost:8080/employees/filter?email=harry.spotter@hogwarts.co.uk
```

## Task #3: Update

1. Missing right now is still a way to update an existing item. We want to add this behavior to our solution.

As per usual you will find an example request within th `employee.http` file:

```http request
### Update an employee
PUT http://localhost:8080/employees/0d096e43-6fec-4ae6-9a33-938a3ded1b49
Content-Type: application/json

{ "givenName" : "Harry", "lastName": "Potter", "email" : "harry.spotter@hogwarts.co.uk", "department": "B.M 1" }
```

The payload has the same structure as the one when creating an employee, but the command behind it is different. We've implemented the necessary Classes and calls already for you - your job is to add the necessary functionality to the method `public Uni<Response> updateEmployee(final UpdateEmployeeCommand command,
@PathParam("employeeId") String employeeId)`. The `Employee` entity provides a method, that accepts the `UpdateCommand`, so you don't need to worry about that, just call it.

This endpoint should return an HTTP 200 and the updated `OutgoingEmployee` entity (so practically exactly what the POST returns) if the updated was successful, an HTTP 404 if the department or employee could not be found and an HTTP 500 in case something goes terribly wrong internally. 