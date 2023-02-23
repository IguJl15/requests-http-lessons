package lessons

import lessons.App.truncOrPad
import java.net.URI
import java.net.http.HttpRequest

class Lesson02(override val args: List<String>) : Lesson {
    private lateinit var uri: URI
    private lateinit var request: HttpRequest

    private lateinit var htmlTag: String

    override fun validateArgs(): String? {
        if (args.size != 2) {
            return """
Você deve passsar a URL que será usada para realizar a requisição HTTP e a TAG HTML que será procurada
Uso: requests-http 02 https://www.google.com h2
            """
        }

        htmlTag = args[1].trim().lowercase()
        if (htmlTag.isEmpty() || htmlTag.matches(Regex("[^a-z]"))) {
            return "A tag HTML passada não é válida"
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

        // find the specified tag content
        val tagRegex = Regex("""<$htmlTag\b[^>]*>(.*?)<\/$htmlTag>""")

        val matches = tagRegex.findAll(response.body())

        prettyPrintMatches(matches.toList())

        return 0
    }

    private fun prettyPrintMatches(matches: List<MatchResult>) {
        if (matches.isEmpty()) {
            println(
                """
Não foi encontrado nenhum elemento $htmlTag no documento retornado de ${uri.toURL()}. Lembre-se:
o conteúdo de uma página não é entregue todo de uma vez pelo servidor, alguns itens são
buscados por scripts da página.
"""
            )

            return
        }

        println("Ocorrências:")
        println("Indice | Conteudo")
        for (i in matches.indices) {
            var content = matches[i].groups[1]?.value
            content = (content ?: "N/A")

            val indice = (i + 1).toString().truncOrPad(7)

            println("$indice| $content")
        }
    }
}

