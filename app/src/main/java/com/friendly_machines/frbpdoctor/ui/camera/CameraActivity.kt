package com.friendly_machines.frbpdoctor.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.Surface
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.friendly_machines.frbpdoctor.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraActivity : AppCompatActivity() {
    companion object {
        val PREPARE_CAMERA = "com.friendly_machines.frbpdoctor.PREPARE_CAMERA_ACTION"
        val SHOOT_CAMERA = "com.friendly_machines.frbpdoctor.SHOOT_CAMERA_ACTION"
        val EXIT_CAMERA = "com.friendly_machines.frbpdoctor.EXIT_CAMERA_ACTION"
    }

    private fun executeIntentAction(intent: Intent) {
        intent.action?.let { action2 ->
            val intent = Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                action = when (action2) {
                    PREPARE_CAMERA -> MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA
                    SHOOT_CAMERA -> MediaStore.ACTION_IMAGE_CAPTURE
                    EXIT_CAMERA -> {
                        return
                    }

                    else -> {
                        return
                    }
                }
                //data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }

    private fun handleIntent() {
        if (intent != null) {
            executeIntentAction(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            val requestPermissionActivityContract = ActivityResultContracts.RequestMultiplePermissions()
            val launcher = registerForActivityResult(requestPermissionActivityContract) { granted ->
                if (granted.values.all { it }) {
                    handleIntent()
                }
            }
            launcher.launch(arrayOf(Manifest.permission.CAMERA))
            //launcher.unregister()
        } else {
            handleIntent()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            CoroutineScope(Dispatchers.Main).launch {
                executeIntentAction(intent)
            }
        }
    }
}