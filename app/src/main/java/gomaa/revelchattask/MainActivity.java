package gomaa.revelchattask;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import gomaa.revelchattask.Adapters.MessageAdapter;
import gomaa.revelchattask.Model.Chat;
import gomaa.revelchattask.Model.User;

public class MainActivity extends AppCompatActivity {


    TextView username;

    FirebaseUser currentUser;
    DatabaseReference reference;
    FirebaseAuth mAuth;


    ImageButton btn_send;
    EditText txt_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        txt_send = findViewById(R.id.txt_send);

        intent = getIntent();
         final String userid = intent.getStringExtra("userid");



      //  currentUser = FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = txt_send.getText().toString();
                if (!msg.equals("")){
                    sendMessage( currentUser.getUid(), msg);
                }else{
                    Toast.makeText(MainActivity.this, "You can't send Empty message", Toast.LENGTH_SHORT).show();
                }
                txt_send.setText("");
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               /* User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
*/

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    readMessgae(currentUser.getUid(), userid);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage (String sender, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("message",message);

        reference.child(currentUser.getUid()).push().setValue(hashMap);
    }

    private void readMessgae( final String my_id, final String user_id){
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getSender().equals(user_id) ||
                          chat.getSender().equals(my_id)){
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MainActivity.this, mchat);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // to sign out
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_sign_out) {
            mAuth.getInstance().signOut();
           // Snackbar.make(activity_main, "you have signed out", Snackbar.LENGTH_SHORT).show();


            sendUserToLoginActivity();
        }
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            VerifyUserExistance();
        }
    }


    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//User cant back to Register activity

        startActivity(loginIntent);
        finish();
    }

    private void VerifyUserExistance() {

        String currentUserID = mAuth.getCurrentUser().getUid();


        reference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.child("name").exists())) {


                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(MainActivity.this, "else", Toast.LENGTH_SHORT).show();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }



}
