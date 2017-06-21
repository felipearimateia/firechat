package org.androidbh.firechat

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by felipets on 6/20/17.
 */
class Repository private constructor(){

    val MESSAGES_CHILD = "messages"
    val USERS_CHILD = "users"
    val TAG = Repository::class.java.simpleName

    val executor: Executor
    val auth: FirebaseAuth
    val messageReference: DatabaseReference
    val userReference: DatabaseReference
    val storageRef: StorageReference
    var user: User? = null

    private var messageEventListener: ChildEventListener? = null
    private var messageCallback: (message: Message) -> Unit = {}

    init {

        executor = Executors.newCachedThreadPool()
        auth = FirebaseAuth.getInstance()

        messageReference = FirebaseDatabase.getInstance()
                .reference.child(MESSAGES_CHILD)

        userReference = FirebaseDatabase.getInstance()
                .reference.child(USERS_CHILD)

        storageRef = FirebaseStorage.getInstance()
                .getReferenceFromUrl(BuildConfig.URL_CLOUD_STORAGE)

    }

    private object Holder { val INSTANCE = Repository() }

    companion object {
        val instance: Repository by lazy { Holder.INSTANCE }
    }

    fun isLogged(): Boolean {
        return auth.currentUser != null
    }

    fun loadCurrentUser() = userReference.child(auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(e: DatabaseError?) {
                    FirebaseCrash.report(e?.toException())
                }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    user = snapshot?.getValue(User::class.java)
                }
            })

    fun sendMessage(message: Message) {
        messageReference.push().setValue(message)
    }

    fun uploadPhoto(photo: File, user: User, callback:()->Unit) {
        val file = Uri.fromFile(photo)

        val photoRef = storageRef.child(file.lastPathSegment)
        val meta = StorageMetadata.Builder().setCustomMetadata("uid", user.uid).build()

        photoRef.putFile(file, meta).addOnFailureListener { e ->
            FirebaseCrash.report(e)
            callback()

        }.addOnSuccessListener { taskSnapshot ->
            callback()
        }


        photoRef.putFile(file)
    }

    fun addMessageEventListener(callback: (message:Message) -> Unit) {
        this.messageCallback = callback

        messageEventListener = (object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

            override fun onChildChanged(dataSnapshot: DataSnapshot?, s: String?) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
                val message = dataSnapshot?.getValue(Message::class.java)
                message?.let {
                    it.id = dataSnapshot.key
                    messageCallback(it)
                }
            }

            override fun onChildRemoved(p0: DataSnapshot?) {}
        })

        messageReference.addChildEventListener(messageEventListener)

    }

    fun removeMessageEventListner() {
        messageReference.removeEventListener(messageEventListener)
    }

    fun like(message: Message) {
        messageReference.child(message.id)
                .child("likes")
                .push()
                .setValue(user?.uid)
    }

}