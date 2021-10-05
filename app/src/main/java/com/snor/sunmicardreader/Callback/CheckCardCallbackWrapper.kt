package com.snor.sunmicardreader.Callback

import android.os.Bundle
import android.os.RemoteException
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2

open class CheckCardCallbackWrapper : CheckCardCallbackV2.Stub() {
    @Throws(RemoteException::class)
    override fun findMagCard(info: Bundle) {
    }

    @Throws(RemoteException::class)
    override fun findICCard(atr: String) {
    }

    @Throws(RemoteException::class)
    override fun findRFCard(uuid: String) {
    }

    @Throws(RemoteException::class)
    override fun onError(code: Int, message: String) {
    }

    @Throws(RemoteException::class)
    override fun findICCardEx(info: Bundle) {
    }

    @Throws(RemoteException::class)
    override fun findRFCardEx(info: Bundle) {
    }

    @Throws(RemoteException::class)
    override fun onErrorEx(info: Bundle) {
    }
}