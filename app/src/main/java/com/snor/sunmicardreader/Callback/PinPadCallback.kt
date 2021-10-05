package com.snor.sunmicardreader.Callback

import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2

open class PinPadCallback : PinPadListenerV2.Stub(){
    override fun onPinLength(p0: Int) {
    }

    override fun onConfirm(p0: Int, p1: ByteArray?) {
    }

    override fun onCancel() {
    }

    override fun onError(p0: Int) {
    }
}