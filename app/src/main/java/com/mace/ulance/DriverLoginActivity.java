package com.mace.ulance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.NetworkStats;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverLoginActivity extends AppCompatActivity {
    private FirebaseAuth nauth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private EditText nemail, npassword;
    private Button nlogin, nregistration;
    DatabaseReference ref;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        nauth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user.getUid());
                    if(ref!=null){

                                                              Intent intent = new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                                                               startActivity(intent);
                                                               finish();
                                                               return;
                                                           }}



            }
        };
        nemail = (EditText) findViewById(R.id.email);
        npassword = (EditText) findViewById(R.id.password);
        nlogin = (Button) findViewById(R.id.login);
        nregistration = (Button) findViewById(R.id.registration);
        nregistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverLoginActivity.this, OTP.class);
                startActivity(intent);
                /*final String email = nemail.getText().toString();
                final String password = npassword.getText().toString();
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(),"Enter email!",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(),"Enter password!",Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    nauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverLoginActivity.this, "Sign up error", Toast.LENGTH_SHORT).show();
                            } else {
                                String user_id = nauth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);

                                current_user_db.setValue(true);

                            }
                        }
                    });
                }*/
            }
        });
        nlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = nemail.getText().toString();
                final String password = npassword.getText().toString();
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(),"Enter email!",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(),"Enter password!",Toast.LENGTH_SHORT).show();
                    return;
                }
                else {

                    nauth.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverLoginActivity.this, "Sign in error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        nauth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        nauth.removeAuthStateListener(firebaseAuthListener);
    }
}





