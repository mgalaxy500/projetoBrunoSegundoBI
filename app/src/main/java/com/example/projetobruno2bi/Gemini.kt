package com.example.projetobruno2bi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import android.util.Log

class Gemini : AppCompatActivity() {

    private val client = OkHttpClient()
    private val apiKey = "AIzaSyCKfJn1vfIUKoxdg5-_EXpeChpC_2RZxfA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_gemini)

        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val inputPrompt = findViewById<EditText>(R.id.inputPrompt)
        val respostaGemini = findViewById<TextView>(R.id.respostaGemini)

        btnEnviar.setOnClickListener {
            val pergunta = "Olá Gemini! Considerando que hoje é o dia dos namorados, faça uma\n" +
                    "receita romântica usando os ingredientes entre aspas. No final, gere um nome\n" +
                    "para a receita e uma pequena descrição.\n" +
                    "Exemplo: Digite “tenho salsicha, macarrão instantâneo e ketchup”" +
                    " e receba:\n" +
                    "“Salsichas da Paixão ao Molho Vermelho do Desejo”\n" +
                    "A IA cria nome, receita e frase de efeito pra postar no Instagram:\n" +
                    "“O tempero? Foi o nosso amor.”\n" +
                    "Enfim, estes são os ingredientes:".trimIndent() + inputPrompt.text.toString()
            enviarPerguntaGemini(pergunta) { resposta ->
                runOnUiThread {
                    respostaGemini.text = resposta
                }
            }
        }
    }

    private fun enviarPerguntaGemini(pergunta: String, callback: (String) -> Unit) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

        val json = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", pergunta)
                ))
            ))
        }

        val mediaType = "application/json".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Erro: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val resposta = response.body?.string() ?: "Sem resposta"
                try {
                    val jsonResposta = JSONObject(resposta)
                    val texto = jsonResposta
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    callback(texto)
                } catch (e: Exception) {
                    callback("Erro ao ler resposta: ${e.message}")
                }
            }
        })
    }
}
