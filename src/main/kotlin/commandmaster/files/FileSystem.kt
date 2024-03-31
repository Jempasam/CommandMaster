package commandmaster.files

import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists

class FileSystem(val server: MinecraftServer) {

    fun getPath(path: List<String>): Path{
        var result=server.getSavePath(WorldSavePath.GENERATED)
        for(part in path){
            var filtered=part.filter { it.isLetterOrDigit() || it=='_' || it=='.' }
            if(filtered.startsWith(".."))continue
            else if(filtered==".")continue
            result=result.resolve(filtered)
        }
        return result
    }

    fun mkdir(path: List<String>): Boolean{
        return runCatching {
            getPath(path).createDirectory()
        }.isSuccess
    }

    fun write(path: List<String>, content: InputStream): Boolean{
        val fpath=getPath(path)
        return runCatching {
            val file=fpath.toFile()
            if(!file.exists())file.createNewFile()
            val output=file.outputStream()
            while(true){
                val byte=content.read()
                if(byte==-1)break
                output.write(byte)
            }
            output.close()
        }.isSuccess
    }

    fun read(path: List<String>) = runCatching { getPath(path).toFile().inputStream() }

    fun remove(path: List<String>)= getPath(path).deleteIfExists()

    fun list(path: List<String>) = getPath(path) .toFile() .listFiles() ?.map {it.name} ?: listOf()

    fun tree(path: List<String>, action: (Int,String,List<String>)->Unit){
        val mpath=path.toMutableList()
        fun func(depth: Int){
            val content=list(path)
            for(file in content){
                mpath.add(file)
                action(depth,file,mpath)
                func(depth+1)
                mpath.removeLast()
            }
        }
        func(0)
    }

    fun exist(path: List<String>) = getPath(path).toFile().exists()

    companion object{
        fun path(path: String)=path.split("/")

        fun getPath(name: String, context: CommandContext<*>): List<String>{
            return path(context.getArgument(name,String::class.java))
        }
    }
}