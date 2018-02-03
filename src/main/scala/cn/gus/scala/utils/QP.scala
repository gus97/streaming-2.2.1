package cn.gus.scala.utils

import java.io.{File, PrintWriter}

import scala.collection.mutable
import scala.util.Random

object QP {

  def main(args: Array[String]): Unit = {

    val writer = new PrintWriter(new File("/Users/guxichang/Desktop/random.txt"))

    for (_ <- 1 to 100000) {


      val pok = mutable.Buffer[Int]()

      for (x <- 3 to 15) {
        pok += (x * 10 + 1, x * 10 + 2, x * 10 + 3, x * 10 + 4)
      }

      pok += (99, 100)

      val s_pok = Random.shuffle(pok)

      val p1 = s_pok.take(17)
      s_pok --= p1

      val p2 = s_pok.take(17)
      s_pok --= p2

      val p3 = s_pok.take(17)

      s_pok --= p3

      val three = s_pok

//      println(p1)
//      println(p2)
//      println(p3)
//      println(three)

      if (p1.sum + p2.sum + p3.sum + three.sum == 5009) {

        writer.print((p1.sortWith(_ > _) + "$" + p2.sortWith(_ > _) + "$" + p3.sortWith(_ > _) + "$" + three + "\n")
          .replace("ArrayBuffer(", "").replace(")", "").replace(" ", ""))
      }
    }
    writer.close
  }
}
