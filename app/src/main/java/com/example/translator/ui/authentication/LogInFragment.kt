package com.example.translator.ui.authentication

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.translator.R
import com.example.translator.databinding.FragmentLogInBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogInFragment : Fragment(R.layout.fragment_log_in) {

    private lateinit var binding: FragmentLogInBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLogInBinding.bind(view)

        binding.logInButton.setOnClickListener {
            val email = binding.loginInputEmailEditText.text.toString()
            val password = binding.loginInputPasswordEditText.text.toString()
            viewModel.logIn(email, password)
        }

        viewModel.loginState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(),
                    getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }

        setEditTextListeners()
    }

    private fun setEditTextListeners() {
        binding.apply {
            loginInputEmail.editText?.addTextChangedListener { name ->
                checkEmail(name.toString())
            }
            loginInputPassword.editText?.addTextChangedListener { password ->
                checkPassword(password.toString())
            }
        }
    }

    /**
     * Check email
     * Этот метод используется для проверки корректности введенной почты.
     * @param email
     */
    fun checkEmail(email: String): Boolean {
        return if (email.trim().isEmpty()) {
            binding.loginInputEmail.error = getString(R.string.email_error)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginInputEmail.error = getString(R.string.invalid_email)
            false
        } else {
            binding.loginInputEmail.isErrorEnabled = false
            true
        }
    }

    private fun checkPassword(password: String): Boolean {
        val passwordRegex = "^[a-zA-Z0-9@#\$%^&+=]+$"

        return when {
            password.trim().isEmpty() -> {
                binding.loginInputPassword.error = getString(R.string.password_error)
                false
            }

            password.length < 6 || password.length > 24 -> {
                binding.loginInputPassword.error = getString(R.string.password_length_error)
                false
            }

            !password.matches(passwordRegex.toRegex()) -> {
                binding.loginInputPassword.error = getString(R.string.password_invalid_characters_error)
                false
            }

            else -> {
                binding.loginInputPassword.isErrorEnabled = false
                true
            }
        }
    }

    private fun isValidDetails(): Boolean {
        binding.apply {
            return checkEmail(loginInputEmail.editText?.text.toString()) &&
                    checkPassword(loginInputPassword.editText?.text.toString())
        }
    }
}