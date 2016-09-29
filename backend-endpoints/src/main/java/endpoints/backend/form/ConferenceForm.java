package endpoints.backend.form;

import com.google.common.collect.ImmutableList;

import java.util.Date;
import java.util.List;

/** A simple Java object(POJO) representing a Conference form sent from the client. */
public class ConferenceForm {
    private String name; /** The name of the conference */
    private String description; /** The description of the conference */
    private List<String> topics; /** Topics that are discussed in the conference */
    private String city; /** Where the conference will take place */
    private Date startDate; /** Start date of the conference */
    private Date endDate; /** End date of the conference */
    private int maxAttendees; /** The capacity of the conference */

    /** Just making the default constructor private */
    private ConferenceForm(){}

    /**
     * @param name
     * @param description
     * @param topics
     * @param city
     * @param startDate
     * @param endDate
     * @param maxAttendees
     */
    public ConferenceForm(String name, String description, List<String> topics, String city,
                          Date startDate, Date endDate, int maxAttendees){
        this.name = name;
        this.description = description;
        this.topics = topics == null ? null : ImmutableList.copyOf(topics);
        this.city = city;
        this.startDate = startDate == null ? null : new Date(startDate.getTime());
        this.endDate = endDate == null ? null : new Date(endDate.getTime());
        this.maxAttendees = maxAttendees;
    }

    //getters
    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    public List<String> getTopics(){
        return topics;
    }
    public String getCity(){
        return city;
    }
    public Date getStartDate(){
        return startDate;
    }
    public Date getEndDate(){
        return endDate;
    }
    public int getMaxAttendees(){
        return maxAttendees;
    }
}
