package by.aermakova.simplecamera

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val PERMISSION_REQUEST_CAMERA = 0
private const val PERMISSION_REQUEST_EXTERNAL_STORAGE = 1

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var camera: Camera? = null
    private var preview: CameraPreview? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission(Manifest.permission.CAMERA, PERMISSION_REQUEST_CAMERA)
        val captureButton: Button = findViewById(R.id.button_capture)
        captureButton.setOnClickListener {
            requestPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                PERMISSION_REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    private val picture = Camera.PictureCallback { data, _ ->
        val pictureFile: File = getOutputMediaFile(MEDIA_TYPE_IMAGE) ?: run {
            Log.d("MainActivity", "Error creating media file, check storage permissions")
            return@PictureCallback
        }

        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getOutputMediaFile(type: Int): File? {
        val root = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                createFile(root, "SimplePhoto", "IMG_$timeStamp.jpg")
            }
            else -> null
        }
    }

    private fun createFile(dir: String, folder: String, fileTitle: String): File {
        val directory = File(dir, folder).apply { mkdir() }
        return File(directory, fileTitle)
    }

    private fun startCamera() {
        button_capture.visibility = View.VISIBLE
        camera = getCameraInstance()

        preview = camera?.let {
            CameraPreview(this, it)
        }

        preview?.also {
            val pr: FrameLayout = findViewById(R.id.camera_preview)
            pr.addView(it)
        }
    }

    private fun getCameraInstance(): Camera? {
        return try {
            Camera.open()
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        camera?.release()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults[permissions.indexOf(Manifest.permission.CAMERA)] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                toastMessage(getText(R.string.required_permission_was_denied).toString())
            }
        } else if (requestCode == PERMISSION_REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[permissions.indexOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)]
                == PackageManager.PERMISSION_GRANTED
            ) {
                camera?.takePicture(null, null, picture)
            } else {
                toastMessage(getText(R.string.required_permission_was_denied).toString())
            }
        }
    }

    private fun toastMessage(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT)
            .show()
    }

    private fun requestPermission(permission: String, code: Int) {
        if (shouldShowRequestPermissionRationaleCompat(permission)) {
            requestPermissionsCompat((arrayOf(permission)), code)
        } else {
            toastMessage(getText(R.string.permission_not_available).toString())
            requestPermissionsCompat((arrayOf(permission)), code)
        }
    }
}