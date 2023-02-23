package lessons

import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.channels.Channels

object HttpClientProvider {
    private val client = HttpClient.newBuilder().build()

    fun isUrlValid(url: String): Boolean {
        try {
            val uri = URI(url)
            HttpRequest.newBuilder(uri).build()
            return true
        } catch (e: IllegalArgumentException) {
            App.printVerbose(e.localizedMessage)
            App.printVerbose(e.cause)

            return false
        }
    }

    fun fetch(request: HttpRequest): HttpResponse<String>? {
        App.printVerbose("Realizando requisição para \"${request.uri().host}\"")

        val startTime = System.currentTimeMillis()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val endTime = System.currentTimeMillis()

        val elapsedTime = endTime - startTime

        val statusCode = response.statusCode()
        App.printResponseDetails(response, request, elapsedTime)

        if (!(statusCode in 200 until 300)) {
            println("Houve algo de errado durante a requisição.")
            if (!App.verbose) println("Tente novamente utilizando o argumento '--verbose' para visualizar mais detalhes.")
            return null
        }

        return response
    }

    fun downloadFile(url: URL, outputFile: File) {
        url.openStream().use {
            Channels.newChannel(it).use { rbc ->
                FileOutputStream(outputFile).use { fos ->
                    fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
                }
            }
        }
    }
}