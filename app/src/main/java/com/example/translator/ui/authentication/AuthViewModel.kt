package com.example.translator.ui.authentication

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.translator.data.remote.authentication.AuthWithEmailAndPassword
import com.example.translator.domain.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _loginState = MutableLiveData<Result<Boolean>>()
    val loginState: LiveData<Result<Boolean>> get() = _loginState

    private val _signUpState = MutableLiveData<Result<Boolean>>()
    val signUpState: LiveData<Result<Boolean>> get() = _signUpState

    fun logIn(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.logIn(email, password)
            _loginState.postValue(result)
        }
    }

    fun signUp(email: String, password: String, name: String, profilePicture: Uri) {
        viewModelScope.launch {
            val result = repository.signUp(email, password, name, profilePicture)
            _signUpState.postValue(result)
        }
    }
}