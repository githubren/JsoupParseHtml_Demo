import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception
import java.net.URL
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

object Main {
    private const val url = "https://www.cnblogs.com"

    @JvmStatic
    fun main(argo: Array<String>) {

//        getDataByReadText()
        getDataByJsoup()
    }

    /**
     * 通过Jsoup爬取
     */
    private fun getDataByJsoup() {
        val document = Jsoup.connect(url).get()
        parseDocument(document)
    }

    /**
     * 通过url。readText()爬取
     */
    private fun getDataByReadText() {
        val html = URL(url).readText()
        val document = Jsoup.parse(html)
        println(document.getElementsByTag("title").first())
        parseDocument(document)
    }

    /**
     * 解析document
     */
    private fun parseDocument(document: Document) {
        val postList = document.getElementById("post_list")
        val postItems = postList.getElementsByClass("post-item")
        val infoes = arrayListOf<Info>()
        postItems.forEach {
            val titleEle = it.select(".post-item-text>.post-item-title")
            println("文章标题：" + titleEle.text())
            println("文章地址：" + titleEle.attr("href"))
            val footEle = it.select(".post-item .post-item-author")
            println(("文章作者：" + footEle.text()))
            println(("作者主页：" + footEle.attr("href")))
            val imageEle = it.select(".post-item .avatar")
            println("文章图片：" + imageEle.attr("src"))
            println("*********************************************")
            val info = Info(
                    title = titleEle.text(),
                    addr = titleEle.attr("href"),
                    author = footEle.text(),
                    authorMainPage = footEle.attr("href"),
                    articleImg = imageEle.attr("src"))

            infoes.add(info)
        }
        saveToDataBase(infoes)
    }

    private fun saveToDataBase(datas:ArrayList<Info>){
        val driver = "com.mysql.cj.jdbc.Driver"
        val url = "jdbc:mysql://localhost:3306/crawler?characterEncoding=utf-8&serverTimezone=UTC"
        val user = "root"
        val password = "123456"
        var conn : Connection? = null
        try {
            Class.forName(driver)
            conn = DriverManager.getConnection(url, user, password)
        }catch (e:ClassNotFoundException){
            e.printStackTrace()
        }
        var i = 0
        val sql = "insert into new_table(`title`,`addr`,`author`,`author_main_page`,`article_img`) " +
                "values(?,?,?,?,?)"
        var pstmt : PreparedStatement? = null
        try {
            pstmt = conn?.prepareStatement(sql)
            datas.forEachIndexed { index, s ->
                pstmt?.setString(1,s.title)
                pstmt?.setString(2,s.addr)
                pstmt?.setString(3,s.author)
                pstmt?.setString(4,s.authorMainPage)
                pstmt?.setString(5,s.articleImg)
                i = pstmt?.executeUpdate()?:0
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}