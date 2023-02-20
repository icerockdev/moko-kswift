package dev.icerock.moko.kswift.plugin

class InMemoryAppendable : Appendable {
    private val sb = StringBuilder()
    override fun append(csq: CharSequence?): java.lang.Appendable {
        sb.append(csq)
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): java.lang.Appendable {
        sb.insert(0, csq, start, end)
        return this
    }

    override fun append(c: Char): java.lang.Appendable {
        sb.append(c)
        return this
    }

    override fun toString(): String = sb.toString()
}
