import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

object ReadFile {

    fun readMarkDownFileByRaw(filename: String): ArrayList<String> {
        val fileContent = ArrayList<String>()
        var str: String
        var count = 0
        val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(filename), "UTF-8"))
        try {
            while (bufferedReader.readLine().also { str = it } != null) {
                if (count == 0) {
                    str = str.substring(1)
                    count++
                }
                if (str != "") {
                    fileContent.add(str)
                } else {
                    fileContent.add(" ")
                }
            }
            }catch (_:Exception){
            }
        return fileContent
    }
}