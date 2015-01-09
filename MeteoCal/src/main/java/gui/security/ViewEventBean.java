/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package gui.security;

import business.security.boundary.EventManager;
import business.security.boundary.UserInformationLoader;
import business.security.entity.Event;
import business.security.entity.Invite;
import business.security.entity.User;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author m-daniele
 */
@Named
@ViewScoped
public class ViewEventBean implements Serializable{
    
    private Event event;
    
    @EJB
    private EventManager eventManager;
    
    @EJB
    private UserInformationLoader userInformationLoader;
    
    private List<User> accepted;
    private List<User> refused;
    private List<User> pendent;
    
    @PostConstruct
    public void init(){
        temp();
    }
    
    private void temp(){
        //Prelevo l'id passato in GET
        long idEvent = Long.parseLong(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id"));
        System.out.println("ID EVENTO "+idEvent);
        //Aggiorno la lista degli eventi
        setEvent(eventManager.getEventById(idEvent));
        /**
         * Se l'utente loggato è il creatore devo caricare la lista degli:
         * - utenti che hanno accettato la partecipazione
         * - utenti che hanno rifiutato la partecipazione
         * - utenti che devono ancora rispondere
         */
        if(event.getOrganizer().getEmail()==eventManager.getLoggedUser().getEmail()){
            accepted = eventManager.getAcceptedPeople(event);
            refused = eventManager.getRefusedPeople(event);
            pendent = eventManager.getPendentPeople(event);
        } else{
            accepted =refused = pendent = null;
        }
    }
    
    public String viewProfile(User u){
        return "userProfile?faces-redirect=true&amp;email="+u.getEmail();
    }
    
    /**
     * @return the event
     */
    public Event getEvent() {
        return event;
    }
    
    /**
     * @param event the event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }
    
    
    public void acceptInvitation() {
        eventManager.addParticipantToEvent(event);
        //return "event?faces-redirect=true";
    }
    
    public void refuseInvitation() {
        eventManager.removeParticipantFromEvent(event);
        //return "event?faces-redirect=true";
    }
    
    public void reAcceptInvitation() {
        eventManager.addParticipantToEvent(event);
        //return "event?faces-redirect=true";
    }
    
    public void deleteParticipation() {
        eventManager.removeParticipantFromEvent(event);
        //return "event?faces-redirect=true";
    }
    
    
    public boolean getFindInviteStatusInvited() {
        return userInformationLoader.findInviteStatus(event).getStatus()== Invite.InviteStatus.invited;
    }
    
    public boolean getFindInviteStatusAccepted() {
        return userInformationLoader.findInviteStatus(event).getStatus() == Invite.InviteStatus.accepted;
    }
    public boolean getFindInviteStatusNotAccepted() {
        return userInformationLoader.findInviteStatus(event).getStatus() == Invite.InviteStatus.notAccepted;
    }
    public boolean getFindInviteStatusDelayed() {
        return userInformationLoader.findInviteStatus(event).getStatus() == Invite.InviteStatus.delayedEvent;
    }

    /**
     * @return the accepted
     */
    public List<User> getAccepted() {
        return accepted;
    }

    /**
     * @return the refused
     */
    public List<User> getRefused() {
        return refused;
    }

    /**
     * @return the pendent
     */
    public List<User> getPendent() {
        return pendent;
    }
    
}
