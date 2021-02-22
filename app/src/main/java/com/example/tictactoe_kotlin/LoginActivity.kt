package com.example.tictactoe_kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private val myRef = FirebaseDatabase.getInstance().reference
    private var name: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance();
    }

    override fun onStart() {
        super.onStart()
        loadMain()
    }

    fun buSubmit(view: View) {
        var username = splitEmail(et_email.text.toString())

        if (et_email.text.isEmpty()) {
            Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show()
            return
        } else if (et_email.text.isEmpty()) {
            Toast.makeText(this, "Invalid Email Id", Toast.LENGTH_SHORT).show()
            return
        } else if (et_password.text.isEmpty()) {
            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show()
            return
        } else if(checkForDots(username)){
            Toast.makeText(this, "Username Cannot contain Dots", Toast.LENGTH_SHORT).show()
            return
        }
        name = et_name.text.toString()
        loginToFirebase(et_email.text.toString(), et_password.text.toString())
    }

    fun loginToFirebase(email: String, password: String) {

        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    var currentUser = mAuth!!.currentUser
                    var users = myRef.child("Users")

                    if(currentUser!=null){
                        var username = splitEmail(currentUser.email.toString())

                        users.child(username).child("Uid").setValue(currentUser.uid)
                        users.child(username).child("Name").setValue(name)
                    }

                    loadMain()
                } else {
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun loadMain() {
        var currentUser = mAuth!!.currentUser
        if (currentUser != null) {

            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", currentUser.email)
            if (name!=null) {
                intent.putExtra("Name", name)
            }

            startActivity(intent)
        }
    }

    fun splitEmail(str:String) = str.split("@")[0]

    fun checkForDots(str:String):Boolean{
        for (i in 0..(str.length-1)){
            if (str[i].toString().equals(".")){
                return true
            }
        }
        return false
    }
}