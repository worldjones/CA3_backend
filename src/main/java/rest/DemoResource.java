package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.AddressDTO;
import dto.ChuckDTO;
import dto.CombinedDTO;
import dto.DadDTO;
import dto.SwabiDTO;
import entities.Address;
import entities.Role;
import entities.User;
import facades.UserFacade;
import java.io.IOException;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import fetcher.Fetcher;
import utils.EMF_Creator;
import utils.HttpUtils;

/**
 * @author lam@cphbusiness.dk
 */
@Path("info")
public class DemoResource {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();

    private static final UserFacade FACADE = UserFacade.getUserFacade(EMF);
    
    @Context
    private UriInfo context;

    private static EntityManagerFactory emf;

    @Context
    SecurityContext securityContext;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getInfoForAll() {
        return "{\"msg\":\"Hello anonymous\"}";
    }

    //Just to verify if the database is setup
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all")
    public String allUsers() {

        EntityManager em = EMF.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("select u from User u", entities.User.class);
            List<User> users = query.getResultList();
            return "[" + users.size() + "]";
        } finally {
            em.close();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user")
    @RolesAllowed("user")
    public String getFromUser() {
        String thisuser = securityContext.getUserPrincipal().getName();
        return "{\"msg\": \"Hello to User: " + thisuser + "\"}";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("admin")
    @RolesAllowed("admin")
    public String getFromAdmin() {
        String thisuser = securityContext.getUserPrincipal().getName();
        return "{\"msg\": \"Hello to (admin) User: " + thisuser + "\"}";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("jokes")
    @RolesAllowed("user")
    public String getJokes() throws IOException {
        Fetcher fe = new Fetcher();

        CombinedDTO cDTO = fe.getJokes();

        return GSON.toJson(cDTO);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("swabi")
    public String getSwabi() throws IOException {
        Fetcher fe = new Fetcher();

        SwabiDTO sDTO = fe.getSwabi();

        return GSON.toJson(sDTO);
    }
    
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("address")
    @RolesAllowed({"user", "admin"})
    public String getAddress() {
        String thisuser = securityContext.getUserPrincipal().getName();
        AddressDTO aDTO = FACADE.getAddress(thisuser);
        return GSON.toJson(aDTO);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("populate")
    public String populate() throws IOException {

        EntityManagerFactory emf = EMF_Creator.createEntityManagerFactory();
        EntityManager em = emf.createEntityManager();

        Address address = new Address("Bygade", 5);
        Address address2 = new Address("Hovedgade", 25);
        User user = new User("user", "hello");
        User admin = new User("admin", "with");
        User both = new User("user_admin", "you");

        em.getTransaction().begin();
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

        return GSON.toJson("Users added");
    }

}
