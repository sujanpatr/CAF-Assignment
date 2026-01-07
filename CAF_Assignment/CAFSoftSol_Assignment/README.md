# Config and Price Engine Assignment

A Spring Boot application implementing two main features:
1. **Config File Parser** - Parse and retrieve configuration by section
2. **TSV Price Engine** - Upload TSV files and query prices by SKU and time

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
com.assignment/
├── config/          # Assignment 1: Config Parser
│   ├── controller/  # REST API endpoints
│   ├── service/     # Business logic
│   ├── parser/      # File parsing logic
│   ├── model/       # Domain models
│   └── dto/         # Data Transfer Objects
└── price/           # Assignment 2: TSV Price Engine
    ├── controller/  # REST API endpoints
    ├── service/     # Business logic
    ├── parser/      # TSV parsing logic
    ├── model/       # Domain models
    └── dto/         # Data Transfer Objects
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+ (optional - can use IDE)

### Building the Project

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Build JAR file
mvn clean package

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📋 Assignment 1: Config File Parser

### Overview

Parses configuration files with sections and properties. Supports:
- Single-value properties
- Comma-separated list values
- Fast in-memory retrieval

### Config File Format

```
Gateway
endpoint = https://xyz.in
certurl = https://cloud.internalportal.com
download loc = /home/user/temp

CXO
endpont = http://internal.cxo.com
redirect url =
broker = http://cxobroker.in
topic = test_cxo_topic, test_cxo_topic_1

Order Service
broker = https://orbroker.in
topic = test_os_topic_1, test_os_topic_2
```

### API Endpoints

#### Load Config File

**Note:** For this assignment, config loading is done programmatically. In production, you might add an upload endpoint.

#### Get Config Section

```bash
GET /config?section={sectionName}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/config?section=Order%20Service"
```

**Example Response:**
```json
{
  "broker": "https://orbroker.in",
  "topic": ["test_os_topic_1", "test_os_topic_2"]
}
```

**Response Codes:**
- `200 OK` - Section found
- `400 Bad Request` - Missing section parameter
- `404 Not Found` - Section doesn't exist

### Testing Config Parser

#### Unit Tests

```bash
# Run all config tests
mvn test -Dtest=ConfigParserTest
mvn test -Dtest=ConfigServiceTest
mvn test -Dtest=ConfigControllerTest
```

#### Manual Testing with cURL

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Load config (programmatically or via service):**
   The service needs to load config first. You can do this via:
   - A separate endpoint (not implemented in this assignment)
   - Or programmatically in a test/initialization

3. **Query config section:**
   ```bash
   curl -X GET "http://localhost:8080/config?section=Order%20Service"
   ```

## 📋 Assignment 2: TSV Price Engine

### Overview

Uploads TSV files containing price offers and provides fast time-based price lookups.

### TSV File Format

```
SkuID | StartTime | EndTime | Price
u00006541|10:00|10:15|101
i00006111|10:02|10:05|100
u09099000|10:00|10:08|5000
t12182868|10:00|20:00|87
b98989000|00:30|07:00|9128
u00006541|10:05|10:10|99
t12182868|14:00|15:00|92
```

**Format Details:**
- Columns are pipe-separated (`|`)
- Time format: `HH:mm` (24-hour format)
- Price: Numeric value

### API Endpoints

#### Upload TSV File

```bash
POST /price/upload
Content-Type: multipart/form-data
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/price/upload" \
  -F "file=@/path/to/prices.tsv"
```

**Example Response:**
```
File uploaded successfully
```

**Response Codes:**
- `200 OK` - File uploaded successfully
- `400 Bad Request` - Invalid file or missing file
- `500 Internal Server Error` - Server error during processing

#### Query Price

```bash
GET /price?skuid={skuId}&time={HH:mm}
```

**Parameters:**
- `skuid` (required) - The SKU identifier
- `time` (optional) - Time in HH:mm format

**Example Request:**
```bash
# With time
curl -X GET "http://localhost:8080/price?skuid=u00006541&time=10:03"

# Without time (returns NOT SET)
curl -X GET "http://localhost:8080/price?skuid=u00006541"
```

**Example Response (Price Found):**
```json
{
  "price": 101.0
}
```

**Example Response (Price Not Found):**
```json
{
  "price": "NOT SET"
}
```

**Response Codes:**
- `200 OK` - Request processed (price found or NOT SET)
- `400 Bad Request` - Missing skuid or invalid time format

### Testing Price Engine

#### Unit Tests

```bash
# Run all price tests
mvn test -Dtest=TsvPriceParserTest
mvn test -Dtest=PriceServiceTest
mvn test -Dtest=PriceControllerTest
```

#### Manual Testing with cURL

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Upload TSV file:**
   ```bash
   curl -X POST "http://localhost:8080/price/upload" \
     -F "file=@src/test/resources/test-prices.tsv"
   ```

3. **Query price:**
   ```bash
   # Query price at specific time
   curl -X GET "http://localhost:8080/price?skuid=u00006541&time=10:03"
   
   # Query without time (returns NOT SET)
   curl -X GET "http://localhost:8080/price?skuid=u00006541"
   
   # Query non-existent SKU
   curl -X GET "http://localhost:8080/price?skuid=INVALID&time=10:03"
   ```

## 🧪 Test Coverage

### Test Strategy (TDD)

This project follows **Test-Driven Development (TDD)**:
1. ✅ Write failing tests first
2. ✅ Implement code to make tests pass
3. ✅ Refactor if needed

### Test Files

**Assignment 1:**
- `ConfigParserTest.java` - 8 test cases
- `ConfigServiceTest.java` - 7 test cases
- `ConfigControllerTest.java` - 6 test cases

**Assignment 2:**
- `TsvPriceParserTest.java` - 6 test cases
- `PriceServiceTest.java` - 8 test cases
- `PriceControllerTest.java` - 7 test cases

**Total:** 42 test cases covering:
- ✅ Happy paths
- ✅ Edge cases
- ✅ Error handling
- ✅ Boundary conditions

### Running All Tests

```bash
# Run all tests
mvn test

# Run with coverage (if configured)
mvn test jacoco:report
```

## 📁 Project Structure

```
Assignment/
├── pom.xml
├── README.md
├── .gitignore
├── src/
│   ├── main/
│   │   ├── java/com/assignment/
│   │   │   ├── ConfigPriceEngineApplication.java
│   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   │   └── ConfigController.java
│   │   │   │   ├── service/
│   │   │   │   │   └── ConfigService.java
│   │   │   │   ├── parser/
│   │   │   │   │   └── ConfigParser.java
│   │   │   │   ├── model/
│   │   │   │   │   └── ConfigSection.java
│   │   │   │   └── dto/
│   │   │   │       └── ConfigResponse.java
│   │   │   └── price/
│   │   │       ├── controller/
│   │   │       │   └── PriceController.java
│   │   │       ├── service/
│   │   │       │   └── PriceService.java
│   │   │       ├── parser/
│   │   │       │   └── TsvPriceParser.java
│   │   │       ├── model/
│   │   │       │   └── PriceOffer.java
│   │   │       └── dto/
│   │   │           └── PriceResponse.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/assignment/
│       │   ├── config/
│       │   │   ├── parser/
│       │   │   │   └── ConfigParserTest.java
│       │   │   ├── service/
│       │   │   │   └── ConfigServiceTest.java
│       │   │   └── controller/
│       │   │       └── ConfigControllerTest.java
│       │   └── price/
│       │       ├── parser/
│       │       │   └── TsvPriceParserTest.java
│       │       ├── service/
│       │       │   └── PriceServiceTest.java
│       │       └── controller/
│       │           └── PriceControllerTest.java
│       └── resources/
│           ├── test-config.txt
│           └── test-prices.tsv
```

## 🔧 Technologies Used

- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Framework
- **Maven** - Build tool
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Spring Web** - REST API support
- **Spring Validation** - Input validation

## 📝 Key Design Decisions

### 1. Clean Architecture
- **Separation of concerns**: Controller → Service → Parser/Repository
- **Dependency injection**: Spring's `@Service`, `@RestController`
- **Testability**: Easy to mock dependencies

### 2. In-Memory Storage
- **Config Store**: `Map<String, ConfigSection>` for O(1) lookup
- **Price Store**: `Map<String, List<PriceOffer>>` grouped by SKU
- **Optimization**: Offers sorted by start time for efficient time-based lookup

### 3. TDD Approach
- Tests written before implementation
- High test coverage (42 test cases)
- Tests serve as documentation

### 4. Error Handling
- Proper HTTP status codes (200, 400, 404, 500)
- Meaningful error messages
- Input validation

### 5. Time Handling
- Uses `LocalTime` for time representation
- Supports 24-hour format (HH:mm)
- Boundary conditions handled (inclusive start and end)

## 🚀 Running the Application

### Option 1: Maven

```bash
mvn spring-boot:run
```

### Option 2: IDE

1. Open project in IntelliJ IDEA or Eclipse
2. Run `ConfigPriceEngineApplication.java`
3. Application starts on `http://localhost:8080`

### Option 3: JAR File

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/config-price-engine-1.0.0.jar
```

## 📊 Example cURL Commands

### Assignment 1: Config Parser

```bash
# Get Order Service config
curl -X GET "http://localhost:8080/config?section=Order%20Service"

# Get Gateway config
curl -X GET "http://localhost:8080/config?section=Gateway"

# Get non-existent section (404)
curl -X GET "http://localhost:8080/config?section=NonExistent"

# Missing section parameter (400)
curl -X GET "http://localhost:8080/config"
```

### Assignment 2: Price Engine

```bash
# Upload TSV file
curl -X POST "http://localhost:8080/price/upload" \
  -F "file=@src/test/resources/test-prices.tsv"

# Query price with time
curl -X GET "http://localhost:8080/price?skuid=u00006541&time=10:03"

# Query price without time
curl -X GET "http://localhost:8080/price?skuid=u00006541"

# Query non-existent SKU
curl -X GET "http://localhost:8080/price?skuid=INVALID&time=10:03"

# Invalid time format (400)
curl -X GET "http://localhost:8080/price?skuid=u00006541&time=25:00"
```

## 🎯 Interview Talking Points

### Architecture & Design
1. **Clean Architecture**: Clear separation between layers (Controller → Service → Parser)
2. **TDD**: Test-driven development with 42 test cases
3. **SOLID Principles**: Single Responsibility, Dependency Inversion
4. **Design Patterns**: Service Layer, DTO Pattern, Repository Pattern (in-memory)

### Performance
1. **Fast Lookups**: O(1) for config sections, O(n) for price queries (n = offers per SKU)
2. **In-Memory Storage**: Optimized for read-heavy workloads
3. **Sorted Data**: Price offers sorted by start time for efficient time-based queries

### Code Quality
1. **Readable Code**: Clear naming, comments, JavaDoc
2. **Error Handling**: Proper HTTP status codes, validation
3. **Test Coverage**: Comprehensive unit and integration tests
4. **Modularity**: Easy to extend and maintain

### Technical Skills
1. **Spring Boot**: REST APIs, Dependency Injection, Testing
2. **Java 17**: Modern Java features
3. **Maven**: Build and dependency management
4. **Testing**: JUnit 5, Mockito, Spring Test

## 📚 Further Improvements

1. **Persistence**: Add database support (PostgreSQL, MongoDB)
2. **Caching**: Add Redis for distributed caching
3. **Validation**: Enhanced input validation with Bean Validation
4. **Logging**: Structured logging with SLF4J/Logback
5. **API Documentation**: Swagger/OpenAPI documentation
6. **Security**: Add authentication/authorization
7. **Monitoring**: Add metrics and health checks
8. **Docker**: Containerize the application

## 📄 License

This is an assignment project for educational purposes.

