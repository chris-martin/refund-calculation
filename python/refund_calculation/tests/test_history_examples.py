from __future__ import absolute_import

import sure

from collections import namedtuple
from decimal import Decimal
from string import ascii_letters

from .. import *


def make_char_event_sequence(*deltas):
    return make_event_sequence(deltas, ascii_letters)


def test_01():
    """up, down a little, down to zero"""
    events = make_char_event_sequence(10, -3, -7)
    history_from_event_sequence(events[:1]).should.equal(
        History(
            open=(Open(10, 'a'),)
        )
    )
    history_from_event_sequence(events).should.equal(
        History(
            closed=(Closed(3, 'a', 'b'), Closed(7, 'a', 'c'))
        )
    )


def test_02():
    """up, up, down mostly, down to zero"""
    events = make_char_event_sequence(10, 10, -18, -2)
    history_from_event_sequence(events[:2]).should.equal(
        History(
            open=(Open(10, 'a'), Open(10, 'b'))
        )
    )
    history_from_event_sequence(events[:3]).should.equal(
        History(
            closed=(Closed(10, 'a', 'c'), Closed(8, 'b', 'c')),
            open=(Open(2, 'b'),),
        )
    )
    history_from_event_sequence(events).should.equal(
        History(
            closed=(
                Closed(10, 'a', 'c'),
                Closed(8, 'b', 'c'),
                Closed(2, 'b', 'd'),
            )
        )
    )


def test_03():
    """up, up, down a little, down to zero"""
    events = make_char_event_sequence(10, 10, -2, -18)
    history_from_event_sequence(events).should.equal(
        History(
            closed=(
                Closed(2, 'a', 'c'),
                Closed(8, 'a', 'd'),
                Closed(10, 'b', 'd'),
            )
        )
    )


def test_04():
    """up, down, up, down, down"""
    events = make_char_event_sequence(10, -4, 10, -7, -2)
    history_from_event_sequence(events).should.equal(
        History(
            closed=(
                Closed(4, 'a', 'b'),
                Closed(6, 'a', 'd'),
                Closed(1, 'c', 'd'),
                Closed(2, 'c', 'e'),
            ),
            open=(Open(7, 'c'),),
        )
    )


def test_05():
    """down into debt, up, down to zero"""
    events = make_char_event_sequence(-3, 10, -7)
    history_from_event_sequence(events[:1]).should.equal(
        History(
            debt=3
        )
    )
    history_from_event_sequence(events).should.equal(
        History(
            closed=(Closed(7, 'b', 'c'),)
        )
    )


def test_06():
    """up, down into debt, up"""
    events = make_char_event_sequence(10, -16, 10)
    history_from_event_sequence(events).should.equal(
        History(
            closed=(Closed(10, 'a', 'b'),),
            open=(Open(4, 'c'),),
        )
    )
