## Ticket Service - Walmart Labs Coding Challenge

This project is a Spring Boot application, and its architecture is simple.  

`Requests <-> RestController <-> Service <-> Spring Data JPA <-> H2 Database`.  

### Commands
* Build and run all tests: `mvn clean install`
* Build and skip all tests: `mvn clean install -DskipTests`
* Run only integration test: `mvn -Dtest=TicketServiceIT test`  
  The integration test calls real APIs and methods. Check `TicketServiceIT` class for more 
  information.
* Start application: `mvn spring-boot:run`  
  At startup, the application initializes H2 schemas and loads data from `data.sql`.  

### APIs
For convenience purpose, all `POST` calls can be made without `@RequestBody`.
* Get the number of seats available in the venue:  
  `curl localhost:8080/v1/numSeatsAvailable`
* Find and hold a specific number of seats for a specific customer:  
  `curl -X POST localhost:8080/v1/findAndHoldSeats/{numSeats}/{customerEmail}`
* Reserve seats for a specific seat hold id and a specific customer:  
  `curl -X POST localhost:8080/v1/reserveSeats/{seatHoldId}/{customerEmail}`
* Reset the venue and mark all seats available:  
  `curl -X POST localhost:8080/admin/resetVenue`

### Assumptions
* The venue is divided into multiple sections (e.g., `section101`, `section102`). Each section
  is represented by one row in the `VenueSection` table and has properties `id`, `totalSeats` and 
  `availableSeats`.
* `numSeatsAvailable` (number of seats available in the venue) is the sum of `availableSeats` 
  in all venue sections.
* The best available seats on behalf of a customer should lie in SAME venue section. If there is no 
  such venue section, the application will find and hold seats in multiple sections. 
* If there are not enough seats (`numSeatsAvailable < numSeats`), the application will hold all 
  available seats. For example, given that a customer wants to hold 500 seats, and 
  `section101` has 100 available seats, `section102` has 200 available seats, all other sections are 
  full, the application will hold all 300 available seats in `section101` and `section102`.
* After the seats are held successfully, a `SeatHold` record will be written into database. The 
  `SeatHold` object contains a list of `SeatDetail` objects, each `SeatDetail` object contains the 
  information of a `VenueSection` and the number of seats held in the `VenueSection`.
* `SeatHold` expires within a period of time. The expiration time can be configured in 
  `application.yml`. There is a `RecycleService` scheduled to run every `fixedRate` of time to 
  check database and clean up expired `SeatHold` records. `fixedRate` can also be configured in 
  `application.yml`.
* A customer can only access his/her own `SeatHold` records, and can only commit/reserve a 
  `SeatHold` before it expires.
* After a `SeatHold` is reserved, it will be deleted from database and a `Reservation` record will 
  be written into database. The `Reservation` object also contains a list of `SeatDetail` objects, 
  each `SeatDetail` object contains the information of a `VenueSection` and the number of seats 
  reserved in the `VenueSection`.

### Technologies
* Spring Security is not implemented. Validations which are usually handled by Spring Security 
  (e.g., email validation) are not implemented either.
* H2 is used as in-memory database. After application starts up, H2 console can be accessed at 
  `http://localhost:8080/h2`.
* Pessimistic locking is used considering multiple instances can read and write the `VenueSection` 
  table concurrently. Pessimistic locking is delegated to database so it also works for distributed
  systems.
  
### Followup
In real world, a ticket service application should be able to handle millions of requests per 
second. To design such a real world application, we need to work with distributed systems. Below is 
my solution to a ticket service application which can handle 1,000,000 requests per second.
1. In 1 second, 1,000,000 requests come into the systems from all over the world.
2. The requests are distributed by the load balancer into 50 web servers. Each web server processes 
   20,000 requests.
3. The 50 web servers shard the requests (by userId) into 50 queues. Each queue receives 20,000 
   requests.
4. For each queue, there is a ticket service application running to read the messages. There 
   are 50 ticket service applications in total. 
5. Each application reads a number (e.g., 1000) of messages per time, calculates the total 
   `numSeats` (the number of seats to find and hold) and then queries the `VenueSection` table to 
   find and hold seats.
6. Only one database instance is needed for the `VenueSection` table. At most 50 instances query the
   `VenueSection` table concurrently because there are 50 ticket service applications, but 
   pessimistic locking is still required to ensure consistency.
7. After querying the `VenueSection` table, each application successfully holds `numSeats` of seats, 
   and then assigns the seats (if `numSeats` > 0) to users by creating `SeatHold` records in the 
   `SeatHold` table.
8. After `SeatHold` is commit and reserved, the application creates `Reservation` record in the 
   `Reservation` table.
9. 50 database instances are needed for `SeatHold` and `Reservation` tables (one for each ticket
   service application).
10. A scheduled `RecycleService` task is running as standalone application or micro-service to check 
    databases and clean up expired `SeatHold` records.
