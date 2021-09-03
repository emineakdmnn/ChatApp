package com.emineakduman.chatapp.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.emineakduman.chatapp.R
import com.emineakduman.chatapp.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.profile_page_content.*
import java.io.FileNotFoundException
import java.io.InputStream


class ProfileActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val mStoreRef: StorageReference by lazy { FirebaseStorage.getInstance().reference }
    private val mCurrentUser: FirebaseUser by lazy { mAuth.currentUser!! }

    private lateinit var mUserReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog

    private val GALLERY_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(profile_page_toolbar)

        setUserDatas()

        change_image_fab.setOnClickListener { changeImage() }
        status_text.setOnClickListener { showStatusChangeDialog() }
    }
    private fun setUserDatas() {
        val userId = mCurrentUser.uid
        mUserReference = mDatabase.reference.child(Constants.CHILD_USERS).child(userId)

        mUserReference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child(Constants.CHILD_NAME).value.toString()
                val profileImage = dataSnapshot.child(Constants.CHILD_PPIMAGE).value.toString()
                val status = dataSnapshot.child(Constants.CHILD_STATUS).value.toString()

                profile_page_collapsing.title = name
                profile_page_toolbar.title = name
                status_text.text = status

                if(profileImage == "no_image") {
                    Glide.with(applicationContext).load(R.drawable.defaultuser).into(profile_page_pimage)
                } else {
                    Glide.with(applicationContext).load(profileImage).into(profile_page_pimage)
                }
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            progressDialog = ProgressDialog(this).apply {
                title = "Lütfen bekleyiniz...."
                setMessage("Profile resmi yükleniyor")
                setCancelable(false)
                show()
            }

            try {
                val imageUri = data?.data
                val imageStream = imageUri?.let { contentResolver.openInputStream(it) }
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                profile_page_pimage.setImageBitmap(selectedImage)

                val filePath = mStoreRef.child(Constants.PPFOLDER).child("${System.currentTimeMillis()}-${mCurrentUser.uid}")

                filePath.putFile(imageUri!!).continueWithTask{ task ->
                    if(!task.isSuccessful) task.exception?.let { throw it }
                    filePath.downloadUrl
                }.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val downloadUri = it.result
                        saveImageUrltoStorage(downloadUri.toString())
                    } else {
                        Toast.makeText(this, "Hata oluştu", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this, "Hata oluştu!", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Resim seçmediniz", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveImageUrltoStorage(url: String) {
        mDatabase.reference.child(Constants.CHILD_USERS).child(mCurrentUser.uid).child(Constants.CHILD_PPIMAGE)
            .setValue(url).addOnCompleteListener {
                if(it.isComplete) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Resim yüklendi", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun changeImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(intent, "Profile resmi seçiniz"), GALLERY_REQUEST_CODE)
    }

    private fun showStatusChangeDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView: View = layoutInflater.inflate(R.layout.custom_dialog, null)
        dialogBuilder.setView(dialogView)

        val edt: EditText = dialogView.findViewById(R.id.dialog_status_text)
        dialogBuilder.setTitle("Durum değiştir")
        dialogBuilder.setPositiveButton("Uygula") { dialog, which ->
            val dialogStatusText = edt.text.toString()

            setStatusText(dialogStatusText)
        }

        dialogBuilder.setNegativeButton("İptal") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun setStatusText(status: String) {
        mDatabase.reference.child(Constants.CHILD_USERS).child(mCurrentUser.uid).child(Constants.CHILD_STATUS)
            .setValue(status).addOnCompleteListener {
                if(it.isSuccessful) {
                    Toast.makeText(this, "Durum değiştirildi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Durum değiştirilemedi", Toast.LENGTH_SHORT).show()
                }
            }
    }
}


