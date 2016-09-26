package endpoints.backend.form;
/**
 * Pojo representing a profile form on the client side.
 */
public class ProfileForm {
    private String displayName;//Any string user wants us to display him/her on this system.
    private TeeShirtSize teeShirtSize;

    /** Just making the default constructor private. */
    private ProfileForm () {}
    /**
     * Constructor for ProfileForm, solely for unit test.
     * @param displayName A String for displaying the user on this system.
     * @param notificationEmail An e-mail address for getting notifications from this system.
     */
    public ProfileForm(String displayName, TeeShirtSize teeShirtSize) {
        this.displayName = displayName;
        this.teeShirtSize = teeShirtSize;
    }

    public String getDisplayName() {
        return displayName;
    }
    public TeeShirtSize getTeeShirtSize() {
        return teeShirtSize;
    }

    public static enum TeeShirtSize {
        NOT_SPECIFIED,
        XS,
        S,
        M,
        L,
        XL,
        XXL,
        XXXL
    }
}
