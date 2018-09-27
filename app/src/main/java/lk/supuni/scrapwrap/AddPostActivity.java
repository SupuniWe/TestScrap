package lk.supuni.scrapwrap;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.request.MultiPartRequest;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import lk.supuni.scrapwrap.models.PostModel;
import lk.supuni.scrapwrap.utils.FileUtils;
import lk.supuni.scrapwrap.utils.FirebaseUtils;
import lk.supuni.scrapwrap.utils.GlobalClass;
import lk.supuni.scrapwrap.utils.MultipartRequest;
import lk.supuni.scrapwrap.utils.PermissionUtils;

import static lk.supuni.scrapwrap.utils.GlobalClass.URL_CHECK_SCRAPS;

public class AddPostActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 71;
    PostModel postModel;
    FirebaseUtils firebaseUtils;

    ImageView imgBtnAddPostImage;
    Button btnAddPostCheckImage;
    Button btnAddPostChooseGallery;
    TextView txtAddPostImageDetails;

    Uri imageFileUri;
    File imageFile;
    RequestQueue requestQueue;
    GlobalClass globalClass;
    String postDesc;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseUtils = new FirebaseUtils();
        globalClass = (GlobalClass) this.getApplicationContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        imgBtnAddPostImage = findViewById(R.id.imgBtnAddPostImage);
        btnAddPostCheckImage = findViewById(R.id.btnAddPostCheckImage);
        txtAddPostImageDetails = findViewById(R.id.txtAddPostImageDetails);
        btnAddPostChooseGallery = findViewById(R.id.btnAddPostChooseGallery);

        imgBtnAddPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        btnAddPostCheckImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImageToCheck();
            }
        });

        btnAddPostChooseGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        postModel = new PostModel();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = new File(getExternalCacheDir(),
                String.valueOf(System.currentTimeMillis()) + ".jpeg");
        imageFileUri = Uri.fromFile(imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        AddPostActivity.this.startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageFileUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageFileUri);
                imgBtnAddPostImage.setImageBitmap(bitmap);
                imageFile = FileUtils.getFileFromImageUri(AddPostActivity.this, imageFileUri);
                imageFileUri = Uri.parse(FileUtils.getRealPathFromURI(AddPostActivity.this, imageFileUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                super.onActivityResult(requestCode, resultCode, data);

                Log.d(GlobalClass.TAG, "Image : " + requestCode);
                Log.d(GlobalClass.TAG, "Image : " + resultCode);
                Log.d(GlobalClass.TAG, "Image : " + data);
                getCurrentLocation();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageFileUri);
                imgBtnAddPostImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.d(GlobalClass.TAG, "Image Error : " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    PermissionUtils.LOCATION_PERMISSION,
                    PermissionUtils.LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle current location object
                                //TODO : get location and add post to database
                                postModel.setLongitude(location.getLongitude());
                                postModel.setLatitude(location.getLatitude());
                                txtAddPostImageDetails.setText(String.format("Latitude : %s\nLongitude : %s", location.getLatitude(), location.getLongitude()));
                            }
                        }

                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), getString(R.string.service_not_available), Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }

    private void addToCommunity() {
        Long timestamp = System.currentTimeMillis();
        postModel.setTimestamp(timestamp);

        //Set user
        String userID = "user1";
        postModel.setPublisher(userID);

        String key = firebaseUtils.pushObject(GlobalClass.PATH_POSTS, postModel);
        postModel.setId(key);
        postModel.setVerified(false);

        firebaseUtils.updatePost(AddPostActivity.this, postModel, imageFile);
    }

    private void sendImageToCheck() {
        if (imageFileUri != null) {
            Log.d(GlobalClass.TAG, "Response : Sending");
            Log.d(GlobalClass.TAG, "Response : FILE : " + imageFileUri.getPath());
            File im = new File(imageFileUri.getPath());
            MultipartRequest multipartRequest = new MultipartRequest(URL_CHECK_SCRAPS,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(GlobalClass.TAG, "Response : Error : " + error.getMessage());
                            error.printStackTrace();
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(GlobalClass.TAG, "Response : " + response);
                            try {
                                JSONArray array = response.getJSONArray("predictions");
                                StringBuilder desc = new StringBuilder();

                                for (int i = 0; i < 5; i++) {
                                    JSONObject object = array.getJSONObject(i);
                                    double value = object.getDouble("probability") * 100.0;
                                    String className = object.getString("class");

                                    desc.append(className).append(" : ").append(String.format(Locale.US, "%.2f", value)).append("%\n");
                                }
                                postDesc = desc.toString();
                                txtAddPostImageDetails.setText(postDesc);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //TODO: available logic
                            Toast.makeText(getApplicationContext(), "uploaded", Toast.LENGTH_LONG).show();
                            boolean scrapAvailable = true;
                            if (scrapAvailable) {
                                showCleanerDialog();
                            } else {
                                Toast.makeText(getApplicationContext(), "No scraps found", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, im);
            requestQueue.add(multipartRequest);
        }
    }

    private void showCleanerDialog() {
        AlertDialog.Builder cleanerDialog = new AlertDialog.Builder(AddPostActivity.this);
        cleanerDialog.setIcon(android.R.drawable.ic_dialog_info);
        cleanerDialog.setTitle("Clean this ?");
        cleanerDialog.setMessage("Scrap Details : " + postDesc + "\n\nHow do you want to clean this");
        cleanerDialog.setCancelable(true);
        cleanerDialog.setPositiveButton("Myself", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //TODO: Accept clean yourself
                globalClass.setSelectedPostImageUri(imageFileUri);
                globalClass.setSelectedPost(postModel);
                Intent intent = new Intent(AddPostActivity.this, CleanActivity.class);
                AddPostActivity.this.startActivity(intent);
            }
        });

        cleanerDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        cleanerDialog.setNeutralButton("Post to community", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //TODO: Post to community
                addToCommunity();
            }
        });

        AlertDialog dialog1 = cleanerDialog.create();
        dialog1.show();
    }
}
