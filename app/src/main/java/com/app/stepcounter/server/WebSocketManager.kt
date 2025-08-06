package com.app.stepcounter // O il tuo package corretto

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object WebSocketManager {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    // 1. Rimuoviamo il vecchio 'updateListener'.
    // Al suo posto, creiamo un MutableSharedFlow.
    // Questo emetterà i messaggi a tutti coloro che si "iscrivono" per ascoltare.
    private val _messages = MutableSharedFlow<String>(replay = 1)
    val messages = _messages.asSharedFlow()


    // --- USA QUESTO PER L'EMULATORE ---
    // private const val SERVER_URL = "ws://10.0.2.2:3000"
    private const val SERVER_URL = "ws://192.168.1.34:3000"

    // 2. I metodi setListener e removeListener non sono più necessari.

    /**
     * Avvia la connessione al server WebSocket.
     */
    fun start() {
        if (webSocket != null) {
            println("WebSocket già connesso.")
            return
        }

        println("Avvio connessione WebSocket a $SERVER_URL")
        val request = Request.Builder().url(SERVER_URL).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("Connessione WebSocket aperta!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Nuovo messaggio ricevuto dal server: $text")
                // 3. Emettiamo il messaggio nel Flow, così chiunque sia in ascolto lo riceverà.
                _messages.tryEmit(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket in chiusura: $code / $reason")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("Errore WebSocket: ${t.message}")
                // Possiamo emettere un messaggio speciale di errore nel Flow
                _messages.tryEmit("{\"type\":\"error\", \"message\":\"Errore di connessione: ${t.message}\"}")
                this@WebSocketManager.webSocket = null
            }
        })
    }

    /**
     * Invia un messaggio di testo al server.
     */
    fun sendMessage(message: String) {
        if (webSocket != null) {
            println("Invio messaggio: $message")
            webSocket?.send(message)
        } else {
            println("Impossibile inviare il messaggio, WebSocket non connesso.")
            // Anche qui potremmo emettere un errore
            _messages.tryEmit("{\"type\":\"error\", \"message\":\"Non sei connesso, impossibile inviare il messaggio.\"}")
        }
    }

    /**
     * Chiude la connessione WebSocket.
     */
    fun stop() {
        println("Chiusura connessione WebSocket.")
        webSocket?.close(1000, "Connessione chiusa manualmente dall'utente.")
        webSocket = null
    }
}