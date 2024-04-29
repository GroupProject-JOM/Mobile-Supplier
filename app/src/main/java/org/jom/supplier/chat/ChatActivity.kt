package org.jom.supplier.chat

import android.app.Dialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.jom.supplier.AddCookiesInterceptor
import org.jom.supplier.CollectionItem
import org.jom.supplier.CollectionsAdapter
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.supply.SupplyApi
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

interface ChatApi {
    @GET("JOM_war_exploded/chat")
    fun getData(
        @Query("to") id: Int,
    ): Call<ResponseBody>
}

class ChatActivity : AppCompatActivity() {

    private lateinit var jwt: String
    private lateinit var backButton: ImageView
    private lateinit var recycleViewChat: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    val chatItems = mutableListOf<ChatItem>()

    // get instance of methods class
    val methods = Methods()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val cookieManager = CookieManager.getInstance()
        val cookies = methods.getAllCookies(cookieManager)

        // get jwt from cookie
        for (cookie in cookies) {
            if (cookie.first == "jwt") {
                jwt = cookie.second
            }
        }
        val cookiesMap = mapOf(
            "jwt" to jwt,
        )

        // bind jwt for request
        val client = OkHttpClient.Builder()
            .addInterceptor(AddCookiesInterceptor(cookiesMap))
            .build()

        // generate request
        val retrofit = Retrofit.Builder()
            .baseUrl(methods.getBackendUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val chatApi = retrofit.create(ChatApi::class.java)

        // call get data function to get data from backend
        chatApi.getData(3).enqueue(object : retrofit2.Callback<ResponseBody> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
            ) {
                if (response.code() == 200) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)
                        val messages = jsonObject.getJSONArray("messages")

                        for (i in 0 until messages.length()) {
                            val item = messages.getJSONObject(i)

                            val id = item.getInt("id")
                            val sender = item.getInt("sender")
                            val content = item.getString("content")
                            val receiver = item.getInt("receiver")

                            chatItems.add(
                                ChatItem(
                                    id,
                                    sender,
                                    receiver,
                                    content,
                                )
                            )
                            // Set up RecyclerView and its adapter after data is fetched
                            runOnUiThread {
                                chatAdapter.notifyDataSetChanged()
                            }
                        }

                    }
                } else if (response.code() == 202) {
                    println("No messages")
                    Log.d("TAG", "No messages")

                } else if (response.code() == 401) {
                    // unauthorized
                } else {
                    Log.d("TAG", "Went wrong")
                    Log.d("TAG", response.code().toString())
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                // Handle failure
                Log.d("TAG", "An error occurred: $t")
            }
        })

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // recycle views handle
        recycleViewChat = findViewById(R.id.recycleViewChat)
        recycleViewChat.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(chatItems)
        recycleViewChat.adapter = chatAdapter

        //WebSocket
        var payload = methods.getPayload(jwt);

        // assign values to socket operation variables
        val senderId = methods.floatToInt(payload["user"])

        runBlocking {
            val socket = OkHttpClient().newWebSocket(
                Request.Builder()
                    .url("ws://10.0.2.2:8090/JOM_war_exploded/chat/${senderId}")
                    .build(),
                object : WebSocketListener() {
                    private val isConnected = AtomicBoolean(false)

                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        println("WebSocket opened: $response")
                        isConnected.set(true)
                        CoroutineScope(Dispatchers.IO).launch {
                            println("Socket Opened")
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        println("WebSocket onMessage: $text")
                        if (text.isNotEmpty()) {
                            val id = 0
                            val sender = 3
                            val content = text
                            val receiver = 1

                            chatItems.add(
                                ChatItem(
                                    id,
                                    sender,
                                    receiver,
                                    content,
                                )
                            )
                            // Set up RecyclerView and its adapter after data is fetched
                            runOnUiThread {
                                chatAdapter.notifyDataSetChanged()
                            }

                            recycleViewChat.scrollToPosition(chatAdapter.itemCount - 1);
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        println("WebSocket closed: $code $reason")
                        isConnected.set(false)
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        println("WebSocket error: $t")
                        isConnected.set(false)
                    }
                }
            )

            //send message
            val sendButton: ImageButton = findViewById(R.id.sendButton)
            val msgCotainer: EditText = findViewById(R.id.messageContainer)
            sendButton.setOnClickListener {
                val trimmedMsg = msgCotainer.text.toString().trim()
                if (trimmedMsg.isEmpty()) {
                    println("Message cannot be empty")
                } else {
                    socket.send("${senderId}:${msgCotainer.text.toString()}")

                    val id = 0
                    val sender = senderId
                    val content = msgCotainer.text.toString()
                    val receiver = 3

                    chatItems.add(
                        ChatItem(
                            id,
                            sender,
                            receiver,
                            content,
                        )
                    )
                    // Set up RecyclerView and its adapter after data is fetched
                    runOnUiThread {
                        chatAdapter.notifyDataSetChanged()
                    }

                    msgCotainer.text = null
                    recycleViewChat.scrollToPosition(chatAdapter.itemCount - 1);
                }
            }
        }

        val msgCotainer: EditText = findViewById(R.id.messageContainer)
        msgCotainer.setOnClickListener {
            recycleViewChat.scrollToPosition(chatAdapter.itemCount - 1);
        }

        recycleViewChat.scrollToPosition(chatAdapter.itemCount - 1);

        //back
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { this.onBackPressed() }
    }
}