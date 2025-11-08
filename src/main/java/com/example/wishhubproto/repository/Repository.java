package com.example.wishhubproto.repository;

import com.example.wishhubproto.model.Lists;
import com.example.wishhubproto.model.User;

import com.example.wishhubproto.model.Wish;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

//  GENERAL DOCUMENTATION AND EXPLANATION

// ResultSet:
// ResultSet is a Java Interface in the java.sql package. It can hold the result of database query(the raw data), i. e.
// the result of executing a SELECT statement.
// Each row in the result corresponds to one record, and each column maps to a field that the query includes in its SELECT statement.
// The JdbcTemplate class reads from this ResultSet, and converts rows of SQL data by a rowmapper into Java objects.

// When reading from the ResultSet, after a query, the methods .getInt() and .getString() are used to retrieve values from columns
 //specified by " ", and the method .next() moves the implicit *cursor down to the next row, where .getInt() and getString().

// By default, the ResultSet is read-only and it is read from top and dowwwards, with the .next() method.

// *Cursor refers to where the current row to be read from is - imagine a little arrow that points out from the row, on the row that is beign in focus by Java's in-memo´ry.

// The use of this Interface acts as a bridge between raw SQL data and the java objects in the application.



// KeyHolder:
//


//Practice of selecting specific columns in SELECT statements, instead of just writing *, when the methods either way aims
//to retreive to select all columns of a table - reasons:
// - it is easier to build the tables and add columns, as the repository methods still only selects the same columns in
//   in the SELECT statement. MODULARITY. This avoids breaking the contract between methods and retreiving uncessarry data.
//   Mapping reliability in other words.

@org.springframework.stereotype.Repository
public class Repository {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;


    private final JdbcTemplate jdbcTemplate;

    public Repository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Pay in mind, that the constructors of the model classes are used here, and the order of the arguemnts should match
    //the order in their respectivee constructors.

    //RowMapper - maps a ResultSet to a User object.
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getInt("UserID"),
            rs.getString("UserName"),
            rs.getString("PasswordHash"),
            rs.getString("Email"),
            rs.getTimestamp("CreatedAtTimeOf"));

    //RowMapper - maps a Resultset to a Wish object.
    //ERROR The constructor call doesn't match the constructor on all arguments - lists is missing.
    private final RowMapper<Wish> wishRowMapper = (rs, rowNum) -> new Wish(
            rs.getInt("WishID"),
            rs.getString("WishName"),
            rs.getString("Description"),
            rs.getString("ImgDataPath"));

    private final RowMapper<Lists> listsRowMapper = (rs, rowNum) -> new Lists(
            rs.getString("ListName"),
            rs.getInt("ListID"),
            rs.getInt("UserID"));

    public Repository() {
        jdbcTemplate = null;
    }


    //METHOD 1 ______________________________________________________________________________________________________

    //Supports a POST method - a new, populated User object is passed to this method, populated fields from thymeleaf form and populated object from controller method.

    // Method to insert a new user into the database
    //Controller method and service method ought to insert the values by a User object, that they have create into this
    //placeholder.

    //WHY DOES IT NEED TO HAVE A USER OBJECT?
    public User createUserAndReturn(User user) {
        String sql = "INSERT INTO users (UserName, PasswordHash, Email) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            return ps;
        }, keyHolder);
        //keyHolder); is the second argument to the jdbcTemplate.update() method.

        // Get the auto-generated ID from the insert
        int newUserId = keyHolder.getKey().intValue();

        // Returns the full inserte d user (to include timestamp)
        //The returned object can be used in a controller method to confirm if a new user was created +
        //the new user can be displayed.
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE UserID = ?", userRowMapper, newUserId);
    }

    //Method 2 _______________________________________________________________________________________________________

    // Delete a wish by ID
    //Supports a POST method - Wish id field is passsed to this method.

    //This repository method deletes a Wish object/row from the Wish table in databsae.
    //Due to the design of the databse, any entry with the same ID in the list-relationships table in database will be
    //deleted as well. Because of CASCADE keyword.
    public boolean deleteWishById(int wishId) {
        String sql = "DELETE FROM Wishes WHERE WishID = ?";
        int rowsAffected = jdbcTemplate.update(sql, wishId);
        return rowsAffected > 0;
    }

    //Method 3 _____________________________________________________________________________________________________________


    //Updates a wish
    //Supports a POST method - new populated Wish object is returned to this method, populated fields from thymeleaf form and populated object from controller method.
    public Wish updateWishAndReturn(Wish wish) {
        String sql = "UPDATE Wishes SET Title = ?, Description = ?, ImgDataPath = ? WHERE WishID = ?";
        int rowsAffected = jdbcTemplate.update(sql, wish.getTitle(), wish.getDescription(), wish.getImgDataPath(), wish.getWishID());

        if (rowsAffected > 0) {
            return jdbcTemplate.queryForObject("SELECT * FROM Wishes WHERE WishID = ?", wishRowMapper, wish.getWishID());
        } else {
            return null;
        }
    }

    //Method 4

    //Supports a POST method - Controller method provides the ListID and UserID necessary for recognizing which wish rows and list rows that should be deleted.
    //WishID is not necessary to pass to this method, as ListID is coupled with WishID in the junction table Wish-Relationship.

    //Returns a boolean to confirm to the controller method, whether any rows were affected, i.e. deleted in this case.

    public boolean deleteListAndWishes(int listId, int userId) {
        // Step 1: verify ownership
        Integer tempUserID = jdbcTemplate.queryForObject(
                "SELECT UserID FROM Lists WHERE ListID = ?",
                Integer.class,
                listId
        );
        if (tempUserID == null || tempUserID != userId) {
            return false; // not authorized to delete
        }

        jdbcTemplate.update("DELETE FROM ListsRelationship WHERE ListID = ?", listId);

        jdbcTemplate.update("DELETE FROM Wishes WHERE WishID NOT IN (SELECT WishID FROM ListsRelationship)");

        int rowsAffected = jdbcTemplate.update("DELETE FROM Lists WHERE ListID = ?", listId);

        return rowsAffected > 0;
    }

    //Method 5 ___________________________________________________________________________________________________

    //Opens a List from main view of the user's lists, where the wishes are all presentd.

    //There can only be selected a list pertaining the logged-in user. A check on user exists here, despite the user selecting a
    //wish list from a view where only the appropriate lists to the user are shown.

    //The clause: WHERE Lists.ListID = ? AND Lists.UserID = ?, ensures that only wishes pertaining the speicific user are selected.

    public List<Wish> getWishesByListAndUser(int listId, int userId) {
        String sql = """
        SELECT Wishes.WishID, Wishes.WishName, Wishes.Description, Wishes.ImgDataPath
        FROM Wishes
        JOIN ListsRelationship ON Wishes.WishID = ListsRelationship.WishID
        JOIN Lists ON ListsRelationship.ListID = Lists.ListID
        WHERE Lists.ListID = ? AND Lists.UserID = ?""";

        return jdbcTemplate.query(sql, wishRowMapper, listId, userId);
    }

    //Method 6 ___________________________________________________________________________________________________

    //This method creates a new WishList. It requires a populate Lists object from the template and controller method,
    //aaaaaand, a userID, as a WishList is personal. The userID should be obtained from the logged-in user.(in the session?).

    //
    public Lists createNewWishList(Lists lists, int userID) {
        String sql = "INSERT INTO Lists (ListName, UserID) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, lists.getListName());
            ps.setInt(2, userID);
            return ps;
        }, keyHolder);

        int generatedId = keyHolder.getKey().intValue();

        return new Lists( lists.getListName(), generatedId, userID);
    }

    //Method 7 _____________________________________________________________________________________________________

    //Retrieves all the lists, so that they can be shown by name and the user can be shown too on the template.

    public List<Lists> getAllListsByUser(int userID) {
        String sql = "SELECT ListID, ListName, UserID FROM Lists WHERE UserID = ?";
        return jdbcTemplate.query(sql, listsRowMapper, userID);
    }

    //Method 8 ______________________________________________________________________________________________________
    public User authenticateUser(User user) {
        String sql = "SELECT * FROM users WHERE UserName = ?";

        try {
            User foundUser = jdbcTemplate.queryForObject(sql, userRowMapper, user.getUserName());

            if (foundUser.getPasswordHash().equals(user.getPasswordHash())) {
                return foundUser;
            } else {
                return null;
            }
        } catch (EmptyResultDataAccessException e) {
            return null; // No user found with that username
        }
    }

    //Method 9 _____________________________________________________________________________________________________
    public Wish createWishForList(Wish wish, int listId) {
        // Step 1: Insert the wish itself
        String sql = "INSERT INTO Wishes (Title, Description, ImgDataPath) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, wish.getWishName());
            ps.setString(2, wish.getWishDescription());
            ps.setString(3, wish.getImgDataPath());
            return ps;
        }, keyHolder);

        int generatedWishId = keyHolder.getKey().intValue();

        // Link the new wish to the list in ListsRelationship
        String insertRelationshipSql = "INSERT INTO ListsRelationship (WishID, ListID) VALUES (?, ?)";
        jdbcTemplate.update(insertRelationshipSql, generatedWishId, listId);

        // Return the newly created wish (optional, for confirmation)
        String selectSql = "SELECT WishID, Title AS WishName, Description, ImgDataPath FROM Wishes WHERE WishID = ?";
        return jdbcTemplate.queryForObject(selectSql, wishRowMapper, generatedWishId);
    }

}
