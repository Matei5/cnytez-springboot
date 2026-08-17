
## Info
Ensure you have Docker Desktop opened and running before running the tests. Some of the tests use Testcontainers to spin up a PostgreSQL database.

## Run
### Run all tests:
./mvnw test

### Run specific test classes:
./mvnw test -Dtest="*ControllerTest"
./mvnw test -Dtest="*E2ETest"
./mvnw test -Dtest="*IntegrationTest"

### Run individual test:
.\mvnw.cmd test -Dtest="PostServiceTest"
.\mvnw.cmd test -Dtest=" -- Test class name -- "

