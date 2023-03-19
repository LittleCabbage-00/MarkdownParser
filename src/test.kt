import java.io.File

fun isFileExists(file: File): Boolean {
    return file.exists() && !file.isDirectory
}

fun main() {
    val filePath = "."+File.separator+"test.md"
    val file = File(filePath)

    if (isFileExists(file)) {
        println("File exists!!")
    } else {
        println("File doesn't exist or program doesn't have access to it")
    }
}

