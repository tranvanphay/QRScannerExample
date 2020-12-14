package com.phaytran.qrscannerexample

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.phaytran.qrscannerexample.model.QRGeoModel
import com.phaytran.qrscannerexample.model.QRUrlModel
import com.phaytran.qrscannerexample.model.QRVCardModel
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private lateinit var zxScan: ZXingScannerView
    private lateinit var txtResult:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        zxScan = findViewById(R.id.zxscan)
        txtResult = findViewById(R.id.txt_result)
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    zxScan.setResultHandler(this@MainActivity)
                    zxScan.startCamera()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    Toast.makeText(this@MainActivity,"You should enable this permission",Toast.LENGTH_SHORT).show()
                }

            }).check()
    }

    override fun handleResult(rawResult: Result?) {
        processRawResult(rawResult!!.text)
    }

    private fun processRawResult(text: String?) {
        if(text!!.startsWith("BEGIN:")){
            val token  = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val qrvCardModel = QRVCardModel()
            for(i in token.indices){
                if(token[i].startsWith("BEGIN:")){
                    qrvCardModel.type = token[i].substring("BEGIN:".length)
                }else if(token[i].startsWith("N:")){
                    qrvCardModel.name = token[i].substring("N:".length)
                }else if(token[i].startsWith("ORG:")){
                    qrvCardModel.org = token[i].substring("ORG:".length)
                }else if(token[i].startsWith("TEL:")){
                    qrvCardModel.tel = token[i].substring("TEL:".length)
                }else if(token[i].startsWith("URL:")){
                    qrvCardModel.url = token[i].substring("URL:".length)
                }else if(token[i].startsWith("EMAIL:")){
                    qrvCardModel.email = token[i].substring("EMAIL:".length)
                }else if(token[i].startsWith("ADR:")){
                    qrvCardModel.address = token[i].substring("ADR:".length)
                }else if(token[i].startsWith("NOTE:")){
                    qrvCardModel.note = token[i].substring("NOTE:".length)
                }else if(token[i].startsWith("SUMMARY:")){
                    qrvCardModel.summary = token[i].substring("SUMMARY:".length)
                }else if(token[i].startsWith("DTSTART:")){
                    qrvCardModel.dtStart = token[i].substring("DTSTART:".length)
                }else if(token[i].startsWith("DTEND:")){
                    qrvCardModel.dtEnd = token[i].substring("DTEND:".length)
                }
                if(qrvCardModel.type.equals("VCARD")){
                    txtResult.text = qrvCardModel.name
                }else{
                    txtResult.text = qrvCardModel.type
                }
            }
        }else if(text!!.startsWith("http://") || text!!.startsWith("https://") || text.startsWith("www.")){
            val qrUrlModel = QRUrlModel()
            qrUrlModel.url = text!!
            txtResult.text = qrUrlModel.url
        }else if(text!!.startsWith("geo:")){
            val qrGeoModel = QRGeoModel()
            val delims = "[ , ?q= ]+"
            val token = text.split(delims.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for(i in token.indices){
                if(token[i].startsWith("geo:")){
                    qrGeoModel.lat = token[i].substring("geo:".length)
                }
            }
            qrGeoModel.lat = token[0].substring("geo:".length)
            qrGeoModel.lng = token[1]
            qrGeoModel.geoPlace = token[2]
            txtResult.text = qrGeoModel.lat+" / "+qrGeoModel.lng
        }else{
            txtResult.text = text!!
        }
    }
}