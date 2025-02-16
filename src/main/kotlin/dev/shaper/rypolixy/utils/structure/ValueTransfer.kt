package dev.shaper.rypolixy.utils.structure

import java.math.BigInteger

object ValueTransfer {

    fun ULong.toBigInteger(): BigInteger {
        // ULong을 8바이트의 빅엔디언(ByteOrder.BIG_ENDIAN) 배열로 변환
        val bytes = ByteArray(8) { i -> ((this shr ((7 - i) * 8)) and 0xFFUL).toByte() }
        // 가장 높은 비트가 1(음수로 해석될 가능성)이면, 부호 없는 값으로 만들기 위해 앞에 0 바이트 추가
        return if (bytes[0].toInt() and 0x80 != 0) {
            BigInteger(byteArrayOf(0) + bytes)
        } else {
            BigInteger(bytes)
        }
    }

}