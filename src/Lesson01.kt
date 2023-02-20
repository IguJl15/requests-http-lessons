package lessons

import lessons.App.truncOrPad
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Lesson01(override val args: List<String>) : Lesson {

    private val client = HttpClient.newBuilder().build()
    private lateinit var uri: URI
    private lateinit var request: HttpRequest

    override fun validateArgs(): String? {
        if (args.size != 1) {
            return "Você deve passsar a URL que será usada para realizar a requisição HTTP" +
                    "\nUso: requests-http [OPTION] 01 https://www.google.com"
        }

        try {
            uri = URI(args.first())
            request = HttpRequest.newBuilder(uri).build()
        } catch (e: IllegalArgumentException) {
            App.printVerbose(e.localizedMessage)
            App.printVerbose(e.cause)

            return "O esquema da URL passada não é válida (você digitou a url correta?)"
        }

        return null
    }

    override fun execute(): Int {
        App.printVerbose("Realizando requisição para \"${uri.host}\"")
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        val statusCode = response.statusCode()
        App.printVerbose("Código da resposta: $statusCode")
        App.printResponseDetails(response, request)


        if (statusCode != 200) {
            println("Houve algo de errado durante a requisição.")
            if (!App.verbose) println("Tente novamente utilizando o argumento '--verbose' para visualizar mais detalhes.")
            return 1
        }

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

