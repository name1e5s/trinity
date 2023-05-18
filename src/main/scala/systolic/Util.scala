package systolic

import scala.language.postfixOps

object Util {
  def comb[T](ls: Seq[T]*): Seq[Seq[T]] = {
    ls.foldLeft(Seq(Seq[T]()))((acc, x) => for (i <- acc; j <- x) yield i :+ j)
  }

  def matMul(a: Seq[Seq[Double]], b: Seq[Seq[Int]]): Seq[Seq[Int]] = {
    (for (row <- a)
      yield for (col <- b.transpose)
        yield row zip col map Function.tupled(_ * _) sum) map (_ map (_.toInt))
  }

  def matMul[A](a: Seq[Seq[A]], b: Seq[Seq[A]])(implicit n: Numeric[A]) = {
    import n._
    for (row <- a)
      yield for (col <- b.transpose)
        yield row zip col map Function.tupled(_ * _) sum
  }

  def matMul2[A: Numeric](a: Seq[Seq[A]], b: Seq[A]): Seq[A] =
    matMul(a, Seq(b).transpose).flatten

  def permutationsSgn[T]: List[T] => List[(Int, List[T])] = {
    case Nil => List((1, Nil))
    case xs =>
      for {
        (x, i) <- xs.zipWithIndex
        (sgn, ys) <- permutationsSgn(xs.take(i) ++ xs.drop(1 + i))
      } yield {
        val sgni = sgn * (2 * (i % 2) - 1)
        (sgni, (x :: ys))
      }
  }

  def det[A](m_ : Seq[Seq[A]])(implicit n: Numeric[A]) = {
    import n._

    val m = m_.map(_.map(_.toInt))

    val summands =
      for {
        (sgn, sigma) <- permutationsSgn(m.indices.toList).toList
      } yield {
        val factors =
          for (i <- m.indices)
            yield m(i)(sigma(i))
        factors.toList.foldLeft(sgn)({ case (x, y) => x * y })
      }
    summands.foldLeft(0)({ case (x, y) => x + y })
  }

  def dependencyvecs(e: Expr): Seq[(Systolic#Local, Seq[Int])] = e match {
    case Add(l, r)      => dependencyvecs(l) ++ dependencyvecs(r)
    case Multiply(l, r) => dependencyvecs(l) ++ dependencyvecs(r)
    case SMux(c, t, f) =>
      dependencyvecs(c) ++ dependencyvecs(t) ++ dependencyvecs(f)
    case Ref(loc, dir, _, _, _) => Seq((loc, dir.map(-_)))
    case _ => throw new Exception(s"unknown in dep vec ${e.getClass}")
  }
}
