package com.snor.sunmicardreader

import android.annotation.SuppressLint
import android.os.*
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.snor.sunmicardreader.Callback.CheckCardCallbackWrapper
import com.snor.sunmicardreader.Callback.EMVCallback
import com.snor.sunmicardreader.Callback.PinPadCallback
import com.snor.sunmicardreader.databinding.ActivityMainBinding
import com.snor.sunmicardreader.util.ByteUtil
import com.snor.sunmicardreader.util.TLV
import com.snor.sunmicardreader.util.TLVUtil
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidl.AidlErrorCode
import com.sunmi.pay.hardware.aidlv2.bean.EMVTransDataV2
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sunmi.paylib.SunmiPayKernel
import java.util.*


class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by viewBinding()

    private var mSMPayKernel: SunmiPayKernel? = null
    private var mReadCardOptV2: ReadCardOptV2? = null
    private var mEMVOptV2: EMVOptV2? = null
    private var mPinPadOptV2: PinPadOptV2? = null

    //FOR UI
    private val cardType = MutableLiveData<String>()
    private val result = MutableLiveData<String>()

    private var mCardNo: String = ""
    private var mCardType = 0
    private var mPinType: Int? = null

    override fun onStart() {
        super.onStart()

        mSMPayKernel = SunmiPayKernel.getInstance()
        mSMPayKernel!!.initPaySDK(applicationContext, object : SunmiPayKernel.ConnectCallback {
            override fun onDisconnectPaySDK() {}
            override fun onConnectPaySDK() {
                try {
                    mReadCardOptV2 = mSMPayKernel!!.mReadCardOptV2
                    mEMVOptV2 = mSMPayKernel!!.mEMVOptV2
                    mPinPadOptV2 = mSMPayKernel!!.mPinPadOptV2
                    Log.e("dd--", "SDK INIT SUCCESSFUL")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    private fun checkCard(cardType: Int) {
        try {
            mEMVOptV2!!.abortTransactProcess()
            mEMVOptV2!!.initEmvProcess()

            mReadCardOptV2!!.checkCard(cardType, mCheckCardCallback, 60)
        } catch (e: Exception) {
            e.printStackTrace()
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
                    println(cardType.value)
                    println(result.value)

                    mCardType = AidlConstants.CardType.IC.value
                    transactProcess()

                    cancelCheckCard()

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
                    println(cardType.value)
                    println(result.value)

                    mCardType = AidlConstants.CardType.MAGNETIC.getValue()
                    cancelCheckCard()
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
                    println(cardType.value)
                    println(result.value)

                    mCardType = AidlConstants.CardType.NFC.getValue()
                    transactProcess()

                    cancelCheckCard()

                }
            }
        }

        override fun onError(code: Int, message: String) {
            super.onError(code, message)
            val error = "onError:$message -- $code"
            println("Error : $error")
        }
    }

    private fun cancelCheckCard() {
        try {
            mReadCardOptV2!!.cancelCheckCard()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun transactProcess() {
        Log.e("dd--", "transactProcess")
        try {
            val emvTransData = EMVTransDataV2()
            emvTransData.amount = "10"
            emvTransData.flowType = 1
            emvTransData.cardType = mCardType
            mEMVOptV2!!.transactProcess(emvTransData, mEMVListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mEMVListener = object : EMVCallback(){

        override fun onAppFinalSelect(p0: String?) {
            super.onAppFinalSelect(p0)

            Log.e("dd--", "onAppFinalSelect value:$p0")

            // set normal tlv data
            val tags = arrayOf("5F2A", "5F36")
            val value = arrayOf("0643", "00")
            mEMVOptV2!!.setTlvList(AidlConstants.EMV.TLVOpCode.OP_NORMAL, tags,value)


            if (p0 != null && p0.isNotEmpty()){
                val isVisa = p0.startsWith("A000000003")
                val isMaster =
                    (p0.startsWith("A000000004") || p0.startsWith("A000000005"))

                if (isVisa){
                    // VISA(PayWave)
                    Log.e("dd--", "detect VISA card")
                    val tagsPayWave = arrayOf("DF8124", "DF8125", "DF8126")
                    val valuesPayWave = arrayOf(
                        "999999999999", "999999999999", "000000000000")
                    mEMVOptV2!!.setTlvList(
                        AidlConstants.EMV.TLVOpCode.OP_PAYWAVE,
                        tagsPayWave,
                        valuesPayWave
                    )
                }else if(isMaster){
                    // MasterCard(PayPass)
                    Log.e("dd--", "detect MasterCard card")
                    // set PayPass tlv data
                    val tagsPayPass = arrayOf(
                        "DF8117", "DF8118", "DF8119", "DF811F", "DF811E", "DF812C",
                        "DF8123", "DF8124", "DF8125", "DF8126",
                        "DF811B", "DF811D", "DF8122", "DF8120", "DF8121"
                    )
                    val valuesPayPass = arrayOf(
                        "E0", "F8", "F8", "E8", "00", "00",
                        "000000000000", "000000100000", "999999999999", "000000100000",
                        "30", "02", "0000000000", "000000000000", "000000000000"
                    )
                    mEMVOptV2!!.setTlvList(
                        AidlConstants.EMV.TLVOpCode.OP_PAYPASS,
                        tagsPayPass,
                        valuesPayPass
                    )
                }


            }
            mEMVOptV2!!.importAppFinalSelectStatus(0)
        }

        override fun onConfirmCardNo(p0: String?) {
            super.onConfirmCardNo(p0)
            Log.e("dd--", "onConfirmCardNo cardNo:$p0")
            mCardNo = p0!!
            mEMVOptV2!!.importCardNoStatus(0)
        }

        override fun onRequestShowPinPad(p0: Int, p1: Int) {
            super.onRequestShowPinPad(p0, p1)
            Log.e("dd---", "onRequestShowPinPad pinType:$p0 remainTime:$p1")
            // 0 - online pin, 1 - offline pin
            mPinType = p0
            initPidPad()
        }

        override fun onCertVerify(p0: Int, p1: String?) {
            super.onCertVerify(p0, p1)
            Log.e("dd---", "onCertVerify certType:$p0 certInfo:$p1")
        }


        override fun onTransResult(p0: Int, p1: String?) {
            super.onTransResult(p0, p1)
            Log.e("dd---", "onTransResult code:$p0 desc:$p1")
            getExpireDateAndCardholderName()
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        cancelCheckCard()
    }


    private fun initPidPad(){
        Log.e("dd---", "initPinPad")
        try {
            val pinPadConfig = PinPadConfigV2()
            pinPadConfig.pinPadType = 0 //0 show default pin pad , 1 custom
            pinPadConfig.pinType = mPinType!! //0 online 1 offline
            pinPadConfig.isOrderNumKey = true

            // ascii格式转换成的byte 例如 “123456”.getBytes("us ascii")
            val panBytes = mCardNo.substring(mCardNo.length - 13, mCardNo.length - 1)
                .toByteArray(charset("US-ASCII"))
            pinPadConfig.pan = panBytes

            pinPadConfig.timeout = 15 * 1000 // input password timeout
            pinPadConfig.pinKeyIndex = 1 // pik index
            pinPadConfig.maxInput = 6
            pinPadConfig.minInput = 0

            pinPadConfig.keySystem = 0 // 0 - MkSk 1 - DuKpt
            pinPadConfig.algorithmType = 0 // 0 - 3DES 1 - SM4
            mPinPadOptV2!!.initPinPad(pinPadConfig, mPinPadListener)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private val mPinPadListener = object: PinPadCallback(){
        override fun onPinLength(p0: Int) {
            super.onPinLength(p0)
            Log.e("dd--", "onPinLength:$p0")
        }

        override fun onConfirm(p0: Int, p1: ByteArray?) {
            super.onConfirm(p0, p1)
            if (p1 != null) {
                val hexStr = ByteUtil.bytes2HexStr(p1)
                Log.e("dd--", "onConfirm pin block:$hexStr")
            }
        }

        override fun onCancel() {
            super.onCancel()
            Log.e("dd--", "onCancel")
        }

        override fun onError(p0: Int) {
            super.onError(p0)
            Log.e("dd--", "onError: ${AidlErrorCode.valueOf(p0).msg}")
        }

    }



    @Throws(RemoteException::class)
    private fun getExpireDateAndCardholderName() {
        val out = ByteArray(64)
        val tags = arrayOf("5F24", "5F20")
        val len: Int = mEMVOptV2!!.getTlvList(AidlConstants.EMV.TLVOpCode.OP_NORMAL, tags, out)
        if (len > 0) {
            val bytesOut = Arrays.copyOf(out, len)
            val hexStr = ByteUtil.bytes2HexStr(bytesOut)
            val map: Map<String, TLV> = TLVUtil.buildTLVMap(hexStr)
            val tlv5F24: TLV? = map["5F24"] // expire date
            val tlv5F20: TLV? = map["5F20"] // cardholder name
            var expireDate = ""
            var cardholder = ""
            if (tlv5F24!=null && tlv5F24.value != null) {
                expireDate = tlv5F24.value
            }
            if (tlv5F20 != null && tlv5F20.value != null) {
                val value: String = tlv5F20.value
                val bytes = ByteUtil.hexStr2Bytes(value)
                cardholder = String(bytes)
            }
            val finalExpireDate = expireDate
            val month = expireDate.substring(2,4)
            val year = expireDate.substring(0,2)
            val finalCardholder = cardholder

            Log.e("dd--", "expireDate month:$month year:$year")
            Log.e("dd--", "cardholder:$cardholder")

        }
    }


}