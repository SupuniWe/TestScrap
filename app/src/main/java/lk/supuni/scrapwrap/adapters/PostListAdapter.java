package lk.supuni.scrapwrap.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;
import java.util.List;

import lk.supuni.scrapwrap.AddPostActivity;
import lk.supuni.scrapwrap.CleanActivity;
import lk.supuni.scrapwrap.R;
import lk.supuni.scrapwrap.models.PostModel;
import lk.supuni.scrapwrap.utils.DateTimeUtils;
import lk.supuni.scrapwrap.utils.FileUtils;
import lk.supuni.scrapwrap.utils.FirebaseUtils;
import lk.supuni.scrapwrap.utils.GlobalClass;

import static lk.supuni.scrapwrap.utils.GlobalClass.TAG;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.PostViewHolder> {

    FirebaseUtils firebaseUtils;
    private List<PostModel> postModelList;

    public PostListAdapter(List<PostModel> postModelList) {
        this.postModelList = postModelList;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        firebaseUtils = new FirebaseUtils();

        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int position) {
        Log.d(TAG, "Position : " + position);
        final Context context = holder.postView.getContext();
        final PostModel post = postModelList.get(position);
        String time = DateTimeUtils.getRelativeDateTime(context, post.getTimestamp());
        holder.txtPostPublished.setText(time);
        holder.txtPostPublisher.setText(context.getString(R.string.format_post_list_published_by, post.getPublisher()));

        holder.postView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalClass globalClass = (GlobalClass) context.getApplicationContext();
                globalClass.setSelectedPost(post);
                Intent intent = new Intent(context.getApplicationContext(), CleanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
            }
        });

        if (post.isVerified()) {
            holder.txtPostVerifier.setText(context.getString(R.string.format_post_list_verified_by, post.getVerifier()));
            try {
                Log.d(GlobalClass.TAG, " FIREBASEUTIL : LOAD : POST : POST_ID : " + post.getId());
                final File localFile = File.createTempFile("confirm", "jpeg");
                firebaseUtils.getStorageReference().child(GlobalClass.PATH_CONFIRM_IMAGES + "/" + post.getId()).getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Log.d(GlobalClass.TAG, " FIREBASEUTIL : LOAD : DOWNLOADED CONFIRM : POST_ID : " + post.getId());
                                Bitmap image = FileUtils.getBitmapFromFile(localFile);
                                holder.imgPostImage.setImageBitmap(image);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //set post without image
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.txtPostVerifier.setVisibility(View.GONE);
            try {
                Log.d(GlobalClass.TAG, " FIREBASEUTIL : LOAD : POST : POST_ID : " + post.getId());
                final File localFile = File.createTempFile("images", "jpeg");
                firebaseUtils.getStorageReference().child(GlobalClass.PATH_IMAGES + "/" + post.getId()).getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Log.d(GlobalClass.TAG, " FIREBASEUTIL : LOAD : DOWNLOADED : POST_ID : " + post.getId());
                                Bitmap image = FileUtils.getBitmapFromFile(localFile);
                                holder.imgPostImage.setImageBitmap(image);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //set post without image
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return postModelList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        public TextView txtPostPublished, txtPostPublisher, txtPostVerifier;
        public View postView;
        public ImageView imgPostImage;
//        public ImageView imgVerifiedImage;

        public PostViewHolder(final View view) {
            super(view);
            postView = view;
            txtPostPublished = view.findViewById(R.id.txtPostPublished);
            txtPostPublisher = view.findViewById(R.id.txtPostPublisher);
            txtPostVerifier = view.findViewById(R.id.txtPostVerifier);
            imgPostImage = view.findViewById(R.id.imgPostImage);
//            imgVerifiedImage = view.findViewById(R.id.imgVerifiedImage);

        }
    }
}
