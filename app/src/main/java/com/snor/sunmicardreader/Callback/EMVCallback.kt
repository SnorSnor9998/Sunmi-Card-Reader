package com.snor.sunmicardreader.Callback

import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2

open class EMVCallback : EMVListenerV2.Stub() {
    override fun onWaitAppSelect(p0: MutableList<EMVCandidateV2>?, p1: Boolean) {
    }

    override fun onAppFinalSelect(p0: String?) {
    }

    override fun onConfirmCardNo(p0: String?) {
    }

    override fun onRequestShowPinPad(p0: Int, p1: Int) {
    }

    override fun onRequestSignature() {
    }

    override fun onCertVerify(p0: Int, p1: String?) {
    }

    override fun onOnlineProc() {
    }

    override fun onCardDataExchangeComplete() {
    }

    override fun onTransResult(p0: Int, p1: String?) {
    }

    override fun onConfirmationCodeVerified() {
    }

    override fun onRequestDataExchange(p0: String?) {
    }
}