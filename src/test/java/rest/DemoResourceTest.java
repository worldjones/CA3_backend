package rest;

import entities.Address;
import entities.RenameMe;
import entities.Role;
import entities.User;
import utils.EMF_Creator;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
//Uncomment the line below, to temporarily disable this test
//@Disabled

public class DemoResourceTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
    private static User user, admin, both;

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        //System.in.read();

        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();
        httpServer.shutdownNow();
    }

    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the EntityClass used below to use YOUR OWN (renamed) Entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            //Delete existing users and roles to get a "fresh" database
            em.createQuery("delete from User").executeUpdate();
            em.createQuery("delete from Role").executeUpdate();
            em.createQuery("delete from Address").executeUpdate();

            Address address = new Address("Bygade", 5);
            Address address2 = new Address("Hovedgade", 25);
            user = new User("user", "hello");
            admin = new User("admin", "with");
            both = new User("user_admin", "you");

            Role userRole = new Role("user");
            Role adminRole = new Role("admin");
            user.addRole(userRole);
            admin.addRole(adminRole);
            both.addRole(userRole);
            both.addRole(adminRole);
            address.addUser(user);
            address.addUser(admin);
            address2.addUser(both);
            em.persist(address);
            em.persist(address2);
            em.persist(userRole);
            em.persist(adminRole);
            em.persist(user);
            em.persist(admin);
            em.persist(both);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    //This is how we hold on to the token after login, similar to that a client must store the token somewhere
    private static String securityToken;

    //Utility method to login and set the returned securityToken
    private static void login(String role, String password) {
        String json = String.format("{username: \"%s\", password: \"%s\"}", role, password);
        securityToken = given()
                .contentType("application/json")
                .body(json)
                //.when().post("/api/login")
                .when().post("/login")
                .then()
                .extract().path("token");
        //System.out.println("TOKEN ---> " + securityToken);
    }

    private void logOut() {
        securityToken = null;
    }

    @Test
    public void testServerIsUp() {
        given().when().get("/xxx").then().statusCode(200);
    }

    //This test assumes the database contains two rows
    @Test
    public void testCountAll() throws Exception {
        given()
                .contentType("application/json")
                .get("/info/all").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body(equalTo("[3]"));
    }

    @Test
    public void testGetSwabi() throws Exception {
        given()
                .contentType("application/json")
                .get("/info/swabi").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("SpaceShipName", equalTo("Death Star"))
                .body("SpaceShipModel", equalTo("DS-1 Orbital Battle Station"))
                .body("PersonName", equalTo("Luke Skywalker"))
                .body("PersonGender", equalTo("male"))
                .body("planetName", equalTo("Tatooine"))
                .body("planetPopulation", equalTo("200000"));
    }

    @Test
    public void testGetJokes() {
        login("user", "hello");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .when()
                .get("/info/jokes").then()
                .statusCode(200)
                .body("cJoke", notNullValue())
                .body("cJokeID", notNullValue())
                .body("dJoke", notNullValue())
                .body("dJokeID", notNullValue());
    }

    @Test
    public void testGetFacts() {
        login("admin", "with");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .when()
                .get("/info/facts").then()
                .statusCode(200)
                .body("cType", notNullValue())
                .body("cText", notNullValue())
                .body("dType", notNullValue())
                .body("dText", notNullValue());
    }
    
    @Test
    public void testGetAddress() {
        login("user", "hello");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .when()
                .get("/info/address").then()
                .statusCode(200)
                .body("street", equalTo(admin.getAddress().getStreet()))
                .body("houseNumber", equalTo(admin.getAddress().getHouseNumber()));
    }
}
