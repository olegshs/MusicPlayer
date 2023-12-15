package ga.sharich.musicplayer

object Helper {
    fun formatDuration(durationMs: Int, precision: Int = 0): String {
        return formatDuration(durationMs.toLong(), precision)
    }

    fun formatDuration(durationMs: Long, precision: Int = 0): String {
        val durationS = durationMs / 1000
        val h = durationS / 3600
        val m = durationS / 60
        val s = durationS % 60

        val formatted = if (h > 0) {
            "%d:%02d:%02d".format(h, m, s)
        } else {
            "%d:%02d".format(m, s)
        }

        if (precision <= 0) {
            return formatted
        }

        val maxPrecision = 3
        val p = if (precision > maxPrecision) {
            maxPrecision
        } else {
            precision
        }

        val ms = "%03d".format(durationMs % 1000).substring(0, p)
        return "%s.%s".format(formatted, ms)
    }
}
