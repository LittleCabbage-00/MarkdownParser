import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ScanFile {
    //```
    internal const val CODE_ENTER = "code-enter"

    //``` end
    internal const val CODE_EXIT = "code-exit"

    //``` <code> ```
    internal const val CODE_RAW = "code-raw"

    /**
     * > code
     * > code
     */
    internal const val QUOTE_ENTER = "quote-enter"

    /**
     * > code
     * > code
     *   code
     */
    internal const val QUOTE_EXIT = "quote-exit"

    //> code
    internal const val QUOTE_RAW = "quote-raw"

    //> code area
    internal const val QUOTE_SINGLE = "quote-single"

    //- code
    internal const val UNORDERED_LIST_ENTER = "unordered-list-enter"

    /**
     * - code
     * - code
     *   code
     */
    internal const val UNORDERED_LIST_EXIT = "unordered-list-exit"

    /**
     * - code
     * - code area
     */

    internal const val UNORDERED_LIST_RAW = "unordered-list-raw"

    //- code area
    internal const val UNORDERED_LIST_SINGLE = "unordered-list-single"

    /**
     * 1. code
     * 2. code
     */
    internal const val ORDERED_LIST_ENTER = "ordered-list-enter"

    /**
     * 1. code
     * 2. code
     *    code
     */
    internal const val ORDERED_LIST_EXIT = "ordered-list-exit"

    /**
     * 1. code
     * 2. code area
     */
    internal const val ORDERED_LIST_RAW = "ordered-list-raw"

    //1. code 2. code area
    internal const val ORDERED_LIST_SINGLE = "ordered-list-single"

    internal const val TITLE = "title"

    internal const val BLANK_RAW = "blank-raw"

    //---------
    internal const val HR = "hr"

    internal const val NONE = "none"
    fun CodeAreaParse(mdContent: ArrayList<String>, mdType: ArrayList<String>) {

        var code_enter = false
        var code_exit = false
        for (i in mdContent.indices) {
            val s = mdContent[i]
            if (s.length > 2 && s.startsWith("```")) {
                if (!code_enter && !code_exit) {
                    //将文件内容变为" "
                    mdContent[i] = " "
                    //设置对应该行的类型为刚刚进入代码区
                    mdType.add(CODE_ENTER)
                    //设置code_enter标志变量为true，代表下面的是代码区
                    code_enter = true
                } else if (code_enter && !code_exit) {
                    mdContent[i] = " "
                    mdType.add(CODE_EXIT)
                    //让退出的标记变量变为false，代表代码块已经退出
                    //让二者都变为false，为遇到下一个代码块做准备
                    code_exit = false
                    code_enter = false
                } else {  //这儿都不会进来
                    mdType.add(CODE_RAW)
                }
            } else if (code_enter && !code_exit) {
                mdType.add(CODE_RAW)
            } else { //在此过程中判断空行的类型,空行在读入时都变为了" "，其他非代码区的都设置为OTHER类型
                if (s == " ") {   //如果是空行，则将类型添加
                    mdType.add(BLANK_RAW)
                } else if (s == "------") {  //判断是否是水平分割线，如果是添加其类型
                    mdType.add(HR)
                } else if (s.startsWith("#")) {  //判断是否是标题，如果是添加其类型,具体是几号标题在具体生成代码时进行判断
                    mdType.add(TITLE)
                } else {
                    mdType.add(NONE)
                }
            }
        }
    }
    fun QuoteListTitleParse(mdContent: ArrayList<String>, mdType: ArrayList<String>) {
        //第一步：解析引用块部分，分为单行和多行,其实就是判断首位是不是>,且代码行中出现的>不用解析
        //       所以，我们应该设置一个标志变量，用来标志当前的位置是否是代码区
        var isCodeArea = false
        //开始逐行判断是否是引用区，由于涉及到前后两行才能进行判断区域，所以以下循环
        for (i in 1 until mdContent.size - 1) {
            //得到当前及相邻两行，从而更好的判断区域
            val now = mdContent[i]
            val last = mdContent[i - 1]
            val next = mdContent[i + 1]
            //先判断是否是代码区，如果是代码区，则直接跳过
            if (mdType[i] == CODE_ENTER) {
                isCodeArea = true
                continue
            }
            if (mdType[i] == CODE_EXIT) {
                isCodeArea = false
                continue
            }
            //只有当不是代码区时，才判断当前的行是不是引用区或者其他区
            if (!isCodeArea) {
                //判断是否是多行引用区的入口
                if (now.length > 0 && now.startsWith(">") && !last.startsWith(">") && next.startsWith(">")) {
                    //说明该行是多行引用区的入口，所以改变其类型
                    mdType[i] = QUOTE_ENTER
                } else if (now.length > 0 && now.startsWith(">") && last.startsWith(">") && !next.startsWith(">")) {
                    //说明该行是多行引用区的终点，所以改变其类型
                    mdType[i] = QUOTE_EXIT
                } else if (now.length > 0 && now.startsWith(">") && last.startsWith(">") && next.startsWith(">")) {
                    mdType[i] = QUOTE_RAW
                } else if (now.length > 0 && now.startsWith(">") && !last.startsWith(">") && !next.startsWith(">")) {
                    mdType[i] = QUOTE_SINGLE
                } else if (now.startsWith("-") && now != "------" && !last.startsWith("-") && next.startsWith("-")) {
                    mdType[i] = UNORDERED_LIST_ENTER
                } else if (now.startsWith("-") && now != "------" && last.startsWith("-") && !next.startsWith("-")) {
                    mdType[i] = UNORDERED_LIST_EXIT
                } else if (now.startsWith("-") && now != "------" && last.startsWith("-") && next.startsWith("-")) {
                    mdType[i] = UNORDERED_LIST_RAW
                } else if (now.startsWith("-") && now != "------" && !last.startsWith("-") && !next.startsWith("-")) {
                    mdType[i] = UNORDERED_LIST_SINGLE
                } else if (now.length > 1 && now[0] >= '1' && now[0] <= '9' && now[1] == '.' &&
                    !(last.length > 1 && last[0] >= '1' && last[0] <= '9' && last[1] == '.') && next.length > 1 && next[0] >= '1' && next[0] <= '9' && next[1] == '.'
                ) {
                    mdType[i] = ORDERED_LIST_ENTER
                } else if (now.length > 1 && now[0] >= '1' && now[0] <= '9' && now[1] == '.' && last.length > 1 && last[0] >= '1' && last[0] <= '9' && last[1] == '.' &&
                    !(next.length > 1 && next[0] >= '1' && next[0] <= '9' && next[1] == '.')
                ) {
                    mdType[i] = ORDERED_LIST_EXIT
                } else if (now.length > 1 && now[0] >= '1' && now[0] <= '9' && now[1] == '.' && last.length > 1 && last[0] >= '1' && last[0] <= '9' && last[1] == '.' && next.length > 1 && next[0] >= '1' && next[0] <= '9' && next[1] == '.') {
                    mdType[i] = ORDERED_LIST_RAW
                } else if (now.length > 1 && now[0] >= '1' && now[0] <= '9' && now[1] == '.' &&
                    !(last.length > 1 && last[0] >= '1' && last[0] <= '9' && last[1] == '.') &&
                    !(next.length > 1 && next[0] >= '1' && next[0] <= '9' && next[1] == '.')
                ) {
                    mdType[i] = ORDERED_LIST_ENTER
                }
            }
        }
    }

    fun InLineElemParseAndConvertToHtml(mdContent: ArrayList<String>, mdType: ArrayList<String>) {
        for (i in mdContent.indices) {
            val now = mdContent[i]
            val nowType = mdType[i]
            //如果该行是标题，则进行标题html的生成，同时对其行内元素进行解析
            if (nowType == TITLE) {
                //声明一个变量用来记录标题是<hi>
                var count = 0
                for (j in 0 until now.length) {
                    if (now[j] == '#') {
                        count++
                    } else break
                }
                //生成该标题对应的html,并解析该行的行内元素
                mdContent[i] = "<h$count>" + InLineElenParse(
                    now.substring(count).trim { it <= ' ' }) + "</h" + count + ">"
            }
            //如果该行类型为空行，则将该行的内容设置为<br>,但由于br本身高度太高，所以在此利用div设置好高度来替换换行
            if (nowType == BLANK_RAW) {
                mdContent[i] = "<div style=\"height: 5px;\"></div>"
            }
            //如果该行是代码区入口，则按照下面进行解决,写入class的目的是为了让显示更加好看,<xml></xmp>标签一包围，html就不会解析这个内容了
            if (nowType == CODE_ENTER) {
                mdContent[i] = "<div style=\"height: 5px;\"></div>"
                mdContent[i + 1] =
                    "<pre><div class=\"left-raw\" ></div><div class=\"code-content\"><xmp style=\"margin:0 0;\">" + mdContent[i + 1]
            }
            //如果该行是代码行，则直接写入即可,即不用管
            if (nowType == CODE_RAW) {
            }
            //如果该行是代码行出口，则写入固定格式的代码
            if (nowType == CODE_EXIT) {
                mdContent[i - 1] = mdContent[i - 1] + "</xmp></div></pre>"
                mdContent[i] = "<div style=\"height: 5px;\"></div>"
            }
            //如果该行是多行无序列表行，则进行如下解析
            if (nowType == UNORDERED_LIST_ENTER) {
                mdContent[i] = "<ul><li>" + InLineElenParse(now.substring(1).trim { it <= ' ' }) + "</li>"
            }
            if (nowType == UNORDERED_LIST_EXIT) {
                mdContent[i] = "<li>" + InLineElenParse(now.substring(1).trim { it <= ' ' }) + "</li></ul>"
            }
            if (nowType == UNORDERED_LIST_RAW) {
                mdContent[i] = "<li>" + InLineElenParse(now.substring(1).trim { it <= ' ' }) + "</li>"
            }
            if (nowType == UNORDERED_LIST_SINGLE) {
                mdContent[i] = "<ul><li>" + InLineElenParse(now.substring(1).trim { it <= ' ' }) + "</li></ul>"
            }
            //有序列表区的判断
            if (nowType == ORDERED_LIST_ENTER) {
                mdContent[i] = "<ol><li>" + InLineElenParse(now.substring(2).trim { it <= ' ' }) + "</li>"
            }
            if (nowType == ORDERED_LIST_EXIT) {
                mdContent[i] = "<li>" + InLineElenParse(now.substring(2).trim { it <= ' ' }) + "</li></ol>"
            }
            if (nowType == ORDERED_LIST_RAW) {
                mdContent[i] = "<li>" + InLineElenParse(now.substring(2).trim { it <= ' ' }) + "</li>"
            }
            if (nowType == ORDERED_LIST_SINGLE) {
                mdContent[i] = "<ol><li>" + InLineElenParse(now.substring(2).trim { it <= ' ' }) + "</li></ol>"
            }
            //多行的引用行区的处理,对应代码中的左侧边框为绿色的部分html代码
            if (nowType == QUOTE_ENTER) {
                mdContent[i] = "<p class=\"left-green-title\">" + InLineElenParse(now.substring(1).trim { it <= ' ' })
            }
            if (nowType == QUOTE_EXIT) {
                mdContent[i] = InLineElenParse(now.substring(1).trim { it <= ' ' }) + "</p>"
            }
            if (nowType == QUOTE_RAW) {
                mdContent[i] = InLineElenParse(now.substring(1).trim { it <= ' ' })
            }
            if (nowType == QUOTE_SINGLE) {
                mdContent[i] = "<p class=\"left-green-title\">" + InLineElenParse(
                    now.substring(1).trim { it <= ' ' } + "</p>")
            }
            if (nowType == HR) {
                mdContent[i] = "<hr/>"
            }
            if (nowType == NONE) {
                mdContent[i] = "<p>" + InLineElenParse(now.trim { it <= ' ' }) + "</p>"
            }
        }
    }

    fun InLineElenParse(content: String): String {
        var content = content
        for (i in 0 until content.length) {
            //第一步：解析看行内是否有图片，即是否存在![]()这种格式,长度为5，所以i到line.length()-4就行了
            if (i < content.length - 4 && content[i] == '!' && content[i + 1] == '[') {
                //开始找在i+1位置之后第一次出现]的位置
                val index = content.indexOf(']', i + 1)
                //如果找到了，则index位置之后的下一个位置应该为(，才符合markdown中图片的格式,同时在(之后应该有)才行
                if (index != -1 && content[index + 1] == '(' && content.indexOf(')', index + 2) != -1) {
                    //到此，说明存在图片的行内元素，开始解析为html
                    val index1 = content.indexOf(')', index + 2)
                    val imgAlt = content.substring(i + 2, index)
                    val imgSrc = content.substring(index + 2, index1)
                    //开始替换其中的markdown格式的图片标签为html格式的图片标签
                    content = content.replace(
                        content.substring(i, index1 + 1),
                        "<img src=\"$imgSrc\" alt=\"$imgAlt\" class=\"img-content\" />"
                    )
                }
            }
            //解析超链接，markdown中超链接格式为[]()，解决思路和上面一样
            if (i < content.length - 3 && content[0] == '[' || i > 0 && content[i] == '[' && content[i - 1] != '!') {
                //开始找在i+1位置之后第一次出现]的位置
                val index = content.indexOf(']', i + 1)
                //如果找到了，则index位置之后的下一个位置应该为(，才符合markdown中图片的格式,同时在(之后应该有)才行
                if (index != -1 && content[index + 1] == '(' && content.indexOf(')', index + 2) != -1) {
                    //到此，说明存在超链接这一个行内元素，开始解析为html
                    val index1 = content.indexOf(')', index + 2)
                    val linkCont = content.substring(i + 1, index)
                    val linkHref = content.substring(index + 2, index1)
                    //开始替换其中的markdown格式的图片标签为html格式的图片标签
                    content = content.replace(content.substring(i, index1 + 1), "<a href=\"$linkHref\">$linkCont</a>")
                }
            }
            // 解析粗体** **，判断行内是否有标注为粗体的部分
            if (i < content.length - 3 && content[i] == '*' && content[i + 1] == '*') {
                val index = content.indexOf("**", i + 1)
                if (index != -1) {
                    val cont = content.substring(i + 2, index)
                    content = content.replace(content.substring(i, index + 2), "<b>$cont</b>")
                }
            }
            // 解析斜体* *，判断行内是否有标注为斜体的部分
            if (i < content.length - 2 && content[i] == '*' && content[i + 1] != '*') {
                val index = content.indexOf('*', i + 1)
                if (index != -1 && content[index + 1] != '*') {
                    val cont = content.substring(i + 1, index)
                    content = content.replace(content.substring(i, index + 1), "<i>$cont</i>")
                }
            }
            //解析单行引用` `
            if (i < content.length - 2 && content[i] == '`' && content[i + 1] != '`') {
                val index = content.indexOf('`', i + 1)
                if (index != -1 && content[index + 1] != '`') {
                    val cont = content.substring(i + 1, index)
                    content = content.replace(
                        content.substring(i, index + 1),
                        "<span class=\"back-green-radius-content\">$cont</span>"
                    )
                }
            }
            //解析删除线~~ ~~
            if (i < content.length - 3 && content[i] == '~' && content[i + 1] == '~') {
                val index = content.indexOf("~~", i + 1)
                if (index != -1) {
                    val cont = content.substring(i + 2, index)
                    content = content.replace(content.substring(i, index + 2), "<s>$cont</s>")
                }
            }
            //解析高亮模块== ==，可以跨越多行
            if (i < content.length - 3 && content[i] == '=' && content[i + 1] == '=') {
                val index = content.indexOf("==", i + 1)
                if (index != -1) {
                    val cont = content.substring(i + 2, index)
                    content = content.replace(
                        content.substring(i, index + 2),
                        "<span class=\"back-yellow-content\">$cont</span>"
                    )
                }
            }
        }
        return content
    }

    fun PrintToHtml(mdContent: ArrayList<String>, filename: String) {
        val file = File("web//$filename")
        if (!file.exists()) {
            try {
                file.createNewFile()
                //然后开始逐行写入这些内容，先写前面固定的
                val fileOutputStream = FileOutputStream(file)
                fileOutputStream.write("<!DOCTYPE html>\r\n".toByteArray())
                fileOutputStream.write("<html lang=\"zh-CN\">\r\n".toByteArray())
                fileOutputStream.write("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>\r\n".toByteArray())
                fileOutputStream.write("<head>\r\n".toByteArray())
                fileOutputStream.write("<script src=\"js/jquery.min.js\"></script>\r\n".toByteArray())
                fileOutputStream.write("<script src=\"js/markdown.js\"></script>\r\n".toByteArray())
                fileOutputStream.write("<link rel=\"shortcut icon\" type=\"image/x-icon\">\r\n".toByteArray())
                fileOutputStream.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/markdown.css\" />\r\n".toByteArray())
                fileOutputStream.write("<title>MyMarkdown</title>\r\n".toByteArray())
                fileOutputStream.write("</head>\r\n".toByteArray())
                fileOutputStream.write("<body>\r\n".toByteArray())
                fileOutputStream.write("<div class=\"main-content\">\r\n".toByteArray())
                //开始循环写解析后的内容
                for (i in mdContent.indices) {
                    fileOutputStream.write(
                        """${mdContent[i]}
""".toByteArray()
                    )
                }
                //开始写末尾
                fileOutputStream.write("</div>\r\n".toByteArray())
                fileOutputStream.write("</body>\r\n".toByteArray())
                fileOutputStream.write("</html>\r\n".toByteArray())
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}