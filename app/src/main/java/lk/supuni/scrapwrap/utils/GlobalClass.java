package lk.supuni.scrapwrap.utils;

import android.app.Application;
import android.net.Uri;

import java.io.File;

import lk.supuni.scrapwrap.models.PostModel;

public class GlobalClass extends Application {

    public static final String TAG = "SCRAPPOSTLOG";

    public static final String PATH_POSTS = "posts";
    public static final String PATH_IMAGES = "images";
    public static final String PATH_CONFIRM_IMAGES = "confirm_images";

    //    public static final String URL_CHECK_SCRAPS = "https://image-classfier-rest.herokuapp.com/upload/";
    static String IP_ADDRESS = "192.168.137.1";
    public static final String URL_CHECK_SCRAPS = "http://" + IP_ADDRESS + ":5000/upload";

    private PostModel selectedPost = null;
    private Uri selectedPostImageUri = null;

    public PostModel getSelectedPost() {
        return selectedPost;
    }

    public void setSelectedPost(PostModel selectedPost) {
        this.selectedPost = selectedPost;
    }

    public Uri getSelectedPostImageUri() {
        return selectedPostImageUri;
    }

    public void setSelectedPostImageUri(Uri selectedPostImageUri) {
        this.selectedPostImageUri = selectedPostImageUri;
    }
}
