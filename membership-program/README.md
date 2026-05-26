# FirstClub Membership Program

A comprehensive backend system for a tiered membership program with subscription-based benefits, built with Spring Boot and Java 17.

## Table of Contents
- [Features](#features)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [Concurrency Handling](#concurrency-handling)
- [Prerequisites](#prerequisites)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Demo Scenarios](#demo-scenarios)
- [Database Schema](#database-schema)

## Features

### Membership Plans
- **Monthly**: $99/month (30 days)
- **Quarterly**: $249/quarter (90 days) - Save 16%
- **Yearly**: $899/year (365 days) - Save 25%

### Membership Tiers

#### SILVER (Entry Level)
- 5% discount on selected items
- Free delivery on orders above $50
- **Default tier** for all new members

#### GOLD (Mid Level)
- 10% discount on selected items
- Free delivery on all orders
- Access to exclusive weekly deals
- **Eligibility**: Any one of:
  - 5+ orders in a month
  - $500+ spent in a month
  - VIP cohort membership

#### PLATINUM (Premium)
- 15% discount on all items
- Free express delivery
- Premium exclusive deals
- Early access to sales and new products
- 24/7 priority customer support
- **Eligibility**: Either:
  - 10+ orders AND $1000+ spent in a month
  - PREMIUM cohort membership

### User Actions
- View available plans and tiers
- Subscribe to a membership plan
- Upgrade/downgrade membership tier
- Cancel membership
- Track current membership status and expiry
- Automatic tier evaluation based on user activity

## Architecture

### Entity Design
```
User
├── UserMembership (1:1)
│   ├── MembershipPlan (M:1)
│   └── MembershipTier (M:1)
│       ├── TierBenefit (1:M)
│       └── TierCriteria (1:M)
└── Order (1:M)
```

### Key Components

1. **Entities**: JPA entities with proper relationships and auditing
2. **Repositories**: Spring Data JPA repositories with custom queries
3. **Services**: Business logic layer with transaction management
4. **Controllers**: REST API endpoints
5. **Strategy Pattern**: Pluggable tier evaluation strategies
6. **DTOs**: Data transfer objects for API requests/responses

## Design Patterns

### 1. Strategy Pattern
**Location**: `com.firstclub.membership.strategy`

The tier evaluation system uses the Strategy pattern to allow flexible, configurable tier calculation:

```java
public interface TierEvaluationStrategy {
    boolean isEligible(User user, MembershipTier tier);
    int getPriority();
}
```

**Implementations**:
- `OrderCountStrategy`: Evaluates based on number of orders
- `OrderValueStrategy`: Evaluates based on total order value
- `CohortStrategy`: Evaluates based on user cohort
- `CombinedStrategy`: Evaluates based on combined criteria

**Benefits**:
- Easy to add new evaluation criteria
- Configurable priority of strategies
- Separation of concerns

### 2. Builder Pattern
Used extensively for creating complex entities:
```java
User user = User.builder()
    .email("user@example.com")
    .name("John Doe")
    .cohort("VIP")
    .build();
```

### 3. Repository Pattern
Spring Data JPA repositories provide abstraction over data access:
```java
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<UserMembership> findByUserIdWithLock(Long userId);
}
```

## Concurrency Handling

### Optimistic Locking
**Implementation**: JPA `@Version` annotation

```java
@Entity
public class UserMembership {
    @Version
    private Long version;
    // ...
}
```

### Thread-Safe Operations
All tier upgrade/downgrade operations use:
1. **Optimistic Locking**: Prevents lost updates
2. **Retry Logic**: Automatic retry on version conflicts
3. **Synchronized Methods**: Critical tier evaluation synchronized

```java
@Transactional
@Retryable(
    retryFor = {OptimisticLockException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
public MembershipResponse upgradeTier(TierUpgradeRequest request) {
    UserMembership membership = membershipRepository.findByUserIdWithLock(userId);
    // Critical section protected by optimistic locking
}
```

### Concurrent Tier Evaluation
- `TierEvaluator.evaluateTier()` is synchronized
- Safe for concurrent order processing
- Automatic tier upgrades after order completion

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Running the Application

### 1. Build the Application
```bash
cd membership-program
mvn clean install
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Access H2 Console (Optional)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:membershipdb`
- Username: `sa`
- Password: (leave empty)

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### User Management

#### Create User
```http
POST /api/users
Content-Type: application/json

{
  "email": "user@example.com",
  "name": "John Doe",
  "cohort": "REGULAR"
}
```

#### Get User by ID
```http
GET /api/users/{userId}
```

#### Get All Users
```http
GET /api/users
```

### Membership Plans & Tiers

#### Get All Plans
```http
GET /api/memberships/plans
```

**Response**:
```json
[
  {
    "id": 1,
    "planType": "MONTHLY",
    "price": 99.00,
    "durationInDays": 30,
    "description": "Monthly subscription with flexible cancellation"
  }
]
```

#### Get All Tiers
```http
GET /api/memberships/tiers
```

**Response**:
```json
[
  {
    "id": 1,
    "level": "SILVER",
    "priority": 1,
    "description": "Entry-level membership with basic benefits",
    "benefits": [
      {
        "benefitType": "DISCOUNT",
        "description": "5% discount on selected items",
        "discountPercentage": 5.00
      }
    ]
  }
]
```

### Subscription Management

#### Subscribe to Membership
```http
POST /api/memberships/subscribe
Content-Type: application/json

{
  "userId": 1,
  "planType": "MONTHLY",
  "tierLevel": "SILVER",
  "autoRenewal": true
}
```

**Response**:
```json
{
  "membershipId": 1,
  "userId": 1,
  "userEmail": "john.doe@example.com",
  "planType": "MONTHLY",
  "tierLevel": "SILVER",
  "status": "ACTIVE",
  "startDate": "2024-01-15T10:00:00",
  "expiryDate": "2024-02-14T10:00:00",
  "amountPaid": 99.00,
  "autoRenewal": true,
  "benefits": [...],
  "isActive": true,
  "isExpired": false
}
```

#### Get Current Membership
```http
GET /api/memberships/user/{userId}
```

#### Upgrade Tier
```http
PUT /api/memberships/upgrade
Content-Type: application/json

{
  "userId": 1,
  "newTierLevel": "GOLD"
}
```

#### Downgrade Tier
```http
PUT /api/memberships/downgrade
Content-Type: application/json

{
  "userId": 1,
  "newTierLevel": "SILVER"
}
```

#### Cancel Membership
```http
DELETE /api/memberships/user/{userId}
```

#### Evaluate Tier (Manual)
```http
POST /api/memberships/evaluate/{userId}
```

### Order Management

#### Create Order
```http
POST /api/orders
Content-Type: application/json

{
  "userId": 1,
  "totalAmount": 150.00
}
```

**Note**: Creating an order automatically triggers tier evaluation!

## Demo Scenarios

### Scenario 1: Basic Subscription Flow

```bash
# 1. Get available plans
curl http://localhost:8080/api/memberships/plans

# 2. Get available tiers
curl http://localhost:8080/api/memberships/tiers

# 3. Subscribe user to monthly SILVER plan
curl -X POST http://localhost:8080/api/memberships/subscribe \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "planType": "MONTHLY",
    "tierLevel": "SILVER",
    "autoRenewal": true
  }'

# 4. Check membership status
curl http://localhost:8080/api/memberships/user/1
```

### Scenario 2: Automatic Tier Upgrade via Orders

```bash
# User starts with SILVER tier
# Create 5 orders to qualify for GOLD tier

for i in {1..5}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{
      "userId": 1,
      "totalAmount": 120.00
    }'
done

# Check membership - should auto-upgrade to GOLD
curl http://localhost:8080/api/memberships/user/1
```

### Scenario 3: VIP Cohort User

```bash
# 1. Create VIP user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "vip@example.com",
    "name": "VIP User",
    "cohort": "VIP"
  }'

# 2. Subscribe to membership
curl -X POST http://localhost:8080/api/memberships/subscribe \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "planType": "YEARLY",
    "tierLevel": "SILVER"
  }'

# 3. Manually evaluate tier - should upgrade to GOLD (VIP cohort)
curl -X POST http://localhost:8080/api/memberships/evaluate/2

# 4. Check new tier
curl http://localhost:8080/api/memberships/user/2
```

### Scenario 4: PLATINUM Tier Qualification

```bash
# Create 10 orders worth $1000+ total
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{
      "userId": 1,
      "totalAmount": 150.00
    }'
done

# Check membership - should auto-upgrade to PLATINUM
curl http://localhost:8080/api/memberships/user/1
```

### Scenario 5: Manual Tier Downgrade

```bash
# Downgrade from GOLD to SILVER
curl -X PUT http://localhost:8080/api/memberships/downgrade \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "newTierLevel": "SILVER"
  }'
```

### Scenario 6: Cancel Membership

```bash
# Cancel membership
curl -X DELETE http://localhost:8080/api/memberships/user/1

# Check status
curl http://localhost:8080/api/memberships/user/1
```

## Database Schema

### Core Tables
- `users`: User information
- `membership_plans`: Available subscription plans
- `membership_tiers`: Tier definitions (SILVER, GOLD, PLATINUM)
- `tier_benefits`: Configurable benefits per tier
- `tier_criteria`: Configurable criteria for tier eligibility
- `user_memberships`: Active user subscriptions
- `orders`: User order history

### Configuration Examples

#### Adding a New Tier Benefit
```java
TierBenefit newBenefit = TierBenefit.builder()
    .tier(goldTier)
    .benefitType(BenefitType.CUSTOM)
    .description("Free gift wrapping")
    .additionalInfo("Available on all orders")
    .build();
```

#### Adding a New Tier Criteria
```java
TierCriteria newCriteria = TierCriteria.builder()
    .tier(platinumTier)
    .criteriaType(CriteriaType.ORDER_COUNT)
    .minOrderCount(20)
    .description("Complete 20 orders in a month")
    .build();
```

## Extensibility

### Adding a New Evaluation Strategy
1. Implement `TierEvaluationStrategy` interface
2. Add `@Component` annotation
3. Spring will automatically discover and use it

```java
@Component
public class CustomStrategy implements TierEvaluationStrategy {
    @Override
    public boolean isEligible(User user, MembershipTier tier) {
        // Your custom logic
    }

    @Override
    public int getPriority() {
        return 4; // Lower = higher priority
    }
}
```

### Configurable Benefits
All tier benefits are stored in the database and can be modified without code changes through the `tier_benefits` table.

### Configurable Criteria
Tier advancement criteria are fully configurable through the `tier_criteria` table:
- Order count thresholds
- Order value thresholds
- Cohort requirements
- Combined criteria

## Best Practices Implemented

1. **Separation of Concerns**: Clear separation between entities, repositories, services, and controllers
2. **SOLID Principles**:
   - Single Responsibility: Each class has one responsibility
   - Open/Closed: Strategy pattern allows extension without modification
   - Dependency Inversion: Depends on abstractions (interfaces)
3. **Transaction Management**: Proper `@Transactional` boundaries
4. **Exception Handling**: Global exception handler with proper HTTP status codes
5. **Validation**: Bean validation on DTOs
6. **Auditing**: Automatic timestamps on entities
7. **Optimistic Locking**: Concurrency control with version fields
8. **Retry Logic**: Automatic retry on optimistic lock failures
9. **Logging**: Comprehensive logging with SLF4J
10. **Builder Pattern**: Clean object construction

## Testing the Concurrency

To test concurrent tier upgrades:

```bash
# Terminal 1
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{"userId": 1, "totalAmount": 200.00}' &
done
wait

# All concurrent requests will be handled safely with optimistic locking
```

## Technology Stack

- **Java 17**: Latest LTS version
- **Spring Boot 3.2.0**: Framework
- **Spring Data JPA**: Data access
- **Hibernate**: ORM
- **H2 Database**: In-memory database
- **Lombok**: Reduce boilerplate
- **Maven**: Build tool
- **Jackson**: JSON serialization

## Project Structure

```
membership-program/
├── src/
│   ├── main/
│   │   ├── java/com/firstclub/membership/
│   │   │   ├── MembershipApplication.java
│   │   │   ├── config/
│   │   │   │   └── DataInitializer.java
│   │   │   ├── controller/
│   │   │   │   ├── MembershipController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/
│   │   │   │   ├── BenefitDTO.java
│   │   │   │   ├── CreateOrderRequest.java
│   │   │   │   ├── MembershipResponse.java
│   │   │   │   ├── PlanDTO.java
│   │   │   │   ├── SubscriptionRequest.java
│   │   │   │   ├── TierDTO.java
│   │   │   │   └── TierUpgradeRequest.java
│   │   │   ├── entity/
│   │   │   │   ├── BenefitType.java
│   │   │   │   ├── CriteriaType.java
│   │   │   │   ├── MembershipPlan.java
│   │   │   │   ├── MembershipStatus.java
│   │   │   │   ├── MembershipTier.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderStatus.java
│   │   │   │   ├── PlanType.java
│   │   │   │   ├── TierBenefit.java
│   │   │   │   ├── TierCriteria.java
│   │   │   │   ├── TierLevel.java
│   │   │   │   ├── User.java
│   │   │   │   └── UserMembership.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── MembershipException.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── MembershipPlanRepository.java
│   │   │   │   ├── MembershipTierRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── UserMembershipRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── service/
│   │   │   │   ├── MembershipService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   └── UserService.java
│   │   │   └── strategy/
│   │   │       ├── CohortStrategy.java
│   │   │       ├── CombinedStrategy.java
│   │   │       ├── OrderCountStrategy.java
│   │   │       ├── OrderValueStrategy.java
│   │   │       ├── TierEvaluationStrategy.java
│   │   │       └── TierEvaluator.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

## License

This project is developed as part of the FirstClub membership program assessment.
