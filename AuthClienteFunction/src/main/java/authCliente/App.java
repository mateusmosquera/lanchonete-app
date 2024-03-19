package authcliente;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DateSourceProperties db;

    public App() {
        this.db = new DateSourceProperties();
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {

            String document = input.getPathParameters().get("document");

            if (document != null){
                Connection connection = DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword());

                PreparedStatement statement = connection.prepareStatement("SELECT * FROM clientes where cpf="+ document);

                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    String output = "{ \"message\": \"Costumer Found\"}";
                    return response.withStatusCode(200).withBody(output);
                }
            }

            return response.withStatusCode(404).withBody("{ \"message\": \"Costumer Not Found\"}");

        } catch (SQLException e) {
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

}
