
package cn.edu.sicnucs.bml.experiment2_mvc


class ProgramAdviserModel {
    // 模拟查询业务逻辑
    fun queryData(input: String): String {
        return if (input.isBlank()) {
            "输入不能为空，请重新输入！"
        } else {
            "MVC模型层返回结果：你查询的内容是「$input」"
        }
    }
}
