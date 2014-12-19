/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package business.security.boundary;

import business.security.entity.Event;
import business.security.entity.Group;
import business.security.entity.User;
import business.security.entity.WeatherCondition;
import business.security.object.NameSurnameEmail;
import java.beans.Statement;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
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

    public void save(Event event) {
        event.setGroupName(Group.EVENT);
        em.persist(event);
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
    
    public void updateUser(User user) {
        System.out.println(user.getName());
        Query qUpdateUser = em.createQuery("UPDATE USER u SET u.name = '" + user.getName() + 
                "', u.surname = '" + user.getSurname() + 
                //"', u.birthday = " + user.getBirthday() + 
                "', u.phoneNumber = ' " + user.getPhoneNumber() + 
                "', u.residenceTown = '" + user.getResidenceTown() +
                "', u.email = '" + user.getEmail() + 
                "' WHERE u.email = '" +user.getEmail() + "'");
        qUpdateUser.executeUpdate();
    }
    
   public void findCreatedEvent() {
        Query qCreatedEvent = em.createQuery("SELECT e.name FROM EVENT e WHERE e.organizer.email = '" + getLoggedUser().getEmail() + "'");
        List<String> lista = new ArrayList<String>(); 
        lista = qCreatedEvent.getResultList();
    }
   
   public NameSurnameEmail findUser(String email) {
       Query qFindUserName = em.createQuery("SELECT u.name FROM USER u WHERE u.email = '" + email + "'"); 
       Query qFindUserSurname = em.createQuery("SELECT u.surname FROM USER u WHERE u.email = '" + email + "'"); 
       NameSurnameEmail newElement = new NameSurnameEmail(); 
       newElement.setName((String)qFindUserName.getSingleResult());
       newElement.setSurname((String)qFindUserSurname.getSingleResult());
       newElement.setEmail(email);
       System.out.println("" + newElement.getName() + "|||| sdfddkfnekfd");
       System.out.println("" + newElement.getSurname() + "|||| sdfddkfnekfd");
       
       
       
       /*newElement.setName(element.get(0));
       newElement.setSurname(element.get(1)); 
       newElement.setEmail(element.get(2));*/
       return newElement; 
   }
    
}