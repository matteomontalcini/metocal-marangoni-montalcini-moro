/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package business.security.object;

/**
 *
 * @author DanieleMarangoni
 */
public class NameSurnameEmail {
    
    public NameSurnameEmail(){
        
    }
    
    private String name; 
    private String surname; 
    private String email; 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String toString(){
        return "Name: " + name + " Surname: " + surname + " Email: " + email;
    }
    
}