package authcliente;

public class DateSourceProperties {
    private String host;
    private int port;
    private String dbName;
    private String username;
    private String password;

    public DateSourceProperties() {
        this.host = System.getenv("RDS_HOSTNAME");
        this.port = "5432";
        this.dbName = System.getenv("RDS_DB_NAME");
        this.username = System.getenv("RDS_USERNAME");
        this.password = System.getenv("RDS_PASSWORD");

    }

    public DateSourceProperties(String host, int port, String dbName, String username, String password) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.username = username;
        this.password = password;
    }

    public String getJdbcUrl(){
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
