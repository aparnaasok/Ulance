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

public class DriverReg extends AppCompatActivity {
    private Button submit;
    private EditText dname,dphno,demail,oname,make,model,year,reg_no,dpass,dconpass;
    private FirebaseAuth mAuth;
    private String nm,ph,em,onm,mk,mdl,yr,regn,dp,dc;


    private AwesomeValidation awesomeValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_reg);
        mAuth=FirebaseAuth.getInstance();


        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        submit=(Button)findViewById(R.id.regbutton);
        dname=(EditText)findViewById(R.id.dname);
        dphno=(EditText)findViewById(R.id.dphno);
        // dphno.addTextChangedListener(new PhoneNumberFormattingTextWatcher("US"));
        demail=(EditText)findViewById(R.id.demail);
        oname=(EditText)findViewById(R.id.oname);
        make=(EditText)findViewById(R.id.make);
        model=(EditText)findViewById(R.id.model);
        year=(EditText)findViewById(R.id.year);
        reg_no=(EditText)findViewById(R.id.reg_no);
        dpass=(EditText)findViewById(R.id.dpass);
        dconpass=(EditText)findViewById(R.id.dconpass);
        String regexPassword = ".{8,}";

        awesomeValidation.addValidation(this, R.id.dname, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        awesomeValidation.addValidation(this, R.id.demail, Patterns.EMAIL_ADDRESS, R.string.emailerror);
        awesomeValidation.addValidation(this, R.id.dphno, RegexTemplate.NOT_EMPTY,R.string.mobileerror);
        dphno.addTextChangedListener(new PhoneNumberFormattingTextWatcher("IN"));
        awesomeValidation.addValidation(this, R.id.oname, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        awesomeValidation.addValidation(this,R.id.make,RegexTemplate.NOT_EMPTY, R.string.invalid_name);
        awesomeValidation.addValidation(this,R.id.model,RegexTemplate.NOT_EMPTY, R.string.invalid_name);
        awesomeValidation.addValidation(this,R.id.year,RegexTemplate.NOT_EMPTY, R.string.invalid_name);
        awesomeValidation.addValidation(this,R.id.reg_no,RegexTemplate.NOT_EMPTY, R.string.invalid_name);
        // awesomeValidation.addValidation(this, R.id.dpass, regexPassword, R.string.invalid_password);
        //awesomeValidation.addValidation(this,R.id.dconpass,R.id.dpass,R.string.invalid_confirm_password);






        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dp=dpass.getText().toString();
                dc=dconpass.getText().toString();
                //Toast.makeText(this,dc,Toast.LENGTH_LONG).show();
                if(!dp.equals(dc)){
                    Toast.makeText(DriverReg.this,"Confirmation of password failed!",Toast.LENGTH_LONG).show();
                }
                //first validate the form then move ahead
                //if this becomes true that means validation is successful
                if (awesomeValidation.validate()) {


                    Toast.makeText(DriverReg.this, "Validation Successful", Toast.LENGTH_LONG).show();



                    final String email = demail.getText().toString();
                    final String password = dp;
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverReg.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(DriverReg.this,"success",Toast.LENGTH_SHORT).show();
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                                current_user_db.setValue(true);
                                nm = dname.getText().toString();
                                ph = dphno.getText().toString();
                                em = demail.getText().toString();
                                onm = oname.getText().toString();
                                mk = make.getText().toString();
                                mdl = model.getText().toString();
                                regn = reg_no.getText().toString();
                                yr = year.getText().toString();
                                Map userInfo = new HashMap();
                                userInfo.put("name", nm);
                                userInfo.put("phone", ph);
                                userInfo.put("email", em);
                                userInfo.put("ownername", onm);
                                userInfo.put("make", mk);
                                userInfo.put("model", mdl);
                                userInfo.put("year", yr);
                                userInfo.put("regno", regn);
                                userInfo.put("driver_password", dp);
                                current_user_db.updateChildren(userInfo);

                                Intent intent = new Intent(DriverReg.this, DriverMapActivity.class);
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
