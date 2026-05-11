package ml.docilealligator.infinityforreddit

class proxycheck {
    fun getSystemProxy(): String? {
        val proxyHost = System.getProperty("http.proxyHost")
        val proxyPort = System.getProperty("http.proxyPort")

        return if (!proxyHost.isNullOrEmpty() && !proxyPort.isNullOrEmpty()) {
            "$proxyHost:$proxyPort"
        } else null
    }
}

