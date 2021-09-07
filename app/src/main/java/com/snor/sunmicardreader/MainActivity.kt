package com.snor.sunmicardreader

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.snor.sunmicardreader.databinding.ActivityMainBinding
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sunmi.paylib.SunmiPayKernel
import java.lang.Exception
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by viewBinding()

    private var mSMPayKernel: SunmiPayKernel? = null
    private var mReadCardOptV2: ReadCardOptV2? = null

    private val cardType = MutableLiveData<String>()
    private val result = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSMPayKernel = SunmiPayKernel.getInstance()
        mSMPayKernel!!.initPaySDK(this.applicationContext, object : SunmiPayKernel.ConnectCallback {
            override fun onDisconnectPaySDK() {}
            override fun onConnectPaySDK() {
                try {
                    mReadCardOptV2 = mSMPayKernel!!.mReadCardOptV2
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        //show card type
        cardType.observe(this, Observer {
            binding.txtType.text = it
        })

        //show result
        result.observe(this, Observer {
            binding.txtResult.text = it
        })


        binding.btnIC.setOnClickListener {
            checkCard(AidlConstants.CardType.IC.value)
        }

        binding.btnMSC.setOnClickListener {
            checkCard(AidlConstants.CardType.MAGNETIC.value)
        }

        binding.btnNFC.setOnClickListener {
            checkCard(AidlConstants.CardType.NFC.value)
        }

        binding.btnAll.setOnClickListener {
            val cardType: Int = AidlConstants.CardType.MAGNETIC.value or AidlConstants.CardType.NFC.value or
                    AidlConstants.CardType.IC.value
            checkCard(cardType)
        }

    }

    private val mCheckCardCallback: CheckCardCallbackV2 = object : CheckCardCallbackWrapper() {
        @SuppressLint("SetTextI18n")
        override fun findICCard(atr: String) {
            super.findICCard(atr)

            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    cardType.value = "Type: IC"
                    result.value = "Result: $atr"
                }
            }
        }

        @SuppressLint("SetTextI18n")
        override fun findMagCard(info: Bundle) {
            super.findMagCard(info)

            val track1 = info.getString("TRACK1")
            val track2 = info.getString("TRACK2")
            val track3 = info.getString("TRACK3")

            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    cardType.value = "Type: Magnetic"
                    result.value =
                        "Result:\n Track 1: $track1 \nTrack 2: $track2 \nTrack 3: $track3 \n"
                }
            }
        }

        @SuppressLint("SetTextI18n")
        override fun findRFCard(uuid: String) {
            super.findRFCard(uuid)

            GlobalScope.launch {
                withContext(Dispatchers.Main){
                    cardType.value = "Type: NFC"
                    result.value = "Result:\n UUID: $uuid"
                }
            }
        }

        override fun onError(code: Int, message: String) {
            super.onError(code, message)
            val error = "onError:$message -- $code"
            println("Error : $error")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cancelCheckCard()
    }

    private fun checkCard(cardType: Int) {
        try {
            mReadCardOptV2!!.checkCard(cardType, mCheckCardCallback, 60)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelCheckCard() {
        try {
            mReadCardOptV2!!.cancelCheckCard()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}