package com.snor.sunmicardreader

import android.app.Application
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2

class BaseApp : Application() {


    companion object{

        var mBasicOptV2: BasicOptV2? = null
        var mReadCardOptV2: ReadCardOptV2? = null
        var mEMVOptV2: EMVOptV2? = null
        var mPinPadOptV2: PinPadOptV2? = null
        var mSecurityOptV2: SecurityOptV2? = null

    }

}