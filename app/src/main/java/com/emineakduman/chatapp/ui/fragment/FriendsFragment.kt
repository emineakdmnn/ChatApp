package com.emineakduman.chatapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emineakduman.chatapp.R
import com.emineakduman.chatapp.model.User
import com.emineakduman.chatapp.ui.activity.ChatActivity
import com.emineakduman.chatapp.ui.adapter.FriendsAdapter
import com.emineakduman.chatapp.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_friends.*

class FriendsFragment : Fragment(), FriendsAdapter.OnFriendClickListener {

    private val mUserDatabase: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child(Constants.CHILD_USERS)
    }

    private lateinit var adapter: FriendsAdapter
    private var userList: ArrayList<User> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        adapter = FriendsAdapter(requireActivity(), userList)
        friends_recyclerView.layoutManager = LinearLayoutManager(activity)
        friends_recyclerView.adapter = adapter

        mUserDatabase.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                if (dataSnapshot.value != null) {
                    try {
                        val model = dataSnapshot.getValue(User::class.java)
                        val friendKey = dataSnapshot.ref.key

                        if(!currentUserId?.equals(friendKey)!!) {
                            userList.add(model!!)
                            adapter.notifyItemInserted(userList.size - 1)
                        }

                    } catch (e: Exception) {
                        e.message?.let { Log.e("onChildAdded", it) }
                    }
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })

        adapter.setOnFriendClickListener(this)
    }

    override fun onFriendClick(user: User) {
        mUserDatabase.orderByChild(Constants.CHILD_NAME).equalTo(user.name)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnaphot: DataSnapshot) {
                    val clickedUserKey = dataSnaphot.children.iterator().next().ref.key

                    val intent = Intent(activity, ChatActivity::class.java)
                    intent.putExtra(Constants.EXTRA_NAME, user.name)
                    intent.putExtra(Constants.EXTRA_ID, clickedUserKey)
                    startActivity(intent)
                }

            })
    }
}
