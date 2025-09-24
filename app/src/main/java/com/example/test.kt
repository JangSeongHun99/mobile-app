package com.example

    fun main() {
        println("Hello, world!")
        print("Hello, world!\n")
        println("Hello, world!")

        val name = "dydwns"
        println("Im, " + name + "!")
        println("HI, $name!")


        fun add(a: Int, b: Int = 20001) = a + b

        val result = add(10)
        println("result = " + result)


        println("1부터 5까지")
        for (i in 1..5)
        {
            println(i)
        }
        println("\n1부터 10까지 홀수:")
        for (i in 1..10 step 2) {
            println(i)

            }
    }


