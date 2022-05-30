package com.kitesoft.tpquickplacebykakao.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.kitesoft.tpquickplacebykakao.G
import com.kitesoft.tpquickplacebykakao.R
import com.kitesoft.tpquickplacebykakao.databinding.ActivityEmailSignInBinding
import com.kitesoft.tpquickplacebykakao.model.UserAccount

class EmailSignInActivity : AppCompatActivity() {

    val binding: ActivityEmailSignInBinding by lazy { ActivityEmailSignInBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_email_sign_in)
        setContentView(binding.root)

        //툴바에 업버튼 만들기
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        //[참고.]벡터 에셋으로 만든 이미지는 tint 가 있으니 직접 수정해야 검정색이 됨.

        binding.btnSignIn.setOnClickListener { clickSignIn() }
    }

    //업버튼 클릭시에 액티비티 종료
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignIn(){

        var email:String= binding.etEmail.text.toString()
        var password:String= binding.etPassword.text.toString()

        //Firebase Firestore DB에서 이메일 로그인 확인
        val db:FirebaseFirestore= FirebaseFirestore.getInstance()
        db.collection("emailUsers")
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get().addOnSuccessListener {

                if(it.documents.size>0){
                    //로그인 성공...
                    var id:String= it.documents[0].id
                    G.userAccount= UserAccount(id, email)

                    //로그인 성공되었으니 곧바로 MainActivity로 이동..
                    val intent:Intent= Intent(this, MainActivity::class.java)

                    //기존 task의 모든 액티비티를 제거하고 새로운 task로 시작.
                    //[why? EmailSignInActivity 뿐만 아니라 LoginActivity 도 back stack에 존재하기에 한꺼번에 finish 하기위해 back stack을 지우고 새로 시작]
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }else{
                    //로그인 성공이 안되었다면...
                    AlertDialog.Builder(this).setMessage("이메일과 비밀번호를 다시 확인해주시기바랍니다.").create().show()
                    binding.etEmail.requestFocus()
                    binding.etEmail.selectAll()
                }

            }.addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}