/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package business.security.boundary;

import business.security.entity.Event;
import business.security.entity.Invite;
import business.security.entity.NotificationType;
import business.security.entity.User;
import business.security.entity.WeatherCondition;
import business.security.object.NameSurnameEmail;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class EventManager {

    @PersistenceContext
    EntityManager em;
    
    @Inject
    Principal principal;
    
    @EJB
    private NotificationManager notificationManager;
    
    @EJB
    private UserInformationLoader userInformationLoader; 
    
   @EJB
   private SearchManager searchManager; 
   
   private List<NameSurnameEmail> invitedPeople;
    
    private List<NameSurnameEmail> partialResults;
    
    private Event e; 
    
    private WeatherCondition acceptedWeatherConditions; 
    
    public EventManager() {
        e = new Event(); 
        acceptedWeatherConditions = new WeatherCondition();
        invitedPeople = new ArrayList<>(); 
        partialResults = new ArrayList<>(); 
        notificationManager = new NotificationManager();
    }
    

    public void createEvent(Event event, WeatherCondition awc) {
        event.setOrganizer(getLoggedUser());
        save(awc);
        event.setAcceptedWeatherConditions(awc);
        em.persist(event);
        setEvent(event);
        notificationManager.setEvent(event);
    }
    
    public void save(WeatherCondition weatherCondition) {
        em.persist(weatherCondition);
    }

    public void deleteEvent() {
        em.remove(getLoggedUser());
    }

    public User getLoggedUser() {
        return em.find(User.class, principal.getName());
    }

   
 
 public boolean checkDateConsistency() {
      if (e.getTimeStart().after(e.getTimeEnd())) {
          return false;
      } else {
          for(Event ev : userInformationLoader.loadCreatedEvents()) {
              if(e.getTimeStart().after(ev.getTimeStart()) && e.getTimeStart().before(ev.getTimeEnd()) || e.getTimeEnd().after(ev.getTimeStart()) && e.getTimeEnd().before(ev.getTimeEnd())) {
                  return false; 
              }
          }
          for(Event ev : userInformationLoader.loadAcceptedEvents()) {
              if(e.getTimeStart().after(ev.getTimeStart()) && e.getTimeStart().before(ev.getTimeEnd()) || e.getTimeEnd().after(ev.getTimeStart()) && e.getTimeEnd().before(ev.getTimeEnd())) {
                  return false; 
              }
          }
      }
      return true; 
  }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public Event getEvent() {
        return e;
    }

    public void setEvent(Event event) {
        this.e = event;
    }
    
    public List<NameSurnameEmail> getPartialResults() {
        if (partialResults == null) {
            partialResults = new ArrayList<>();
        }
        return partialResults;
    }

    public void setPartialResults(List<NameSurnameEmail> partialResults) {
        this.partialResults = partialResults;
    }
  
    public List<NameSurnameEmail> getInvitedPeople() {
        if (invitedPeople == null) {
            invitedPeople = new ArrayList<>();
        }
        return invitedPeople;
    }

    public void setInvitedPeople(List<NameSurnameEmail> invitedPeople) {
        this.invitedPeople = invitedPeople;
    }
    
    

    public WeatherCondition getAcceptedWeatherConditions() {
        return acceptedWeatherConditions;
    }

    public void setAcceptedWeatherConditions(WeatherCondition acceptedWeatherConditions) {
        this.acceptedWeatherConditions = acceptedWeatherConditions;
    }
    
    public void addInvitation(String email) {
        NameSurnameEmail element = searchManager.findNameSurnameEmailFromUser(email);
        notificationManager.createInviteNotification(element);
        invitedPeople.add(element);
    }
    
    public void addInvitation(String name, String surname) {
        NameSurnameEmail element = searchManager.findNameEmailSurnameFromNameSurname(name, surname).get(0);
        notificationManager.createInviteNotification(element);
        invitedPeople.add(element);
    }
    
    public void addInvitation(NameSurnameEmail element) {
        notificationManager.createInviteNotification(element);
        invitedPeople.add(element);
        partialResults = new ArrayList<>(); 
    }
    
    public void removeEvent() {
        Query setEventDeleted = em.createQuery("UPDATE EVENT event SET event.deleted =?1 WHERE event.id = ?2");
        setEventDeleted.setParameter(1, true); 
        setEventDeleted.setParameter(2, e.getId());
        setEventDeleted.executeUpdate();
        notificationManager.setEvent(e);
        notificationManager.sendNotifications(NotificationType.deletedEvent);
    }
    
    public void updateEventInformation() {
        Query updateWeatherCondition = em.createQuery("UPDATE WeatherCondition w SET w.precipitation =?1, w.wind =?2, w.temperature =?3 WHERE w.id =?4");   
        updateWeatherCondition.setParameter(1, acceptedWeatherConditions.getPrecipitation()); 
        updateWeatherCondition.setParameter(2, acceptedWeatherConditions.getWind()); 
        updateWeatherCondition.setParameter(3, acceptedWeatherConditions.getTemperature()); 
        updateWeatherCondition.setParameter(4, acceptedWeatherConditions.getId());
        updateWeatherCondition.executeUpdate();
        
        Query updateEventInformation = em.createQuery ("UPDATE EVENT event SET event.name =?1, event.town =?2, event.address =?3, event.description =?4, event.predefinedTypology = ?5 WHERE event.id =?6"); 
        updateEventInformation.setParameter(1, e.getName());
        updateEventInformation.setParameter(2, e.getTown());
        updateEventInformation.setParameter(3, e.getAddress());
        updateEventInformation.setParameter(4, e.getDescription());
        updateEventInformation.setParameter(5, e.getPredefinedTypology());
        updateEventInformation.setParameter(6, e.getId());
        updateEventInformation.executeUpdate();
        
        
        //if the date is changed
        Query findEventThroughId = em.createQuery("SELECT event from EVENT event WHERE event.id =?1 ");
        findEventThroughId.setParameter(1, e.getId()); 
        Event ev = ((List<Event>) findEventThroughId.getResultList()).get(0); 
        if (!ev.getTimeStart().equals(e.getTimeStart()) || !ev.getTimeEnd().equals(e.getTimeEnd())) {
            notificationManager.setEvent(e);
            notificationManager.sendNotifications(NotificationType.delayedEvent);
            Query updateDateOfEvent = em.createQuery("UPDATE EVENT event SET event.timeStart =?1, event.timeEnd =?2 WHERE event.id =?3");
            updateDateOfEvent.setParameter(1, e.getTimeStart());
            updateDateOfEvent.setParameter(2, e.getTimeEnd());
            updateDateOfEvent.setParameter(3, e.getId());
            updateDateOfEvent.executeUpdate();
        }
        /*notificationManager.setEvent(e);
        notificationManager.sendNotifications(NotificationType.delayedEvent);*/
    }
    
    public void addInvitation() {
        notificationManager.setEvent(e);
    }

    


    

    
    
}
