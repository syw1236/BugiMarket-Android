package com.example.bugimarket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.loginBtn).setOnClickListener { //로그인 버튼을 누를 시에
            val userEmail = findViewById<EditText>(R.id.inputLoginEmail).text.toString() //입력한 이메일
            val password = findViewById<EditText>(R.id.inputLoginPassword).text.toString() //입력한 비밀번호
            if(userEmail.length==0 || password.length==0){ //이메일 또는 패스워드가 입력되어 있지 않은 경우
                Toast.makeText(this, "로그인 실패하셨습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {
                doLogin(userEmail, password) //로그인 함수 호출
            }
        }

        findViewById<Button>(R.id.singupBtnInLogin).setOnClickListener {  //회원가입 버튼을 누를 시에
            startActivity(Intent(this,SignUpActivity::class.java)) //SignUpActivity로 이동
        }

    }
    private fun doLogin(userEmail:String, password:String){ //로그인하는 함수
        Firebase.auth.signInWithEmailAndPassword(userEmail,password).addOnCompleteListener(this){
            if(it.isSuccessful){
                startActivity(Intent(this,MainActivity::class.java)) //로그인 완료시 MainActivity로 이동
                Toast.makeText(this, "로그인 되셨습니다. 즐거운 거래하세요.", Toast.LENGTH_SHORT).show()
                finish() //해당 LoginActivity를 끝냄
            }
            else{ //로그인 실패할 시에
                Toast.makeText(this, "로그인 실패했습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
            }

        }
    }
}