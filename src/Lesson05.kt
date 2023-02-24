package lessons

import org.jsoup.Jsoup
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpRequest
import java.nio.charset.Charset

class Lesson05(override val args: List<String>) : Lesson {
    private var searchQuery = ""
    private lateinit var uri: URI
    private lateinit var request: HttpRequest


    override fun validateArgs(): String? {
        if (args.isEmpty()) {
            return """
Voce deve passar o termo de pesquisa com pelo menos um caractere
Uso: requests-http 05 dia de hoje

Todos os parametros passados serao tratados como um texto para pesquisa, porém é aconselhável que você utilize aspas para que o terminal ignore caracteres especiais
Uso: requests-http 05 "jobs | gates"
""".trimIndent()
        }

        searchQuery = if (args.size == 1) {
            args.last()
        } else {
            args.reduce { acc, s -> "$acc $s" }
        }

        val encodedQuery = URLEncoder.encode(searchQuery, Charset.defaultCharset())
        val url = "https://www.google.com/search?q=$encodedQuery"
        uri = URI(url)

        if (!HttpClientProvider.isUrlValid(url)) {
            return "Algum termo da sua pesquisa parece criar um erro. Por favor, revise algum caractere especial em seu termo de pesquisa"
        }

        request = HttpRequest.newBuilder(uri)
            .header(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36"
            )
            .build()

        return null
    }

    override fun execute(): Int {

        val response = HttpClientProvider.fetch(request)
            ?: return 1

        val parsedHtml = Jsoup.parse(response.body(), "https://www.google.com")

        val labelElements = parsedHtml.select("a h3")
        val linkElements = labelElements.map { it.parent() }

        val results = labelElements.mapIndexed() { index, element ->
            Pair(element.text(), linkElements[index]!!.attr("href"))
        }

        prettyPrintResults(results)

        println("Sua pesquisa pode ser acessada em: " + uri.toURL().toString())

        return 0
    }

    private fun prettyPrintResults(results: List<Pair<String, String>>) {
        if (results.isEmpty()) {
            println("Não há nenhum resultado para o termo de pesquisa \"$searchQuery\". Tente simplificar sua busca ou verificar a ortografia.")
            return
        }
        println("┌────────RESULTADOS DA PESQUISA: ")
        for (i in results.indices) {
            val label = results[i].first
            val link = results[i].second

            println(
                """
                    ├┬ $label")
                    │└ $link
                    │
                """.trimIndent()
            )
        }
        println("└────────FIM DOS RESULTADOS")
    }
}

