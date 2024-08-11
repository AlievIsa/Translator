package com.example.translator.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.translator.R
import com.example.translator.databinding.FragmentHomeBinding
import com.google.android.material.internal.ViewUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val speechRecognizer: SpeechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(requireContext()) }
    private lateinit var speechIntent: Intent

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpeechRecognizer()

        binding = FragmentHomeBinding.bind(view)
        binding.apply {
            viewModel.currentSourceLang?.let { sourceLangChip.text = it.title }
            viewModel.currentTargetLang?.let { targetLangChip.text = it.title }
            checkLanguagesAndShowError()

            sourceLangChip.setOnClickListener {
                openLangChooser(it)
            }
            targetLangChip.setOnClickListener {
                openLangChooser(it)
            }

            actionSwapLang.setOnClickListener {
                swapLanguages()
            }

            translationTextView.text = viewModel.translation.value

            sourceEditText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    updateTextLayoutIcon(text)
                    if (text.isNullOrEmpty()) {
                        viewModel.clearTranslationField()
                        translationTextView.text = ""
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            })

            sourceTextInputLayout.setEndIconOnClickListener {
                val text = binding.sourceEditText.text.toString()
                if (text.isNotEmpty() && text.isNotBlank() && viewModel.isTranslationEnable) {
                    performTranslation(text.trim())
                } else {
                    startVoiceInput()
                }
            }

            sourceTextInputLayout.setOnClickListener {
                checkLanguagesAndShowError()
            }

            rootLayout.setOnClickListener {
                hideKeyboard()
            }
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId)
                {
                    R.id.action_history -> {
                        findNavController().navigate(R.id.historyFragment)
                        true
                    }
                    R.id.action_selected -> {
                        findNavController().navigate(R.id.selectedFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun openLangChooser(chip: View) {
        val languages = viewModel.languages.value ?: emptyList()
        val popupMenu = PopupMenu(requireContext(), chip)
        repeat(languages.size) {
            popupMenu.menu.add(Menu.NONE, it, it, languages[it].title)
        }

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when(chip.id) {
                binding.sourceLangChip.id -> {
                    viewModel.currentSourceLang = languages[item.itemId]
                    binding.sourceLangChip.text = viewModel.currentSourceLang!!.title
                }
                binding.targetLangChip.id -> {
                    viewModel.currentTargetLang = languages[item.itemId]
                    binding.targetLangChip.text = viewModel.currentTargetLang!!.title
                }
            }
            checkLanguagesAndShowError()
            false
        }
    }

    private fun updateTextLayoutIcon(text: CharSequence?) {
        if (text.isNullOrEmpty() || text.isBlank()) {
            binding.sourceTextInputLayout.endIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_mic_24)
            binding.sourceTextInputLayout.hint = requireActivity().resources.getString(R.string.enter_text)
        } else {
            binding.sourceTextInputLayout.endIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_g_translate_24)
            binding.sourceTextInputLayout.hint = ""
        }
    }

    private fun swapLanguages() {
        val tempLang = viewModel.currentSourceLang
        viewModel.currentSourceLang = viewModel.currentTargetLang
        viewModel.currentTargetLang = tempLang

        binding.sourceLangChip.text = viewModel.currentSourceLang?.title ?: resources.getString(R.string.source)
        binding.targetLangChip.text = viewModel.currentTargetLang?.title ?: resources.getString(R.string.target)

        checkLanguagesAndShowError()
    }

    private fun checkLanguagesAndShowError() {
        if (viewModel.currentSourceLang == null || viewModel.currentTargetLang == null) {
            binding.sourceTextInputLayout.error = getString(R.string.choose_languages_error)
            viewModel.isTranslationEnable = false
            //binding.actionSwapLang.isEnabled = false
        } else if (viewModel.currentSourceLang == viewModel.currentTargetLang) {
            binding.sourceTextInputLayout.error = getString(R.string.same_language_error)
            viewModel.isTranslationEnable = false
        } else {
            binding.sourceTextInputLayout.error = null
            viewModel.isTranslationEnable = true
            //binding.actionSwapLang.isEnabled = true
        }
    }

    private fun performTranslation(text: String) {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.getTranslation(text)
        viewModel.translation.observe(viewLifecycleOwner) { translatedText ->
            binding.progressBar.visibility = View.INVISIBLE
            binding.translationTextView.text = translatedText
        }
        hideKeyboard()
    }

    private fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            speechRecognizer.startListening(speechIntent)
            Toast.makeText(requireContext(),
                getString(R.string.speak_now), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Toast.makeText(requireContext(),
                    getString(R.string.voice_input_stopped), Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    binding.sourceEditText.setText(spokenText)
                    if (viewModel.isTranslationEnable) {
                        performTranslation(spokenText)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_now))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceInput()
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.permission_for_audio_recording_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        binding.sourceTextInputLayout.clearFocus()
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}