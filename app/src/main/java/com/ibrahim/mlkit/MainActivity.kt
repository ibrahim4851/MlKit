package com.ibrahim.mlkit

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

import androidx.core.content.FileProvider

class MainActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath : String
    private var bitmap: Bitmap? = null
    private lateinit var text_image: ImageView
    private lateinit var path_select_view: LinearLayout
    private lateinit var detect_btn: Button
    private lateinit var opengallery: Button
    private lateinit var opencamera: Button

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text_image = findViewById(R.id.text_image)
        path_select_view = findViewById(R.id.path_select_view)
        detect_btn = findViewById(R.id.detect_btn)
        opengallery = findViewById(R.id.opengallery)
        opencamera = findViewById(R.id.opencamera)

        opencamera.setOnClickListener {
            camera()
        }

        opengallery.setOnClickListener {
            if (!checkPermission()) {
                requestPermissions(listOf(storage_permission).toTypedArray(), 100)
            } else {
                pickImage()
            }
        }

        detect_btn.setOnClickListener() { detectImage() }
    }

    fun detectImage() {
        val recognizer = TextRecognition.getClient()
        bitmap?.let {
            val image = InputImage.fromBitmap(it, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    Toast.makeText(this, visionText.text, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
        }
        if (bitmap == null) Toast.makeText(this, "Please select image!", Toast.LENGTH_SHORT)
            .show()
    }

    private fun camera(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(
                packageManager
            ) != null
        ) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
            }
            if (photoFile != null) {
                val photoURI =
                    FileProvider.getUriForFile(this, "com.ibrahim.mlkit.fileprovider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, 1)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        currentPhotoPath = image.absolutePath
        return image
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(): Boolean {
        return checkSelfPermission(storage_permission) == PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (isAllPermissionGranted(permissions)) {
                    pickImage()
                } else {

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_CANCELED) {
            if (requestCode == PICK_IMAGE) {
                data?.data?.let {
                    bitmap = null
                    bitmap = getBitmapFromUri(it)
                    Glide.with(this)
                        .load(bitmap)
                        .into(text_image)
                }
            } else if (requestCode == 1) {
                val file = File(currentPhotoPath)
                val photoBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                text_image.setImageBitmap(photoBitmap)
                bitmap = photoBitmap
            }
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isAllPermissionGranted(permissions: Array<out String>): Boolean {
        permissions.forEach {
            if (checkSelfPermission(it) != PERMISSION_GRANTED) return false
        }
        return true
    }

    companion object {
        const val storage_permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        const val PICK_IMAGE = 101
    }
}