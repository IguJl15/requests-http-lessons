package lessons

import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface Lesson {
    val args: Collection<String>
    fun validateArgs(): String?
    fun execute(): Int
}

object App {
    var verbose = false
    var content = false

    fun parseFlags(options: Collection<String>) {
        if ("--verbose" in options || "-v" in options) this.verbose = true
        if ("--content" in options || "-c" in options) this.content = true
    }

    fun printVerbose(message: Any?) {
        if (verbose) println(message)
    }

    fun printHelp() {
        val help = """
Usage: http-requests-lessons [OPTION] QUESTION_NUMBER <QUESTION_ARGUMENTS>

Questions:
01:
Exibir links de uma página (URL) extraindo-os de tags <a>
ARGUMENTS: URL
Usage: http-requests-lessons 01 https://www.google.com

02:
Baixe uma página e exiba o conteúdo de uma determinada tag lida pelo teclado.
ARGUMENTS: URL, TAG
Usage: http-requests-lessons 01 https://www.google.com svg
Aviso: Caso utilize a tag div o processo pode demorar mais que o normal e muito conteudo será impresso no terminal

03: 
Receba uma página como entrada e um termo a ser buscado e liste as 
ocorrências dentro dessa página. Atente para extrair o texto da página sem as 
tags e, ao encontrar uma ocorrência do termo, exiba os 20 caracteres antes e 
20 caracteres depois.
ARGUMENTS: URL, TERMO
Usage: http-requests-lessons 01 URL

Options:
-v, --verbose   Imprime mais detalhes sobre a operação
-c, --content   Utilizando '--verbose', imprime o conteúdo da requisição (quando válido)
-h, --help      Imprime este texto de ajuda
"""
        println(help)
    }

    fun String.truncOrPad(width: Int): String {
        return if (length > width) subSequence(0, width).toString()
        else padEnd(width)
    }

    fun printResponseDetails(response: HttpResponse<String>, request: HttpRequest, elapsedTime: Long) {
        printVerbose("Tempo da requisição: ${elapsedTime}ms")
        printVerbose("Código de status: ${response.statusCode()}")
        printVerbose("\nCabeçalho da Requisição:")
        printVerbose(request.headers())
        printVerbose("\nCabeçalho da Resposta:")
        printVerbose(response.headers())
        if (content) {
            printVerbose("\nConteúdo da Resposta:")
            printVerbose(response.body())
        }
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty() || "-h" in args || "--help" in args) {
        App.printHelp()
        return
    }

    val flags = args.filter { it.startsWith('-') }.toSet()
    App.parseFlags(flags)

    val questionNumber = args.first { it.toIntOrNull() != null }
    // everythin ahead the number are question arguments
    val questionArguments = args.slice(args.indexOf(questionNumber) + 1..args.lastIndex).minus(flags)

    val lesson: Lesson? = when (questionNumber.toInt()) {
        1 -> Lesson01(questionArguments)
        else -> null
    }

    if (lesson == null) {
        println("Escolha uma dentre as opções disponiveis. Utilize '--help' visualizar as opções")
        return
    }

    executeLesson(lesson)
}

fun executeLesson(lesson: Lesson): Int {
    val argsError = lesson.validateArgs()
    if (argsError != null) {
        println(argsError)
        return 1
    }

    return lesson.execute()
}
