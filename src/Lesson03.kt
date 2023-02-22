package lessons

import lessons.App.truncOrPad
import java.net.URI
import java.net.http.HttpRequest

class Lesson03(override val args: List<String>) : Lesson {
    private lateinit var uri: URI
    private lateinit var request: HttpRequest

    private lateinit var query: String

    override fun validateArgs(): String? {
        if (args.size != 2) {
            return "Você deve passsar a URL que será usada para realizar a requisição HTTP e a expressão a ser usada na procura" +
                    "\nUso: requests-http 03 https://www.google.com Google" +
                    "\n\nVoce pode tambem por o texto entre aspas para caracteres especiais que o terminal não deve ler:" +
                    "\nUso: requests-http 03 https://flutter.dev \"beautiful mobile\"" +
                    "\n\nUtilize o mesmo recurso para expressões regulares que contenham caracteres que devem ser ignorados pelo terminal:" +
                    "\nUso: requests-http 03 https://flutter.dev \"app[ s]?\""
        }

        query = args[1].trim().lowercase()
        if (query.isEmpty()) {
            return "O termo de pesquisa não pode estar vazio"
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
        val findRegex = Regex("""(?<=>)([^<>]*)($query)([^<>]*)(?=<)""")
        println(findRegex.pattern)
        val matches = findRegex.findAll(response.body())

        prettyPrintMatches(matches.toList())

        return 0
    }

    private fun prettyPrintMatches(matches: List<MatchResult>) {
        if (matches.isEmpty()) {
            println("Não foi encontrado resultados para o a expressão de pesquisa \"$query\" no documento retornado de ${uri.toURL()}.")
            return
        }

        println("Ocorrências:")
        println("Indice | Conteudo")
        println("----------------------------------------------------")
        for (i in matches.indices) {
            val match = matches[i]

            var preffix = match.groups[1]?.value ?: ""
            if (preffix.length > 20) preffix = preffix.substring(preffix.length - 20..preffix.lastIndex)

            var suffix = match.groups.last()?.value ?: ""
            if (suffix.length > 20) suffix = suffix.substring(0..20)

            val textFound = match.groups[2]?.value

            val indice = (i + 1).toString().truncOrPad(7)
            println("$indice| $preffix$textFound$suffix")
            println(
                "       | " +
                        " ".repeat(preffix.length) +
                        "^".repeat(textFound!!.length) +
                        " ".repeat(suffix.length)
            )
            println("----------------------------------------------------")
        }
    }
}

