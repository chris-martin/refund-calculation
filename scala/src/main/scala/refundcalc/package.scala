package object refundcalc {

  type Amount = BigDecimal
  val zero = BigDecimal(0)

  case class Event[+T](time: T, delta: Amount)

  def makeEventSequence[T](deltas: Seq[Amount], times: Iterable[T]):
    Seq[Event[T]] = (times, deltas).zipped.map(Event(_, _)).toSeq
}
