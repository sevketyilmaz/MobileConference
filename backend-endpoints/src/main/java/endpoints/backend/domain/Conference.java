package endpoints.backend.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import endpoints.backend.form.ConferenceForm;
import endpoints.backend.service.OfyService;

/**
 * Conference class stores conference information
 */
@Entity
public class Conference {
    private static final String DEFAULT_CITY = "Default City";
    private static final List<String> DEFAULT_TOPICS = ImmutableList.of("Default", "Topic");

    @Id
    private Long id; /** The Id for the Datastore Key, Automatic Id assignment for entities of Conference class. */

    @Index
    private String name;/** Name of the conference*/

    private String description; /** Description of the conference */

    @Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Profile> profileKey; //** Holds Profile Key as a parent */

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String organizerUserId; //** User id of the organizer */

    @Index
    private List<String> topics; //** Topics related to this conference */

    @Index(IfNotDefault.class)
    private String city; //** The name of the city that the conference takes place */

    private Date startDate; //** Starting date of this conference */

    private Date endDate; //** Ending date of this conference */

    @Index
    private int month; //** Indicating starting month derived from startDate. We need this for composite query specifiying the starting month*/

    @Index
    private int maxAttendees; //** Maximum capacity of this conference */

    @Index
    private int seatsAvailable; //** Number of seats currently available */

    //** Just making the default constructor private. */
    private Conference(){}

    public Conference(final Long id, final String organizerUserId, final ConferenceForm conferenceForm){
        Preconditions.checkNotNull(conferenceForm.getName(), "The name is required!");
        this.id = id;
        this.profileKey = Key.create(Profile.class, organizerUserId);
        this.organizerUserId = organizerUserId;
        updateWithConferenceForm(conferenceForm);
    }

    //getters setters
    public Long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Profile> getProfileKey(){
        return profileKey;
    }

    //** Get string version of the key */
    public String getWebsafeKey(){
        return Key.create(profileKey, Conference.class, id).getString();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getOrganizerUserId(){
        return organizerUserId;
    }

    /**
     * Return organizer's display name.
     * @return organizer's display name. If there is no profile, return his/her userId.
     */
    public String getOrganizerDisplayName(){
        Profile organizer = OfyService.ofy().load().key(getProfileKey()).now();
        if(organizer == null) {
            return organizerUserId;
        }else{
            return organizer.getDisplayName();
        }
    }

    /**
     * Returns defensive copy of topics if not null
     * @return defensive copy of topics if not null
     */
    public List<String> getTopics(){
        return topics == null ? null : ImmutableList.copyOf(topics);
    }

    public String getCity(){
        return city;
    }

    /**
     * Returns a defensive copy of startDate if not null
     * @return a defensive copy of startDate if not null
     */
    public Date getStartDate(){
        return startDate == null ? null : new Date(startDate.getTime());
    }

    /**
     * Returns a defensive copy of endDate if not null
     * @return a defensive copy of endDate if not null
     */
    public Date getEndDate(){
        return endDate == null ? null : new Date(endDate.getTime());
    }

    public int getMonth(){
        return month;
    }
    public int getMaxAttendees(){
        return maxAttendees;
    }
    public int getSeatsAvailable(){
        return seatsAvailable;
    }

    /**
     * Updates the conference with ConferenceForm.
     * This method used upon object creation as well as updating existing Conferences.
     *
     * @param conferenceForm contains form data sent from the client
     */
    public void updateWithConferenceForm(ConferenceForm conferenceForm){
        this.name = conferenceForm.getName();
        this.description = conferenceForm.getDescription();
        List<String> topics = conferenceForm.getTopics();
        this.topics = topics == null || topics.isEmpty() ? DEFAULT_TOPICS : topics;
        this.city = conferenceForm.getCity() == null ? DEFAULT_CITY : conferenceForm.getCity();

        Date startDate = conferenceForm.getStartDate();
        this.startDate = startDate == null ? null : new Date(startDate.getTime());
        Date endDate = conferenceForm.getEndDate();
        this.endDate = endDate == null ? null : new Date(endDate.getTime());

        // Getting starting month for composite querry
        if(this.startDate != null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.startDate);
            this.month = calendar.get(calendar.MONTH) + 1; //calendar.MONTH is zero base, so adding one.
        }

        // Check maxAttendees value against the number of allready allocated seats
        int seatsAllocated = maxAttendees - seatsAvailable;
        if(conferenceForm.getMaxAttendees() < seatsAllocated){
            throw new IllegalArgumentException(seatsAllocated + " seats are allready allocated, "
                        +  "but you tried to set maxAttendees to " + conferenceForm.getMaxAttendees());
        }

        // The initial number of seatsAvailable is the same as maxAttendees.
        // However, if there ara already some seats allocated, we should subtract that number
        this.maxAttendees = conferenceForm.getMaxAttendees();
        this.seatsAvailable = this.maxAttendees - seatsAllocated;
    }

    public void bookSeats(final int number){
        if(seatsAvailable < number){
            throw new IllegalArgumentException("There are no seats available!");
        }
        seatsAvailable -= number;
    }

    public void giveBackSeats(final int number){
        if(seatsAvailable + number > maxAttendees){
            throw new IllegalArgumentException("The number of seats will exceeds the capacity!");
        }
        seatsAvailable += number;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Id: " + id + "/n")
                .append("Name: ").append(name).append("/n");
        if(city != null){
            sb.append("City: ").append(city).append("/n");
        }
        if(topics != null && topics.size() > 0){
            sb.append("Topics:\n");
            for(String topic : topics){
                sb.append("/t").append(topic).append("\n");
            }
        }
        if(startDate != null){
            sb.append("StartDate: ").append(startDate.toString()).append("/n");
        }
        if(endDate != null){
            sb.append("EndDate: ").append(endDate.toString()).append("/n");
        }
        sb.append("MaxAttendees: ").append(maxAttendees).append("/n");

        return sb.toString();
    }
}
