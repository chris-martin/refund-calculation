Turns a chronological sequence of balance-adjustment events into a
timeline of periods during which particular balances were held.

For example, this sequence of events ...

* A: *$10*
* B: *$10*
* C: *($18)*
* D: *($2)*

... is turned into this timeline.

```
  A             B             C             D
  |             |             |             |
  |_____________|_____________|             |
  |                           |             |
  |           $10             |             |
  |___________________________|             |
                |             |             |
                |     $8      |             |
                |_____________|_____________|
                |                           |
                |            $2             |
                |___________________________|
```

This lets you determine, for each balance reduction event, the events from
which the balance was added. In this example, we can see that:

* The $18 reduction in event C came from $10 of event A and $8 of B.
* The $2 reduction in event D came entirely from event B. 
