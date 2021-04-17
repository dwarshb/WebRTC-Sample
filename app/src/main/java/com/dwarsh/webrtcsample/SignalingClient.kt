package com.dwarsh.webrtcsample

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.io.StringReader
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class SignalingClient(
    private val meetingID : String,
    private val listener: SignalingClientListener
) : CoroutineScope {

    companion object {
        private const val HOST_ADDRESS = "192.168.0.12"
    }

    var jsonObject : JSONObject?= null

    private val job = Job()

    val TAG = "SignallingClient"

    val db = Firebase.firestore

    private val gson = Gson()

    override val coroutineContext = Dispatchers.IO + job

//    private val client = HttpClient(CIO) {
//        install(WebSockets)
//        install(JsonFeature) {
//            serializer = GsonSerializer()
//        }
//    }

    private val sendChannel = ConflatedBroadcastChannel<String>()

    init {
        connect()
    }

    private fun connect() = launch {
        db.enableNetwork().addOnSuccessListener {
            listener.onConnectionEstablished()
        }
        val sendData = sendChannel.offer("")
        sendData.let {
            Log.v(this@SignalingClient.javaClass.simpleName, "Sending: $it")
//            val data = hashMapOf(
//                    "data" to it
//            )
//            db.collection("calls")
//                    .add(data)
//                    .addOnSuccessListener { documentReference ->
//                        Log.e(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e(TAG, "Error adding document", e)
//                    }
        }
        try {
            db.collection("calls")
                .document(meetingID)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data
                        if (data?.containsKey("type")!! &&
                            data.getValue("type").toString() == "OFFER") {
                                listener.onOfferReceived(SessionDescription(
                                    SessionDescription.Type.OFFER,data["sdp"].toString()))
                        } else if (data?.containsKey("type") &&
                            data.getValue("type").toString() == "ANSWER") {
                                listener.onAnswerReceived(SessionDescription(
                                    SessionDescription.Type.ANSWER,data["sdp"].toString()))
                        } else if (data?.containsKey("type") &&
                                data.getValue("type").toString() == "END_CALL")
                                    listener.onCallEnded()
                        if (data?.containsKey("serverUrl")) {
                            listener.onIceCandidateReceived(
                                IceCandidate(data["sdpMid"].toString(),
                                Math.toIntExact(data["sdpMLineIndex"] as Long),
                                data["sdpCandidate"].toString()))
                        }
                        Log.d(TAG, "Current data: ${snapshot.data}")
                    } else {
                        Log.d(TAG, "Current data: null")
                    }
                }
//            db.collection("calls").document(meetingID)
//                    .get()
//                    .addOnSuccessListener { result ->
//                        val data = result.data
//                        if (data?.containsKey("type")!! && data.getValue("type").toString() == "OFFER") {
//                            Log.e(TAG, "connect: OFFER - $data")
//                            listener.onOfferReceived(SessionDescription(SessionDescription.Type.OFFER,data["sdp"].toString()))
//                        } else if (data?.containsKey("type") && data.getValue("type").toString() == "ANSWER") {
//                            Log.e(TAG, "connect: ANSWER - $data")
//                            listener.onAnswerReceived(SessionDescription(SessionDescription.Type.ANSWER,data["sdp"].toString()))
//                        }
//                    }
//                    .addOnFailureListener {
//                        Log.e(TAG, "connect: $it")
//                    }

        } catch (exception: Exception) {
            Log.e(TAG, "connectException: $exception")

        }
    }

    fun sendIceCandidate(candidate: IceCandidate?,isJoin : Boolean) = runBlocking {
        val candidateConstant = hashMapOf(
            "serverUrl" to candidate?.serverUrl,
            "sdpMid" to candidate?.sdpMid,
            "sdpMLineIndex" to candidate?.sdpMLineIndex,
            "sdpCandidate" to candidate?.sdp
        )
        val type = when {
            isJoin -> "answerCandidate"
            else -> "offerCandidate"
        }
        db.collection("calls")
            .document(meetingID)
            .update(candidateConstant as Map<String, Any>)
            .addOnSuccessListener {
                Log.e(TAG, "sendIceCandidate: Success" )
            }
            .addOnFailureListener {
                Log.e(TAG, "sendIceCandidate: Error $it" )
            }
    }

    fun destroy() {
//        client.close()
        job.complete()
    }
}
