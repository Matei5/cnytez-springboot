
## Info
Ensure you have Docker Desktop opened and running before running the tests. Some of the tests use Testcontainers to spin up a PostgreSQL database.

The cross-service image test also requires the .NET 8 SDK. It starts a test-only image server on an available local port and does not use AWS or S3.

## Run
### Run all tests:
./mvnw test

### Run specific test classes:
./mvnw test -Dtest="*ControllerTest"
./mvnw test -Dtest="*E2ETest"
./mvnw test -Dtest="*IntegrationTest"

### Run individual test:
/mvnw test -Dtest="PostServiceTest"
/mvnw test -Dtest=" -- Test class name -- "

### Run the backend and image server round-trip test:
./mvnw test -Dgroups=cross-service -Dtest=ImageServerRoundTripTest

