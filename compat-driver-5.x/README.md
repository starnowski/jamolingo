# Jamolingo - Compatibility Driver 5.x

The primary goal of this module is to test the compatibility of the [`core`](../core) module with the **MongoDB Java Driver version 5.x**.

It serves as an integration testing suite to ensure that the OData-to-MongoDB translation and projection logic remains functional and correct with the 5.x driver series.

To facilitate these tests, the module utilizes:
*   **[`junit5-mongo-extension`](../junit5-mongo-extension)**: To manage MongoDB data loading and lifecycle during tests.
*   **[`json`](../common/json) module**: To handle configuration overrides and dynamic mapping adjustments for specific test cases.

## Running Tests

To execute the compatibility tests:

```bash
mvnw test -pl compat-driver-5.x
```