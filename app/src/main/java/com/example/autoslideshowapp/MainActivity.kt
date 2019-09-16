package com.example.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.ContentUris.withAppendedId
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.ViewDebug
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URI
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null
    private var mHandler = Handler()

    private var indexN: Int = 0
    private var indexM : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var checkSelf : Boolean = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                 getContentsInfo(0)
                 checkSelf = true
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            getContentsInfo(0)
            checkSelf  = true
        }

        next.setOnClickListener {
            if (checkSelf ) {
                if (mTimer != null) {
                } else {
                    getContentsInfo(1)
                }
            }
        }

        stopAndStrat.setOnClickListener {
            if (checkSelf ) {
                if (stopAndStrat.text == "停止") {
                    stopAndStrat.text = "再生"
                    mTimer!!.cancel()
                    mTimer = null
                } else if (stopAndStrat.text == "再生") {
                    stopAndStrat.text = "停止"
                    autoSlideshow()
                }
            }

        }

        back.setOnClickListener {
            if(checkSelf ) {
                if (mTimer != null) {
                } else {
                    getContentsInfo(-1)
                }
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo(0)
                }
        }
    }

    private fun getContentsInfo(moveTo: Int) {
        val resolver = contentResolver
        val cursor :Cursor? = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null)

        if (cursor != null) {
            var fieldIndex:Int
            var id :Long
            var imageUri: Uri

            if (moveTo == 0) {
                cursor.moveToFirst()
                for (i in 0 until cursor.count) {
                    indexM = i
                    cursor.moveToNext()
                }
                cursor.moveToFirst()
                fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                id = cursor.getLong(fieldIndex)
                imageUri = withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)
                autoSlideshow()

            } else if (moveTo == 1) {
                if(indexN >= indexM){
                    indexN = 0
                } else{
                    indexN++
                }
                cursor.moveToPosition(indexN)
                fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                id = cursor.getLong(fieldIndex)
                imageUri = withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)

            } else if(moveTo == -1){
                if(indexN <= 0){
                    indexN = indexM
                } else{
                    indexN--
                }
                cursor.moveToPosition(indexN)
                fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                id = cursor.getLong(fieldIndex)
                imageUri = withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)
            }


            cursor.close()
        }
    }

    private fun autoSlideshow(){
        if (mTimer == null){
            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mHandler.post {
                        getContentsInfo(1)
                    }
                }
            },2000,2000)
        }
    }

}
