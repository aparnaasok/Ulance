package com.mace.ulance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.makeText;

class Custreg extends AppCompatActivity {
    private Button submit;
    private EditText cname,cphno,cemail,cpass,cconpass;
    private FirebaseAuth mAuth;
    private String nm,ph,em,cp,conp;


    private AwesomeValidation awesomeValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_reg);
        mAuth=FirebaseAuth.getInstance();


        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        submit=(Button)findViewById(R.id.regbutton);
        cname=(EditText)findViewById(R.id.cname);
        cphno=(EditText)findViewById(R.id.cphno);
        // dphno.addTextChangedListener(new PhoneNumberFormattingTextWatcher("US"));
        cemail=(EditText)findViewById(R.id.cemail);
        cpass=(EditText)findViewById(R.id.cpass);
        cconpass=(EditText)findViewById(R.id.cconpass);
        String regexPassword = ".{8,}";

        awesomeValidation.addValidation(this, R.id.cname, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        awesomeValidation.addValidation(this, R.id.cemail, Patterns.EMAIL_ADDRESS, R.string.emailerror);
        awesomeValidation.addValidation(this, R.id.cphno, RegexTemplate.NOT_EMPTY,R.string.mobileerror);
        cphno.addTextChangedListener(new PhoneNumberFormattingTextWatcher("IN"));
        // awesomeValidation.addValidation(this, R.id.dpass, regexPassword, R.string.invalid_password);
        //awesomeValidation.addValidation(this,R.id.dconpass,R.id.dpass,R.string.invalid_confirm_password);






        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cp=cpass.getText().toString();
                conp=cconpass.getText().toString();
                //Toast.makeText(this,dc,Toast.LENGTH_LONG).show();
                if(!cp.equals(conp)){
                    Toast.makeText(Custreg.this,"Confirmation of password failed!",Toast.LENGTH_LONG).show();
                }
                //first validate the form then move ahead
                //if this becomes true that means validation is successful
                if (awesomeValidation.validate()) {


                    Toast.makeText(Custreg.this, "Validation Successful", Toast.LENGTH_LONG).show();



                    final String email = cemail.getText().toString();
                    final String password = conp;
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Custreg.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(Custreg.this,"success",Toast.LENGTH_SHORT).show();
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                                current_user_db.setValue(true);
                                nm = cname.getText().toString();
                                ph = cphno.getText().toString();
                                em = cemail.getText().toString();
                                Map userInfo = new HashMap();
                                userInfo.put("name", nm);
                                userInfo.put("phone", ph);
                                userInfo.put("email", em);
                                userInfo.put("driver_password", cp);
                                current_user_db.updateChildren(userInfo);

                                Intent intent = new Intent(Custreg.this, CustomerMapActivity.class);
                                startActivity(intent);
                                finish();



                            }

                        }



                    });


                    //process the data further

                }




            }
        });
    }

}
