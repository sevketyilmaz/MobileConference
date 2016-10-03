package endpoints.backend.spi;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

import endpoints.backend.Constants;
import endpoints.backend.domain.AppEngineUser;
import endpoints.backend.domain.Conference;
import endpoints.backend.domain.Profile;
import endpoints.backend.form.ConferenceForm;
import endpoints.backend.form.ProfileForm;
import endpoints.backend.form.ProfileForm.TeeShirtSize;

import static endpoints.backend.service.OfyService.factory;
import static endpoints.backend.service.OfyService.ofy;

/**
 * Defines conference APIs.
 */
@Api(name = "conference",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID },
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
        String userId = getUserId(user);
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
        String userId = getUserId(user);
        Key key = Key.create(Profile.class, userId);
        Profile profile = (Profile) ofy().load().key(key).now();
        return profile;
    }

    /**
     * Creates a new Conference object and stores it in the datastore
     *
     * @param user An user who invokes this method, null when the user is not signed in.
     * @param conferenceForm A ConferenceForm object representing user's input.
     * @return A newly created Conference object.
     * @throws UnauthorizedException when the user is not signed in.
     */
    @ApiMethod(name = "createConference", path = "conference", httpMethod = HttpMethod.POST)
    public Conference createConference(final User user, final ConferenceForm conferenceForm)
            throws UnauthorizedException{
        if(user == null){
            throw new UnauthorizedException("Authorization Required!");
        }

        // (Lesson 4)
        // Get the userId of the logged in User
        String userId = getUserId(user);

        // (Lesson 4)
        // Get the key for the User's Profile
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        // (Lesson 4)
        // Allocate a key for the conference -- let App Engine allocate the ID
        // Dont forget the include parent Profile in the allocated ID
        final Key<Conference> conferenceKey = factory().allocateId(profileKey, Conference.class);

        // (Lesson 4)
        // Get the Conference Id from the Key
        final long conferenceId = conferenceKey.getId();

        // (Lesson 4)
        // Get the existing Profile entity for the current user if there is one
        // otherwise create a new Profile entity with the default values
        Profile profile= getProfileFromUser(user);

        // (Lesson 4)
        // Create a new Conference Entity, specifying the user's profile entity
        // as the parent of the conference
        Conference conference = new Conference(conferenceId, userId, conferenceForm);

        // (Lesson 4)
        // Save Conference and Profile entities
        ofy().save().entities(conference, profile).now();

        return conference;
    }

    /**
     * Gets the Profile Entity for the current user or creates if its doesnt exist
     *
     * @param user
     * @return user's profile
     */
    private static Profile getProfileFromUser(User user) {
        // First fetch the user's Profile from Datastore
        Profile profile = ofy().load().key(Key.create(Profile.class, getUserId(user))).now();
        if(profile == null){ //create new profile with default values
            String email = user.getEmail();
            profile = new Profile(getUserId(user),
                        extractDefaultDisplayNameFromEmail(email),
                        email, TeeShirtSize.NOT_SPECIFIED);
        }
        return profile;
    }

    /**
     * This is a ugly workaround for null userId for Android clients,
     * look AppEngineUser class for more explanation
     *
     * @param user A User object injected by the cloud endpoints
     * @return the App Engine userId for the user.
     */
    private static String getUserId(User user){
        String userId = user.getUserId();
        if(userId == null){//userId is null, so trying to obtain it from the datastore
            AppEngineUser appEngineUser = new AppEngineUser(user);
            ofy().save().entity(appEngineUser).now();
            // Begin new session for not using session cache
            Objectify objectify = ofy().factory().begin();
            AppEngineUser savedUser = objectify.load().key(appEngineUser.getKey()).now();
            userId = savedUser.getUser().getUserId();
        }

        return userId;
    }
}
