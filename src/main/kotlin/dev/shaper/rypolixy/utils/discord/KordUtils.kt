package dev.shaper.rypolixy.utils.discord

import dev.kord.core.entity.User
import dev.kord.rest.Image
import dev.kord.rest.route.CdnUrl

object KordUtils {
    fun User.avatarUrl(imgSize: Image.Size = Image.Size.Size1024): String {
        val sizeString = when (imgSize) {
            Image.Size.Size16 -> "?size=16"
            Image.Size.Size32 -> "?size=32"
            Image.Size.Size64 -> "?size=64"
            Image.Size.Size128 -> "?size=128"
            Image.Size.Size256 -> "?size=256"
            Image.Size.Size512 -> "?size=512"
            Image.Size.Size1024 -> "?size=1024"
            Image.Size.Size2048 -> "?size=2048"
            Image.Size.Size4096 -> "?size=4096"
        }
        return this.avatar?.cdnUrl?.toUrl {
            CdnUrl.UrlFormatBuilder().apply {
                format  = Image.Format.WEBP
                size    = imgSize
            }
        } + sizeString
    }
}