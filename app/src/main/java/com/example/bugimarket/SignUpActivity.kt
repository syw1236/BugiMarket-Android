package com.example.bugimarket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SignUpActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sigin_up)

        findViewById<Button>(R.id.singupBtn).setOnClickListener {
            val userEmail = findViewById<EditText>(R.id.inputSignupEmail).text.toString() //입력한 이메일을 가져옴
            val password = findViewById<EditText>(R.id.inputSignupPassword).text.toString() //입력한 비밀번호를 가져옴
            createAuth(userEmail,password)
        }



    }
    private fun createAuth(userEmail:String,password:String){ //회원가입 함수
        Firebase.auth.createUserWithEmailAndPassword(userEmail,password).addOnCompleteListener(this){
            if(it.isSuccessful){
                startActivity(Intent(this,LoginActivity::class.java)) //회원가입 완료 시 로그인 창으로 이동
            }
            finish() //해당 activity를 끝냄
        }
    }
}