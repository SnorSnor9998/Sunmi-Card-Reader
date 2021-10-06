package com.snor.sunmicardreader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import com.snor.sunmicardreader.databinding.ActivityInitBinding
import sunmi.paylib.SunmiPayKernel

class InitActivity : AppCompatActivity() {

    private val binding : ActivityInitBinding by viewBinding()
    private var mSMPayKernel: SunmiPayKernel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)


        mSMPayKernel = SunmiPayKernel.getInstance()
        mSMPayKernel!!.initPaySDK(this,object : SunmiPayKernel.ConnectCallback {
            override fun onDisconnectPaySDK() {}
            override fun onConnectPaySDK() {
                try {
                    BaseApp.mReadCardOptV2 = mSMPayKernel!!.mReadCardOptV2
                    BaseApp.mEMVOptV2 = mSMPayKernel!!.mEMVOptV2
                    BaseApp.mPinPadOptV2 = mSMPayKernel!!.mPinPadOptV2
                    BaseApp.mBasicOptV2 = mSMPayKernel!!.mBasicOptV2
                    BaseApp.mSecurityOptV2 = mSMPayKernel!!.mSecurityOptV2
                    Log.e("dd--", "SDK INIT SUCCESSFUL")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })


        binding.button.setOnClickListener {
            val i = Intent(this,MainActivity::class.java)
            startActivity(i)
            finish()
        }

    }


}