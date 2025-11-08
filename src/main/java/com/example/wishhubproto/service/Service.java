package com.example.wishhubproto.service;

import com.example.wishhubproto.model.Lists;
import com.example.wishhubproto.model.User;
import com.example.wishhubproto.model.Wish;
import com.example.wishhubproto.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Service
public class Service {

    private final Repository repository;

    public Repository(Repository repository) {
        this.repository = repository;
    }

    //Method 1 in repo - create user

    //receives a user object from the controller method and returns it
    public User createUserAndReturn(User user) {
        return repository.createUserAndReturn(user);
    }

    //Method 2 in repo - delete wish

    public boolean deleteWishByID(int wishID) {
        return repository.deleteWishById(wishID);
    }

    //Method 3 in repo -
    public Wish updateWishAndReturn(Wish wish) {
        return repository.updateWishAndReturn(wish);
    }

    //Method 4 in repo -
    public boolean deleteWishAndWishes(int listId, int userId) {
        return repository.deleteListAndWishes(listId, userId);
    }

    //Method 5 in repo -
    public List<Wish> getWishesByListAndUser(int listId, int userId) {
        return repository.getWishesByListAndUser(listId, userId);
    }

    //Method 6 in repo -
    public Lists createNewWishList(Lists lists, int userID) {
        return repository.createNewWishList(lists, userID);
    }

    //Method 7 in repo -
    public List<Lists> getAllListsByUser(int userID) {
        return repository.getAllListsByUser(userID);
    }
    //Method 8  in repo -
    public User authenticateUser(User user) {
        return repository.authenticateUser(user);
    }
    //Method 9 in repo -
    public Wish createWishForList(Wish wish, int listId) {
        return repository.createWishForList(wish, listId);
    }
}
