package me.thomasdao.glue;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by thomasdao on 1/5/16.
 */
@ModelName("Post")
public class Post implements Pinnable {
    private String id, content;
    private Date createdAt, updatedAt;
    private User author;
    private ArrayList<Comment> comments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public String unique() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (id != null ? !id.equals(post.id) : post.id != null) return false;
        if (content != null ? !content.equals(post.content) : post.content != null) return false;
        if (createdAt != null ? !createdAt.equals(post.createdAt) : post.createdAt != null)
            return false;
        if (updatedAt != null ? !updatedAt.equals(post.updatedAt) : post.updatedAt != null)
            return false;
        if (author != null ? !author.equals(post.author) : post.author != null) return false;
        return !(comments != null ? !comments.equals(post.comments) : post.comments != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        return result;
    }
}
