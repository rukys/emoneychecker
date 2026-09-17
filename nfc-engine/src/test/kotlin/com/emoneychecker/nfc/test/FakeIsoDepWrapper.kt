package com.emoneychecker.nfc.test

import com.emoneychecker.nfc.apdu.IsoDepWrapper
import java.io.IOException

class FakeIsoDepWrapper(
    private val responses: Map<String, ByteArray>
) : IsoDepWrapper {
    private var connected = false
    var closeCallCount = 0

    override fun connect() {
        connected = true
    }

    override fun close() {
        connected = false
        closeCallCount++
    }

    override fun isConnected() = connected
    override val maxTransceiveLength = 261

    override fun transceive(command: ByteArray): ByteArray {
        val key = command.joinToString(" ") { "%02X".format(it) }
        return responses[key]
            ?: throw IOException("No fake response for APDU: $key")
    }
}
