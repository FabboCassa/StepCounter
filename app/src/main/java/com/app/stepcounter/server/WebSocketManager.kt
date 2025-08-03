import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

// Interfaccia per ricevere gli aggiornamenti dal WebSocket
// La tua UI (Activity/Fragment) implementerà questa interfaccia per reagire ai messaggi
interface WebSocketUpdateListener {
    fun onWebSocketUpdate(message: String)
    fun onWebSocketError(error: String)
}

object WebSocketManager {

    // Il nostro client per effettuare la connessione
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    // L'ascoltatore che riceverà gli aggiornamenti
    // Può essere la tua Activity, il tuo Fragment o un ViewModel
    private var updateListener: WebSocketUpdateListener? = null

    // URL del tuo server WebSocket.
    // Assicurati di usare l'IP corretto se testi su un dispositivo fisico.
    // "10.0.2.2" è l'IP speciale per connettersi al localhost del computer dall'emulatore Android.
    // Se il tuo server è su "ws://localhost:3000", usa questo indirizzo.
    private const val SERVER_URL = "ws://10.0.2.2:3000" // <-- MODIFICA QUESTO URL

    fun setListener(listener: WebSocketUpdateListener) {
        updateListener = listener
    }

    fun removeListener() {
        updateListener = null
    }

    /**
     * Avvia la connessione al server WebSocket.
     */
    fun start() {
        // Controlla se siamo già connessi per evitare connessioni multiple
        if (webSocket != null) {
            println("WebSocket già connesso.")
            return
        }

        println("Avvio connessione WebSocket a $SERVER_URL")
        val request = Request.Builder().url(SERVER_URL).build()

        // Creiamo il WebSocket e lo assegniamo alla nostra variabile
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            // Questo metodo viene chiamato quando la connessione è stata stabilita con successo.
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("Connessione WebSocket aperta!")
                // Qui potresti inviare un primo messaggio, ad esempio per l'autenticazione
                // webSocket.send("{\"type\":\"auth\", \"token\":\"user_token\"}")
            }

            // Questo metodo viene chiamato ogni volta che arriva un messaggio dal server.
            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Nuovo messaggio ricevuto: $text")
                // Invia il messaggio all'ascoltatore (la nostra UI)
                updateListener?.onWebSocketUpdate(text)
            }

            // Metodo per messaggi binari (non lo useremo per ora)
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Gestione messaggi binari se necessario
            }

            // Questo metodo viene chiamato quando il WebSocket si sta chiudendo.
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket in chiusura: $code / $reason")
                webSocket.close(1000, null)
            }

            // Questo metodo viene chiamato in caso di errore di connessione.
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("Errore WebSocket: ${t.message}")
                updateListener?.onWebSocketError("Errore di connessione: ${t.message}")
                // Resettiamo il nostro webSocket per poterci riconnettere in futuro
                this@WebSocketManager.webSocket = null
            }
        })
    }

    /**
     * Invia un messaggio di testo al server.
     * @param message Il messaggio da inviare (preferibilmente in formato JSON).
     */
    fun sendMessage(message: String) {
        if (webSocket != null) {
            println("Invio messaggio: $message")
            webSocket?.send(message)
        } else {
            println("Impossibile inviare il messaggio, WebSocket non connesso.")
            updateListener?.onWebSocketError("Non sei connesso, impossibile inviare il messaggio.")
        }
    }

    /**
     * Chiude la connessione WebSocket.
     */
    fun stop() {
        println("Chiusura connessione WebSocket.")
        // 1000 è il codice standard per una chiusura normale.
        webSocket?.close(1000, "Connessione chiusa manualmente dall'utente.")
        webSocket = null
    }
}