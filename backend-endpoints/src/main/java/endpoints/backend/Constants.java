package endpoints.backend;


import com.google.api.server.spi.Constant;

/**
 * Contains the client IDs and scopes for allowed clients consuming the conference API.
 *
 * client id: 1029328229768-r35s95s81dnls6u9dlod4nbj48t755al.apps.googleusercontent.com
 * client secret: 5FW3ZFOSZ2MMx9OWMhTWhw5w
 */

public class Constants {
    public static final String WEB_CLIENT_ID = "1029328229768-r35s95s81dnls6u9dlod4nbj48t755al.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID = "replace this with your Android client ID";
    public static final String IOS_CLIENT_ID = "replace this with your iOS client ID";
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;
    public static final String EMAIL_SCOPE = Constant.API_EMAIL_SCOPE;
    public static final String API_EXPLORER_CLIENT_ID = Constant.API_EXPLORER_CLIENT_ID;

    public static final String MEMCACHE_ANNOUNCEMENTS_KEY = "RECENT_ANNOUNCEMENTS";

}
