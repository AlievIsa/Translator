package com.example.translator.data.remote.authentication

import android.net.Uri
import android.util.Log
import com.example.translator.data.remote.authentication.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class FirebaseAuthImpl : AuthWithEmailAndPassword {

    private val auth = Firebase.auth
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?>
        get() = _currentUser.asStateFlow()

    init {
        if (auth.currentUser != null) {
            _currentUser.update {
                auth.currentUser?.let {
                    User(
                        id = it.uid,
                        name = it.displayName,
                        photoUrl = it.photoUrl
                    )
                }
            }
        }
    }

    override suspend fun logIn(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            _currentUser.update {
                auth.currentUser?.let {
                    User(
                        id = it.uid,
                        name = it.displayName,
                        photoUrl = it.photoUrl
                    )
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String,
        photoUri: Uri
    ): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser!!
                    val storage =
                        FirebaseStorage.getInstance().reference.child("avatars/${user.uid}.jpg")
                    storage.putFile(photoUri).addOnSuccessListener {
                        storage.downloadUrl.addOnSuccessListener { uri ->
                            Log.d("Download uri", "uri: ${uri}")
                            user.updateProfile(
                                UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .setPhotoUri(uri)
                                    .build()
                            )
                        }.addOnFailureListener {
                            Log.d("Download uri", "error: ${it.message}")
                        }
                    }
                }.await()
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    override suspend fun signOut(): Result<Boolean> {
        return try {
            auth.signOut()
            _currentUser.update {
                auth.currentUser?.let {
                    User(
                        id = it.uid,
                        name = it.displayName,
                        photoUrl = it.photoUrl
                    )
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}