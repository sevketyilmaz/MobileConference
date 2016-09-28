package endpoints.backend.spi;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;

import endpoints.backend.Constants;
import endpoints.backend.domain.Profile;
import endpoints.backend.form.ProfileForm;
import endpoints.backend.form.ProfileForm.TeeShirtSize;

import static endpoints.backend.service.OfyService.ofy;

/**
 * Defines conference APIs.
 */
@Api(name = "conference",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID },
    description = "API for the Conference Central Backend application.")
public class ConferenceApi {

    /*
     * Get the display name from the user's email. For example, if the email is
     * lemoncake@example.com, then the display name becomes "lemoncake."
     */
    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }

    /**
     * Creates or updates a Profile object associated with the given user
     * object.
     *
     * @param user A User object injected by the cloud endpoints.
     * @param profileForm A ProfileForm object sent from the client form.
     * @return Profile object just created.
     * @throws UnauthorizedException when the User object is null.
     */
    // Declare this method as a method available externally through Endpoints
    @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
    // The request that invokes this method should provide data that
    // conforms to the fields defined in ProfileForm

    // 1 Pass the ProfileForm parameter
    // 2 Pass the User parameter
    public Profile saveProfile(final User user, ProfileForm profileForm)
            throws UnauthorizedException {
        // 2 If the user is not logged in, throw an UnauthorizedException
        if(user == null){
            throw new UnauthorizedException("Authorization Required!");
        }

        // 2 Get the userId and mainEmail
        String mainEmail = user.getEmail();
        String userId = user.getUserId();
        // Get displayname and teeshrit size sent by the request
        String displayName = profileForm.getDisplayName();
        TeeShirtSize teeShirtSize = profileForm.getTeeShirtSize();

        // Get the profile from datastore if it exist, create otherwise
        Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
        if(profile == null){
            if(displayName == null) displayName = extractDefaultDisplayNameFromEmail(user.getEmail());
            if(teeShirtSize == null) teeShirtSize = TeeShirtSize.NOT_SPECIFIED;
            profile = new Profile(userId, displayName, mainEmail, teeShirtSize);
        }else { //profile entity already exist, update it
            profile.update(displayName, teeShirtSize);
        }

        // 3 (In Lesson 3)
        // Save the Profile entity in the datastore
        ofy().save().entity(profile).now();
        // Return the profile
        return profile;
    }

    /**
     * Returns a Profile object associated with the given user object. The cloud
     * endpoints system automatically inject the User object.
     *
     * @param user A User object injected by the cloud endpoints.
     * @return Profile object.
     * @throws UnauthorizedException when the User object is null.
     */
    @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        // load the Profile Entity
        String userId = user.getUserId();
        Key key = Key.create(Profile.class, userId);
        Profile profile = (Profile) ofy().load().key(key).now();
        return profile;
    }
}
