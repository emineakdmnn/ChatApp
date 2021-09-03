package com.emineakduman.chatapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.emineakduman.chatapp.R
import com.emineakduman.chatapp.model.Message
import com.emineakduman.chatapp.ui.adapter.ChatAdapter
import com.emineakduman.chatapp.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mRef: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val mCurrentUserId by lazy { mAuth.currentUser?.uid }

    private lateinit var mChatUserId: String
    private lateinit var adapter: ChatAdapter
    private var messageList: ArrayList<Message> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val chatUserName = intent.extras?.getString(Constants.EXTRA_NAME)
        mChatUserId = intent.extras?.getString(Constants.EXTRA_ID).toString()

        setSupportActionBar(chat_toolbar)
        supportActionBar?.title = chatUserName

        adapter = ChatAdapter(this, messageList)
        chat_recyclerView.layoutManager = LinearLayoutManager(this).also { it.stackFromEnd = true }
        chat_recyclerView.adapter = adapter

        loadMessages()

        send_message_btn.setOnClickListener { sendMessage() }
    }

    private fun loadMessages() {
        mRef.child(Constants.MESSAGES).child(mCurrentUserId!!).child(mChatUserId)
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildAdded(dataSnaphot: DataSnapshot, p1: String?) {
                    val message = dataSnaphot.getValue(Message::class.java)
                    adapter.add(message!!)
                    chat_recyclerView.scrollToPosition(chat_recyclerView.adapter?.itemCount?.minus(1)!!)
                }

                override fun onChildRemoved(p0: DataSnapshot) {

                }

            })
    }

    private fun sendMessage() {
        val message = send_message_edt.text.toString()

        if(message.isNotEmpty()) {
            val currentUserRef = "messages/$mCurrentUserId/$mChatUserId"
            val chatUserRef = "messages/$mChatUserId/$mCurrentUserId"

            val userMessageRef: DatabaseReference = mRef.child(Constants.MESSAGES).child(mCurrentUserId!!).child(mChatUserId).push()
            val messageId = userMessageRef.key

            val messageMap: HashMap<String, Any> = HashMap()
            messageMap["message"] = message
            messageMap["time"] = System.currentTimeMillis().toString()
            messageMap["from"] = mCurrentUserId!!

            val messageUserMap = mutableMapOf<String, Any>()
            messageUserMap["$currentUserRef/$messageId"] = messageMap
            messageUserMap["$chatUserRef/$messageId"] = messageMap

            send_message_edt.setText("")

            mRef.updateChildren(messageUserMap)
        }
    }



}
