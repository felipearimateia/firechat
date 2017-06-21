package org.androidbh.firechat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.provider.MediaStore
import android.app.Activity
import android.util.Log
import java.io.File
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.crash.FirebaseCrash
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_IMAGE_SELECTOR = 999
    private var adapter: MessageAdapter? = null

    var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (!Repository.instance.isLogged()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            Repository.instance.loadCurrentUser()
        }

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build()

        initView()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val id = item?.itemId

        if (id == R.id.action_add_photo) {
            MainActivityPermissionsDispatcher
                    .dispatchPhotoSelectionIntentWithCheck(this)
            return true
        }
        else if (id == R.id.sign_out_menu){
            signOut()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        progressBar.show()

        Repository.instance.addMessageEventListener { message ->
            progressBar.hide()
            adapter?.add(message)
        }
    }

    override fun onPause() {
        super.onPause()
        Repository.instance.removeMessageEventListner()
        adapter?.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(data.data, filePathColumn, null, null, null)
            if (cursor == null || cursor.count < 1) {
                return
            }
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            if (columnIndex < 0) { // no column index
                return
            }
            val currentPhoto = File(cursor.getString(columnIndex))
            uploadPhoto(currentPhoto)
            cursor.close()

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        MainActivityPermissionsDispatcher
                .onRequestPermissionsResult(this,requestCode, grantResults)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult)
        FirebaseCrash.log("%d : %s"
                .format(connectionResult.errorCode, connectionResult.errorMessage))
    }

    private fun initView() {
        editMessage.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage(v.text.toString())
                v.text = ""
                hideSofInput(v)
            }

            return@setOnEditorActionListener true
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true

        adapter = MessageAdapter(this, mutableListOf(), {message ->
            Repository.instance.like(message)
        })

        messageRecyclerView.layoutManager = layoutManager
        messageRecyclerView.adapter = adapter
    }

    private fun sendMessage(text: String) {
        val message = Message(text, Repository.instance.user!!)
        Repository.instance.sendMessage(message)
    }

    private fun hideSofInput(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun dispatchPhotoSelectionIntent() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        this.startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECTOR)
    }

    private fun signOut() {
        Repository.instance.auth.signOut()
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun uploadPhoto(file: File) {
        progressBar.show()
        Repository.instance.uploadPhoto(file, Repository.instance.user!!,  {
            progressBar.hide()
        })
    }
}
