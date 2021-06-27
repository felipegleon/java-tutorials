package com.tutorial.springbootreactive.tutorial;

public class UserWithComments {

    private User user;
    private Comments comments;

    public UserWithComments(User user, Comments comments){
        this.user = user;
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "UserWithComments { user = " + user + ", comments = " + comments + "}";
    }
}
