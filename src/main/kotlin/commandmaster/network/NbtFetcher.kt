package commandmaster.network

import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.StringNbtReader
import org.apache.http.HttpResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpResponse.BodySubscriber
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object NbtFetcher {
    fun fetchNbt(url: URI): CompletableFuture<Result<NbtElement>> {
        val client=HttpClient.newHttpClient()
        val request=HttpRequest
            .newBuilder(url)
            .GET()
            .timeout(Duration.ofSeconds(5))
            .build()
        return client.sendAsync(request, BodyHandlers.ofString()).thenApply { runCatching { StringNbtReader.parse(it.body()) } }
    }

}

fun main() {
    NbtFetcher.fetchNbt(URI.create("http://localhost:8080/test.txt")).thenApply {
        println(it)
    }.join()

}