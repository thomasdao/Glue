package me.thomasdao.glue;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN)
public class GlueTest {
    @Before
    public void setUp() throws Exception {
        // Register Model
        Glue.init(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        Glue.clearAll();
    }

    @Test
    public void canSaveStringToPreference() throws Exception {
        CacheHelper.save("key", "val");
        assertEquals("val", CacheHelper.getString("key"));
    }

    @Test
    public void canSaveObjectToPreference() throws Exception {
        User user = new User();
        user.setId("1");
        user.setUsername("thomas");
        user.setAge(28);
        Glue.pin(user);

        // Make sure user can be retrieved from memory cache
        assertEquals(user, Glue.get("1", User.class));

        // Clear memory cache to force load from preference
        Glue.clearMemoryCache();
        assertEquals(user, Glue.get("1", User.class));

        // Setup some relationships
        User journalist = new User();
        journalist.setId("2");
        journalist.setUsername("john");
        journalist.setAge(40);
        Glue.pin(journalist);

        Post post = new Post();
        post.setId("1");
        post.setAuthor(journalist);
        post.setContent("Hello. This is a new article!");
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        Glue.pin(post);

        // Make sure post can be retrieved from memory cache
        assertEquals(post, Glue.get("1", Post.class));

        // Clear memory to make sure post can load its relationships
        Glue.clearMemoryCache();
        assertEquals(post, Glue.get("1", Post.class));

        // User now make some comments on the post
        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setId("1");
        comment.setComment("Great article!");
        Glue.pin(comment);

        Comment comment1 = new Comment();
        comment1.setAuthor(journalist);
        comment1.setId("2");
        comment1.setComment("Thanks");
        Glue.pin(comment1);

        ArrayList<Comment> comments = new ArrayList<>();
        comments.add(comment);
        comments.add(comment1);
        post.setComments(comments);
        Glue.pin(post);

        // Make sure post can be retrieved from memory cache
        assertEquals(post, Glue.get("1", Post.class));

        // Clear memory to make sure post can load its relationships
        Glue.clearMemoryCache();
        assertEquals(post, Glue.get("1", Post.class));

        // Make sure post now have two comments
        Post existingPost = (Post) Glue.get("1", Post.class);
        ArrayList<Comment> comments1 = existingPost.getComments();
        assertEquals(2, comments1.size());
        assertEquals(comment, comments1.get(0));
        assertEquals(comment1, comments1.get(1));
    }
}