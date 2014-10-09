package refundcalc

import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSuite, Matchers}

class WindowHistoryProperties extends FunSuite with Matchers
    with GeneratorDrivenPropertyChecks {

  val deltas = for {
    n <- Gen.choose(0, 40)
    sign <- Gen.choose(-1, 1)
  } yield BigDecimal(n) / 2 * sign

  val eventSequences = Gen.listOf(deltas)
    .map(deltas => makeEventSequence(deltas, Stream.from(1)))

  def isSorted[A](xs: Seq[A])(implicit ord: Ordering[A]): Boolean = {
    import ord._
    xs.isEmpty || (xs.view, xs.tail).zipped.forall(_ <= _)
  }

  test("Windows should be returned in chronological order.") {
    forAll(eventSequences) { events =>
      val history = WindowHistory.fromEventSequence(events)
      assert(
        isSorted(history.windows)(Ordering.by(w =>
          (w.start, w.endOption.getOrElse(Int.MaxValue)))),
        history.windows.toString()
      )
    }
  }

  test("The sum of open windows should equal the sum of the deltas.") {
    forAll(eventSequences) { events =>
      val history = WindowHistory.fromEventSequence(events)
      val sumOfDeltas = events.map(_.delta).sum
      if (sumOfDeltas > zero) {
        history.open.map(_.amount).sum should equal(sumOfDeltas)
        history.debt should equal(zero)
      } else if (sumOfDeltas < zero) {
        history.open.size should equal(zero)
        history.debt should equal(-sumOfDeltas)
      } else {
        history.open.size should equal(zero)
        history.debt should equal(zero)
      }
    }
  }
}
