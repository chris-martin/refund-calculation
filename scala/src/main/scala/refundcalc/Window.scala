package refundcalc

sealed trait Window[+T] {
  def amount: Amount
  def start: T
  def endOption: Option[T]
}

case class Closed[+T](amount: Amount, start: T, end: T) extends Window[T] {
  assert(amount > zero)
  override def endOption: Option[T] = Some(end)
}

case class Open[+T](amount: Amount, start: T) extends Window[T] {
  assert(amount > zero)
  override def endOption: Option[T] = None
  def close[S >: T](end: S) = Closed(amount, start, end)
  def amount_(f: Amount => Amount) = copy(amount = f(amount))
}
