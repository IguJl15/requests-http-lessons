package lessons

import java.io.File
import java.net.URL
import java.nio.file.Files
import kotlin.io.path.Path

class Lesson04(override val args: List<String>) : Lesson {
    private lateinit var url: URL
    private var path = "imageOutput/image."
    private var extension = ""

    override fun validateArgs(): String? {
        if (args.isEmpty() || args.size > 2) {
            return """
Você deve passsar a URL da imagem que será HTTP e o download da mesma
Uso: requests-http 04 https://hpg.com.br/wp-content/uploads/2023/10/nft-precos-comprar-1.png

Caso deseje, você pode tambem especificar o caminho de saída da imagem que deve conter o nome do arquivo de saída (padrão: "./imageOutput/image.xxx". A extensão (png, jpg, svg) é detectada automaticamente a patir da URL):
Uso: requests-http 04 https://hpg.com.br/wp-content/uploads/2023/10/nft-precos-comprar-1.png ./outDir/image.png
"""
        }

        val url = args[0]
        if (!HttpClientProvider.isUrlValid(url)) {
            return "O esquema da URL passada não é válida (você digitou a url correta?)"
        }
        this.url = URL(url)

        if (args.size == 2) {
            path = args[1]
        } else {
            extension = url.split(".").lastOrNull() ?: ""
            path += extension
        }

        return null
    }

    override fun execute(): Int {
        val file = File(path)

        if (file.exists()) {
            val deleted = file.delete()
            if (!deleted) {
                println("Não foi possivel apagar o arquivo ja existente para substituí-lo")
                return 1
            }
        } else {
            val absolutePath = file.absolutePath
            val directoryPath =
                Path(
                    absolutePath.removeRange(
                        absolutePath.lastIndexOf(File.separator)..absolutePath.lastIndex
                    )
                )

            Files.createDirectories(directoryPath)
        }

        file.createNewFile()

        // if response is null, HttpClientProvider must provide more details. Due that, just stop function
        HttpClientProvider.downloadFile(url, file)

        return 0
    }
}

