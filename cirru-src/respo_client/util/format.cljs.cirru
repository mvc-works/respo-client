
ns respo-client.util.format $ :require
  [] clojure.string :as string

defn dashed->camel
  (x)
    dashed->camel | x false
  (acc piece promoted?)
    if (= piece |)
      , acc
      let
        (cursor $ get piece 0)
          piece-followed $ subs piece 1
        if (= cursor |-)
          recur acc piece-followed true
          recur
            str acc $ if promoted? (string/upper-case cursor)
              , cursor
            , piece-followed false

defn prop->attr (x)
  case x (|class-name |class)
    , x

defn event->string (x)
  subs (name x)
    , 3

defn event->prop (x)
  string/replace (name x)
    , |- |

defn event->edn (event)
  -- .log js/console "|simplify event:" event
  case (.-type event)
    |click $ {} :type :click
    |dblclick $ {} :type :dblclick
    |keydown $ {} :type :keydown :key-code (.-keyCode event)
    |input $ {} :type :input :value
      .-value $ .-target event
    |change $ {} :type :change :value
      .-value $ .-target event
    |focus $ {} (:type :focus)
    {} :type (.-type event)
      , :msg "|not recognized event"
