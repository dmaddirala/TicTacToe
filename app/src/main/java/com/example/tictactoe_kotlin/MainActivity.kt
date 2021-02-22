package com.example.tictactoe_kotlin

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    var buFlag: Boolean = false
    var player1 = ArrayList<Int>()
    var player2 = ArrayList<Int>()
    var buttons = ArrayList<Button>()
    var activePlayer = 1
    var currentName:String? = null
    var emailRequest:String? = null
    var sessionId:String? = null
    var mySymbol:String? = null
    var theirSymbol:String? = null

    lateinit var currentUser:FirebaseUser
    lateinit var currentEmail:String
    lateinit var currentUsername:String

    lateinit var dialogInvite: Dialog
    lateinit var dialogRequest: Dialog
    lateinit var cancel:Button
    lateinit var invite:Button
    lateinit var emailInviteEt:EditText
    lateinit var accept:Button
    lateinit var decline:Button
    lateinit var emailRequestTv:TextView

    private var mAuth: FirebaseAuth? = null
    var myRef = FirebaseDatabase.getInstance().getReference()
    var users = myRef.child("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var extras = getIntent().getExtras()

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth!!.currentUser!!
        currentEmail = currentUser!!.email.toString()
        currentUsername = splitEmail(currentUser!!.email.toString())

        Log.i("TAG2", "Username: "+currentUsername)

        dialogInvite = Dialog(this)
        dialogInvite.setContentView(R.layout.dialog_invite)
        dialogInvite.setCancelable(false)
        dialogInvite.window!!.attributes.windowAnimations = R.style.animation

        dialogRequest = Dialog(this)
        dialogRequest.setContentView(R.layout.dialog_request)
        dialogRequest.setCancelable(false)
        dialogRequest.window!!.attributes.windowAnimations = R.style.animation

        invite = dialogInvite.findViewById<Button>(R.id.btn_invite);
        cancel = dialogInvite.findViewById<Button>(R.id.btn_cancel);
        emailInviteEt = dialogInvite.findViewById<EditText>(R.id.et_email_invite);

        accept = dialogRequest.findViewById<Button>(R.id.btn_accept);
        decline = dialogRequest.findViewById<Button>(R.id.btn_decline);
        emailRequestTv = dialogRequest.findViewById<EditText>(R.id.tv_email_invitation);


        var bu0 = findViewById<Button>(R.id.btn_0)
        var bu1 = findViewById<Button>(R.id.btn_1)
        var bu2 = findViewById<Button>(R.id.btn_2)
        var bu3 = findViewById<Button>(R.id.btn_3)
        var bu4 = findViewById<Button>(R.id.btn_4)
        var bu5 = findViewById<Button>(R.id.btn_5)
        var bu6 = findViewById<Button>(R.id.btn_6)
        var bu7 = findViewById<Button>(R.id.btn_7)
        var bu8 = findViewById<Button>(R.id.btn_8)
        var nameTv = findViewById<TextView>(R.id.tv_name)

        buttons = arrayListOf(bu0, bu1, bu2, bu3, bu4, bu5, bu6, bu7, bu8)

        users.child(currentUsername).child("Name").get().addOnSuccessListener {
            currentName = it.value.toString()
            nameTv.setText("Welcome\n $currentName !")
        }

        invite.setOnClickListener({
            var emailInvite = emailInviteEt.text.toString()
            users.child(emailInvite).child("Requests").push().setValue(currentEmail)
            Toast.makeText(this,"Invitation Sent to "+emailInvite, Toast.LENGTH_SHORT).show()

            myRef.child("PlayGame").child(currentUsername+emailInvite).setValue(true)
            playGame1(currentUsername+emailInvite )
            mySymbol = "X"
            theirSymbol = "O"

            emailInviteEt.setText("")
            dialogInvite.dismiss()
        })

        cancel.setOnClickListener({
            emailInviteEt.setText("")
            dialogInvite.dismiss()
        })

        accept.setOnClickListener({

            var emailRequest = splitEmail(emailRequestTv.text.toString())
            var opponentName:String? = null

            users.child(emailRequest).child("Name").get().addOnSuccessListener {snapshot ->
                opponentName = snapshot.value.toString()
                nameTv.setText(opponentName + "\nvs\n" + currentName)
            }

            playGame1(emailRequest+currentUsername )

            mySymbol = "O"
            theirSymbol = "X"
            dialogRequest.dismiss()
        })

        decline.setOnClickListener({
            dialogRequest.dismiss()
        })

        incomingCalls()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        when(id){

            R.id.action_reset -> {
                reset()
                myRef.child("PlayGame").child(sessionId.toString()).setValue(false)
            }
            R.id.action_invite -> {
                dialogInvite.show()
            }
        }


        return super.onOptionsItemSelected(item)
    }

    fun buClick(view: View) {
        val buSelected = view as Button
        var cellId = 0
        when (buSelected.id) {

            R.id.btn_0 -> cellId = 0
            R.id.btn_1 -> cellId = 1
            R.id.btn_2 -> cellId = 2
            R.id.btn_3 -> cellId = 3
            R.id.btn_4 -> cellId = 4
            R.id.btn_5 -> cellId = 5
            R.id.btn_6 -> cellId = 6
            R.id.btn_7 -> cellId = 7
            R.id.btn_8 -> cellId = 8
        }
        myRef.child("PlayGame").child(sessionId!!).child(cellId.toString()).setValue(currentUsername)
    }
    
    fun incomingCalls(){
        users.child(currentUsername).child("Requests").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val td = snapshot.value as HashMap<String,Any>
                    if(td!=null){
                        var value:String
                        for(key in td.keys){
                            emailRequest = td[key].toString()
                            break
                        }
                        users.child(currentUsername).child("Requests").setValue(true)
                        dialogRequest.show()
                        emailRequestTv.setText(emailRequest)
                    }
                }catch (e:Exception){

                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    //To Reset all the Buttons
    fun reset(){
        for(button in buttons){
            button.setBackgroundColor(resources.getColor(R.color.light_grey))
            button.setText("")
            button.isEnabled = true
            activePlayer = 1
        }
        player1.clear()
        player2.clear()

    }

    fun playGame1(sessionId:String){
        this.sessionId = sessionId
        myRef.child("PlayGame").child(sessionId).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if(snapshot.children.count()!=0){
                        var value:String
                        var key:Int
                        for(i in snapshot.children){
                            value = i.value.toString()
                            key = i.key!!.toInt()
//                            Log.i("TAG3", "i.Key: "+i.key+"    i.value: "+i.value)
                            if(value==currentUsername){
                                activePlayer = if(mySymbol=="X") 1 else 2
                            }else{
                                activePlayer = if(mySymbol=="X") 2 else 1
                            }
                            checkBox(key)
                        }

                    }else{
                        reset()
                    }
                }catch (e:Exception){
                    Log.i("TAG3", "Data Changed Exception: "+e)

                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun playGame(cellId: Int, buSelected: Button) {

        if (activePlayer==1) {
            buSelected.setBackgroundColor(Color.RED)
            buSelected.setText("X")
            player1.add(cellId)
            activePlayer = 2
            buFlag = false
        } else {
            buSelected.setBackgroundColor(Color.GREEN)
            buSelected.setText("O")
            player2.add(cellId)
            activePlayer = 1
            buFlag = true
        }
        buSelected.isEnabled = false
        findWinner()

    }

    fun checkBox(cellId:Int){
        val buSelected = buttons.get(cellId)
        if (activePlayer==1) {
            buSelected.setBackgroundColor(Color.RED)
            buSelected.setText("X")
            player1.add(cellId)
            activePlayer = 2
            buFlag = false
        } else {
            buSelected.setBackgroundColor(Color.GREEN)
            buSelected.setText("O")
            player2.add(cellId)
            activePlayer = 1
            buFlag = true
        }
        buSelected.isEnabled = false



    }

    fun findWinner(){
        var winner = 0
        // Row 1
        if(player1.contains(0)&& player1.contains(1)&& player1.contains(2)){
            winner = 1
        }
        else if(player2.contains(0)&& player2.contains(1)&& player2.contains(2)){
            winner = 2
        }

        //Row 2
        else if(player1.contains(3)&& player1.contains(4)&& player1.contains(5)){
            winner = 1
        }
        else if(player2.contains(3)&& player2.contains(4)&& player2.contains(5)){
            winner = 2
        }

        //Row 3
        else if(player1.contains(6)&& player1.contains(7)&& player1.contains(8)){
            winner = 1
        }
        else if(player2.contains(6)&& player2.contains(7)&& player2.contains(8)){
            winner = 2
        }

        //Column 1
        else if(player1.contains(0)&& player1.contains(3)&& player1.contains(6)){
            winner = 1
        }
        else if(player2.contains(0)&& player2.contains(3)&& player2.contains(6)){
            winner = 2
        }

        //Column 2
        else if(player1.contains(1)&& player1.contains(4)&& player1.contains(7)){
            winner = 1
        }
        else if(player2.contains(1)&& player2.contains(4)&& player2.contains(7)){
            winner = 2
        }

        //Column 3
        else if(player1.contains(2)&& player1.contains(5)&& player1.contains(8)){
            winner = 1
        }
        else if(player2.contains(2)&& player2.contains(5)&& player2.contains(8)){
            winner = 2
        }

        //Diagonal 1
        else if(player1.contains(0)&& player1.contains(4)&& player1.contains(8)){
            winner = 1
        }
        else if(player2.contains(0)&& player2.contains(4)&& player2.contains(8)){
            winner = 2
        }

        //Diagonal 2
        else if(player1.contains(2)&& player1.contains(4)&& player1.contains(6)){
            winner = 1
        }
        else if(player2.contains(2)&& player2.contains(4)&& player2.contains(6)){
            winner = 2
        }


        if (winner != 0){
            Toast.makeText(this, "Player " + winner + " Wins the Match", Toast.LENGTH_LONG).show()
            for(button in buttons){
                button.isEnabled = false
            }
            Handler().postDelayed({
                reset()
            }, 1250)
        }

        else if ((player1.size + player2.size)==9){
            Toast.makeText(this, "It's a Draw", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                reset()
            }, 1250)
        }
    }

    fun splitEmail(str:String):String = str.split("@")[0]
}