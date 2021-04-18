package com.dwarsh.webrtcsample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    private lateinit var rtcClient: RTCClient
    private lateinit var signallingClient: SignalingClient

    private val audioManager by lazy { RTCAudioManager.create(this) }

    private var meetingID : String = "test-call"

    private var isJoin = false

    private var isMute = false

    private var isVideoPaused = false

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
//            signallingClient.send(p0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (intent.hasExtra("meetingID"))
            meetingID = intent.getStringExtra("meetingID")!!
        if (intent.hasExtra("isJoin"))
            isJoin = intent.getBooleanExtra("isJoin",false)
        checkCameraAndAudioPermission()
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        switch_camera_button.setOnClickListener {
            rtcClient.switchCamera()
        }

        video_button.setOnClickListener {
            if (isVideoPaused) {
                isVideoPaused = false
                video_button.setImageResource(R.drawable.ic_baseline_videocam_off_24)
            } else {
                isVideoPaused = true
                video_button.setImageResource(R.drawable.ic_baseline_videocam_24)
            }
            rtcClient.enableVideo(isVideoPaused)
        }
        mic_button.setOnClickListener {
            if (isMute) {
                isMute = false
                mic_button.setImageResource(R.drawable.ic_baseline_mic_off_24)
            } else {
                isMute = true
                mic_button.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            rtcClient.enableAudio(isMute)
        }
        end_call_button.setOnClickListener {
            rtcClient.endCall(meetingID)
            remote_view.isGone = false
            Constants.isCallEnded = true
            finish()
            startActivity(Intent(this@MainActivity, StartActivity::class.java))
        }
    }

    private fun checkCameraAndAudioPermission() {
        if ((ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this,AUDIO_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED)) {
            requestCameraAndAudioPermission()
        } else {
            onCameraAndAudioPermissionGranted()
        }
    }

    private fun onCameraAndAudioPermissionGranted() {
        rtcClient = RTCClient(
                application,
                object : PeerConnectionObserver() {
                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        signallingClient.sendIceCandidate(p0,isJoin)
                        rtcClient.addIceCandidate(p0)
                    }

                    override fun onAddStream(p0: MediaStream?) {
                        super.onAddStream(p0)
                        p0?.videoTracks?.get(0)?.addSink(remote_view)
                    }
                }
        )
        rtcClient.initSurfaceView(remote_view)
        rtcClient.initSurfaceView(local_view)
        rtcClient.startLocalVideoCapture(local_view)
        signallingClient =  SignalingClient(meetingID,createSignallingClientListener())
        if (!isJoin)
            rtcClient.call(sdpObserver,meetingID)

    }

    private fun createSignallingClientListener() = object : SignalingClientListener {
        override fun onConnectionEstablished() {
            end_call_button.isClickable = true
        }

        override fun onOfferReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            rtcClient.answer(sdpObserver,meetingID)
            Constants.isIntiatedNow = false
            remote_view_loading.isGone = true
        }

        override fun onAnswerReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            Constants.isIntiatedNow = false
            remote_view_loading.isGone = true
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            rtcClient.addIceCandidate(iceCandidate)
        }

        override fun onCallEnded() {
            if (!Constants.isCallEnded) {
                Constants.isCallEnded = true
                rtcClient.endCall(meetingID)
                finish()
                startActivity(Intent(this@MainActivity, StartActivity::class.java))
            }
        }
    }

    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION) &&
            ActivityCompat.shouldShowRequestPermissionRationale(this, AUDIO_PERMISSION) &&
            !dialogShown) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION, AUDIO_PERMISSION), CAMERA_AUDIO_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
                .setTitle("Camera And Audio Permission Required")
                .setMessage("This app need the camera and audio to function")
                .setPositiveButton("Grant") { dialog, _ ->
                    dialog.dismiss()
                    requestCameraAndAudioPermission(true)
                }
                .setNegativeButton("Deny") { dialog, _ ->
                    dialog.dismiss()
                    onCameraPermissionDenied()
                }
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_AUDIO_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraAndAudioPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        signallingClient.destroy()
        super.onDestroy()
    }
}