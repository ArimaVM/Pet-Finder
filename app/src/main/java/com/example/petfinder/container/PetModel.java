package com.example.petfinder.container;

public class PetModel {
    String MAC_ADDRESS, Name, Breed, Sex, Birthdate, Allergies, Treats, Medications, VetName, VetContact, Image, PetFeederID;
    Integer Age, Weight;
    String added_timestamp, updated_timestamp;

    public void nullify(){
        MAC_ADDRESS = "";
        Name = "";
        Breed = "";
        Sex = "";
        Birthdate = "";
        VetName = "";
        VetContact = "";
        Image = "";
        Age = 0;
        Weight = 0;
        Allergies = "";
        Treats = "";
        Medications = "";
        PetFeederID = "";
    }

    public PetModel() {
        nullify();
    }

    public String getMAC_ADDRESS() {
        return MAC_ADDRESS;
    }
    public void setMAC_ADDRESS(String MAC_ADDRESS) {
        this.MAC_ADDRESS = MAC_ADDRESS;
    }

    public String getImage() {
        return Image;
    }
    public void setImage(String image) {
        Image = image;
    }

    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }

    public String getBreed() {
        return Breed;
    }
    public void setBreed(String breed) {
        Breed = breed;
    }

    public String getSex() {
        return Sex;
    }
    public void setSex(String sex) {
        Sex = sex;
    }

    public String getBirthdate() {
        return Birthdate;
    }
    public void setBirthdate(String birthdate) {
        Birthdate = birthdate;
    }

    public Integer getAge() {
        return Age;
    }
    public void setAge(Integer age) {
        Age = age;
    }

    public Integer getWeight() {
        return Weight;
    }
    public void setWeight(Integer weight) {
        Weight = weight;
    }

    public String getAllergies() {
        return Allergies;
    }
    public void setAllergies(String allergies) {
        Allergies = allergies;
    }

    public String getTreats() {
        return Treats;
    }
    public void setTreats(String treats) {
        Treats = treats;
    }

    public String getMedications() {
        return Medications;
    }
    public void setMedications(String medications) {
        Medications = medications;
    }

    public String getVetName() {
        return VetName;
    }
    public void setVetName(String vetName) {
        VetName = vetName;
    }

    public String getVetContact() {
        return VetContact;
    }
    public void setVetContact(String vetContact) {
        VetContact = vetContact;
    }

    public String getPetFeederID() {
        return PetFeederID;
    }
    public void setPetFeederID(String petFeederID) {
        PetFeederID = petFeederID;
    }

    public String getAdded_timestamp() {
        return added_timestamp;
    }
    public void setAdded_timestamp(String added_timestamp) {
        this.added_timestamp = added_timestamp;
    }

    public String getUpdated_timestamp() {
        return updated_timestamp;
    }
    public void setUpdated_timestamp(String updated_timestamp) {
        this.updated_timestamp = updated_timestamp;
    }
}
