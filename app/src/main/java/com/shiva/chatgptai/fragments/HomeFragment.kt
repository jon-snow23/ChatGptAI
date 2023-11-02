package com.shiva.chatgptai.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.shiva.loginandsignup.R
import com.squareup.picasso.Picasso
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var inputText: EditText
    private lateinit var generateBtn: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var imageView: ImageView

    companion object {
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    }

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        inputText = view.findViewById(R.id.input_text)
        generateBtn = view.findViewById(R.id.generate_btn)
        progressBar = view.findViewById(R.id.progress_bar)
        imageView = view.findViewById(R.id.image_view)

        generateBtn.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isEmpty()) {
                inputText.error = "Text can't be empty"
                return@setOnClickListener
            }
            callAPI(text)
        }
    }

    private fun callAPI(text: String) {
        // API CALL
        setInProgress(true)
        val jsonBody = JSONObject()
        try {
            jsonBody.put("prompt", text)
            jsonBody.put("size", "256x256")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val requestBody = RequestBody.create(
            JSON_MEDIA_TYPE.toMediaTypeOrNull(),
            jsonBody.toString()
        )
        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/generations")
            .header("Authorization", "Bearer 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")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Failed to generate image",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonObject = response.body?.string()?.let { JSONObject(it) }
                    val imageUrl =
                        jsonObject?.getJSONArray("data")?.getJSONObject(0)?.getString("url")
                    if (imageUrl != null) {
                        loadImage(imageUrl)
                    }
                    setInProgress(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun setInProgress(inProgress: Boolean) {
        activity?.runOnUiThread {
            if (inProgress) {
                progressBar.visibility = View.VISIBLE
                generateBtn.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                generateBtn.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE
            }
        }
    }

    private fun loadImage(url: String) {
        // Load image
        activity?.runOnUiThread {

            Picasso.get().load(url).into(imageView)
        }
    }

}