import java.io.File

object ConvertRun {
    //按行存储md文件内容到内存
    private var mdContent = ArrayList<String>()
    //存储md每一行对应的标签类型
    private val mdType = ArrayList<String>()
    @JvmStatic
    fun main(args: Array<String>) {
        //获取文件内容
        mdContent = ReadFile.readMarkDownFileByRaw("." + File.separator + "test.md")
        //第一次文件扫描，确定代码块区域位置
        ScanFile.CodeAreaParse(mdContent, mdType)
        //第二次文档扫描，判断引用块、有序列表块、无序列表块
        ScanFile.QuoteListTitleParse(mdContent, mdType)
        //第三次的文件扫描，把文件转换为html文件
        ScanFile.InLineElemParseAndConvertToHtml(mdContent, mdType)
        //把转换后的代码生成为html,把文件输出到web目录
        val filename = "index.html"
        val indexFile = File("web" + File.separator + "index.html")
        if (!indexFile.exists()) {
            ScanFile.PrintToHtml(mdContent, filename)
            println("文件已创建")
        } else {
            println("文件已存在，删除后重新生成")
            indexFile.delete()
            ScanFile.PrintToHtml(mdContent, filename)
        }
    }
}