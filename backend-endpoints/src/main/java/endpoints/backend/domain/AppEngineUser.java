package endpoints.backend.domain;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
/**
 *  When you create an application that uses Endpoints,
 *  the code to define the endpoints is the same regardless of
 *  whether the endpoints are used by a web app or an Android app.
 *  However, when the Android app injects the user into an endpoints API call,
 *  the User object does not include the userId.
 *  In our Conference Central app we have used the userId to uniquely identify the
 *  logged-in user, and obviously this won't work for Android users.
 *  So we have defined a class AppEngineUser that compensates for the lack
 *  of the userId in Android Users
 */
@Entity
public class AppEngineUser {
    @Id
    private String email;
    private User user;

    private AppEngineUser(){}
    public AppEngineUser(User user){
        this.user = user;
        this.email = user.getEmail();
    }

    public User getUser(){
        return user;
    }

    public Key<AppEngineUser> getKey(){
        return Key.create(AppEngineUser.class, email);
    }
}
