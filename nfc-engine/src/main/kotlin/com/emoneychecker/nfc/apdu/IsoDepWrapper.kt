package com.emoneychecker.nfc.apdu

import android.nfc.tech.IsoDep
import java.io.IOException

interface IsoDepWrapper {
    fun connect()
    fun close()
    fun isConnected(): Boolean
    @Throws(IOException::class)
    fun transceive(command: ByteArray): ByteArray
    val maxTransceiveLength: Int
}

class AndroidIsoDepWrapper(private val isoDep: IsoDep) : IsoDepWrapper {
    override fun connect() = isoDep.connect()
    override fun close() = isoDep.close()
    override fun isConnected(): Boolean = isoDep.isConnected
    override fun transceive(command: ByteArray): ByteArray {
        val cmdHex = command.joinToString(" ") { "%02X".format(it) }
        try {
            val resp = isoDep.transceive(command)
            val respHex = resp.joinToString(" ") { "%02X".format(it) }
            android.util.Log.d("EmoneyNfc", "APDU TX: $cmdHex -> RX: $respHex")
            return resp
        } catch (e: Exception) {
            android.util.Log.e("EmoneyNfc", "APDU TX: $cmdHex -> FAILED: ${e.javaClass.simpleName} ${e.message}")
            throw e
        }
    }
    override val maxTransceiveLength: Int get() = isoDep.maxTransceiveLength
}
