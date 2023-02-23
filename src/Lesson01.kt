package lessons

import lessons.App.truncOrPad
import java.net.URI
import java.net.http.HttpRequest

class Lesson01(override val args: List<String>) : Lesson {
    private lateinit var uri: URI
    private lateinit var request: HttpRequest

    override fun validateArgs(): String? {
        if (args.size != 1) {
            return """
Você deve passsar a URL que será usada para realizar a requisição HTTP
Uso: requests-http [OPTION] 01 https://www.google.com
"""
        }

        val url = args[0]
        if (!HttpClientProvider.isUrlValid(url)) {
            return "O esquema da URL passada não é válida (você digitou a url correta?)"
        }
        uri = URI(url)
        request = HttpRequest.newBuilder(uri).build()

        return null
    }

    override fun execute(): Int {
        // if response is null, HttpClientProvider must provide more details. Due that, just stop function
        val response = HttpClientProvider.fetch(request)
            ?: return 1

        // find the tag <a>
        val aTagRegex = Regex("""<a\b[^>]*href="([^"]*)"[^>]*>(.*?)<\/a>""")

        val matches = aTagRegex.findAll(response.body())

        prettyPrintMatches(matches)
        return 0
    }

    private fun prettyPrintMatches(matches: Sequence<MatchResult>) {
        val maxWidth = 50

        var biggestWidth = matches.maxOf { it.groups[2]!!.value.length }
        biggestWidth = if (biggestWidth > maxWidth) maxWidth else biggestWidth

        for (match in matches) {
            val tagContent = match.groups[2]?.value
            val tagHrefLink = match.groups[1]?.value

            val label = (tagContent ?: "N/A").truncOrPad(biggestWidth)
            val link = (tagHrefLink ?: "N/A")

            println("$label: $link")
        }
    }
}

