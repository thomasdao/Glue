package me.thomasdao.glue;

/**
 * Created by thomasdao on 1/5/16.
 */
@ModelName("Comment")
public class Comment implements Pinnable {
    private String id, comment;
    private User author;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Override
    public String unique() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Comment comment1 = (Comment) o;

        if (id != null ? !id.equals(comment1.id) : comment1.id != null) return false;
        if (comment != null ? !comment.equals(comment1.comment) : comment1.comment != null)
            return false;
        return !(author != null ? !author.equals(comment1.author) : comment1.author != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        return result;
    }
}
