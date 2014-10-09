package refundcalc

import refundcalc.WindowHistory.head_

case class WindowHistory[+T](
  closed: Vector[Closed[T]] = Vector.empty,
  open: Vector[Open[T]] = Vector.empty,
  debt: Amount = zero
) {

  assert(debt >= zero)
  if (debt > zero) assert(open.isEmpty)

  /**
    Chronological (primarily by start event, and secondarily by end event).
  */
  def windows: Vector[Window[T]] = closed ++ open

  def apply[S >: T](event: Event[S]): WindowHistory[S] =
    step(event.time, event.delta)

  private def step[S >: T](time: S, delta: Amount): WindowHistory[S] =

    // The easiest case - A change of zero is no change at all.
    if (delta == zero) this

    // The poorest case - The account is underwater.
    else if (debt > zero) {
      // The change is high enough to get us out of debt, and
      // recurse with the remainder.
      if (delta > debt) debt_(_ => 0).step(time, delta - debt)
      // The change just alters the amount of debt.
      else debt_(_ - delta)
    }

    // An increase creates a new open window.
    else if (delta > zero) open_(_ :+ Open(delta, time))

    // A decrease is more complicated.
    else open.headOption match {
      // If the account is completely zero (neither debt nor balance),
      // the negative delta just takes us into debt.
      case None => debt_(_ - delta)

      // The account has some open window. It will be reduced.
      case Some(top) =>
        // If the change consumes the entire top window, convert it
        // into a closed window, and recurse with the remainder.
        if (-delta >= top.amount)
          closed_(_ :+ top.close(time))
            .open_(_.tail)
            .step(time, delta + top.amount)

        // If the top window is greater than the delta, only close
        // part of it.
        else
          closed_(_ :+ top.amount_(_ => -delta).close(time))
            .open_(head_(_.amount_(_ + delta)))
    }

  def debt_(f: Amount => Amount) = copy(debt = f(debt))

  def open_[S >: T](f: Vector[Open[T]] => Vector[Open[S]]) =
    copy(open = f(open))

  def closed_[S >: T](f: Vector[Closed[T]] => Vector[Closed[S]]) =
    copy(closed = f(closed))
}

object WindowHistory {

  val nil = WindowHistory[Nothing]()

  def fromEventSequence[T](events: Traversable[Event[T]]): WindowHistory[T] =
    events.foldLeft[WindowHistory[T]](nil)(_ apply _)

  private def head_[A](f: A => A): Vector[A] => Vector[A] =
    v => v.updated(0, f(v.head))
}
