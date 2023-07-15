package com.example.petfinder.container;

public class RecordModel {
    String id, name, breed, sex, age, birthdate, weight, image, addedtime, updatedtime, petFeederID;

    public RecordModel(String id, String name, String breed, String sex, String birthdate, String age, String weight, String image, String addedtime, String updatedtime) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.sex = sex;
        this.birthdate = birthdate;
        this.age = age;
        this.weight = weight;
        this.image = image;
        this.addedtime = addedtime;
        this.updatedtime = updatedtime;
        this.petFeederID = null;
    }
    public RecordModel(String id, String name, String breed, String sex, String birthdate, String age, String weight, String image, String addedtime, String updatedtime, String petFeederId) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.sex = sex;
        this.birthdate = birthdate;
        this.age = age;
        this.weight = weight;
        this.image = image;
        this.addedtime = addedtime;
        this.updatedtime = updatedtime;
        this.petFeederID = petFeederId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }
    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getAge() {
        return age;
    }
    public void setAge(String age) {
        this.age = age;
    }

    public String getWeight() {
        return weight;
    }
    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public String getAddedtime() {
        return addedtime;
    }
    public void setAddedtime(String addedtime) {
        this.addedtime = addedtime;
    }

    public String getUpdatedtime() {
        return updatedtime;
    }
    public void setUpdatedtime(String updatedtime) {
        this.updatedtime = updatedtime;
    }

    public String getPetFeederID() {
        return petFeederID;
    }
    public void setPetFeederID(String petFeederID) {
        this.petFeederID = petFeederID;
    }
}
