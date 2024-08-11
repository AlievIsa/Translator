package com.example.translator.ui.authentication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.translator.R
import com.example.translator.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private lateinit var binding: FragmentSignUpBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var photoUri: Uri

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignUpBinding.bind(view)

        binding.signUpButton.setOnClickListener {
            if (isValidDetails()) {
                val email = binding.inputEmailEditText.text.toString()
                val name = binding.inputNameEditText.text.toString()
                val password = binding.inputPasswordEditText.text.toString()
                val confirmPassword = binding.inputConfirmPasswordEditText.text.toString()
                viewModel.signUp(email, password, name, photoUri)
            }
        }

        viewModel.signUpState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(),
                    getString(R.string.sign_up_successful), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.chooseAvatarImageView.setOnClickListener {
            pickImageFromGallery()
        }

        setEditTextListeners()
    }

    private fun pickImageFromGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            photoUri = data?.data!!
            binding.chooseAvatarImageView.setImageURI(photoUri)
        }
    }

    private fun setEditTextListeners() {
        binding.apply {
            inputNameEditText.addTextChangedListener { name ->
                checkName(name.toString())
            }

            inputEmailEditText.addTextChangedListener { email ->
                checkEmail(email.toString())
            }

            inputPasswordEditText.addTextChangedListener { password ->
                checkPassword(password.toString())
            }

            inputConfirmPasswordEditText.addTextChangedListener { confirmPassword ->
                checkConfirmPassword(confirmPassword.toString())
            }
        }
    }

    private fun checkAvatar(): Boolean {
        if (binding.chooseAvatarImageView.drawable == null) { // Если аватар не выбран
            Toast.makeText(context, getString(R.string.avatar_error), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun checkConfirmPassword(confirmPassword: String): Boolean {
        return if (confirmPassword != binding.inputPasswordEditText.text.toString()) {
            binding.inputConfirmPasswordLayout.error = getString(R.string.confirm_password_error)
            false
        } else {
            binding.inputConfirmPasswordLayout.isErrorEnabled = false
            true
        }
    }

    private fun checkPassword(password: String): Boolean {
        // Шаблон регулярного выражения для соответствия буквам, цифрам и специальным символам
        val passwordRegex =
            "^[a-zA-Z0-9@#\$%^&+=]+$"

        return when {
            password.trim().isEmpty() -> {
                binding.inputPasswordLayout.error = getString(R.string.password_error)
                false
            }

            password.length < 6 || password.length > 24 -> {
                binding.inputPasswordLayout.error = getString(R.string.password_length_error)
                false
            }

            !password.matches(passwordRegex.toRegex()) -> {
                binding.inputPasswordLayout.error =
                    getString(R.string.password_invalid_characters_error)
                false
            }

            else -> {
                binding.inputPasswordLayout.isErrorEnabled = false
                true
            }
        }
    }

    private fun checkEmail(email: String): Boolean {
        return if (email.trim().isEmpty()) {
            binding.inputEmailLayout.error = getString(R.string.email_error)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmailLayout.error = getString(R.string.invalid_email)
            false
        } else {
            binding.inputEmailLayout.isErrorEnabled = false
            true
        }
    }

    private fun checkName(name: String): Boolean {
        val trimName = name.trim()
        return if (trimName.isEmpty()) {
            binding.inputNameLayout.error = getString(R.string.name_error)
            false
        } else if (trimName.length >= 30) {
            binding.inputNameLayout.error = getString(R.string.name_too_long)
            false
        } else {
            binding.inputNameLayout.isErrorEnabled = false
            true
        }
    }


    private fun isValidDetails(): Boolean {
        if (!checkAvatar()) return false

        binding.let {
            if (checkName(it.inputNameEditText.text.toString().trim()) &&
                checkEmail(it.inputEmailEditText.text.toString().trim()) &&
                checkPassword(it.inputPasswordEditText.text.toString()) &&
                checkConfirmPassword(it.inputConfirmPasswordEditText.text.toString())
            ) {
                return true
            }
        }
        return false
    }

    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 100
    }
}