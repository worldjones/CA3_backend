/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dto;

import entities.Address;


public class AddressDTO {
    private int id;
    private String street;
    private int houseNumber;

    public AddressDTO(int id, String street, int houseNumber) {
        this.id = id;
        this.street = street;
        this.houseNumber = houseNumber;
    }
    
    public AddressDTO(Address a) {
        this.id = a.getId();
        this.street = a.getStreet();
        this.houseNumber = a.getHouseNumber();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }
    
    
    
}
