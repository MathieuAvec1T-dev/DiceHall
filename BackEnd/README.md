# DiceHall Backend

## Getting Started

### Database

To set up your database:
- Create a database using the sql file in Database/
- Put your database url here in BackEnd/src/main/ressources/application.properties: `spring.datasource.url=jdbc:mysql://[yoururl]:[yourport]/dicehall`
- In the same file change `spring.datasource.username=[yourUsername]` and `spring.datasource.password=[yourPassword]`
- Start your Database.

### Start the API

From the Backend folder, use the command:\
`./gradlew bootRun`

### Swagger

To open the Swagger simply use the url:\
http://localhost:8080/swagger-ui/index.html#/