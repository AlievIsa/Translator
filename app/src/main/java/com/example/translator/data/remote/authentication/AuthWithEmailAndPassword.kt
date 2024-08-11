package com.example.translator.data.remote.authentication

import android.net.Uri
import com.example.translator.data.remote.authentication.models.User
import kotlinx.coroutines.flow.StateFlow


interface AuthWithEmailAndPassword {

    val currentUser: StateFlow<User?>

    suspend fun logIn(email: String, password: String): Result<Boolean>

    suspend fun signUp(email: String, password: String, name: String, photoUri: Uri): Result<Boolean>

    suspend fun signOut(): Result<Boolean>

}