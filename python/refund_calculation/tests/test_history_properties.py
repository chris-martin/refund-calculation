from __future__ import absolute_import

import hypothesis
import hypothesis.searchstrategy
import hypothesis.testdecorators
import sure

from collections import namedtuple
from decimal import Decimal
from itertools import count
import sys

from .. import *
from ..util import zip


zero = Decimal('0')


@hypothesis.searchstrategy.strategy_for(Decimal)
class DecimalStrategy(hypothesis.searchstrategy.MappedSearchStrategy):

    def __init__(self, strategies, descriptor, **kwargs):
        hypothesis.searchstrategy.SearchStrategy.__init__(
            self, strategies, descriptor, **kwargs)
        self.mapped_strategy = strategies.strategy(int)

    def pack(self, x):
        return Decimal(x) / 100

    def unpack(self, x):
        return int(x * 100)


EventSequence = namedtuple('EventSequence', ('events',))


@hypothesis.searchstrategy.strategy_for(EventSequence)
class EventSequenceStrategy(hypothesis.searchstrategy.MappedSearchStrategy):

    def __init__(self, strategies, descriptor, **kwargs):
        hypothesis.searchstrategy.SearchStrategy.__init__(
            self, strategies, descriptor, **kwargs)
        self.mapped_strategy = strategies.strategy([Decimal])

    def pack(self, xs):
        return EventSequence(make_event_sequence(xs, count(1)))

    def unpack(self, xs):
        return [x.delta for x in xs.events]


is_sorted = lambda xs: all(a <= b for a, b in zip(xs[:-1], xs[1:]))


@hypothesis.testdecorators.given(EventSequence)
def test_window_order(event_sequence):
    """Windows should be returned in chronological order."""

    history = history_from_event_sequence(event_sequence.events)
    assert is_sorted([
        (w.start, w.end if w.end is not None else sys.maxsize)
        for w in history.windows
    ]), unicode(history.windows)


@hypothesis.testdecorators.given(EventSequence)
def test_delta_sum(event_sequence):
    """The sum of open windows should equal the sum of the deltas."""

    history = history_from_event_sequence(event_sequence.events)
    sum_of_deltas = sum(e.delta for e in event_sequence.events)

    if sum_of_deltas > zero:
        sum(w.amount for w in history.open).should.equal(sum_of_deltas)
        history.debt.should.equal(zero)
    elif sum_of_deltas < zero:
        len(history.open).should.equal(0)
        history.debt.should.equal(-sum_of_deltas)
    else:
        len(history.open).should.equal(0)
        history.debt.should.equal(zero)
