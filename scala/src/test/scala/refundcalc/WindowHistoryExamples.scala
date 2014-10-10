package refundcalc

import org.scalatest.{FunSuite, Matchers}

class WindowHistoryExamples extends FunSuite with Matchers {

  def makeCharEventSequence(deltas: Amount*): Seq[Event[Char]] =
    makeEventSequence(deltas, 'a' to 'z')

  test("up, down a little, down to zero") {
    val events = makeCharEventSequence(10, -3, -7)
    WindowHistory.fromEventSequence(events.take(1)) should equal(
      WindowHistory(
        open=Vector(Open(10, 'a'))
      )
    )
    WindowHistory.fromEventSequence(events) should equal(
      WindowHistory(
        closed=Vector(Closed(3, 'a', 'b'), Closed(7, 'a', 'c'))
      )
    )
  }

  test("up, up, down mostly, down to zero") {
    val events = makeCharEventSequence(10, 10, -18, -2)
    WindowHistory.fromEventSequence(events.take(2)) should equal(
      WindowHistory(
        open=Vector(Open(10, 'a'), Open(10, 'b'))
      )
    )
    WindowHistory.fromEventSequence(events.take(3)) should equal(
      WindowHistory(
        closed=Vector(Closed(10, 'a', 'c'), Closed(8, 'b', 'c')),
        open=Vector(Open(2, 'b'))
      )
    )
    WindowHistory.fromEventSequence(events) should equal(
      WindowHistory(
        closed=Vector(
          Closed(10, 'a', 'c'),
          Closed(8, 'b', 'c'),
          Closed(2, 'b', 'd')
        )
      )
    )
  }

  test("up, up, down a little, down to zero") {
    val events = makeCharEventSequence(10, 10, -2, -18)
    WindowHistory.fromEventSequence(events) should equal(
      WindowHistory(
        closed=Vector(
          Closed(2, 'a', 'c'),
          Closed(8, 'a', 'd'),
          Closed(10, 'b', 'd')
        )
      )
    )
  }

  test("up, down, up, down, down") {
    val events = makeCharEventSequence(10, -4, 10, -7, -2)
    WindowHistory.fromEventSequence(events) should equal(
      WindowHistory(
        closed=Vector(
          Closed(4, 'a', 'b'),
          Closed(6, 'a', 'd'),
          Closed(1, 'c', 'd'),
          Closed(2, 'c', 'e')
        ),
        open=Vector(Open(7, 'c'))
      )
    )
  }

  test("down into debt, up, down to zero") {
    val events = makeCharEventSequence(-3, 10, -7)
    WindowHistory.fromEventSequence(events.take(1)) should equal(
      WindowHistory(
        debt=3
      )
    )
    WindowHistory.fromEventSequence(events) should equal(
      WindowHistory(
        closed=Vector(Closed(7, 'b', 'c'))
      )
    )
  }

  test("up, down into debt, up") {
    val events = makeCharEventSequence(10, -16, 10)
    WindowHistory.fromEventSequence(events) should equal(
      WindowHistory(
        closed=Vector(Closed(10, 'a', 'b')),
        open=Vector(Open(4, 'c'))
      )
    )
  }
}
