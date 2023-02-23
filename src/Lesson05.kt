package lessons

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
"""
        }

        if (args.size == 1) {
            searchQuery = args.last()
        } else {
            searchQuery = args.reduce { acc, s -> "$acc $s" }
        }
        val encodedQuery = URLEncoder.encode(searchQuery, Charset.defaultCharset())
        val url = "https://www.google.com/search?q=$encodedQuery"
        uri = URI(url)

        if (!HttpClientProvider.isUrlValid(url)) {
            return "Algum termo da sua pesquisa parece criar um erro. Por favor, revise algum caractere especial em seu termo de pesquisa"
        }

        request = HttpRequest.newBuilder(uri).build()

        return null
    }

    override fun execute(): Int {

        println("Sua pesquisa pode ser acessada em: " + uri.toURL().toString())

        return 0
    }
}

