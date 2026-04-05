# Shopping Cart (JavaFX + MySQL)

## Prerequisites

- **JDK 17**
- **Maven**
- **MySQL** or **MariaDB** running locally

## Database

1. Start your database server.
2. Run the script **`src/main/resources/sql/shopping_cart_localization.sql`** in a client (MySQL Workbench, `mysql` CLI, etc.). It creates the database, tables, and UI strings.

By default the app connects to:

- URL: `jdbc:mysql://localhost:3306/shopping_cart_localization`
- User: `root`
- Password: *(empty)*

If yours differs, set environment variables before starting:

- `SHOPPING_CART_DB_URL`
- `SHOPPING_CART_DB_USER`
- `SHOPPING_CART_DB_PASSWORD`

## Start the app

From the project root:

```bash
mvn javafx:run
```

Or run **`org.example.ShoppingCartApp`** (or **`org.example.Main`**) from your IDE.

Choose a language, confirm, then use the cart. Each **Add item** writes to the database.

## Tests

```bash
mvn test
```
