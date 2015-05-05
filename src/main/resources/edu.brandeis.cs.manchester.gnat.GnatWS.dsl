def idx = 0
annotations  ( &$annotations.entity.foreach {
             [
                      id : "gnat${idx++}",
                      start : &.@startIndex.text(),
                      end : &.@endIndex.text(),
                      label : "http://vocab.lappsgrid.org/NamedEntity",
                      features : [
                          category : &.@type.text(),
                          word : &.text(),
                          subtype : &.@subtype.text(),
                          ids : &.@ids.text()
                      ]
                  ]
              })