package com.example.learnkotlin

import java.lang.Integer.max
import java.math.BigDecimal
import java.sql.DriverManager.println


fun main() {
    //这是Kotlin打印，所以不用Logs，用println
    println(checkType(1L))
}

//when的练习 & is用来判断类型
fun checkType(num: Number) = when (num) {
    // Float -> 1.0f
    // Long -> 1L
    // "is"用来做类型判断
    is Float -> "FLoat"
    is Double -> "Double"
    is Int -> "Int"
    else -> "not support"
}

//getScore用when的另一种表达去写
fun getScoreWhen(name: String) = when {
    name == "tom" -> 90
    name == "amy" -> 30
    else -> 100
}

//用等号的if else
fun getScore(name: String) = if (name == "tom") {
    90
} else if (name == "amy") {
    30
} else {
    100
}

fun getScore2(name: String) {
    when (name) {
        "tom" -> 90
        "amy" -> 30
        else -> 100
    }
}

//用等号
fun getScore3(name: String) = when (name) {
    "tom" -> 90
    "amy" -> 30
    else -> 100
}


fun largerNumber(num1: Int, num2: Int): Int {
    return max(num1, num2)
}

//用等号&简化返回值类型
fun largerNumber2(num1: Int, num2: Int) = max(num1, num2)
