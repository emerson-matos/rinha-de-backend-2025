# backend-payment-processor

A simple example of a microservice using Clojure, demonstrating clean architecture and component-based design.

## Architecture

This project follows the **ports and adapters (hexagonal) architecture**:

- **Logic**: Pure business logic, no side effects (`logic.clj`).
- **Controller**: Orchestrates logic, adapters, and ports (`controller.clj`).
- **Adapters**: Convert between external and internal data representations (`adapters.clj`).
- **Ports**: Protocols for communication with the outside world (HTTP, storage, etc. in `protocols/`).

### Components

We use [stuartsierra/component](https://github.com/stuartsierra/component) to manage system lifecycle and dependencies. The system is composed of:

- **Config**: Environment configuration.
- **HTTP**: Split into a high-level component (serialization, error handling) and a low-level implementation (`http-kit`).
- **Storage**: In-memory storage for this example.
- **Routes**: Pedestal routes as a component.
- **Service**: Pedestal service configuration.
- **Servlet**: Starts the HTTP server (different for dev and test).

Different environments (`base`, `e2e`, `test`) are supported, each with their own system map and component wiring.

### HTTP API

The service exposes endpoints for managing savings accounts:

- `GET /` – Health check.
- `POST /account/` – Create account.
- `GET /account/from-customer/:customer-id/` – Lookup account by customer.
- `GET /account/lookup/:account-id/` – Get account by ID.
- `POST /account/remove/:account-id/` – Delete account.

## Testing

- **Integration tests** use [state-flow](https://github.com/nubank/state-flow) for world-transition style flows, and [matcher-combinators](https://github.com/nubank/matcher-combinators) for assertions.
- HTTP requests are mocked using a mock HTTP component and helpers.
- Test helpers and macros are in `test/integration/backend_payment_processor/aux/`.
- Run all tests with:
  ```
  lein test
  ```
  Or run only unit/integration tests:
  ```
  lein with-profile +unit test
  lein with-profile +integration test
  ```

## Running the Service

- Start the server:
  ```
  lein run
  ```
  Visit [localhost:8080](http://localhost:8080/) to see the health check.

- For development:
  ```
  lein repl
  ;; then in the REPL:
  (backend-payment-processor.server/run-dev)
  ```

## Project Structure

- `src/backend_payment_processor/` – Main source code
- `test/unit/` – Unit tests
- `test/integration/` – Integration tests and helpers

## Missing Aspects

This is a minimal example. Real-world services would include:

- Endpoint schemas (validation)
- More complex adapters
- Kafka integration
- Real database (e.g., Datomic)
- Advanced configuration management

---

**Note:** This README reflects the current codebase and test setup. If you add new features or change the architecture, update this file accordingly.

Rinha Spec:
https://github.com/zanfranceschi/rinha-de-backend-2025/blob/main/INSTRUCOES.md
