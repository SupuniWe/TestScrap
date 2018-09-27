package lk.supuni.scrapwrap;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import lk.supuni.scrapwrap.adapters.PostListAdapter;
import lk.supuni.scrapwrap.models.PostModel;
import lk.supuni.scrapwrap.utils.FirebaseUtils;
import lk.supuni.scrapwrap.utils.GlobalClass;

public class MainActivity extends AppCompatActivity {

    FirebaseUtils firebaseUtils;
    RecyclerView postRecyclerView;
    SwipeRefreshLayout postSwipeRefreshLayout;
    List<PostModel> postDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseUtils = new FirebaseUtils();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
                MainActivity.this.startActivity(intent);

            }
        });

        postRecyclerView = findViewById(R.id.postRecyclerView);
        postSwipeRefreshLayout = findViewById(R.id.postSwipeRefreshLayout);

        postRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postRecyclerView.setItemAnimator(new DefaultItemAnimator());

        postSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postSwipeRefreshLayout.setRefreshing(true);
                postRecyclerView.setAdapter(new PostListAdapter(postDataSet));
                postSwipeRefreshLayout.setRefreshing(false);
            }
        });

        Query query = firebaseUtils.getReference(GlobalClass.PATH_POSTS).orderByChild("published");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postDataSet = new ArrayList<>();
                postSwipeRefreshLayout.setRefreshing(true);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostModel postModel = snapshot.getValue(PostModel.class);
                    if (postModel != null) {
                        postModel.setId(snapshot.getKey());
                        postDataSet.add(postModel);
                    }
                }

                postRecyclerView.setAdapter(new PostListAdapter(postDataSet));
                postSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(GlobalClass.TAG, "Posts failed to retrieve", databaseError.toException());
                postSwipeRefreshLayout.setRefreshing(false);
            }
        };
        query.addValueEventListener(valueEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
