package com.shiva.chatgptai.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shiva.chatgptai.adapter.Message
import com.shiva.chatgptai.adapter.MessageAdapter
import com.shiva.loginandsignup.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ShareFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var welcomeTextView: TextView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messageList: MutableList<Message>
    private lateinit var messageAdapter: MessageAdapter

    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_share, container, false)

        messageList = ArrayList()
        recyclerView = view.findViewById(R.id.recycler_view)
        welcomeTextView = view.findViewById(R.id.welcome_text)
        messageEditText = view.findViewById(R.id.message_edit_text)
        sendButton = view.findViewById(R.id.send_btn)

        // Setup recycler view
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter
        val llm = LinearLayoutManager(requireContext())
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm

        sendButton.setOnClickListener {
            val question = messageEditText.text.toString().trim()
            addToChat(question, Message.SENT_BY_ME)
            messageEditText.setText("")
            callAPI(question)
            welcomeTextView.visibility = View.GONE
        }

        return view
    }

    private fun addToChat(message: String, sentBy: String) {
        messageList.add(Message(message, sentBy))
        messageAdapter.notifyDataSetChanged()
        recyclerView.smoothScrollToPosition(messageAdapter.itemCount)
    }

    private fun addResponse(response: String) {
        addToChat(response, Message.SENT_BY_BOT)
    }

    private fun callAPI(question: String) {
        // OkHttp
        addToChat("Typing... ", Message.SENT_BY_BOT)
        val jsonBody = JSONObject().apply {
            put("model", "text-davinci-003")
            put("prompt", question)
            put("max_tokens", 4000)
            put("temperature", 0)
        }
        val body = jsonBody.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("https://api.openai.com/v1/completions")
            .header("Authorization", "Bearer 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")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val jsonObject = JSONObject(response.body!!.string())
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val result = jsonArray.getJSONObject(0).getString("text")
                        requireActivity().runOnUiThread {
                            messageList.removeAt(messageList.size - 1)
                            addResponse(result.trim())
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    addResponse("Failed to load response due to ${response.body}")
                }
            }
        })
    }
}