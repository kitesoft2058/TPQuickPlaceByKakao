package com.kitesoft.tpquickplacebykakao.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kitesoft.tpquickplacebykakao.R
import com.kitesoft.tpquickplacebykakao.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    val binding:ActivitySignUpBinding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_sign_up)
        setContentView(binding.root)

        //툴바를 액션바로 설정
        setSupportActionBar(binding.toolbar)
        //액션바에 업버튼 만들기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        //[참고.]벡터 에셋으로 만든 이미지는 tint 가 있으니 직접 수정해야 검정색이 됨.

        binding.btnSignup.setOnClickListener { clickSignUp() }

    }

    //업버튼 클릭시에 액티비티 종료
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }


    private fun clickSignUp(){
        //Firebase FireStore DB에 사용자 정보 저장하기

        var email:String= binding.etEmail.text.toString()
        var password:String= binding.etPassword.text.toString()
        var passwordConfirm:String= binding.etPasswordConfirm.text.toString()

        //유효성 검사 - 패스워드와 패스워드 확인이 맞는지 검사 [ kotlin 은 String 값 비교시에 equals() 대신 == 을 선호함[내부적으로 equals]
        if(password != passwordConfirm){
            AlertDialog.Builder(this).setMessage("패스워드확인에 문제가 있습니다. 다시 확인하여 입력해주시기 바랍니다.").show()
            binding.etPasswordConfirm.selectAll()//써있는 글씨를 모두 선택상태로 하여 손쉽게 새로 입력이 가능하도록...
            return
        }

        //Firestore DB instance 얻어오기
        val db= FirebaseFirestore.getInstance()

        //먼저 같은 이메일이 있는지 확인 [ 아래 저장작업을 먼저 작성 성공한 후에 본 코딩 작성. ]
//        db.collection("emailUsers").get().addOnSuccessListener { queryDocumentSnapshots->
//            for(snapshot : DocumentSnapshot in queryDocumentSnapshots){
//
//                if(email == snapshot.data?.get("email").toString()){
//                    android.app.AlertDialog.Builder(this).setMessage("중복된 이메일이 있습니다. 다시 확인하여 입력해주시기 바랍니다.").show()
//                    binding.etEmail.requestFocus() // 포커스가 없으면 selectAll()이 동작하지 않음.
//                    binding.etEmail.selectAll()//써있는 글씨를 모두 선택상태로 하여 손쉽게 새로 입력이 가능하도록...
//                    return@addOnSuccessListener
//                }
//            }
//
//            //위에서 리턴하지 않으면 중복된 이메일이 없는 것이므로 새로 회원가입하는 아래 코드 작성...
//            //여기부터 코드 먼저 작성!
//            //저장할 값(이메일,비밀번호)을 HashMap으로 저장
//            val user:MutableMap<String, String> = mutableMapOf()
//            user.put("email", email)
//            user.put("password", password)
//
//            // Collection 명은 "emailUsers" 로 지정 [ RDBMS 의 테이블명 같은 역할 ]
//            //.add()를 통해 document 명이 랜덤하게 만들어짐.- 이 랜덤값을 id로 사용하고자 함.(다른 간편로그인방식의 id,email정보와 같은 형태의 정보로 사용하고자.)
//            db.collection("emailUsers").add(user).addOnSuccessListener {
//                AlertDialog.Builder(this)
//                    .setMessage("축하합니다.\n회원가입이 완료되었습니다.")
//                    .setPositiveButton("확인", object : DialogInterface.OnClickListener{
//                        override fun onClick(p0: DialogInterface?, p1: Int) {
//                            finish()
//                        }
//                    }).create().show()
//            }.addOnFailureListener {
//                Toast.makeText(this, "회원가입에 오류가 발생했습니다.\n다시 시도해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
//            }
//        }

        //위 반복문으로 검사하는 방식말고 where 쿼리문으로 특정 필드값의 존재여부 확인..
        db.collection("emailUsers")
            .whereEqualTo("email", email)
            .get().addOnSuccessListener {
                //같은 값을 가진 Document 가 여러개 있을 수 있기에..
                if(it.documents.size >0 ){
                    //개수가 0개 이상이면 찾은 것이므로 중복된 email이 존재하는 것임.
                    AlertDialog.Builder(this).setMessage("중복된 이메일이 있습니다. 다시 확인하여 입력해주시기 바랍니다.").show()
                    binding.etEmail.requestFocus() // 포커스가 없으면 selectAll()이 동작하지 않음.
                    binding.etEmail.selectAll()//써있는 글씨를 모두 선택상태로 하여 손쉽게 새로 입력이 가능하도록...
                }else{
                    //여기부터 코드 먼저 작성!
                    //저장할 값(이메일,비밀번호)을 HashMap으로 저장
                    val user:MutableMap<String, String> = mutableMapOf()
                    user.put("email", email)
                    user.put("password", password)

                    // Collection 명은 "emailUsers" 로 지정 [ RDBMS 의 테이블명 같은 역할 ]
                    //.add()를 통해 document 명이 랜덤하게 만들어짐.- 이 랜덤값을 id로 사용하고자 함.(다른 간편로그인방식의 id,email정보와 같은 형태의 정보로 사용하고자.)
                    db.collection("emailUsers").add(user).addOnSuccessListener {
                        AlertDialog.Builder(this)
                            .setMessage("축하합니다.\n회원가입이 완료되었습니다.")
                            .setPositiveButton("확인", object : DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    finish()
                                }
                            }).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "회원가입에 오류가 발생했습니다.\n다시 시도해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}