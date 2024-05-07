package org.powbot.krulvis.test

class Test(val id: Int, val amount: Int)

fun main() {
    val inventory = listOf(Test(1, 1), Test(1, 1), Test(2, 1), Test(3, 2))
    print(inventory)
}
