package authcliente;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gherkin.deps.com.google.gson.Gson;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DateSourceProperties db;

    private static final String USER_POOL_ID = System.getenv("USER_POOL_ID");
    private static final String CLIENT_ID = System.getenv("CLIENT_ID");

    public App() {
        this.db = new DateSourceProperties();
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        Map<String, String> responseBody = new HashMap<>();
        try {

            String document = input.getPathParameters().get("document");

            if (document != null){
                Connection connection = DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword());

                PreparedStatement statement = connection.prepareStatement("SELECT * FROM cliente where cpf="+ document);

                ResultSet rs = statement.executeQuery();

                if (rs.next()) {

                    Map<String, String> authParameters = new HashMap<>();
                    authParameters.put("EMAIL", rs.getString("email"));

                    AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder.defaultClient();
                    AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                            .withUserPoolId(USER_POOL_ID)
                            .withClientId(CLIENT_ID)
                            .withAuthFlow("ADMIN_NO_SRP_AUTH")
                            .withAuthParameters(authParameters);

                    AdminInitiateAuthResult authResult = cognitoIdentityProvider.adminInitiateAuth(authRequest);

                    responseBody.put("AccessToken", authResult.getAuthenticationResult().getAccessToken());
                    responseBody.put("IdToken", authResult.getAuthenticationResult().getIdToken());
                    response.setStatusCode(200);

                    response.setBody(new Gson().toJson(responseBody));
                    return response;
                }
            }

            return response.withStatusCode(404).withBody("{ \"message\": \"Costumer Not Found\"}");

        } catch (SQLException e) {
            return response
                    .withBody(Arrays.toString(e.getStackTrace()))
                    .withStatusCode(500);
        }
    }

}
