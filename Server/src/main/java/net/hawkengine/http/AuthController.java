package net.hawkengine.http;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


import net.hawkengine.core.utilities.deserializers.TokenAdapter;
import net.hawkengine.model.ServiceResult;
import net.hawkengine.model.User;
import net.hawkengine.model.dto.GithubAuthDto;
import net.hawkengine.model.dto.LoginDto;
import net.hawkengine.model.dto.RegisterDto;
import net.hawkengine.model.payload.TokenInfo;
import net.hawkengine.services.UserService;
import net.hawkengine.services.github.GitHubService;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes("application/json")
@Produces("application/json")
@Path("auth")
public class AuthController {

    private static final String GH_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    //TODO: move this to the config
    private static final String GH_CLIENT_ID = "2d3dbbf586d2260cbd68";
    //TODO: move this to the config
    private static final String GH_CLIENT_SERCRET = "";

    private UserService userService;

    public AuthController(){
        this.userService =  new UserService();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/github")
    public Response updateUserInfo(GithubAuthDto authDto) throws IOException {

        //TODO: implement schema validation
        //extract authorization code
        String code = authDto.getCode();
        //request access token
        String accessToken = getGitHubAccessToken(code);
        //find out the gh user details
        GitHubService ghService = new GitHubService(accessToken);
        Map ghUserDetails = ghService.getMyself();
        //read user email
        String email = (String) ghUserDetails.get("email");

        ArrayList<User> all = (ArrayList<User>) this.userService.getAll().getObject();

        boolean isUserRegistered = false;
        //loop through users to check if the user already exists

        User user = null;
        for(User usr:all){
            String uEmail = usr.getEmail();
            if ( uEmail.equals(email)){
                isUserRegistered = true;
                user = usr;
                break;
            }
        }

        if (!isUserRegistered){
            //register a new user
            user =  new User();
            user.setEmail(email);
            user.setProvider("GitHub");
            userService.add(user);
        }


        return Response.status(Response.Status.CREATED)
                .entity(user)
                .build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @Path("/login")
    public Response login(LoginDto login) throws IOException {
        String hashedPassword = DigestUtils.sha256Hex(login.getPassword());

        ServiceResult serviceResult = this.userService.getByEmailAndPassword(login.getEmail(), hashedPassword);
        if (serviceResult.hasError()){
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(serviceResult)
                    .build();
        }

        User userFromDb = (User) serviceResult.getObject();

        String token = TokenAdapter.createJsonWebToken(userFromDb, 30L);

        Gson gson = new Gson();

        String jsonToken = gson.toJson(token);

        TokenInfo tokenInfo = TokenAdapter.verifyToken(token);

        return Response.ok(jsonToken).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/register")
    public Response register(RegisterDto newUser){
        String hashedPassword = DigestUtils.sha256Hex(newUser.getPassword());

            User user =  new User();
            user.setEmail(newUser.getEmail());
            user.setPassword(hashedPassword);
            ServiceResult serviceResult = this.userService.addUserWithoutProvider(user);

        if (serviceResult.hasError()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(serviceResult)
                    .build();
        } else {
            return Response.status(Response.Status.CREATED)
                    .entity(serviceResult)
                    .build();
        }
    }

    private String getGitHubAccessToken(String authCode) {

        String ghResource = String.format("%s?client_id=%s&client_secret=%s&code=%s",GH_ACCESS_TOKEN_URL,GH_CLIENT_ID,GH_CLIENT_SERCRET,authCode);
        Client client = Client.create();
        WebResource resource = client.resource(ghResource);

        ClientResponse response = resource.get(ClientResponse.class);
        if (response.getStatus() !=200 ){
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        String stringToMatch = response.getEntity(String.class);

        //run through regext to extract the access_token
        Pattern p = Pattern.compile("access_token=.*?&");
        Matcher m = p.matcher(stringToMatch);
        String at = null;
        if (m.find()){
            at = m.group(0);
        }
        if (at!=null){
            return at.replace("access_token=","").replace("&","");
        }else
        {
            return null;
        }
    }

}




