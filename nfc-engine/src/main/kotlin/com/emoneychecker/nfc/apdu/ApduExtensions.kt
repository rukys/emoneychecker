package com.emoneychecker.nfc.apdu

fun ByteArray.sw(): Int {
    if (size < 2) return 0
    return ((this[size - 2].toInt() and 0xFF) shl 8) or (this[size - 1].toInt() and 0xFF)
}

fun ByteArray.isSuccess(): Boolean {
    val status = sw()
    return status == 0x9000 || status == 0x9100
}

fun ByteArray.payload(): ByteArray {
    if (size < 2) return byteArrayOf()
    return copyOf(size - 2)
}

fun String.hexToByteArray(): ByteArray {
    val clean = replace(" ", "")
    val result = ByteArray(clean.length / 2)
    for (i in result.indices) {
        val index = i * 2
        result[i] = clean.substring(index, index + 2).toInt(16).toByte()
    }
    return result
}

fun ByteArray.toHexString(): String = joinToString(" ") { "%02X".format(it) }
